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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.dml;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.AndPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.TableToken;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.OrCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Tables;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Or predicate filler for sharding.
 *
 * @author duhongjun
 * @author zhangliang
 */
@Setter
public final class ShardingOrPredicateFiller implements SQLSegmentFiller<OrPredicateSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        sqlStatement.getRouteConditions().getOrCondition().getAndConditions().addAll(buildCondition(sqlSegment, sqlStatement).getAndConditions());
    }
    
    /**
     * Build condition.
     *
     * @param sqlSegment SQL segment
     * @param sqlStatement SQL statement
     * @return or condition
     */
    public OrCondition buildCondition(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        OrCondition result = new OrCondition();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            List<PredicateSegment> predicates = new LinkedList<>();
            boolean needSharding = false;
            for (PredicateSegment predicate : each.getPredicates()) {
                if (null == predicate.getColumn()) {
                    continue;
                }
                addTableTokenForColumn(sqlStatement, predicate.getColumn());
                if (predicate.getExpression() instanceof ColumnSegment) {
                    addTableTokenForColumn(sqlStatement, (ColumnSegment) predicate.getExpression());
                    needSharding = true;
                    continue;
                }
                Column column = new Column(predicate.getColumn().getName(), getTableName(sqlStatement, predicate));
                if (isShardingCondition(predicate.getOperator()) && shardingRule.isShardingColumn(column.getName(), column.getTableName())) {
                    predicates.add(predicate);
                    needSharding = true;
                }
            }
            if (needSharding) {
                fillResult(sqlStatement, result, predicates);
            } else {
                result.getAndConditions().clear();
                break;
            }
        }
        Set<Integer> filledConditionStopIndexes = new HashSet<>();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (null == predicate.getColumn()) {
                    continue;
                }
                if (predicate.getExpression() instanceof ColumnSegment) {
                    continue;
                }
                if (filledConditionStopIndexes.contains(predicate.getStopIndex())) {
                    continue;
                } else {
                    filledConditionStopIndexes.add(predicate.getStopIndex());
                }
                Column column = new Column(predicate.getColumn().getName(), getTableName(sqlStatement, predicate));
                fillEncryptCondition(column, predicate, sqlStatement);
            }
        }
        return result;
    }
    
    private void addTableTokenForColumn(final SQLStatement sqlStatement, final ColumnSegment column) {
        if (column.getOwner().isPresent()) {
            String owner = column.getOwner().get();
            Optional<Table> logicTable = sqlStatement.getTables().find(owner);
            if (logicTable.isPresent() && !logicTable.get().getAlias().isPresent() && shardingTableMetaData.containsTable(logicTable.get().getName())) {
                sqlStatement.addSQLToken(new TableToken(column.getStartIndex(), owner, column.getOwnerQuoteCharacter(), 0));
            }
        }
    }
    
    private void fillResult(final SQLStatement sqlStatement, final OrCondition orCondition, final List<PredicateSegment> shardingCondition) {
        if (shardingCondition.isEmpty()) {
            return;
        }
        AndCondition andConditionResult = new AndCondition();
        orCondition.getAndConditions().add(andConditionResult);
        for (PredicateSegment each : shardingCondition) {
            Optional<String> tableName = getTableName(each, sqlStatement);
            Column column = new Column(each.getColumn().getName(), tableName.isPresent() ? tableName.get() : getTableName(sqlStatement, each));
            andConditionResult.getConditions().add(each.getExpression().buildCondition(column, sqlStatement.getLogicSQL()));
        }
    }
    
    private void fillEncryptCondition(final Column column, final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
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
        andCondition.getConditions().add(predicateSegment.getExpression().buildCondition(column, sqlStatement.getLogicSQL()));
        sqlStatement.getSQLTokens().add(new EncryptColumnToken(predicateSegment.getColumn().getStartIndex(), predicateSegment.getStopIndex(), column, true));
    }
    
    private boolean isShardingCondition(final String operator) {
        return Symbol.EQ.getLiterals().equals(operator) || ShardingOperator.IN.name().equals(operator) || ShardingOperator.BETWEEN.name().equals(operator);
    }
    
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
    private String getTableName(final SQLStatement sqlStatement, final PredicateSegment predicateSegment) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return sqlStatement.getTables().getSingleTableName();
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            String tableName = getTableName(predicateSegment, currentSelectStatement.getTables());
            if (!"".equals(tableName)) {
                return tableName;
            }
        }
        return getTableName(predicateSegment, currentSelectStatement.getTables());
    }
    
    private Optional<String> getTableName(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (predicateSegment.getColumn().getOwner().isPresent()) {
            Optional<Table> table = sqlStatement.getTables().find(predicateSegment.getColumn().getOwner().get());
            if (table.isPresent()) {
                return Optional.of(table.get().getName());
            }
        }
        return Optional.absent();
    }
    
    private String getTableName(final PredicateSegment predicateSegment, final Tables tables) {
        Collection<String> shardingLogicTableNames = shardingRule.getShardingLogicTableNames(tables.getTableNames());
        if (tables.isSingleTable() || tables.isSameTable() || 1 == shardingLogicTableNames.size() || shardingRule.isAllBindingTables(shardingLogicTableNames)) {
            return tables.getSingleTableName();
        }
        if (predicateSegment.getColumn().getOwner().isPresent()) {
            Optional<Table> table = tables.find(predicateSegment.getColumn().getOwner().get());
            return table.isPresent() ? table.get().getName() : "";
        } else {
            return getTableNameFromMetaData(predicateSegment.getColumn().getName(), tables);
        }
    }
    
    private String getTableNameFromMetaData(final String columnName, final Tables tables) {
        for (String each : tables.getTableNames()) {
            if (shardingTableMetaData.containsColumn(each, columnName)) {
                return each;
            }
        }
        return "";
    }
}
