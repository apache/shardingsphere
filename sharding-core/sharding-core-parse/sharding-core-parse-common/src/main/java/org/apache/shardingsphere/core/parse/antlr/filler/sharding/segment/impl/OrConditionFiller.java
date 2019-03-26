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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.segment.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.filler.sharding.SQLSegmentShardingFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.condition.AndConditionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.condition.ConditionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.condition.OrConditionSegment;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.context.condition.OrCondition;
import org.apache.shardingsphere.core.parse.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.parser.context.table.Tables;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parse.parser.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
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
public final class OrConditionFiller implements SQLSegmentShardingFiller<OrConditionSegment> {
    
    @Override
    public void fill(final OrConditionSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        sqlStatement.getRouteConditions().getOrCondition().getAndConditions().addAll(buildCondition(sqlSegment, sqlStatement, sql, shardingRule, shardingTableMetaData).getAndConditions());
    }
    
    /**
     * Build condition.
     *
     * @param sqlSegment SQL segment
     * @param sqlStatement SQL statement
     * @param sql SQL
     * @param shardingRule databases and tables sharding rule
     * @param shardingTableMetaData sharding table meta data
     * @return or condition
     */
    public OrCondition buildCondition(
            final OrConditionSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        Map<String, String> columnNameToTable = new HashMap<>();
        Map<String, Integer> columnNameCount = new HashMap<>();
        fillColumnTableMap(sqlStatement, shardingTableMetaData, columnNameToTable, columnNameCount);
        return filterCondition(shardingTableMetaData, sqlStatement, sqlSegment, sql, shardingRule);
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
    
    private OrCondition filterCondition(
            final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement, final OrConditionSegment orCondition, final String sql, final ShardingRule shardingRule) {
        OrCondition result = new OrCondition();
        for (AndConditionSegment each : orCondition.getAndConditions()) {
            List<ConditionSegment> shardingCondition = new LinkedList<>();
            boolean needSharding = false;
            for (ConditionSegment condition : each.getConditions()) {
                if (null == condition.getColumn()) {
                    continue;
                }
                addTableTokenForColumn(shardingTableMetaData, sqlStatement, condition.getColumn());
                if (condition.getExpression() instanceof ColumnSegment) {
                    addTableTokenForColumn(shardingTableMetaData, sqlStatement, (ColumnSegment) condition.getExpression());
                    needSharding = true;
                    continue;
                }
                Column column = new Column(condition.getColumn().getName(), getTableName(shardingTableMetaData, shardingRule, sqlStatement, condition));
                if (isShardingCondition(condition.getOperator()) && shardingRule.isShardingColumn(column.getName(), column.getTableName())) {
                    shardingCondition.add(condition);
                    needSharding = true;
                }
            }
            if (needSharding) {
                fillResult(shardingTableMetaData, sqlStatement, shardingRule, result, shardingCondition, sql);
            } else {
                result.getAndConditions().clear();
                break;
            }
        }
        for (AndConditionSegment each : orCondition.getAndConditions()) {
            for (ConditionSegment condition : each.getConditions()) {
                if (null == condition.getColumn()) {
                    continue;
                }
                Column column = new Column(condition.getColumn().getName(), getTableName(shardingTableMetaData, shardingRule, sqlStatement, condition));
                fillEncryptCondition(column, condition, shardingRule, sqlStatement, sql);
            }
        }
        return result;
    }
    
    private void addTableTokenForColumn(final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement, final ColumnSegment column) {
        if (column.getOwner().isPresent()) {
            String owner = column.getOwner().get();
            Optional<Table> logicTable = sqlStatement.getTables().find(owner);
            if (logicTable.isPresent() && !logicTable.get().getAlias().isPresent() && shardingTableMetaData.containsTable(logicTable.get().getName())) {
                sqlStatement.addSQLToken(new TableToken(column.getStartIndex(), 0, SQLUtil.getExactlyValue(owner), QuoteCharacter.getQuoteCharacter(owner)));
            }
        }
    }
    
    private void fillResult(final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement, final ShardingRule shardingRule, final OrCondition orCondition,
                            final List<ConditionSegment> shardingCondition, final String sql) {
        if (shardingCondition.isEmpty()) {
            return;
        }
        AndCondition andConditionResult = new AndCondition();
        orCondition.getAndConditions().add(andConditionResult);
        for (ConditionSegment eachCondition : shardingCondition) {
            Column column = new Column(eachCondition.getColumn().getName(), getTableName(shardingTableMetaData, shardingRule, sqlStatement, eachCondition));
            andConditionResult.getConditions().add(eachCondition.getExpression().buildCondition(column, sql));
        }
    }
    
    private void fillEncryptCondition(final Column column, final ConditionSegment condition, final ShardingRule shardingRule, final SQLStatement sqlStatement, final String sql) {
        if (!shardingRule.getShardingEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
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
        sqlStatement.getSQLTokens().add(new EncryptColumnToken(condition.getColumn().getStartIndex(), condition.getStopIndex(), column, true));
    }
    
    private boolean isShardingCondition(final String operator) {
        return Symbol.EQ.getLiterals().equals(operator) || ShardingOperator.IN.name().equals(operator) || ShardingOperator.BETWEEN.name().equals(operator);
    }
    
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
    private String getTableName(final ShardingTableMetaData shardingTableMetaData, final ShardingRule shardingRule, final SQLStatement sqlStatement, final ConditionSegment conditionSegment) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return getTableName(shardingTableMetaData, shardingRule, sqlStatement.getTables(), conditionSegment);
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            String tableName = getTableName(shardingTableMetaData, shardingRule, currentSelectStatement.getTables(), conditionSegment);
            if (!"".equals(tableName)) {
                return tableName;
            }
        }
        return getTableName(shardingTableMetaData, shardingRule, currentSelectStatement.getTables(), conditionSegment);
    }
    
    private String getTableName(final ShardingTableMetaData shardingTableMetaData, final ShardingRule shardingRule, final Tables tables, final ConditionSegment conditionSegment) {
        Collection<String> shardingLogicTableNames = shardingRule.getShardingLogicTableNames(tables.getTableNames());
        if (tables.isSingleTable() || tables.isSameTable() || 1 == shardingLogicTableNames.size() || shardingRule.isAllBindingTables(shardingLogicTableNames)) {
            return tables.getSingleTableName();
        }
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
