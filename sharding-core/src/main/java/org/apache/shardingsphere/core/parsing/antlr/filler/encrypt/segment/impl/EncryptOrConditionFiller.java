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

package org.apache.shardingsphere.core.parsing.antlr.filler.encrypt.segment.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.encrypt.SQLSegmentEncryptFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.AndConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.ConditionSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import org.apache.shardingsphere.core.parsing.parser.context.table.Table;
import org.apache.shardingsphere.core.parsing.parser.context.table.Tables;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parsing.parser.token.EncryptColumnToken;
import org.apache.shardingsphere.core.rule.EncryptRule;

import com.google.common.base.Optional;

/**
 * Encrypt or condition filler.
 *
 * @author duhongjun
 */
public class EncryptOrConditionFiller implements SQLSegmentEncryptFiller<OrConditionSegment> {

    @Override
    public void fill(final OrConditionSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final EncryptRule encryptRule,
                     final ShardingTableMetaData shardingTableMetaData) {
        Map<String, String> columnNameToTable = new HashMap<>();
        Map<String, Integer> columnNameCount = new HashMap<>();
        fillColumnTableMap(sqlStatement, shardingTableMetaData, columnNameToTable, columnNameCount);
        filterCondition(shardingTableMetaData, sqlStatement, sqlSegment, sql, encryptRule, columnNameToTable, columnNameCount);
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

    private OrCondition filterCondition(final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement, final OrConditionSegment orCondition, final String sql,
            final EncryptRule encryptRule, final Map<String, String> columnNameToTable, final Map<String, Integer> columnNameCount) {
        OrCondition result = new OrCondition();
        for (AndConditionSegment each : orCondition.getAndConditions()) {
            for (ConditionSegment condition : each.getConditions()) {
                if (null == condition.getColumn()) {
                    continue;
                }
                Column column = new Column(condition.getColumn().getName(), getTableName(shardingTableMetaData, sqlStatement, condition));
                fillEncryptCondition(column, condition, encryptRule, sqlStatement, sql);
            }
        }
        return result;
    }
    
    private void fillEncryptCondition(final Column column, final ConditionSegment condition, final EncryptRule encryptRule, final SQLStatement sqlStatement, final String sql) {
        if (!encryptRule.getEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
            return;
        }
        AndCondition andCondition;
        if (0 == sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().size()) {
            andCondition = new AndCondition();
            sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().add(andCondition);
        } else {
            andCondition = sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0);
        }
        andCondition.getConditions().add(condition.getExpression().buildCondition(column, sql));
        sqlStatement.getSQLTokens().add(new EncryptColumnToken(condition.getColumn().getStartIndex(), condition.getColumn().getStopIndex(), column, true));
    }
    
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
    private String getTableName(final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement, final ConditionSegment conditionSegment) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return getTableName(shardingTableMetaData, sqlStatement.getTables(), conditionSegment);
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            String tableName = getTableName(shardingTableMetaData, currentSelectStatement.getTables(), conditionSegment);
            if (!"".equals(tableName)) {
                return tableName;
            }
        }
        return getTableName(shardingTableMetaData, currentSelectStatement.getTables(), conditionSegment);
    }
    
    private String getTableName(final ShardingTableMetaData shardingTableMetaData, final Tables tables, final ConditionSegment conditionSegment) {
        if (conditionSegment.getColumn().getOwner().isPresent()) {
            Optional<Table> table = tables.find(conditionSegment.getColumn().getOwner().get());
            return table.isPresent() ? table.get().getName() : "";
        } else {
            return getTableNameFromMetaData(shardingTableMetaData, tables, conditionSegment.getColumn().getName());
        }
    }
    
    private String getTableNameFromMetaData(final ShardingTableMetaData shardingTableMetaData, final Tables tables, final String columnName) {
        for (String each : tables.getTableNames()) {
            TableMetaData tableMetaData = shardingTableMetaData.get(each);
            if (null != tableMetaData) {
                if (tableMetaData.getColumns().containsKey(columnName)) {
                    return each;
                }
            }
        }
        return "";
    }
}
