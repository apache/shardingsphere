/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.describe;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command describe for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComDescribeExecutor implements CommandExecutor {
    
    private final PostgreSQLConnectionContext connectionContext;
    
    private final PostgreSQLComDescribePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        switch (packet.getType()) {
            case 'S':
                return describePreparedStatement();
            case 'P':
                return Collections.singletonList(connectionContext.getPortal(packet.getName()).describe());
            default:
                throw new UnsupportedOperationException("Unsupported describe type: " + packet.getType());
        }
    }
    
    private List<DatabasePacket<?>> describePreparedStatement() throws SQLException {
        List<DatabasePacket<?>> result = new ArrayList<>(2);
        PostgreSQLPreparedStatement preparedStatement = PostgreSQLPreparedStatementRegistry.getInstance().get(connectionSession.getConnectionId(), packet.getName());
        result.add(preparedStatement.describeParameters());
        Optional<PostgreSQLPacket> rowDescription = preparedStatement.describeRows();
        if (rowDescription.isPresent()) {
            result.add(rowDescription.get());
        } else {
            tryDescribePreparedStatement(preparedStatement);
            preparedStatement.describeRows().ifPresent(result::add);
        }
        return result;
    }
    
    private void tryDescribePreparedStatement(final PostgreSQLPreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement.getSqlStatement() instanceof InsertStatement) {
            describeInsertStatementByShardingSphereMetaData(preparedStatement);
            return;
        }
        tryDescribePreparedStatementByJDBC(preparedStatement);
    }
    
    private void describeInsertStatementByShardingSphereMetaData(final PostgreSQLPreparedStatement preparedStatement) {
        if (!preparedStatement.describeRows().isPresent()) {
            // TODO Consider the SQL `insert into table (col) values ($1) returning id`
            preparedStatement.setRowDescription(PostgreSQLNoDataPacket.getInstance());
        }
        InsertStatement insertStatement = (InsertStatement) preparedStatement.getSqlStatement();
        if (0 == insertStatement.getParameterCount()) {
            return;
        }
        Set<Integer> unspecifiedTypeParameterIndexes = getUnspecifiedTypeParameterIndexes(preparedStatement);
        if (unspecifiedTypeParameterIndexes.isEmpty()) {
            return;
        }
        String schemaName = connectionSession.getSchemaName();
        String logicTableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        TableMetaData tableMetaData = ProxyContext.getInstance().getMetaData(schemaName).getDefaultSchema().get(logicTableName);
        Map<String, ColumnMetaData> columnMetaData = tableMetaData.getColumns();
        List<String> columnNames;
        if (insertStatement.getColumns().isEmpty()) {
            columnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        } else {
            columnNames = insertStatement.getColumns().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
        }
        Iterator<InsertValuesSegment> iterator = insertStatement.getValues().iterator();
        int parameterMarkerIndex = 0;
        while (iterator.hasNext()) {
            InsertValuesSegment each = iterator.next();
            ListIterator<ExpressionSegment> listIterator = each.getValues().listIterator();
            for (int columnIndex = listIterator.nextIndex(); listIterator.hasNext(); columnIndex = listIterator.nextIndex()) {
                ExpressionSegment value = listIterator.next();
                if (!(value instanceof ParameterMarkerExpressionSegment)) {
                    continue;
                }
                if (!unspecifiedTypeParameterIndexes.contains(parameterMarkerIndex)) {
                    parameterMarkerIndex++;
                    continue;
                }
                String columnName = columnNames.get(columnIndex);
                PostgreSQLColumnType parameterType = PostgreSQLColumnType.valueOfJDBCType(columnMetaData.get(columnName).getDataType());
                preparedStatement.getParameterTypes().set(parameterMarkerIndex++, parameterType);
            }
        }
    }
    
    private Set<Integer> getUnspecifiedTypeParameterIndexes(final PostgreSQLPreparedStatement preparedStatement) {
        Set<Integer> unspecifiedTypeParameterIndexes = new HashSet<>();
        ListIterator<PostgreSQLColumnType> parameterTypesListIterator = preparedStatement.getParameterTypes().listIterator();
        for (int index = parameterTypesListIterator.nextIndex(); parameterTypesListIterator.hasNext(); index = parameterTypesListIterator.nextIndex()) {
            if (PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED == parameterTypesListIterator.next()) {
                unspecifiedTypeParameterIndexes.add(index);
            }
        }
        return unspecifiedTypeParameterIndexes;
    }
    
    private void tryDescribePreparedStatementByJDBC(final PostgreSQLPreparedStatement preparedStatement) throws SQLException {
        if (!(connectionSession.getBackendConnection() instanceof JDBCBackendConnection)) {
            return;
        }
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String schemaName = connectionSession.getSchemaName();
        SQLStatementContext<?> sqlStatementContext =
                SQLStatementContextFactory.newInstance(metaDataContexts.getMetaDataMap(), preparedStatement.getSqlStatement(), schemaName);
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, preparedStatement.getSql(), Collections.emptyList());
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(schemaName);
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(logicSQL, metaData, metaDataContexts.getProps());
        ExecutionUnit executionUnitSample = executionContext.getExecutionUnits().iterator().next();
        JDBCBackendConnection backendConnection = (JDBCBackendConnection) connectionSession.getBackendConnection();
        Connection connection = backendConnection.getConnections(executionUnitSample.getDataSourceName(), 1, ConnectionMode.CONNECTION_STRICTLY).iterator().next();
        try (PreparedStatement ps = connection.prepareStatement(executionUnitSample.getSqlUnit().getSql())) {
            populateParameterTypes(preparedStatement, ps);
            populateColumnTypes(preparedStatement, ps);
        }
    }
    
    private void populateParameterTypes(final PostgreSQLPreparedStatement preparedStatement, final PreparedStatement ps) throws SQLException {
        if (0 == preparedStatement.getSqlStatement().getParameterCount()
                || preparedStatement.getParameterTypes().stream().noneMatch(each -> PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED == each)) {
            return;
        }
        ParameterMetaData parameterMetaData = ps.getParameterMetaData();
        for (int i = 0; i < preparedStatement.getSqlStatement().getParameterCount(); i++) {
            if (PostgreSQLColumnType.POSTGRESQL_TYPE_UNSPECIFIED == preparedStatement.getParameterTypes().get(i)) {
                preparedStatement.getParameterTypes().set(i, PostgreSQLColumnType.valueOfJDBCType(parameterMetaData.getParameterType(i + 1)));
            }
        }
    }
    
    private void populateColumnTypes(final PostgreSQLPreparedStatement preparedStatement, final PreparedStatement ps) throws SQLException {
        if (preparedStatement.describeRows().isPresent()) {
            return;
        }
        ResultSetMetaData resultSetMetaData = ps.getMetaData();
        if (null == resultSetMetaData) {
            preparedStatement.setRowDescription(PostgreSQLNoDataPacket.getInstance());
            return;
        }
        List<PostgreSQLColumnDescription> columnDescriptions = new ArrayList<>(resultSetMetaData.getColumnCount());
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            String columnName = resultSetMetaData.getColumnName(columnIndex);
            int columnType = resultSetMetaData.getColumnType(columnIndex);
            int columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
            String columnTypeName = resultSetMetaData.getColumnTypeName(columnIndex);
            columnDescriptions.add(new PostgreSQLColumnDescription(columnName, columnIndex, columnType, columnLength, columnTypeName));
        }
        preparedStatement.setRowDescription(new PostgreSQLRowDescriptionPacket(columnDescriptions.size(), columnDescriptions));
    }
}
