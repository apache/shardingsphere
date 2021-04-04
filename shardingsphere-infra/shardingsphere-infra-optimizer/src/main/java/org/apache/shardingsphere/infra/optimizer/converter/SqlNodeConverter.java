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

package org.apache.shardingsphere.infra.optimizer.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlSelectKeyword;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimizer.operator.BinarySqlOperator;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *  convert shardingsphere ast to calcite ast.
 */
public class SqlNodeConverter {
    
    private static final String JOIN_TYPE_INNER = "INNER";
    
    private static final String JOIN_TYPE_LEFT = "LEFT";
    
    private static final String JOIN_TYPE_RIGHT = "RIGHT";
    
    private static final String JOIN_TYPE_FULL = "FULL";
    
    /**
     * convert shardingsphere ast to calcite ast.
     * @param sqlStatement shardingsphere ast
     * @return an Optional 
     */
    public static Optional<SqlNode> convertSqlStatement(final SQLStatement sqlStatement) {
        try {
            SqlNode sqlNode = convertStatement(sqlStatement);
            return Optional.ofNullable(sqlNode);
        } catch (UnsupportedOperationException e) {
            return Optional.empty();
        }
    }

    private static SqlNode convertStatement(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return convertSelectStatement((SelectStatement) sqlStatement);
        }
        return null;
    }
    
    /**
     * convert select ast.
     * @param selectStatement select ast
     * @return calcite select ast
     */
    public static SqlNode convertSelectStatement(final SelectStatement selectStatement) {
        SqlNodeList keywordList = convertDistinct(selectStatement.getProjections());
        SqlNodeList projections = convertProjections(selectStatement.getProjections());
        SqlNode from = convertTableSegment(selectStatement.getFrom());
        SqlNode where = selectStatement.getWhere().isPresent() ? convertWhere(selectStatement.getWhere().get()) : null;
        SqlNodeList groupBy = convertGroupBy(selectStatement.getGroupBy().isPresent() ? selectStatement.getGroupBy().get() : null);
        SqlNodeList orderBy = convertOrderBy(selectStatement.getOrderBy().isPresent() ? selectStatement.getOrderBy().get() : null);
    
        Optional<LimitSegment> limitSegment = SelectStatementHandler.getLimitSegment(selectStatement);
        Map.Entry<SqlNode, SqlNode> offsetRowCount = convertPagination(limitSegment.isPresent() ? limitSegment.get() : null);
        SqlNode offset = offsetRowCount.getKey();
        SqlNode rowCount = offsetRowCount.getValue();

        return new SqlSelect(SqlParserPos.ZERO, keywordList, projections, from, where, groupBy, null,
                null, orderBy, offset, rowCount, null);
    }
    
    /**
     * convert project ast.
     * @param projections project ast
     * @return calcite project ast
     */
    public static SqlNodeList convertDistinct(final ProjectionsSegment projections) {
        if (projections.isDistinctRow()) {
            return new SqlNodeList(Arrays.asList(SqlSelectKeyword.DISTINCT.symbol(SqlParserPos.ZERO)), SqlParserPos.ZERO);
        }
        return null;
    }
    
    /**
     * convert project.
     * @param projectionsSegment project ast
     * @return calcite project ast
     */
    public static SqlNodeList convertProjections(final ProjectionsSegment projectionsSegment) {
        Collection<ProjectionSegment> projections = projectionsSegment.getProjections();
        List<SqlNode> columnNodes = new ArrayList<>(projections.size());
        for (ProjectionSegment projection : projections) {
            if (projection instanceof ColumnProjectionSegment) {
                columnNodes.add(convertColumnProjection((ColumnProjectionSegment) projection));
            } else if (projection instanceof ExpressionProjectionSegment) {
                columnNodes.add(convertExpressionProjection((ExpressionProjectionSegment) projection));
            }
            // TODO other Projection
        }
        return new SqlNodeList(columnNodes, SqlParserPos.ZERO);
    }
    
    /**
     * convert from clause.
     * @param table from 
     * @return from table <code>SqlNode</code>
     */
    public static SqlNode convertTableSegment(final TableSegment table) {
        if (table instanceof SimpleTableSegment) {
            TableNameSegment tableName = ((SimpleTableSegment) table).getTableName();
            SqlNode tableNameSqlNode = new SqlIdentifier(tableName.getIdentifier().getValue(), SqlParserPos.ZERO);
            if (table.getAlias().isPresent()) {
                SqlNode aliasIdentifier = new SqlIdentifier(table.getAlias().get(), SqlParserPos.ZERO);
                return new SqlBasicCall(SqlStdOperatorTable.AS, new SqlNode[] {tableNameSqlNode, aliasIdentifier}, SqlParserPos.ZERO);
            } else {
                return tableNameSqlNode;
            }
        } else if (table instanceof JoinTableSegment) {
            return convertJoin((JoinTableSegment) table);
        } else if (table instanceof SubqueryTableSegment) {
            return convertSubquery((SubqueryTableSegment) table);
        }
        throw new UnsupportedOperationException("unsupportd TableSegment type: " + table.getClass());
    }
    
    /**
     * convert where clause.
     * @param where where clause
     * @return where <code>SqlNode</code>
     */
    public static SqlNode convertWhere(final WhereSegment where) {
        ExpressionSegment whereExpr = where.getExpr();
        return convertExpression(whereExpr);
    }
    
    /**
     * convert group by clause.
     * @param groupBy  group by 
     * @return group by <code>SqlNode</code>
     */
    public static SqlNodeList convertGroupBy(final GroupBySegment groupBy) {
        if (groupBy == null || groupBy.getGroupByItems() == null || groupBy.getGroupByItems().isEmpty()) {
            return null;
        }
        Collection<OrderByItemSegment> groupByItems = groupBy.getGroupByItems();
        
        // TODO group by having is not supported yet.
        List<SqlNode> groupBySqlNodes = convertOrderByItems(groupByItems);
        return new SqlNodeList(groupBySqlNodes, SqlParserPos.ZERO);
    }
    
    /**
     * convert order by clause.
     * @param orderBy order by clause
     * @return order by <code>SqlNode</code>, or null if order by does not exist
     */
    public static SqlNodeList convertOrderBy(final OrderBySegment orderBy) {
        if (orderBy == null) {
            return null;
        }
        List<SqlNode> orderBySqlNodes = convertOrderByItems(orderBy.getOrderByItems());
        return new SqlNodeList(orderBySqlNodes, SqlParserPos.ZERO);
    }
    
    /**
     * convert pagination.
     * @param limitSegment pagination clause
     * @return offset and fetch <code>SqlNode</code>.
     */
    public static Map.Entry<SqlNode, SqlNode> convertPagination(final LimitSegment limitSegment) {
        if (limitSegment == null) {
            return new SimpleEntry<>(null, null);
        }
    
        SqlNode offsetSqlNode = null;
        SqlNode fetchSqlNode = null;
        
        Optional<PaginationValueSegment> offset = limitSegment.getOffset();
        Optional<PaginationValueSegment> fetch = limitSegment.getRowCount();
        
        if (offset.isPresent()) {
            offsetSqlNode = convertLimit(offset.get());
        }
        
        if (fetch.isPresent()) {
            fetchSqlNode = convertLimit(fetch.get());
        }
        return new SimpleEntry<>(offsetSqlNode, fetchSqlNode);
    }
    
    private static SqlNode convertLimit(final PaginationValueSegment paginationValue) {
        if (paginationValue instanceof NumberLiteralPaginationValueSegment) {
            NumberLiteralPaginationValueSegment offsetValue = (NumberLiteralPaginationValueSegment) paginationValue;
            return SqlLiteral.createExactNumeric(String.valueOf(offsetValue.getValue()), SqlParserPos.ZERO);
        } else {
            ParameterMarkerLimitValueSegment offsetParam = (ParameterMarkerLimitValueSegment) paginationValue;
            return new SqlDynamicParam(offsetParam.getParameterIndex(), SqlParserPos.ZERO);
        }
    }
    
    private static SqlNode convertColumnProjection(final ColumnProjectionSegment columnProjection) {
        ColumnSegment column = columnProjection.getColumn();
        return convertColumnSegment(column);
    }

    private static SqlNode convertExpressionProjection(final ExpressionProjectionSegment expressionProjection) {
        // TODO expression has not been parsed now.
        String expression = expressionProjection.getText();
        return SqlCharStringLiteral.createCharString(expression, SqlParserPos.ZERO);
    }

    private static List<SqlNode> convertOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        List<SqlNode> sqlNodes = Lists.newArrayList();
        for (OrderByItemSegment orderByItemSegment : orderByItems) {
            if (orderByItemSegment instanceof ColumnOrderByItemSegment) {
                sqlNodes.add(convertColumnOrderByToSqlNode((ColumnOrderByItemSegment) orderByItemSegment));
            } else if (orderByItemSegment instanceof ExpressionOrderByItemSegment) {
                sqlNodes.add(convertExpressionOrderByToSqlNode((ExpressionOrderByItemSegment) orderByItemSegment));
            } else if (orderByItemSegment instanceof IndexOrderByItemSegment) {
                sqlNodes.add(convertIndexOrderByToSqlNode((IndexOrderByItemSegment) orderByItemSegment));
            } else if (orderByItemSegment instanceof TextOrderByItemSegment) {
                sqlNodes.add(convertTextOrderByToSqlNode((TextOrderByItemSegment) orderByItemSegment));
            }
        }
        return sqlNodes;
    }

    private static SqlNode convertColumnOrderByToSqlNode(final ColumnOrderByItemSegment columnOrderBy) {
        SqlNode sqlNode = convertColumnSegment(columnOrderBy.getColumn());
        if (Objects.equals(OrderDirection.DESC, columnOrderBy.getOrderDirection())) {
            sqlNode = new SqlBasicCall(SqlStdOperatorTable.DESC, new SqlNode[] {sqlNode}, SqlParserPos.ZERO);
        }
        return sqlNode;
    }
    
    private static SqlNode convertExpressionOrderByToSqlNode(final ExpressionOrderByItemSegment expressionOrderBy) {
        // TODO 
        throw new UnsupportedOperationException("unsupported expression order by");
    }
    
    private static SqlNode convertIndexOrderByToSqlNode(final IndexOrderByItemSegment indexOrderBy) {
        // TODO
        throw new UnsupportedOperationException("unsupported index order by");
    }
    
    private static SqlNode convertTextOrderByToSqlNode(final TextOrderByItemSegment textOrderBy) {
        throw new UnsupportedOperationException("unsupported text order by");
    }
    
    private static SqlNode convertSubquery(final SubqueryTableSegment subquery) {
        // TODO
        throw new UnsupportedOperationException("subquery is not supported");
    }
    
    protected static SqlNode convertJoin(final JoinTableSegment join) {
        String joinType = join.getJoinType();
        SqlNode left = convertTableSegment(join.getLeft());
        SqlNode right = convertTableSegment(join.getRight());
        ExpressionSegment expressionSegment = join.getCondition();
        SqlNode condition = convertExpression(expressionSegment);
    
        SqlLiteral conditionType = condition == null ? JoinConditionType.NONE.symbol(SqlParserPos.ZERO)
                : JoinConditionType.ON.symbol(SqlParserPos.ZERO);
    
        SqlLiteral joinTypeSqlNode;
        if (joinType == null) {
            joinTypeSqlNode = JoinType.COMMA.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_INNER.equals(joinType)) {
            joinTypeSqlNode = JoinType.INNER.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_LEFT.equals(joinType)) {
            joinTypeSqlNode = JoinType.LEFT.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_RIGHT.equals(joinType)) {
            joinTypeSqlNode = JoinType.RIGHT.symbol(SqlParserPos.ZERO);
        } else if (JOIN_TYPE_FULL.equals(joinType)) {
            joinTypeSqlNode = JoinType.FULL.symbol(SqlParserPos.ZERO);
        } else {
            throw new UnsupportedOperationException("unsupported join type " + joinType);
        }
        return new SqlJoin(SqlParserPos.ZERO, left, 
                SqlLiteral.createBoolean(false, SqlParserPos.ZERO),
                joinTypeSqlNode, right, conditionType, condition);
    }

    private static SqlNode convertBinaryOperationExpression(final BinaryOperationExpression binaryOperationExpression) {
        SqlNode left = convertExpression(binaryOperationExpression.getLeft());
        SqlNode right = convertExpression(binaryOperationExpression.getRight());
        String operator = binaryOperationExpression.getOperator();
        BinarySqlOperator binarySqlOperator = BinarySqlOperator.value(operator);
        return new SqlBasicCall(binarySqlOperator.getSqlBinaryOperator(), new SqlNode[] {left, right},
                SqlParserPos.ZERO);
    }

    private static SqlNode convertExpression(final ExpressionSegment expression) {
        if (expression == null) {
            return null; 
        }
        if (expression instanceof LiteralExpressionSegment) {
            return convertToSqlNode((LiteralExpressionSegment) expression);
        } else if (expression instanceof CommonExpressionSegment) {
            return convertToSqlNode((CommonExpressionSegment) expression);
        } else if (expression instanceof ListExpression) {
            List<ExpressionSegment> items = ((ListExpression) expression).getItems();
            SqlNode left = null;
            for (ExpressionSegment item : items) {
                SqlNode sqlNode = convertExpression(item);
                if (sqlNode == null) {
                    continue;
                }
                if (left == null) {
                    left = sqlNode;
                    continue;
                }
                left = new SqlBasicCall(SqlStdOperatorTable.OR, new SqlNode[] {left, sqlNode}, SqlParserPos.ZERO);
            }
            return left;
        } else if (expression instanceof BinaryOperationExpression) {
            return convertBinaryOperationExpression((BinaryOperationExpression) expression);
        } else if (expression instanceof ColumnSegment) {
            return convertColumnSegment((ColumnSegment) expression);
        }
        throw new UnsupportedOperationException("unsupportd TableSegment type: " + expression.getClass());
    }

    private static SqlNode convertColumnSegment(final ColumnSegment columnSegment) {
        Optional<OwnerSegment> owner = columnSegment.getOwner();
        String columnName = columnSegment.getIdentifier().getValue();
        if (owner.isPresent()) {
            return new SqlIdentifier(ImmutableList.of(owner.get().getIdentifier().getValue(), columnName), SqlParserPos.ZERO);
        }
        return new SqlIdentifier(columnName, SqlParserPos.ZERO);
    }

    private static SqlNode convertToSqlNode(final LiteralExpressionSegment literalExpression) {
        Object literals = literalExpression.getLiterals();
        if (literals.getClass() == Integer.class) {
            return SqlLiteral.createExactNumeric(String.valueOf(literalExpression.getLiterals()), SqlParserPos.ZERO);
        } else if (literals.getClass() == String.class) {
            return SqlLiteral.createCharString((String) literalExpression.getLiterals(), SqlParserPos.ZERO);
        }
        return null;
    }
    
    private static SqlNode convertToSqlNode(final CommonExpressionSegment commonExpressionSegment) {
        // TODO 
        throw new UnsupportedOperationException("unsupported common expression");
    }

}
