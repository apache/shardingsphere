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
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.encrypt.dml.EncryptOrPredicateFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.AndPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.OrCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Tables;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
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
        createEncryptOrPredicateFiller().fill(sqlSegment, sqlStatement);
        return result;
    }
    
    private EncryptOrPredicateFiller createEncryptOrPredicateFiller() {
        EncryptOrPredicateFiller result = new EncryptOrPredicateFiller();
        result.setEncryptorEngine(shardingRule.getShardingEncryptorEngine());
        result.setShardingTableMetaData(shardingTableMetaData);
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
        Optional<String> tableName = findTableName(predicateSegment, sqlStatement);
        if (!tableName.isPresent() || !shardingRule.isShardingColumn(predicateSegment.getColumn().getName(), tableName.get())) {
            return Optional.absent();
        }
        Column column = new Column(predicateSegment.getColumn().getName(), tableName.get());
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue predicateCompareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return "=".equals(predicateCompareRightValue.getOperator()) ? createEqualCondition(predicateCompareRightValue, column, sqlStatement.getLogicSQL()) : Optional.<Condition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return createInCondition((PredicateInRightValue) predicateSegment.getRightValue(), column, sqlStatement.getLogicSQL());
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            return createBetweenCondition((PredicateBetweenRightValue) predicateSegment.getRightValue(), column, sqlStatement.getLogicSQL());
        }
        return Optional.absent();
    }
    
    private Optional<Condition> createEqualCondition(final PredicateCompareRightValue expressionSegment, final Column column, final String sql) {
        return expressionSegment.getExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new Condition(column, expressionSegment.getExpression().getSQLExpression(sql))) : Optional.<Condition>absent();
    }
    
    private Optional<Condition> createInCondition(final PredicateInRightValue expressionSegment, final Column column, final String sql) {
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        for (ExpressionSegment each : expressionSegment.getSqlExpressions()) {
            if (!(each instanceof SimpleExpressionSegment)) {
                sqlExpressions.clear();
                break;
            } else {
                sqlExpressions.add(each.getSQLExpression(sql));
            }
        }
        return sqlExpressions.isEmpty() ? Optional.<Condition>absent() : Optional.of(new Condition(column, sqlExpressions));
    }
    
    private Optional<Condition> createBetweenCondition(final PredicateBetweenRightValue expressionSegment, final Column column, final String sql) {
        return expressionSegment.getBetweenExpression() instanceof SimpleExpressionSegment && expressionSegment.getAndExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new Condition(column, expressionSegment.getBetweenExpression().getSQLExpression(sql), expressionSegment.getAndExpression().getSQLExpression(sql)))
                : Optional.<Condition>absent();
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
