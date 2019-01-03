package io.shardingsphere.core.parsing.antlr.filler.impl;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.AndConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.ConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.*;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;

import java.util.*;

public abstract class AbstractFromWhereFiller implements SQLStatementFiller<FromWhereSegment> {
    
    @Override
    public void fill(final FromWhereSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        if (!sqlSegment.getConditions().getAndConditions().isEmpty()) {
            Map<String, String> columnNameToTable = new HashMap<>();
            Map<String, Integer> columnNameCount = new HashMap<>();
            fillColumnTableMap(sqlStatement, shardingTableMetaData, columnNameToTable, columnNameCount);
            OrCondition orCondition = filterShardingCondition(sqlStatement, sqlSegment.getConditions(), sql, shardingRule, columnNameToTable, columnNameCount, shardingTableMetaData);
            sqlStatement.getConditions().getOrCondition().getAndConditions().addAll(orCondition.getAndConditions());
        }
        int count = 0;
        while (count < sqlSegment.getParameterCount()) {
            sqlStatement.increaseParametersIndex();
            count++;
        }
    }
    
    private void fillColumnTableMap(final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData,
                                    final Map<String, String> columnNameToTable, final Map<String, Integer> columnNameCount) {
        if (null == shardingTableMetaData) {
            return;
        }
        for (String each : sqlStatement.getTables().getTableNames()) {
            Collection<String> tableColumns = shardingTableMetaData.getAllColumnNames(each);
            for (String columnName : tableColumns) {
                columnNameToTable.put(columnName, each);
                Integer count = columnNameCount.get(columnName);
                if (null == count) {
                    count = 1;
                } else {
                    count++;
                }
                columnNameCount.put(columnName, count);
            }
        }
    }
    
    private OrCondition filterShardingCondition(final SQLStatement sqlStatement, final OrConditionSegment orCondition, final String sql, final ShardingRule shardingRule,
                                                final Map<String, String> columnNameToTable, final Map<String, Integer> columnNameCount, final ShardingTableMetaData shardingTableMetaData) {
        OrCondition result = new OrCondition();
        for (AndConditionSegment each : orCondition.getAndConditions()) {
            List<ConditionSegment> shardingCondition = new LinkedList<>();
            boolean needSharding = false;
            for (ConditionSegment condition : each.getConditions()) {
                if (null == condition.getColumn()) {
                    continue;
                }
                if (condition.getColumn().getOwner().isPresent() && sqlStatement.getTables().getTableNames().contains(condition.getColumn().getOwner().get())) {
                    sqlStatement.addSQLToken(new TableToken(condition.getColumn().getStartPosition(), 0, condition.getColumn().getOwner().get()));
                }
                if (condition.getExpression() instanceof ColumnSegment) {
                    ColumnSegment rightColumn = (ColumnSegment) condition.getExpression();
                    if (rightColumn.getOwner().isPresent() && sqlStatement.getTables().getTableNames().contains(rightColumn.getOwner().get())) {
                        sqlStatement.addSQLToken(new TableToken(rightColumn.getStartPosition(), 0, rightColumn.getOwner().get()));
                    }
                    needSharding = true;
                    continue;
                }
                if ("".equals(condition.getColumn().getTableName())) {
                    if (sqlStatement.getTables().isSingleTable()) {
                        condition.getColumn().setTableName(sqlStatement.getTables().getSingleTableName());
                    } else {
                        String tableName = columnNameToTable.get(condition.getColumn().getName());
                        Integer count = columnNameCount.get(condition.getColumn().getName());
                        if (null != tableName && 1 == count) {
                            condition.getColumn().setTableName(tableName);
                        }
                    }
                }
                if (shardingRule.isShardingColumn(new Column(condition.getColumn().getName(), condition.getColumn().getTableName()))) {
                    shardingCondition.add(condition);
                    needSharding = true;
                }
            }
            if (needSharding) {
                fillResult(result, sqlStatement, shardingCondition, sql, shardingRule, shardingTableMetaData);
            } else {
                result.getAndConditions().clear();
                break;
            }
        }
        return result;
    }
    
