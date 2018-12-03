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

package io.shardingsphere.core.parsing.antlr.filler.engnie;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.AndConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.ColumnSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.ConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.OrConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLBetweenExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLEqualsExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLInExpressionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * From where segment filler.
 *
 * @author duhongjun
 */
public class FromWhereSegmentFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule,
                     final ShardingTableMetaData shardingTableMetaData) {
        FromWhereSegment fromWhereSegment = (FromWhereSegment) sqlSegment;
        if (!fromWhereSegment.getConditions().getAndConditions().isEmpty()) {
            Map<String, String> columnNameToTable = new HashMap<String, String>();
            Map<String, Integer> columnNameCount = new HashMap<String, Integer>();
            fillColumnTableMap(sqlStatement, shardingTableMetaData, columnNameToTable, columnNameCount);
            OrCondition orCondition = filterShardingCondition(sqlStatement, fromWhereSegment.getConditions(), shardingRule, columnNameToTable, columnNameCount);
            sqlStatement.getConditions().getOrCondition().getAndConditions().addAll(orCondition.getAndConditions());
        }
        int count = 0;
        while (count < fromWhereSegment.getParamenterCount()) {
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
                        if (null != tableName && count.intValue() == 1) {
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
