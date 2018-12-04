/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.filler.engine;

import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.column.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.AndConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.ConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SQLBetweenExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SQLEqualsExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.expr.SQLInExpressionSegment;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * From where filler.
 *
 * @author duhongjun
 */
public class FromWhereFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule,
                     final ShardingTableMetaData shardingTableMetaData) {
        FromWhereSegment fromWhereSegment = (FromWhereSegment) sqlSegment;
        if (!fromWhereSegment.getConditions().getAndConditions().isEmpty()) {
            Map<String, String> columnNameToTable = new HashMap<>();
            Map<String, Integer> columnNameCount = new HashMap<>();
            fillColumnTableMap(sqlStatement, shardingTableMetaData, columnNameToTable, columnNameCount);
            OrCondition orCondition = filterShardingCondition(sqlStatement, fromWhereSegment.getConditions(), shardingRule, columnNameToTable, columnNameCount);
            sqlStatement.getConditions().getOrCondition().getAndConditions().addAll(orCondition.getAndConditions());
        }
        int count = 0;
        while (count < fromWhereSegment.getParameterCount()) {
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
    
    private OrCondition filterShardingCondition(final SQLStatement sqlStatement, final OrConditionSegment orCondition, final ShardingRule shardingRule,
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
                fillResult(result, sqlStatement, shardingCondition);
            } else {
                result.getAndConditions().clear();
                break;
            }
        }
        return result;
    }
    
    private void fillResult(final OrCondition result, final SQLStatement sqlStatement, final List<ConditionSegment> shardingCondition) {
        if (!shardingCondition.isEmpty()) {
            AndCondition andConditionResult = new AndCondition();
            result.getAndConditions().add(andConditionResult);
            for (ConditionSegment eachCondition : shardingCondition) {
                Column column = new Column(eachCondition.getColumn().getName(), eachCondition.getColumn().getTableName());
                if (ShardingOperator.EQUAL == eachCondition.getOperator()) {
                    SQLEqualsExpressionSegment expression = (SQLEqualsExpressionSegment) eachCondition.getExpression();
                    andConditionResult.getConditions().add(new Condition(column, expression.getExpression()));
                } else if (ShardingOperator.IN == eachCondition.getOperator()) {
                    SQLInExpressionSegment expression = (SQLInExpressionSegment) eachCondition.getExpression();
                    andConditionResult.getConditions().add(new Condition(column, expression.getSqlExpressions()));
                } else if (ShardingOperator.BETWEEN == eachCondition.getOperator()) {
                    SQLBetweenExpressionSegment expression = (SQLBetweenExpressionSegment) eachCondition.getExpression();
                    andConditionResult.getConditions().add(new Condition(column, expression.getBeginExpress(), expression.getEndExpress()));
                }
            }
        }
    }
}
