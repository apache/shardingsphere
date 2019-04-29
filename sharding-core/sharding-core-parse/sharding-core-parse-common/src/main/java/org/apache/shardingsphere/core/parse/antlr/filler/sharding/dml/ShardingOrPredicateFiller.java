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
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.AndPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateBetweenRightValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateCompareRightValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateInRightValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.OrCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Tables;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

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
        OrCondition result = fillShardingConditions(sqlSegment, sqlStatement);
        fillEncryptConditions(sqlSegment, sqlStatement);
        return result;
    }
    
    private OrCondition fillShardingConditions(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        OrCondition result = new OrCondition();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            AndCondition andCondition = new AndCondition();
            for (PredicateSegment predicate : each.getPredicates()) {
                Optional<Condition> condition = createShardingCondition(predicate, sqlStatement);
                if (condition.isPresent()) {
                    andCondition.getConditions().add(condition.get());
                }
            }
            if (andCondition.getConditions().isEmpty()) {
                result.getAndConditions().clear();
                return result;
            }
            result.getAndConditions().add(andCondition);
        }
        return result;
    }
    
    private Optional<Condition> createShardingCondition(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (!isShardingCondition(predicateSegment.getOperator())) {
            return Optional.absent();
        }
        Optional<String> tableName = findTableName(predicateSegment, sqlStatement);
        if (!tableName.isPresent() || !shardingRule.isShardingColumn(predicateSegment.getColumn().getName(), tableName.get())) {
            return Optional.absent();
        }
        Column column = new Column(predicateSegment.getColumn().getName(), tableName.get());
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValueSegment) {
            return createEqualCondition((PredicateCompareRightValueSegment) predicateSegment.getRightValue(), column, sqlStatement.getLogicSQL());
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValueSegment) {
            return createInCondition((PredicateInRightValueSegment) predicateSegment.getRightValue(), column, sqlStatement.getLogicSQL());
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValueSegment) {
            return createBetweenCondition((PredicateBetweenRightValueSegment) predicateSegment.getRightValue(), column, sqlStatement.getLogicSQL());
        }
        return Optional.absent();
    }
    
    private boolean isShardingCondition(final String operator) {
        return Symbol.EQ.getLiterals().equals(operator) || ShardingOperator.IN.name().equals(operator) || ShardingOperator.BETWEEN.name().equals(operator);
    }
    
    private Optional<Condition> createEqualCondition(final PredicateCompareRightValueSegment expressionSegment, final Column column, final String sql) {
        SQLExpression sqlExpression = expressionSegment.getExpression().getSQLExpression(sql);
        return isShardingExpressionType(sqlExpression) ? Optional.of(new Condition(column, sqlExpression)) : Optional.<Condition>absent();
    }
    
    private Optional<Condition> createInCondition(final PredicateInRightValueSegment expressionSegment, final Column column, final String sql) {
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        for (ExpressionSegment each : expressionSegment.getSqlExpressions()) {
            SQLExpression sqlExpression = each.getSQLExpression(sql);
            if (!isShardingExpressionType(sqlExpression)) {
                sqlExpressions.clear();
                break;
            } else {
                sqlExpressions.add(sqlExpression);
            }
        }
        return sqlExpressions.isEmpty() ? Optional.<Condition>absent() : Optional.of(new Condition(column, sqlExpressions));
    }
    
    private Optional<Condition> createBetweenCondition(final PredicateBetweenRightValueSegment expressionSegment, final Column column, final String sql) {
        SQLExpression betweenExpression = expressionSegment.getBetweenExpression().getSQLExpression(sql);
        SQLExpression andExpression = expressionSegment.getAndExpression().getSQLExpression(sql);
        return isShardingExpressionType(betweenExpression) && isShardingExpressionType(andExpression)
                ? Optional.of(new Condition(column, betweenExpression, andExpression)) : Optional.<Condition>absent();
    }
    
    private boolean isShardingExpressionType(final SQLExpression sqlExpression) {
        return sqlExpression instanceof SQLPlaceholderExpression || sqlExpression instanceof SQLNumberExpression || sqlExpression instanceof SQLTextExpression;
    }
    
    private void fillEncryptConditions(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        Collection<Integer> stopIndexes = new HashSet<>();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (!(predicate.getRightValue() instanceof ColumnSegment) && stopIndexes.add(predicate.getStopIndex())) {
                    Optional<String> tableName = findTableName(predicate, sqlStatement);
                    if (tableName.isPresent()) {
                        fillEncryptCondition(predicate.getColumn().getName(), tableName.get(), predicate, sqlStatement);
                    }
                }
            }
        }
    }
    
    private void fillEncryptCondition(final String columnName, final String tableName, final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (!shardingRule.getShardingEncryptorEngine().getShardingEncryptor(tableName, columnName).isPresent()) {
            return;
        }
        AndCondition andCondition;
        if (0 == sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().size()) {
            andCondition = new AndCondition();
            sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().add(andCondition);
        } else {
            andCondition = sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0);
        }
        Optional<Condition> condition = createEncryptCondition(predicateSegment, sqlStatement);
        if (condition.isPresent()) {
            andCondition.getConditions().add(condition.get());
            sqlStatement.getSQLTokens().add(new EncryptColumnToken(predicateSegment.getColumn().getStartIndex(), predicateSegment.getStopIndex(), new Column(columnName, tableName), true));
        }
    }
    
    private Optional<Condition> createEncryptCondition(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (!isEncryptCondition(predicateSegment.getOperator())) {
            return Optional.absent();
        }
        Optional<String> tableName = findTableName(predicateSegment, sqlStatement);
        if (!tableName.isPresent() || !shardingRule.getShardingEncryptorEngine().getShardingEncryptor(tableName.get(), predicateSegment.getColumn().getName()).isPresent()) {
            return Optional.absent();
        }
        Column column = new Column(predicateSegment.getColumn().getName(), tableName.get());
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValueSegment) {
            return createEqualCondition((PredicateCompareRightValueSegment) predicateSegment.getRightValue(), column, sqlStatement.getLogicSQL());
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValueSegment) {
            return createInCondition((PredicateInRightValueSegment) predicateSegment.getRightValue(), column, sqlStatement.getLogicSQL());
        }
        return Optional.absent();
    }
    
    private boolean isEncryptCondition(final String operator) {
        return Symbol.EQ.getLiterals().equals(operator) || ShardingOperator.IN.name().equals(operator);
    }
    
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
    private Optional<String> findTableName(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return Optional.of(sqlStatement.getTables().getSingleTableName());
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            Optional<String> tableName = findTableName(predicateSegment, currentSelectStatement.getTables());
            if (tableName.isPresent()) {
                return tableName;
            }
        }
        return findTableName(predicateSegment, currentSelectStatement.getTables());
    }
    
    private Optional<String> findTableName(final PredicateSegment predicateSegment, final Tables tables) {
        Collection<String> shardingLogicTableNames = shardingRule.getShardingLogicTableNames(tables.getTableNames());
        if (tables.isSingleTable() || tables.isSameTable() || 1 == shardingLogicTableNames.size() || shardingRule.isAllBindingTables(shardingLogicTableNames)) {
            return Optional.of(tables.getSingleTableName());
        }
        if (predicateSegment.getColumn().getOwner().isPresent()) {
            Optional<Table> table = tables.find(predicateSegment.getColumn().getOwner().get());
            return table.isPresent() ? Optional.of(table.get().getName()) : Optional.<String>absent();
        } else {
            return findTableNameFromMetaData(predicateSegment.getColumn().getName(), tables);
        }
    }
    
    private Optional<String> findTableNameFromMetaData(final String columnName, final Tables tables) {
        for (String each : tables.getTableNames()) {
            if (shardingTableMetaData.containsColumn(each, columnName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
