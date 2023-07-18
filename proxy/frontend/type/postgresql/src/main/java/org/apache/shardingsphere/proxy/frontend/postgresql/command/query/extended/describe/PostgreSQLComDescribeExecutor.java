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
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.dialect.postgresql.exception.metadata.ColumnNotFoundException;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.InsertStatementHandler;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command describe for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComDescribeExecutor implements CommandExecutor {
    
    private static final String ANONYMOUS_COLUMN_NAME = "?column?";
    
    private final PortalContext portalContext;
    
    private final PostgreSQLComDescribePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        switch (packet.getType()) {
            case 'S':
                return describePreparedStatement();
            case 'P':
                return Collections.singleton(portalContext.get(packet.getName()).describe());
            default:
                throw new UnsupportedSQLOperationException("Unsupported describe type: " + packet.getType());
        }
    }
    
    private List<DatabasePacket> describePreparedStatement() throws SQLException {
        List<DatabasePacket> result = new ArrayList<>(2);
        PostgreSQLServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(packet.getName());
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
    
    private void tryDescribePreparedStatement(final PostgreSQLServerPreparedStatement preparedStatement) throws SQLException {
        if (preparedStatement.getSqlStatementContext().getSqlStatement() instanceof InsertStatement) {
            describeInsertStatementByDatabaseMetaData(preparedStatement);
        } else {
            tryDescribePreparedStatementByJDBC(preparedStatement);
        }
    }
    
    private void describeInsertStatementByDatabaseMetaData(final PostgreSQLServerPreparedStatement preparedStatement) {
        InsertStatement insertStatement = (InsertStatement) preparedStatement.getSqlStatementContext().getSqlStatement();
        Collection<Integer> unspecifiedTypeParameterIndexes = getUnspecifiedTypeParameterIndexes(preparedStatement);
        Optional<ReturningSegment> returningSegment = InsertStatementHandler.getReturningSegment(insertStatement);
        if (insertStatement.getParameterMarkerSegments().isEmpty() && unspecifiedTypeParameterIndexes.isEmpty() && !returningSegment.isPresent()) {
            return;
        }
        String logicTableName = insertStatement.getTable().getTableName().getIdentifier().getValue();
        ShardingSphereTable table = getTableFromMetaData(connectionSession.getDatabaseName(), insertStatement, logicTableName);
        List<String> columnNamesOfInsert = getColumnNamesOfInsertStatement(insertStatement, table);
        preparedStatement.setRowDescription(returningSegment.<PostgreSQLPacket>map(returning -> describeReturning(returning, table))
                .orElseGet(PostgreSQLNoDataPacket::getInstance));
        int parameterMarkerIndex = 0;
        for (InsertValuesSegment each : insertStatement.getValues()) {
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
                String columnName = columnNamesOfInsert.get(columnIndex);
                ShardingSphereColumn column = table.getColumn(columnName);
                ShardingSpherePreconditions.checkState(null != column, () -> new ColumnNotFoundException(logicTableName, columnName));
                preparedStatement.getParameterTypes().set(parameterMarkerIndex++, PostgreSQLColumnType.valueOfJDBCType(column.getDataType()));
            }
        }
    }
    
    private Collection<Integer> getUnspecifiedTypeParameterIndexes(final PostgreSQLServerPreparedStatement preparedStatement) {
        Collection<Integer> result = new HashSet<>();
        ListIterator<PostgreSQLColumnType> parameterTypesListIterator = preparedStatement.getParameterTypes().listIterator();
        for (int index = parameterTypesListIterator.nextIndex(); parameterTypesListIterator.hasNext(); index = parameterTypesListIterator.nextIndex()) {
            if (PostgreSQLColumnType.UNSPECIFIED == parameterTypesListIterator.next()) {
                result.add(index);
            }
        }
        return result;
    }
    
    private ShardingSphereTable getTableFromMetaData(final String databaseName, final InsertStatement insertStatement, final String logicTableName) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(databaseName);
        String schemaName = insertStatement.getTable().getOwner().map(optional -> optional.getIdentifier()
                .getValue()).orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(database.getProtocolType(), databaseName));
        return database.getSchema(schemaName).getTable(logicTableName);
    }
    
    private List<String> getColumnNamesOfInsertStatement(final InsertStatement insertStatement, final ShardingSphereTable table) {
        return insertStatement.getColumns().isEmpty() ? table.getColumnNames() : insertStatement.getColumns().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList());
    }
    
    private PostgreSQLRowDescriptionPacket describeReturning(final ReturningSegment returningSegment, final ShardingSphereTable table) {
        Collection<PostgreSQLColumnDescription> result = new LinkedList<>();
        for (ProjectionSegment each : returningSegment.getProjections().getProjections()) {
            if (each instanceof ShorthandProjectionSegment) {
                table.getColumnValues().stream().map(column -> new PostgreSQLColumnDescription(column.getName(), 0, column.getDataType(), estimateColumnLength(column.getDataType()), ""))
                        .forEach(result::add);
            }
            if (each instanceof ColumnProjectionSegment) {
                ColumnProjectionSegment segment = (ColumnProjectionSegment) each;
                String columnName = segment.getColumn().getIdentifier().getValue();
                ShardingSphereColumn column = table.containsColumn(columnName) ? table.getColumn(columnName) : generateDefaultColumn(segment);
                String alias = segment.getAliasName().orElseGet(column::getName);
                result.add(new PostgreSQLColumnDescription(alias, 0, column.getDataType(), estimateColumnLength(column.getDataType()), ""));
            }
            if (each instanceof ExpressionProjectionSegment) {
                result.add(convertExpressionToDescription((ExpressionProjectionSegment) each));
            }
        }
        return new PostgreSQLRowDescriptionPacket(result);
    }
    
    private ShardingSphereColumn generateDefaultColumn(final ColumnProjectionSegment segment) {
        return new ShardingSphereColumn(segment.getColumn().getIdentifier().getValue(), Types.VARCHAR, false, false, false, true, false);
    }
    
    private PostgreSQLColumnDescription convertExpressionToDescription(final ExpressionProjectionSegment expressionProjectionSegment) {
        ExpressionSegment expressionSegment = expressionProjectionSegment.getExpr();
        String columnName = expressionProjectionSegment.getAliasName().orElse(ANONYMOUS_COLUMN_NAME);
        if (expressionSegment instanceof LiteralExpressionSegment) {
            Object value = ((LiteralExpressionSegment) expressionSegment).getLiterals();
            if (value instanceof String) {
                return new PostgreSQLColumnDescription(columnName, 0, Types.VARCHAR, estimateColumnLength(Types.VARCHAR), "");
            }
            if (value instanceof Integer) {
                return new PostgreSQLColumnDescription(columnName, 0, Types.INTEGER, estimateColumnLength(Types.INTEGER), "");
            }
            if (value instanceof Long) {
                return new PostgreSQLColumnDescription(columnName, 0, Types.BIGINT, estimateColumnLength(Types.BIGINT), "");
            }
            if (value instanceof Number) {
                return new PostgreSQLColumnDescription(columnName, 0, Types.NUMERIC, estimateColumnLength(Types.NUMERIC), "");
            }
        }
        return new PostgreSQLColumnDescription(columnName, 0, Types.VARCHAR, estimateColumnLength(Types.VARCHAR), "");
    }
    
    private int estimateColumnLength(final int jdbcType) {
        switch (jdbcType) {
            case Types.SMALLINT:
                return 2;
            case Types.INTEGER:
                return 4;
            case Types.BIGINT:
                return 8;
            default:
                return -1;
        }
    }
    
    private void tryDescribePreparedStatementByJDBC(final PostgreSQLServerPreparedStatement logicPreparedStatement) throws SQLException {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String databaseName = connectionSession.getDatabaseName();
        SQLStatementContext sqlStatementContext =
                SQLStatementContextFactory.newInstance(metaDataContexts.getMetaData(), logicPreparedStatement.getSqlStatementContext().getSqlStatement(), databaseName);
        QueryContext queryContext = new QueryContext(sqlStatementContext, logicPreparedStatement.getSql(), Collections.emptyList(), logicPreparedStatement.getHintValueContext());
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(databaseName);
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(
                queryContext, database, metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps(), connectionSession.getConnectionContext());
        ExecutionUnit executionUnitSample = executionContext.getExecutionUnits().iterator().next();
        ProxyDatabaseConnectionManager databaseConnectionManager = connectionSession.getDatabaseConnectionManager();
        Connection connection = databaseConnectionManager.getConnections(executionUnitSample.getDataSourceName(), 0, 1, ConnectionMode.CONNECTION_STRICTLY).iterator().next();
        try (PreparedStatement actualPreparedStatement = connection.prepareStatement(executionUnitSample.getSqlUnit().getSql())) {
            populateParameterTypes(logicPreparedStatement, actualPreparedStatement);
            populateColumnTypes(logicPreparedStatement, actualPreparedStatement);
        }
    }
    
    private void populateParameterTypes(final PostgreSQLServerPreparedStatement logicPreparedStatement, final PreparedStatement actualPreparedStatement) throws SQLException {
        if (0 == logicPreparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount()
                || logicPreparedStatement.getParameterTypes().stream().noneMatch(each -> PostgreSQLColumnType.UNSPECIFIED == each)) {
            return;
        }
        ParameterMetaData parameterMetaData = actualPreparedStatement.getParameterMetaData();
        for (int i = 0; i < logicPreparedStatement.getSqlStatementContext().getSqlStatement().getParameterCount(); i++) {
            if (PostgreSQLColumnType.UNSPECIFIED == logicPreparedStatement.getParameterTypes().get(i)) {
                logicPreparedStatement.getParameterTypes().set(i, PostgreSQLColumnType.valueOfJDBCType(parameterMetaData.getParameterType(i + 1), parameterMetaData.getParameterTypeName(i + 1)));
            }
        }
    }
    
    private void populateColumnTypes(final PostgreSQLServerPreparedStatement logicPreparedStatement, final PreparedStatement actualPreparedStatement) throws SQLException {
        if (logicPreparedStatement.describeRows().isPresent()) {
            return;
        }
        ResultSetMetaData resultSetMetaData = actualPreparedStatement.getMetaData();
        if (null == resultSetMetaData) {
            logicPreparedStatement.setRowDescription(PostgreSQLNoDataPacket.getInstance());
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
        logicPreparedStatement.setRowDescription(new PostgreSQLRowDescriptionPacket(columnDescriptions));
    }
}
