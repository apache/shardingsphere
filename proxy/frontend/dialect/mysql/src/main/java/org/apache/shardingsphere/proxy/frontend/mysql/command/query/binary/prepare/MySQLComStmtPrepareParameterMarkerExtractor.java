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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.syntax.column.ColumnNotFoundException;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Parameter marker extractor for MySQL COM_STMT_PREPARE.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLComStmtPrepareParameterMarkerExtractor {
    
    /**
     * Resolve columns for parameter markers.
     *
     * @param sqlStatementContext SQL statement context
     * @param schema schema
     * @return corresponding columns of parameter markers
     */
    public static List<ShardingSphereColumn> resolveColumnsForParameterMarkers(final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema) {
        int parameterCount = sqlStatementContext.getSqlStatement().getParameterCount();
        if (0 == parameterCount) {
            return Collections.emptyList();
        }
        List<ShardingSphereColumn> result = new ArrayList<>(Collections.nCopies(parameterCount, null));
        if (sqlStatementContext instanceof InsertStatementContext) {
            collectColumnsForInsert((InsertStatementContext) sqlStatementContext, schema, result);
        }
        if (sqlStatementContext instanceof UpdateStatementContext) {
            collectColumnsForUpdateAssignments((UpdateStatementContext) sqlStatementContext, schema, result);
        }
        if (sqlStatementContext instanceof WhereContextAvailable) {
            collectColumnsForWhereSegments(sqlStatementContext, schema, result);
        }
        return result;
    }
    
    private static void collectColumnsForInsert(final InsertStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<ShardingSphereColumn> columns) {
        Optional<String> tableName = sqlStatementContext.getSqlStatement().getTable().map(optional -> optional.getTableName().getIdentifier().getValue());
        if (!tableName.isPresent() || !schema.containsTable(tableName.get())) {
            return;
        }
        ShardingSphereTable table = schema.getTable(tableName.get());
        for (InsertValueContext each : sqlStatementContext.getInsertValueContexts()) {
            collectColumnsForInsertValue(each, table, sqlStatementContext.getColumnNames(), columns);
        }
        OnDuplicateUpdateContext onDuplicateUpdateContext = sqlStatementContext.getOnDuplicateKeyUpdateValueContext();
        if (null != onDuplicateUpdateContext) {
            collectColumnsForOnDuplicateUpdate(onDuplicateUpdateContext, schema, sqlStatementContext, columns);
        }
    }
    
    private static void collectColumnsForInsertValue(final InsertValueContext insertValueContext, final ShardingSphereTable table,
                                                     final List<String> insertColumnNames, final List<ShardingSphereColumn> columns) {
        List<ExpressionSegment> valueExpressions = insertValueContext.getValueExpressions();
        for (int index = 0; index < valueExpressions.size(); index++) {
            if (index < insertColumnNames.size()) {
                setParameterMarkerColumns(columns, valueExpressions.get(index), getColumn(table, insertColumnNames.get(index)));
            }
        }
    }
    
    private static void collectColumnsForOnDuplicateUpdate(final OnDuplicateUpdateContext onDuplicateUpdateContext, final ShardingSphereSchema schema,
                                                           final SQLStatementContext sqlStatementContext, final List<ShardingSphereColumn> columns) {
        for (int index = 0; index < onDuplicateUpdateContext.getValueExpressions().size(); index++) {
            setParameterMarkerColumns(columns,
                    onDuplicateUpdateContext.getValueExpressions().get(index), resolveColumn(schema, sqlStatementContext, onDuplicateUpdateContext.getColumns().get(index)));
        }
    }
    
    private static void collectColumnsForUpdateAssignments(final UpdateStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<ShardingSphereColumn> columns) {
        for (ColumnAssignmentSegment each : sqlStatementContext.getSqlStatement().getSetAssignment().getAssignments()) {
            setParameterMarkerColumns(columns, each.getValue(), resolveColumn(schema, sqlStatementContext, each.getColumns().get(0)));
        }
    }
    
    private static void collectColumnsForWhereSegments(final SQLStatementContext sqlStatementContext, final ShardingSphereSchema schema, final List<ShardingSphereColumn> columns) {
        for (WhereSegment each : ((WhereContextAvailable) sqlStatementContext).getWhereSegments()) {
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
    
    private static ShardingSphereColumn getColumn(final ShardingSphereTable table, final String columnName) {
        ShardingSpherePreconditions.checkState(table.containsColumn(columnName), () -> new ColumnNotFoundException(table.getName(), columnName));
        return table.getColumn(columnName);
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
}
