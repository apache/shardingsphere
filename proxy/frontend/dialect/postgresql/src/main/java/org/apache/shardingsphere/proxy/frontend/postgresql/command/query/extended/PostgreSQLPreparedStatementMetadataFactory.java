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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLBinaryColumnType;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * Metadata factory for PostgreSQL prepared statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLPreparedStatementMetadataFactory {
    
    /**
     * Load actual prepared statement for metadata access.
     *
     * @param connectionSession connection session
     * @param preparedStatement prepared statement
     * @return optional actual prepared statement
     * @throws SQLException SQL exception
     */
    public static Optional<PreparedStatement> load(final ConnectionSession connectionSession, final PostgreSQLServerPreparedStatement preparedStatement) throws SQLException {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, connectionSession.getCurrentDatabaseName(), preparedStatement.getHintValueContext())
                .bind(preparedStatement.getSqlStatementContext().getSqlStatement());
        Optional<List<Object>> placeholderParameters = createPlaceholderParameters(metaData, connectionSession.getCurrentDatabaseName(), preparedStatement, sqlStatementContext);
        if (!placeholderParameters.isPresent()) {
            return Optional.empty();
        }
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).bindParameters(placeholderParameters.get());
        }
        QueryContext queryContext = new QueryContext(
                sqlStatementContext, preparedStatement.getSql(), placeholderParameters.get(),
                preparedStatement.getHintValueContext(), connectionSession.getConnectionContext(), metaData);
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), metaData.getProps());
        ShardingSpherePreconditions.checkNotEmpty(executionContext.getExecutionUnits(),
                () -> new SQLException("Can not resolve PostgreSQL prepared statement metadata because no execution unit was generated."));
        ExecutionUnit executionUnit = executionContext.getExecutionUnits().iterator().next();
        ProxyDatabaseConnectionManager databaseConnectionManager = connectionSession.getDatabaseConnectionManager();
        return Optional.of(databaseConnectionManager.getConnections(connectionSession.getUsedDatabaseName(), executionUnit.getDataSourceName(), 0, 1, ConnectionMode.CONNECTION_STRICTLY)
                .iterator().next().prepareStatement(executionUnit.getSqlUnit().getSql()));
    }
    
    private static Optional<List<Object>> createPlaceholderParameters(final ShardingSphereMetaData metaData, final String currentDatabaseName,
                                                                      final PostgreSQLServerPreparedStatement preparedStatement, final SQLStatementContext sqlStatementContext) {
        int parameterCount = sqlStatementContext.getSqlStatement().getParameterCount();
        if (0 == parameterCount) {
            return Optional.of(Collections.emptyList());
        }
        List<ShardingSphereColumn> columns = new ArrayList<>(Collections.nCopies(parameterCount, null));
        Collection<Integer> requiredParameterIndexes = new LinkedHashSet<>(parameterCount, 1F);
        Optional<ShardingSphereSchema> schema = findSchema(metaData, currentDatabaseName, sqlStatementContext);
        if (schema.isPresent()) {
            if (sqlStatementContext instanceof InsertStatementContext) {
                collectColumnsForInsert((InsertStatementContext) sqlStatementContext, schema.get(), columns, requiredParameterIndexes);
            }
            if (sqlStatementContext instanceof UpdateStatementContext) {
                collectColumnsForUpdateAssignments((UpdateStatementContext) sqlStatementContext, schema.get(), columns, requiredParameterIndexes);
            }
            if (sqlStatementContext instanceof WhereContextAvailable) {
                collectColumnsForWhereSegments(sqlStatementContext, schema.get(), columns, requiredParameterIndexes);
            }
        }
        if (!canInferRequiredPlaceholders(requiredParameterIndexes, columns)) {
            return Optional.empty();
        }
        List<Object> result = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            result.add(createPlaceholderParameter(columns.get(i), preparedStatement.getParameterTypes().get(i)));
        }
        return Optional.of(result);
    }
    
    private static Optional<ShardingSphereSchema> findSchema(final ShardingSphereMetaData metaData, final String currentDatabaseName, final SQLStatementContext sqlStatementContext) {
        TablesContext tablesContext = sqlStatementContext.getTablesContext();
        String databaseName = tablesContext.getDatabaseName().orElse(currentDatabaseName);
        if (null == databaseName || !metaData.containsDatabase(databaseName)) {
            return Optional.empty();
        }
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        String defaultSchema = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(databaseName);
        String schemaName = tablesContext.getSchemaName().orElse(defaultSchema);
        return database.containsSchema(schemaName) ? Optional.of(database.getSchema(schemaName)) : Optional.empty();
    }
    
    private static void collectColumnsForInsert(final InsertStatementContext sqlStatementContext, final ShardingSphereSchema schema,
                                                final List<ShardingSphereColumn> columns, final Collection<Integer> requiredParameterIndexes) {
        Optional<String> tableName = sqlStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue());
        if (!tableName.isPresent() || !schema.containsTable(tableName.get())) {
            return;
        }
        ShardingSphereTable table = schema.getTable(tableName.get());
        for (InsertValueContext each : sqlStatementContext.getInsertValueContexts()) {
            collectColumnsForInsertValue(each, table, sqlStatementContext.getColumnNames(), columns, requiredParameterIndexes);
        }
    }
    
    private static void collectColumnsForInsertValue(final InsertValueContext insertValueContext, final ShardingSphereTable table, final List<String> insertColumnNames,
                                                     final List<ShardingSphereColumn> columns, final Collection<Integer> requiredParameterIndexes) {
        List<ExpressionSegment> valueExpressions = insertValueContext.getValueExpressions();
        for (int index = 0; index < valueExpressions.size() && index < insertColumnNames.size(); index++) {
            collectRequiredParameterIndexes(requiredParameterIndexes, valueExpressions.get(index));
            setParameterMarkerColumns(columns, valueExpressions.get(index), getColumn(table, insertColumnNames.get(index)).orElse(null));
        }
    }
    
    private static void collectColumnsForUpdateAssignments(final UpdateStatementContext sqlStatementContext, final ShardingSphereSchema schema,
                                                           final List<ShardingSphereColumn> columns, final Collection<Integer> requiredParameterIndexes) {
        for (ColumnAssignmentSegment each : sqlStatementContext.getSqlStatement().getSetAssignment().getAssignments()) {
            collectRequiredParameterIndexes(requiredParameterIndexes, each.getValue());
            setParameterMarkerColumns(columns, each.getValue(), resolveColumn(schema, sqlStatementContext, each.getColumns().get(0)));
        }
    }
    
    private static void collectColumnsForWhereSegments(final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema,
                                                       final List<ShardingSphereColumn> columns, final Collection<Integer> requiredParameterIndexes) {
        for (WhereSegment each : ((WhereContextAvailable) sqlStatementContext).getWhereSegments()) {
            collectRequiredParameterIndexes(requiredParameterIndexes, each.getExpr());
            collectColumnsForWhereExpression(each.getExpr(), sqlStatementContext, schema, columns);
        }
    }
    
    private static void collectColumnsForWhereExpression(final ExpressionSegment expression,
                                                         final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<ShardingSphereColumn> columns) {
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpression = (BinaryOperationExpression) expression;
            collectColumnsForWhereExpression(binaryExpression.getLeft(), sqlStatementContext, schema, columns);
            collectColumnsForWhereExpression(binaryExpression.getRight(), sqlStatementContext, schema, columns);
            setParameterMarkerColumns(columns, binaryExpression.getRight(), resolveColumn(schema, sqlStatementContext, binaryExpression.getLeft()));
            setParameterMarkerColumns(columns, binaryExpression.getLeft(), resolveColumn(schema, sqlStatementContext, binaryExpression.getRight()));
            return;
        }
        if (expression instanceof InExpression) {
            InExpression inExpression = (InExpression) expression;
            ShardingSphereColumn column = resolveColumn(schema, sqlStatementContext, inExpression.getLeft());
            if (null != column) {
                for (ExpressionSegment each : inExpression.getExpressionList()) {
                    setParameterMarkerColumns(columns, each, column);
                }
            }
            return;
        }
        if (expression instanceof BetweenExpression) {
            BetweenExpression betweenExpression = (BetweenExpression) expression;
            ShardingSphereColumn column = resolveColumn(schema, sqlStatementContext, betweenExpression.getLeft());
            if (null != column) {
                setParameterMarkerColumns(columns, betweenExpression.getBetweenExpr(), column);
                setParameterMarkerColumns(columns, betweenExpression.getAndExpr(), column);
            }
        }
    }
    
    private static ShardingSphereColumn resolveColumn(final ShardingSphereSchema schema, final SQLStatementContext sqlStatementContext, final ExpressionSegment expressionSegment) {
        return expressionSegment instanceof ColumnSegment ? resolveColumn(schema, sqlStatementContext, (ColumnSegment) expressionSegment) : null;
    }
    
    private static ShardingSphereColumn resolveColumn(final ShardingSphereSchema schema, final SQLStatementContext sqlStatementContext, final ColumnSegment columnSegment) {
        String tableName = Optional.ofNullable(columnSegment.getColumnBoundInfo()).map(optional -> optional.getOriginalTable().getValue()).orElse("");
        if (tableName.isEmpty()) {
            tableName = columnSegment.getOwner().map(optional -> optional.getIdentifier().getValue()).orElseGet(() -> getSingleTableName(sqlStatementContext).orElse(""));
        }
        if (tableName.isEmpty() || !schema.containsTable(tableName)) {
            return null;
        }
        ShardingSphereTable table = schema.getTable(tableName);
        String columnName = Optional.ofNullable(columnSegment.getColumnBoundInfo()).map(optional -> optional.getOriginalColumn().getValue()).orElse("");
        if (columnName.isEmpty()) {
            columnName = columnSegment.getIdentifier().getValue();
        }
        return table.containsColumn(columnName) ? table.getColumn(columnName) : null;
    }
    
    private static Optional<String> getSingleTableName(final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        return 1 == tableNames.size() ? Optional.of(tableNames.iterator().next()) : Optional.empty();
    }
    
    private static Optional<ShardingSphereColumn> getColumn(final ShardingSphereTable table, final String columnName) {
        return table.containsColumn(columnName) ? Optional.of(table.getColumn(columnName)) : Optional.empty();
    }
    
    private static void setParameterMarkerColumns(final List<ShardingSphereColumn> columns, final ExpressionSegment expression, final ShardingSphereColumn column) {
        if (null == column) {
            return;
        }
        for (ParameterMarkerExpressionSegment each : ExpressionExtractor.getParameterMarkerExpressions(Collections.singleton(expression))) {
            if (each.getParameterMarkerIndex() < columns.size()) {
                columns.set(each.getParameterMarkerIndex(), column);
            }
        }
    }
    
    private static void collectRequiredParameterIndexes(final Collection<Integer> requiredParameterIndexes, final ExpressionSegment expression) {
        for (ParameterMarkerExpressionSegment each : ExpressionExtractor.getParameterMarkerExpressions(Collections.singleton(expression))) {
            requiredParameterIndexes.add(each.getParameterMarkerIndex());
        }
    }
    
    private static boolean canInferRequiredPlaceholders(final Collection<Integer> requiredParameterIndexes, final List<ShardingSphereColumn> columns) {
        for (int each : requiredParameterIndexes) {
            if (each >= columns.size() || null == columns.get(each)) {
                return false;
            }
        }
        return true;
    }
    
    private static Object createPlaceholderParameter(final ShardingSphereColumn column, final PostgreSQLBinaryColumnType parameterType) {
        return null != column ? createPlaceholderParameter(column.getDataType()) : createPlaceholderParameter(parameterType);
    }
    
    private static Object createPlaceholderParameter(final PostgreSQLBinaryColumnType parameterType) {
        switch (parameterType) {
            case INT2:
            case INT4:
            case OID:
                return 0;
            case INT8:
                return 0;
            case NUMERIC:
                return BigDecimal.ZERO;
            case FLOAT4:
                return 0F;
            case FLOAT8:
                return 0D;
            case BOOL:
                return false;
            case DATE:
                return java.sql.Date.valueOf("1970-01-01");
            case TIME:
            case TIMETZ:
                return Time.valueOf("00:00:00");
            case TIMESTAMP:
            case TIMESTAMPTZ:
                return Timestamp.valueOf("1970-01-01 00:00:00");
            case UNSPECIFIED:
                return 0;
            default:
                return "";
        }
    }
    
    private static Object createPlaceholderParameter(final int jdbcType) {
        switch (jdbcType) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return 0;
            case Types.BIGINT:
                return 0;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return BigDecimal.ZERO;
            case Types.REAL:
            case Types.FLOAT:
                return 0F;
            case Types.DOUBLE:
                return 0D;
            case Types.BIT:
            case Types.BOOLEAN:
                return false;
            case Types.DATE:
                return java.sql.Date.valueOf("1970-01-01");
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return Time.valueOf("00:00:00");
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return Timestamp.valueOf("1970-01-01 00:00:00");
            default:
                return "";
        }
    }
}
