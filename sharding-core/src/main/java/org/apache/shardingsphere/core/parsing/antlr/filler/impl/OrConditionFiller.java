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

package org.apache.shardingsphere.core.parsing.antlr.filler.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.AndConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.ConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.BetweenValueExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.CommonExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.EqualsValueExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.expr.InValueExpressionSegment;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import org.apache.shardingsphere.core.parsing.parser.context.table.Table;
import org.apache.shardingsphere.core.parsing.parser.context.table.Tables;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Or condition filler.
 *
 * @author duhongjun
 */
public final class OrConditionFiller implements SQLStatementFiller<OrConditionSegment> {
    
    @Override
    public void fill(final OrConditionSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        sqlStatement.getConditions().getOrCondition().getAndConditions().addAll(buildCondition(sqlSegment, sqlStatement, sql, shardingRule, shardingTableMetaData).getAndConditions());
    }
    
    /**
     * build condition.
     *
     * @param sqlSegment SQL segment
     * @param sqlStatement SQL statement
     * @param sql SQL
     * @param shardingRule databases and tables sharding rule
     * @param shardingTableMetaData sharding table meta data
     * @return Or Condition
     */
    public OrCondition buildCondition(final OrConditionSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule,
                                      final ShardingTableMetaData shardingTableMetaData) {
        Map<String, String> columnNameToTable = new HashMap<>();
        Map<String, Integer> columnNameCount = new HashMap<>();
        fillColumnTableMap(sqlStatement, shardingTableMetaData, columnNameToTable, columnNameCount);
        return filterShardingCondition(sqlStatement, sqlSegment, sql, shardingRule, columnNameToTable, columnNameCount);
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
                                                final Map<String, String> columnNameToTable, final Map<String, Integer> columnNameCount) {
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
                if (shardingRule.isShardingColumn(new Column(condition.getColumn().getName(), getTableName(shardingRule, sqlStatement, condition)))) {
                    shardingCondition.add(condition);
                    needSharding = true;
                }
            }
            if (needSharding) {
                fillResult(sqlStatement, shardingRule, result, shardingCondition, sql);
            } else {
                result.getAndConditions().clear();
                break;
            }
        }
        return result;
    }
    
    private void fillResult(final SQLStatement sqlStatement, final ShardingRule shardingRule, final OrCondition orCondition, final List<ConditionSegment> shardingCondition, final String sql) {
        if (shardingCondition.isEmpty()) {
            return;
        }
        AndCondition andConditionResult = new AndCondition();
        orCondition.getAndConditions().add(andConditionResult);
        for (ConditionSegment eachCondition : shardingCondition) {
            Column column = new Column(eachCondition.getColumn().getName(), getTableName(shardingRule, sqlStatement, eachCondition));
            if (ShardingOperator.EQUAL == eachCondition.getOperator()) {
                EqualsValueExpressionSegment expressionSegment = (EqualsValueExpressionSegment) eachCondition.getExpression();
                Optional<Condition> condition = buildEqualsCondition(column, expressionSegment.getExpression(), sql);
                if (condition.isPresent()) {
                    andConditionResult.getConditions().add(condition.get());
                }
                continue;
            }
            if (ShardingOperator.IN == eachCondition.getOperator()) {
                InValueExpressionSegment expressionSegment = (InValueExpressionSegment) eachCondition.getExpression();
                List<SQLExpression> expressions = new LinkedList<>();
                for (ExpressionSegment each : expressionSegment.getSqlExpressions()) {
                    Optional<SQLExpression> expression = buildExpression(each, sql);
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
                Optional<SQLExpression> beginExpress = buildExpression(expressionSegment.getBeginExpress(), sql);
                if (!beginExpress.isPresent()) {
                    continue;
                }
                Optional<SQLExpression> endExpress = buildExpression(expressionSegment.getEndExpress(), sql);
                if (!endExpress.isPresent()) {
                    continue;
                }
                andConditionResult.getConditions().add(new Condition(column, beginExpress.get(), endExpress.get()));
            }
        }
    }
    
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
    private String getTableName(final ShardingRule shardingRule, final SQLStatement sqlStatement, final ConditionSegment conditionSegment) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return getTableName(shardingRule, sqlStatement.getTables(), conditionSegment);
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            String tableName = getTableName(shardingRule, currentSelectStatement.getTables(), conditionSegment);
            if (!"".equals(tableName)) {
                return tableName;
            }
        }
        return getTableName(shardingRule, currentSelectStatement.getTables(), conditionSegment);
    }
    
    private String getTableName(final ShardingRule shardingRule, final Tables tables, final ConditionSegment conditionSegment) {
        Collection<String> shardingLogicTableNames = shardingRule.getShardingLogicTableNames(tables.getTableNames());
        if (tables.isSingleTable() || tables.isSameTable() || 1 == shardingLogicTableNames.size() || shardingRule.isAllBindingTables(shardingLogicTableNames)) {
            return tables.getSingleTableName();
        }
        Optional<Table> table = tables.find(conditionSegment.getColumn().getOwner().orNull());
        return table.isPresent() ? table.get().getName() : "";
    }
    
    /**
     * build equals condition.
     *
     * @param column column
     * @param expressionSegment SQL segment
     * @param sql SQL
     * @return Condition
     */
    public Optional<Condition> buildEqualsCondition(final Column column, final ExpressionSegment expressionSegment, final String sql) {
        Optional<SQLExpression> expression = buildExpression(expressionSegment, sql);
        if (expression.isPresent()) {
            return Optional.of(new Condition(column, expression.get()));
        }
        return Optional.absent();
    }
    
    private Optional<SQLExpression> buildExpression(final ExpressionSegment expressionSegment, final String sql) {
        if (!(expressionSegment instanceof CommonExpressionSegment)) {
            return Optional.absent();
        }
        CommonExpressionSegment commonExpressionSegment = (CommonExpressionSegment) expressionSegment;
        if (-1 < commonExpressionSegment.getIndex()) {
            return Optional.<SQLExpression>of(new SQLPlaceholderExpression(commonExpressionSegment.getIndex()));
        }
        if (null != commonExpressionSegment.getValue()) {
            return Optional.<SQLExpression>of(new SQLNumberExpression(commonExpressionSegment.getValue()));
        }
        String expression = sql.substring(commonExpressionSegment.getStartPosition(), commonExpressionSegment.getEndPosition() + 1);
        return Optional.<SQLExpression>of(new SQLTextExpression(expression));
    }
}
