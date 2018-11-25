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
import java.util.Iterator;
import java.util.Map;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.TableAndConditionSegment;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Table and condition segment filler.
 *
 * @author duhongjun
 */
public class TableAndConditionSegmentFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule,
            final ShardingTableMetaData shardingTableMetaData) {
        TableAndConditionSegment tableAndConditionSegment = (TableAndConditionSegment) sqlSegment;
        OrCondition orCondition = tableAndConditionSegment.getConditions().optimize();
        Map<String, String> columnNameToTable = new HashMap<String, String>();
        Map<String, Integer> columnNameCount = new HashMap<String, Integer>();
        fillColumnTableMap(sqlStatement, shardingTableMetaData, columnNameToTable, columnNameCount);
        filterShardingCondition(orCondition, shardingRule, columnNameToTable, columnNameCount);
        sqlStatement.getConditions().getOrCondition().getAndConditions().addAll(orCondition.getAndConditions());
        int count = 0;
        while (count < tableAndConditionSegment.getParamenterCount()) {
            sqlStatement.increaseParametersIndex();
            count++;
        }
    }
    
    private void fillColumnTableMap(final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData,
            final Map<String, String> columnNameToTable, final Map<String, Integer> columnNameCount) {
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
    
    private void filterShardingCondition(final OrCondition orCondition, final ShardingRule shardingRule, final Map<String, String> columnNameToTable, final Map<String, Integer> columnNameCount) {
        Iterator<AndCondition> andConditionIterator = orCondition.getAndConditions().iterator();
        while(andConditionIterator.hasNext()) {
            AndCondition andCondition = andConditionIterator.next();
            Iterator<Condition> conditionIterator = andCondition.getConditions().iterator();
            while(conditionIterator.hasNext()) {
                Condition condition = conditionIterator.next();
                if(null == condition.getColumn()) {
                    conditionIterator.remove();
                    continue;
                }
                if(null == condition.getColumn().getTableName()) {
                    String tableName = columnNameToTable.get(condition.getColumn().getName());
                    Integer count = columnNameCount.get(condition.getColumn().getName());
                    if(null != tableName && count.intValue() == 1) {
                        condition.getColumn().setTableName(tableName);
                    }else {
                        conditionIterator.remove();
                        continue;
                    }
                }
                if(!shardingRule.isShardingColumn(condition.getColumn())) {
                    conditionIterator.remove();
                }
            }
            if(andCondition.getConditions().isEmpty()) {
                andConditionIterator.remove();
            }
        }
    }
}
