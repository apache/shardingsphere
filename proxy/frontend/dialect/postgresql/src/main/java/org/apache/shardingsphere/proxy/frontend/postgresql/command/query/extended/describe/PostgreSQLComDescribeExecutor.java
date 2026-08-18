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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.column.ColumnNotFoundException;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLColumnDescription;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLRowDescriptionPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.database.protocol.postgresql.type.ColumnTypeOIDLoader;
import org.apache.shardingsphere.database.protocol.postgresql.type.PostgreSQLColumnTypeOIDResolver;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLPreparedStatementMetadataFactory;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLPreparedStatementParameterTypeResolver;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        if (preparedStatement.getParameterTypes().stream().anyMatch(each -> PostgreSQLBinaryColumnType.UNSPECIFIED == each) || !preparedStatement.describeRows().isPresent()) {
            tryDescribePreparedStatement(preparedStatement);
        }
        result.add(preparedStatement.describeParameters());
        preparedStatement.describeRows().ifPresent(result::add);
        return result;
    }
    
    private void tryDescribePreparedStatement(final PostgreSQLServerPreparedStatement preparedStatement) throws SQLException {
        PostgreSQLPreparedStatementParameterTypeResolver.tryResolveFromBoundAST(connectionSession, preparedStatement);
        SQLStatement sqlStatement = preparedStatement.getSqlStatementContext().getSqlStatement();
        if (sqlStatement instanceof InsertStatement && describeDMLStatementByDatabaseMetaData(preparedStatement, ((InsertStatement) sqlStatement).getReturning().orElse(null))) {
            return;
        }
        if (sqlStatement instanceof UpdateStatement && describeDMLStatementByDatabaseMetaData(preparedStatement, ((UpdateStatement) sqlStatement).getReturning().orElse(null))) {
            return;
        }
        if (sqlStatement instanceof DeleteStatement && describeDMLStatementByDatabaseMetaData(preparedStatement, ((DeleteStatement) sqlStatement).getReturning().orElse(null))) {
            return;
        }
        tryDescribePreparedStatementByJDBC(preparedStatement);
    }
    
    private boolean describeDMLStatementByDatabaseMetaData(final PostgreSQLServerPreparedStatement preparedStatement, final ReturningSegment returningSegment) {
        if (!preparedStatement.isParameterTypesResolved()) {
            return false;
        }
        if (null != returningSegment) {
            Optional<PostgreSQLRowDescriptionPacket> rowDescription = describeReturning(returningSegment);
            if (!rowDescription.isPresent()) {
                return false;
            }
            preparedStatement.setRowDescription(rowDescription.get());
        } else {
            preparedStatement.setRowDescription(PostgreSQLNoDataPacket.getInstance());
        }
        return true;
    }
    
    private ShardingSphereTable getTableFromMetaData(final String databaseName, final String schemaName, final String logicTableName) {
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getDatabase(databaseName);
        String actualSchemaName = null == schemaName ? new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(databaseName) : schemaName;
        return database.getSchema(actualSchemaName).getTable(logicTableName);
    }

    private List<ShardingSphereIdentifier> getColumnNamesOfInsertStatement(final InsertStatement insertStatement, final ShardingSphereTable table) {
        return insertStatement.getColumns().isEmpty()
                ? table.getColumnNames()
                : insertStatement.getColumns().stream().map(each -> new ShardingSphereIdentifier(each.getIdentifier().getValue())).collect(Collectors.toList());
    }
    
    private Optional<PostgreSQLRowDescriptionPacket> describeReturning(final ReturningSegment returningSegment) {
        Collection<PostgreSQLColumnDescription> result = new LinkedList<>();
        for (ProjectionSegment each : returningSegment.getProjections().getProjections()) {
            if (each instanceof ShorthandProjectionSegment) {
                return Optional.empty();
            }
            if (each instanceof ColumnProjectionSegment) {
                ColumnProjectionSegment segment = (ColumnProjectionSegment) each;
                ColumnSegmentBoundInfo boundInfo = segment.getColumn().getColumnBoundInfo();
                if (!PostgreSQLPreparedStatementParameterTypeResolver.isIncompleteBoundInfo(boundInfo)) {
                    return Optional.empty();
                }
                ShardingSphereTable table = getTableFromMetaData(boundInfo.getOriginalDatabase().getValue(), boundInfo.getOriginalSchema().getValue(), boundInfo.getOriginalTable().getValue());
                if (null == table || !table.containsColumn(boundInfo.getOriginalColumn().getValue())) {
                    return Optional.empty();
                }
                ShardingSphereColumn column = table.getColumn(boundInfo.getOriginalColumn().getValue());
                String alias = segment.getAliasName().orElseGet(column::getName);
                result.add(new PostgreSQLColumnDescription(alias, 0, column.getDataType(), estimateColumnLength(column.getDataType()), column.getTypeName()));
            }
            if (each instanceof ExpressionProjectionSegment) {
                Optional<PostgreSQLColumnDescription> columnDescription = convertExpressionToDescription((ExpressionProjectionSegment) each);
                if (!columnDescription.isPresent()) {
                    return Optional.empty();
                }
                result.add(columnDescription.get());
            }
        }
        return Optional.of(new PostgreSQLRowDescriptionPacket(result));
    }
    
    private Optional<PostgreSQLColumnDescription> convertExpressionToDescription(final ExpressionProjectionSegment expressionProjectionSegment) {
        ExpressionSegment expressionSegment = expressionProjectionSegment.getExpr();
        String columnName = expressionProjectionSegment.getAliasName().orElse(ANONYMOUS_COLUMN_NAME);
        if (expressionSegment instanceof LiteralExpressionSegment) {
            Object value = ((LiteralExpressionSegment) expressionSegment).getLiterals();
            if (value instanceof String) {
                return Optional.of(new PostgreSQLColumnDescription(columnName, 0, Types.VARCHAR, estimateColumnLength(Types.VARCHAR), ""));
            }
            if (value instanceof Integer) {
                return Optional.of(new PostgreSQLColumnDescription(columnName, 0, Types.INTEGER, estimateColumnLength(Types.INTEGER), ""));
            }
            if (value instanceof Long) {
                return Optional.of(new PostgreSQLColumnDescription(columnName, 0, Types.BIGINT, estimateColumnLength(Types.BIGINT), ""));
            }
            if (value instanceof Number) {
                return Optional.of(new PostgreSQLColumnDescription(columnName, 0, Types.NUMERIC, estimateColumnLength(Types.NUMERIC), ""));
            }
        }
        return Optional.empty();
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
        try (PreparedStatement actualPreparedStatement = PostgreSQLPreparedStatementMetadataFactory.load(connectionSession, logicPreparedStatement)) {
            PostgreSQLPreparedStatementParameterTypeResolver.resolveParameterTypes(logicPreparedStatement, actualPreparedStatement);
            populateColumnTypes(logicPreparedStatement, actualPreparedStatement);
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
        Map<Integer, Integer> typeOIDs = ColumnTypeOIDLoader.load(actualPreparedStatement.getConnection(), resultSetMetaData, new PostgreSQLColumnTypeOIDResolver());
        List<PostgreSQLColumnDescription> columnDescriptions = new ArrayList<>(resultSetMetaData.getColumnCount());
        for (int columnIndex = 1; columnIndex <= resultSetMetaData.getColumnCount(); columnIndex++) {
            String columnName = resultSetMetaData.getColumnName(columnIndex);
            int columnType = resultSetMetaData.getColumnType(columnIndex);
            int columnLength = resultSetMetaData.getColumnDisplaySize(columnIndex);
            String columnTypeName = resultSetMetaData.getColumnTypeName(columnIndex);
            columnDescriptions.add(typeOIDs.containsKey(columnIndex)
                    ? new PostgreSQLColumnDescription(columnName, columnIndex, typeOIDs.get(columnIndex), columnLength, PostgreSQLValueFormat.TEXT.getCode())
                    : new PostgreSQLColumnDescription(columnName, columnIndex, columnType, columnLength, columnTypeName));
        }
        logicPreparedStatement.setRowDescription(new PostgreSQLRowDescriptionPacket(columnDescriptions));
    }
}
