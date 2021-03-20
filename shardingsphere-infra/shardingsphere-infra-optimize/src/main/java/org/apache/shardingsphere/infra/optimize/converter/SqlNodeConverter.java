package org.apache.shardingsphere.infra.optimize.converter;

import com.google.common.collect.Lists;
import org.apache.calcite.sql.JoinConditionType;
import org.apache.calcite.sql.JoinType;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlSelectKeyword;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.optimize.operator.BinarySqlOperator;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

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
    
    /**
     * convert shardingsphere ast to calcite ast.
     * @param statementContext shardingsphere ast
     * @return an Optional 
     */
    public static Optional<SqlNode> convertSqlStatement(final SQLStatementContext<?> statementContext) {
        try {
            SqlNode sqlNode = convertStatement(statementContext);
            return Optional.of(sqlNode);
        } catch (UnsupportedOperationException e) {
            return Optional.empty();
        }
    }

    private static SqlNode convertStatement(final SQLStatementContext<?> statementContext) {
        if (statementContext instanceof SelectStatementContext) {
            return convertSelectStatement((SelectStatementContext) statementContext);
        }
        return null;
    }
    
    /**
     * convert select ast.
     * @param selectStatement select ast
     * @return calcite select ast
     */
    public static SqlNode convertSelectStatement(final SelectStatementContext selectStatement) {
        SqlNodeList keywordList = convertDistinct(selectStatement.getProjectionsContext());
        SqlNodeList projections = convertProjections(selectStatement.getProjectionsContext());

        SqlNode from = convertTableSegment(selectStatement.getSqlStatement().getFrom());
        SqlNode where = selectStatement.getWhere().isPresent() ? convertWhere(selectStatement.getWhere().get()) : null;

        // TODO 不支持 group by having
        SqlNodeList groupBy = convertGroupBy(selectStatement.getGroupByContext());

        SqlNodeList orderBy = convertOrderBy(selectStatement.getOrderByContext());
        
        Map.Entry<SqlNode, SqlNode> offsetRowCount = convertPagination(selectStatement.getPaginationContext());

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
    public static SqlNodeList convertDistinct(final ProjectionsContext projections) {
        if (projections.isDistinctRow()) {
            return new SqlNodeList(Arrays.asList(SqlSelectKeyword.DISTINCT.symbol(SqlParserPos.ZERO)), SqlParserPos.ZERO);
        }
        return null;
    }
    
    /**
     * convert project.
     * @param projectionsContext project ast
     * @return calcite project ast
     */
    public static SqlNodeList convertProjections(final ProjectionsContext projectionsContext) {
        Collection<Projection> projections = projectionsContext.getProjections();
        List<SqlNode> columnNodes = new ArrayList<>(projections.size());
        for (Projection projection : projections) {
            if (projection instanceof ColumnProjection) {
                columnNodes.add(convertColumnProjection((ColumnProjection) projection));
            } else if (projection instanceof ExpressionProjection) {
                // TODO
                // expression is not ast current.
                // columnNodes.add(convertExpressionProjection((ExpressionProjectionSegment) projection));
            }
            // TODO
        }
        return new SqlNodeList(columnNodes, SqlParserPos.ZERO);
    }
    
    /**
     * convert column project.
     * @param columnProjection column project
     * @return calcite <code>SqlNode</code>
     */
    public static SqlNode convertColumnProjection(final ColumnProjection columnProjection) {
        String columnName = columnProjection.getName();
        String owner = columnProjection.getOwner();
        return new SqlIdentifier(Arrays.asList(owner == null ? "" : owner, columnName), SqlParserPos.ZERO);
    }

    private static SqlNode convertExpressionProjection(final ExpressionProjectionSegment expressionProjection) {
        String expression = expressionProjection.getText();
        return SqlCharStringLiteral.createCharString(expression, SqlParserPos.ZERO);
    }


    public static SqlNode convertWhere(final WhereSegment where) {
        ExpressionSegment whereExpr = where.getExpr();
        return convertExpression(whereExpr);
        // TODO or
        // TODO not
        // TODO not in
    }

    public static SqlNodeList convertGroupBy(GroupByContext groupBy) {
        Collection<OrderByItem> groupByItems = groupBy.getItems();
        if(groupByItems == null || groupByItems.size() == 0) {
            return null;
        }
        List<SqlNode> groupBySqlNodes = convertOrderByItems(groupByItems);
        return new SqlNodeList(groupBySqlNodes, SqlParserPos.ZERO);
    }

    public static SqlNodeList convertOrderBy(OrderByContext orderBy) {
        Collection<OrderByItem> orderByItems = orderBy.getItems();
        List<SqlNode> orderBySqlNodes = convertOrderByItems(orderByItems);
        return new SqlNodeList(orderBySqlNodes, SqlParserPos.ZERO);
    }

    public static Map.Entry<SqlNode, SqlNode> convertPagination(PaginationContext pagination) {
        if(pagination == null || !pagination.isHasPagination()) {
            return new SimpleEntry<>(null, null);
        }
        // TODO fixme
        SqlNode offSet = SqlLiteral.createExactNumeric(String.valueOf(pagination.getActualOffset()), SqlParserPos.ZERO);

        Optional<Long> rowCountOptional = pagination.getActualRowCount();
        SqlNode rowCount = null;
        if(rowCountOptional.isPresent()) {
            rowCount = SqlLiteral.createExactNumeric(String.valueOf(rowCountOptional.get()), SqlParserPos.ZERO);
        }
        return new SimpleEntry<>(offSet, rowCount);
    }

    public static List<SqlNode> convertOrderByItems(Collection<OrderByItem> orderByItems) {
        List<SqlNode> sqlNodes = Lists.newArrayList();
        for(OrderByItem orderByItem : orderByItems) {
            OrderByItemSegment orderByItemSegment = orderByItem.getSegment();
            if(orderByItemSegment instanceof ColumnOrderByItemSegment) {
                sqlNodes.add(convertColumnOrderByToSqlNode((ColumnOrderByItemSegment)orderByItemSegment));
            } else if(orderByItemSegment instanceof ExpressionOrderByItemSegment) {
                // sqlNodes.add(convertToSqlNode((ExpressionOrderByItemSegment)orderByItemSegment));
            } else if(orderByItemSegment instanceof IndexOrderByItemSegment) {
                // sqlNodes.add(convertToSqlNode((IndexOrderByItemSegment)orderByItemSegment));
            } else if(orderByItemSegment instanceof TextOrderByItemSegment) {
                // sqlNodes.add(convertToSqlNode((TextOrderByItemSegment)orderByItemSegment));
            }
        }
        return sqlNodes;
    }

    public static SqlNode convertColumnOrderByToSqlNode(ColumnOrderByItemSegment columnOrderBy) {
        SqlNode sqlNode = convertToSqlNode(columnOrderBy.getColumn());
        if(Objects.equals(OrderDirection.DESC, columnOrderBy.getOrderDirection())) {
            sqlNode = new SqlBasicCall(SqlStdOperatorTable.DESC, new SqlNode[] { sqlNode }, SqlParserPos.ZERO);
        }
        return sqlNode;
    }

    public static SqlNode convertTableSegment(TableSegment table) {
        if(table instanceof SimpleTableSegment) {
            TableNameSegment tableName = ((SimpleTableSegment)table).getTableName();
            SqlNode tableNameSqlNode = new SqlIdentifier(tableName.getIdentifier().getValue(), SqlParserPos.ZERO);
            if(table.getAlias().isPresent()) {
                SqlNode aliasIdentifier = new SqlIdentifier(table.getAlias().get(), SqlParserPos.ZERO);
                SqlNode tableAsSqlNode = new SqlBasicCall(SqlStdOperatorTable.AS, new SqlNode[] { tableNameSqlNode,
                        aliasIdentifier }, SqlParserPos.ZERO);
                return tableAsSqlNode;
            } else {
                return tableNameSqlNode;
            }
        } else if(table instanceof SubqueryTableSegment) {
            // TODO
        } else if(table instanceof JoinTableSegment) {
            JoinTableSegment join = (JoinTableSegment)table;
            String joinType = join.getJoinType();
            SqlNode left = convertTableSegment(join.getLeft());
            SqlNode right = convertTableSegment(join.getRight());
            ExpressionSegment expressionSegment = join.getCondition();
            SqlNode condition = convertExpression(expressionSegment);

            SqlLiteral conditionType = condition == null?
                    JoinConditionType.NONE.symbol(SqlParserPos.ZERO)
                    : JoinConditionType.ON.symbol(SqlParserPos.ZERO);


            if(joinType == null) {
                return new SqlJoin(SqlParserPos.ZERO, left
                        , SqlLiteral.createBoolean(false, SqlParserPos.ZERO)
                        , JoinType.INNER.symbol(SqlParserPos.ZERO), right
                        , conditionType
                        , condition);
            } else if(joinType == "INNER") {

            }
        }
        throw new UnsupportedOperationException("unsupportd TableSegment type: " + table.getClass());
    }

    public static SqlNode convertBinaryOperationExpression(BinaryOperationExpression binaryOperationExpression) {
        SqlNode left = convertExpression(binaryOperationExpression.getLeft());
        SqlNode right = convertExpression(binaryOperationExpression.getRight());
        String operator = binaryOperationExpression.getOperator();
        BinarySqlOperator binarySqlOperator = BinarySqlOperator.value(operator);
        return new SqlBasicCall(binarySqlOperator.getSqlBinaryOperator(), new SqlNode[] { left, right },
                SqlParserPos.ZERO);
    }

    public static SqlNode convertToSqlNode(ColumnSegment column) {
        String columnName = column.getIdentifier().getValue();
        Optional<OwnerSegment> ownerOptional = column.getOwner();
        String ownernName = "";
        if(ownerOptional.isPresent()) {
            ownernName = ownerOptional.get().getIdentifier().getValue();
        }
        return new SqlIdentifier(Arrays.asList(ownernName, columnName), SqlParserPos.ZERO);
    }

    public static SqlNode convertExpression(ExpressionSegment expression) {
        if(expression instanceof LiteralExpressionSegment) {
            return convertToSqlNode((LiteralExpressionSegment)expression);
        } else if(expression instanceof CommonExpressionSegment) {
            // TODO return convertToSqlNode((CommonExpressionSegment)expression);
        } else if (expression instanceof ListExpression) {
            List<ExpressionSegment> items = ((ListExpression)expression).getItems();
            SqlNode left = null;
            for(ExpressionSegment item : items) {
                SqlNode sqlNode = convertExpression(item);
                if(sqlNode == null) {
                    continue;
                }
                if(left == null) {
                    left = sqlNode;
                    continue;
                }
                left = new SqlBasicCall(SqlStdOperatorTable.OR, new SqlNode[] {left, sqlNode}, SqlParserPos.ZERO);
            }
            return left;
        } else if(expression instanceof BinaryOperationExpression) {
            return convertBinaryOperationExpression((BinaryOperationExpression)expression);
        } else if(expression instanceof ColumnSegment) {
            return convertColumnSegment((ColumnSegment)expression);
        }
        throw new UnsupportedOperationException("unsupportd TableSegment type: " + expression.getClass());
    }

    private static SqlNode convertColumnSegment(ColumnSegment columnSegment) {
        String columnName = columnSegment.getIdentifier().getValue();
        Optional<OwnerSegment> owner = columnSegment.getOwner();
        return new SqlIdentifier(Arrays.asList(owner.isPresent() ? owner.get().getIdentifier().getValue(): "", columnName), SqlParserPos.ZERO);
    }

    public static SqlNode convertToSqlNode(LiteralExpressionSegment literalExpression) {
        Object literals = literalExpression.getLiterals();
        if(literals.getClass() == Integer.class) {
            return SqlLiteral.createExactNumeric(String.valueOf(literalExpression.getLiterals()), SqlParserPos.ZERO);
        } else if(literals.getClass() == String.class) {
            return SqlLiteral.createCharString((String)literalExpression.getLiterals(), SqlParserPos.ZERO);
        }
        return null;
    }

}