    private void fillResult(final OrCondition result, final SQLStatement sqlStatement, final List<ConditionSegment> shardingCondition, final String sql, final ShardingRule shardingRule,
                            final ShardingTableMetaData shardingTableMetaData) {
        if (shardingCondition.isEmpty()) {
            return;
        }
        AndCondition andConditionResult = new AndCondition();
        result.getAndConditions().add(andConditionResult);
        for (ConditionSegment eachCondition : shardingCondition) {
            Column column = new Column(eachCondition.getColumn().getName(), eachCondition.getColumn().getTableName());
            if (ShardingOperator.EQUAL == eachCondition.getOperator()) {
                EqualsValueExpressionSegment expressionSegment = (EqualsValueExpressionSegment) eachCondition.getExpression();
                com.google.common.base.Optional<SQLExpression> expression = buildExpression(sqlStatement, expressionSegment.getExpression(), sql, shardingRule, shardingTableMetaData);
                if (expression.isPresent()) {
                    andConditionResult.getConditions().add(new Condition(column, expression.get()));
                }
                continue;
            }
            if (ShardingOperator.IN == eachCondition.getOperator()) {
                InValueExpressionSegment expressionSegment = (InValueExpressionSegment) eachCondition.getExpression();
                List<SQLExpression> expressions = new LinkedList<>();
                for (ExpressionSegment each : expressionSegment.getSqlExpressions()) {
                    com.google.common.base.Optional<SQLExpression> expression = buildExpression(sqlStatement, each, sql, shardingRule, shardingTableMetaData);
                    if (expression.isPresent()) {
                        expressions.add(expression.get());
                    } else {
                        expressions.clear();
                        break;
                    }
                }
                if (!expressions.isEmpty()) {
                    andConditionResult.getConditions().add(new Condition(column, expressions));
                }
                continue;
            }
            if (ShardingOperator.BETWEEN == eachCondition.getOperator()) {
                BetweenValueExpressionSegment expressionSegment = (BetweenValueExpressionSegment) eachCondition.getExpression();
                com.google.common.base.Optional<SQLExpression> beginExpress = buildExpression(sqlStatement, expressionSegment.getBeginExpress(), sql, shardingRule, shardingTableMetaData);
                if (!beginExpress.isPresent()) {
                    continue;
                }
                com.google.common.base.Optional<SQLExpression> endExpress = buildExpression(sqlStatement, expressionSegment.getEndExpress(), sql, shardingRule, shardingTableMetaData);
                if (!endExpress.isPresent()) {
                    continue;
                }
                andConditionResult.getConditions().add(new Condition(column, beginExpress.get(), endExpress.get()));
            }
        }
    }
    
    private com.google.common.base.Optional<SQLExpression> buildExpression(final SQLStatement sqlStatement, final ExpressionSegment expressionSegment, final String sql, final ShardingRule shardingRule,
                                                                           final ShardingTableMetaData shardingTableMetaData) {
        if (!(expressionSegment instanceof CommonExpressionSegment)) {
            //new ExpressionFiller().fill(expressionSegment, selectStatement, sql, shardingRule, shardingTableMetaData);
            return com.google.common.base.Optional.absent();
        }
        CommonExpressionSegment commonExpressionSegment = (CommonExpressionSegment) expressionSegment;
        if (-1 < commonExpressionSegment.getIndex()) {
            return com.google.common.base.Optional.<SQLExpression>of(new SQLPlaceholderExpression(commonExpressionSegment.getIndex()));
        }
        if (null != commonExpressionSegment.getValue()) {
            return com.google.common.base.Optional.<SQLExpression>of(new SQLNumberExpression(commonExpressionSegment.getValue()));
        }
        String expression = sql.substring(commonExpressionSegment.getStartPosition(), commonExpressionSegment.getEndPosition() + 1);
        return Optional.<SQLExpression>of(new SQLTextExpression(expression));
    }
}
