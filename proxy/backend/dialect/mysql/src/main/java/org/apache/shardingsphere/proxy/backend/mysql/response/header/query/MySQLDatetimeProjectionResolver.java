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

package org.apache.shardingsphere.proxy.backend.mysql.response.header.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import javax.annotation.Nullable;
import java.sql.Types;
import java.util.Collection;
import java.util.Optional;

/**
 * MySQL DATETIME projection resolver.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLDatetimeProjectionResolver {
    
    /**
     * Judge whether a result projection uses a MySQL DATETIME declaration.
     *
     * @param selectStatementContext select statement context
     * @param database database
     * @param columnIndex column index, starting at 1
     * @return whether DATETIME is inferred or retained from the first projection when inference is unavailable
     */
    public static boolean isDatetime(final SelectStatementContext selectStatementContext, final ShardingSphereDatabase database, final int columnIndex) {
        Optional<MySQLBinaryColumnType> columnType = resolveSelect(selectStatementContext, database, columnIndex, true);
        return columnType.isPresent() && MySQLBinaryColumnType.DATETIME == columnType.get();
    }
    
    private static Optional<MySQLBinaryColumnType> resolveSelect(final SelectStatementContext selectStatementContext, final ShardingSphereDatabase database, final int columnIndex,
                                                                 final boolean retainDeclaration) {
        SelectStatement selectStatement = selectStatementContext.getSqlStatement();
        if (selectStatement.getCombine().isPresent()) {
            return resolveCombine(selectStatementContext, database, columnIndex, retainDeclaration);
        }
        ProjectionsContext projectionsContext = selectStatementContext.getProjectionsContext();
        int projectionCount = projectionsContext.getExpandProjections().size();
        if (columnIndex > projectionCount || projectionCount < selectStatement.getProjections().getProjections().size()) {
            return Optional.empty();
        }
        return resolveProjection(selectStatementContext, projectionsContext.getExpandProjections().get(columnIndex - 1), database, retainDeclaration);
    }
    
    private static Optional<MySQLBinaryColumnType> resolveCombine(final SelectStatementContext selectStatementContext, final ShardingSphereDatabase database, final int columnIndex,
                                                                  final boolean retainDeclaration) {
        CombineSegment combine = selectStatementContext.getSqlStatement().getCombine().get();
        SelectStatementContext leftContext = selectStatementContext.getSubqueryContexts().get(combine.getLeft().getStartIndex());
        Optional<MySQLBinaryColumnType> leftType = resolveSelect(leftContext, database, columnIndex, retainDeclaration);
        if (!leftType.isPresent() || retainDeclaration && MySQLBinaryColumnType.DATETIME == leftType.get()) {
            return leftType;
        }
        SelectStatementContext rightContext = selectStatementContext.getSubqueryContexts().get(combine.getRight().getStartIndex());
        Optional<MySQLBinaryColumnType> rightType = resolveRightType(leftContext, rightContext, database, columnIndex);
        if (!rightType.isPresent()) {
            return retainDeclaration ? leftType : Optional.empty();
        }
        if (MySQLBinaryColumnType.NULL == rightType.get() || leftType.get() == rightType.get()) {
            return leftType;
        }
        if (!isMySQLStorage(database)) {
            return retainDeclaration ? leftType : Optional.empty();
        }
        return Optional.of(mergeTemporalTypes(leftType.get(), rightType.get()));
    }
    
    private static Optional<MySQLBinaryColumnType> resolveRightType(final SelectStatementContext leftContext, final SelectStatementContext rightContext,
                                                                    final ShardingSphereDatabase database, final int columnIndex) {
        if (leftContext.getProjectionsContext().getExpandProjections().size() != rightContext.getProjectionsContext().getExpandProjections().size()) {
            return Optional.empty();
        }
        return resolveSelect(rightContext, database, columnIndex, false);
    }
    
    private static boolean isMySQLStorage(final ShardingSphereDatabase database) {
        Collection<StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits().values();
        if (storageUnits.isEmpty()) {
            return false;
        }
        for (StorageUnit each : storageUnits) {
            if (!"MySQL".equals(each.getStorageType().getType())) {
                return false;
            }
        }
        return true;
    }
    
    private static MySQLBinaryColumnType mergeTemporalTypes(final MySQLBinaryColumnType leftType, final MySQLBinaryColumnType rightType) {
        if (MySQLBinaryColumnType.NULL == leftType) {
            return rightType;
        }
        return MySQLBinaryColumnType.NULL == rightType || leftType == rightType ? leftType : MySQLBinaryColumnType.DATETIME;
    }
    
    private static Optional<MySQLBinaryColumnType> resolveProjection(final SelectStatementContext selectStatementContext, final Projection projection,
                                                                     final ShardingSphereDatabase database, final boolean retainDeclaration) {
        if (projection instanceof ColumnProjection) {
            return resolveColumn(selectStatementContext, (ColumnProjection) projection, database, retainDeclaration);
        }
        if (projection instanceof AggregationProjection) {
            return resolveExpression(selectStatementContext, ((AggregationProjection) projection).getAggregationSegment(), database);
        }
        if (projection instanceof ExpressionProjection) {
            ExpressionSegment expression = ((ExpressionProjection) projection).getExpressionSegment().getExpr();
            return null == expression ? Optional.empty() : resolveExpression(selectStatementContext, expression, database);
        }
        if (!(projection instanceof SubqueryProjection)) {
            return Optional.empty();
        }
        SubquerySegment subquery = ((SubqueryProjection) projection).getSubquerySegment().getSubquery();
        return resolveSelect(selectStatementContext.getSubqueryContexts().get(subquery.getStartIndex()), database, 1, false);
    }
    
    private static Optional<MySQLBinaryColumnType> resolveColumn(final SelectStatementContext selectStatementContext, final ColumnProjection projection,
                                                                 final ShardingSphereDatabase database, final boolean retainDeclaration) {
        SelectStatement selectStatement = selectStatementContext.getSqlStatement();
        ColumnSegmentBoundInfo boundInfo = projection.getColumnBoundInfo();
        boolean bound = null != boundInfo && !boundInfo.getOriginalTable().getValue().isEmpty();
        if (!selectStatement.getFrom().isPresent()) {
            return bound ? resolveStoredColumn(projection, database, "") : Optional.empty();
        }
        TableSegment from = selectStatement.getFrom().get();
        if (bound && (from instanceof SimpleTableSegment || selectStatementContext.getSubqueryContexts().isEmpty())) {
            return resolveStoredColumn(projection, database, "");
        }
        return resolveColumnSource(selectStatementContext, projection, database, retainDeclaration, from, bound);
    }
    
    private static Optional<MySQLBinaryColumnType> resolveStoredColumn(final ColumnProjection projection, final ShardingSphereDatabase database, final String tableName) {
        ColumnSegmentBoundInfo boundInfo = projection.getColumnBoundInfo();
        if (null != boundInfo && !boundInfo.getOriginalDatabase().getValue().isEmpty() && !database.getName().equalsIgnoreCase(boundInfo.getOriginalDatabase().getValue())) {
            return Optional.empty();
        }
        String schemaName = null != boundInfo && !boundInfo.getOriginalSchema().getValue().isEmpty() ? boundInfo.getOriginalSchema().getValue() : database.getDefaultSchemaName();
        String originalTableName = null != boundInfo && !boundInfo.getOriginalTable().getValue().isEmpty() ? boundInfo.getOriginalTable().getValue() : tableName;
        String columnName = null != boundInfo && !boundInfo.getOriginalTable().getValue().isEmpty() ? boundInfo.getOriginalColumn().getValue() : projection.getName().getValue();
        ShardingSphereSchema schema = database.getSchema(schemaName);
        return Optional.ofNullable(schema).map(optional -> optional.getTable(originalTableName)).map(optional -> optional.getColumn(columnName))
                .flatMap(MySQLDatetimeProjectionResolver::resolveColumnType);
    }
    
    private static Optional<MySQLBinaryColumnType> resolveColumnType(final ShardingSphereColumn column) {
        switch (column.getDataType()) {
            case Types.TIMESTAMP:
                return Optional.of(column.isDatetime() ? MySQLBinaryColumnType.DATETIME : MySQLBinaryColumnType.TIMESTAMP);
            case Types.DATE:
                return Optional.of(MySQLBinaryColumnType.DATE);
            case Types.TIME:
                return Optional.of(MySQLBinaryColumnType.TIME);
            default:
                return Optional.empty();
        }
    }
    
    private static Optional<MySQLBinaryColumnType> resolveColumnSource(final SelectStatementContext selectStatementContext, final ColumnProjection projection,
                                                                       final ShardingSphereDatabase database, final boolean retainDeclaration, final TableSegment from, final boolean bound) {
        ColumnSegmentBoundInfo boundInfo = projection.getColumnBoundInfo();
        String owner = projection.getOwner().map(IdentifierValue::getValue).orElse("");
        if (null != boundInfo && null != boundInfo.getOwner()) {
            owner = boundInfo.getOwner().getIdentifier().getValue();
        }
        Optional<TableSegment> table = bound && from instanceof SubqueryTableSegment ? Optional.of(from) : findSourceTable(from, owner);
        if (!table.isPresent()) {
            return bound && !containsSubquery(from) ? resolveStoredColumn(projection, database, "") : Optional.empty();
        }
        if (table.get() instanceof SubqueryTableSegment) {
            SubquerySegment subquery = ((SubqueryTableSegment) table.get()).getSubquery();
            SelectStatementContext subqueryContext = selectStatementContext.getSubqueryContexts().get(subquery.getStartIndex());
            return resolveDerivedColumn(subqueryContext, projection, database, retainDeclaration, bound);
        }
        return resolveStoredColumn(projection, database, ((SimpleTableSegment) table.get()).getTableName().getIdentifier().getValue());
    }
    
    private static Optional<TableSegment> findSourceTable(final TableSegment table, final String owner) {
        if (table instanceof JoinTableSegment) {
            Optional<TableSegment> left = findSourceTable(((JoinTableSegment) table).getLeft(), owner);
            Optional<TableSegment> right = findSourceTable(((JoinTableSegment) table).getRight(), owner);
            if (left.isPresent() == right.isPresent()) {
                return Optional.empty();
            }
            return left.isPresent() ? left : right;
        }
        if (table instanceof SubqueryTableSegment) {
            return owner.isEmpty() || table.getAliasName().map(optional -> optional.equalsIgnoreCase(owner)).orElse(false) ? Optional.of(table) : Optional.empty();
        }
        if (table instanceof SimpleTableSegment) {
            String tableOwner = table.getAliasName().orElseGet(() -> ((SimpleTableSegment) table).getTableName().getIdentifier().getValue());
            return owner.isEmpty() || tableOwner.equalsIgnoreCase(owner) ? Optional.of(table) : Optional.empty();
        }
        return Optional.empty();
    }
    
    private static boolean containsSubquery(final TableSegment table) {
        if (table instanceof SubqueryTableSegment) {
            return true;
        }
        return table instanceof JoinTableSegment && (containsSubquery(((JoinTableSegment) table).getLeft()) || containsSubquery(((JoinTableSegment) table).getRight()));
    }
    
    private static Optional<MySQLBinaryColumnType> resolveDerivedColumn(final SelectStatementContext selectStatementContext, final ColumnProjection projection,
                                                                        final ShardingSphereDatabase database, final boolean retainDeclaration, final boolean bound) {
        Integer columnIndex = selectStatementContext.getProjectionsContext().getColumnLabelAndIndexMap().get(projection.getName().getValue());
        if (null == columnIndex) {
            return Optional.empty();
        }
        Optional<MySQLBinaryColumnType> columnType = resolveSelect(selectStatementContext, database, columnIndex, retainDeclaration);
        return !columnType.isPresent() && retainDeclaration && bound ? resolveStoredColumn(projection, database, "") : columnType;
    }
    
    private static Optional<MySQLBinaryColumnType> resolveExpression(final SelectStatementContext selectStatementContext, final ExpressionSegment expression, final ShardingSphereDatabase database) {
        if (expression instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) expression;
            return resolveColumn(selectStatementContext, new ColumnProjection(column.getOwner().map(optional -> optional.getIdentifier()).orElse(null), column.getIdentifier(), null,
                    selectStatementContext.getSqlStatement().getDatabaseType(), null, null, column.getColumnBoundInfo()), database, false);
        }
        if (expression instanceof AggregationProjectionSegment) {
            AggregationProjectionSegment aggregation = (AggregationProjectionSegment) expression;
            return (AggregationType.MIN == aggregation.getType() || AggregationType.MAX == aggregation.getType()) && 1 == aggregation.getParameters().size()
                    ? resolveExpression(selectStatementContext, aggregation.getParameters().iterator().next(), database)
                    : Optional.empty();
        }
        if (expression instanceof LiteralExpressionSegment && ((LiteralExpressionSegment) expression).isNullLiteral()) {
            return Optional.of(MySQLBinaryColumnType.NULL);
        }
        if (expression instanceof CaseWhenExpression) {
            CaseWhenExpression caseWhen = (CaseWhenExpression) expression;
            return resolveResultTypes(selectStatementContext, caseWhen.getThenExprs(), caseWhen.getElseExpr(), database);
        }
        if (expression instanceof FunctionSegment) {
            return resolveFunction(selectStatementContext, (FunctionSegment) expression, database);
        }
        return Optional.empty();
    }
    
    private static Optional<MySQLBinaryColumnType> resolveResultTypes(final SelectStatementContext selectStatementContext, final Collection<ExpressionSegment> expressions,
                                                                      @Nullable final ExpressionSegment elseExpr, final ShardingSphereDatabase database) {
        MySQLBinaryColumnType result = MySQLBinaryColumnType.NULL;
        for (ExpressionSegment each : expressions) {
            Optional<MySQLBinaryColumnType> columnType = resolveExpression(selectStatementContext, each, database);
            if (!columnType.isPresent()) {
                return Optional.empty();
            }
            result = mergeTemporalTypes(result, columnType.get());
        }
        if (null != elseExpr) {
            Optional<MySQLBinaryColumnType> elseType = resolveExpression(selectStatementContext, elseExpr, database);
            if (!elseType.isPresent()) {
                return Optional.empty();
            }
            result = mergeTemporalTypes(result, elseType.get());
        }
        return MySQLBinaryColumnType.DATETIME == result && !isMySQLStorage(database) ? Optional.empty() : Optional.of(result);
    }
    
    private static Optional<MySQLBinaryColumnType> resolveFunction(final SelectStatementContext selectStatementContext, final FunctionSegment function, final ShardingSphereDatabase database) {
        if (null == function.getOwner() && "COALESCE".equalsIgnoreCase(function.getFunctionName())) {
            return resolveResultTypes(selectStatementContext, function.getParameters(), null, database);
        }
        return isCurrentTimestampFunction(function) && isMySQLStorage(database) ? Optional.of(MySQLBinaryColumnType.DATETIME) : Optional.empty();
    }
    
    private static boolean isCurrentTimestampFunction(final FunctionSegment function) {
        if (null != function.getOwner()) {
            return false;
        }
        String functionName = function.getFunctionName();
        return "CURRENT_TIMESTAMP".equalsIgnoreCase(functionName) || "NOW".equalsIgnoreCase(functionName)
                || "LOCALTIME".equalsIgnoreCase(functionName) || "LOCALTIMESTAMP".equalsIgnoreCase(functionName);
    }
}
