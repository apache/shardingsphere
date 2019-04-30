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
import org.apache.shardingsphere.core.parse.antlr.filler.common.dml.PredicateUtils;
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
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.OrCondition;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
        Optional<String> tableName = PredicateUtils.findTableName(predicateSegment, sqlStatement, shardingTableMetaData);
        if (!tableName.isPresent() || !shardingRule.isShardingColumn(predicateSegment.getColumn().getName(), tableName.get())) {
            return Optional.absent();
        }
        Column column = new Column(predicateSegment.getColumn().getName(), tableName.get());
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue predicateCompareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return "=".equals(predicateCompareRightValue.getOperator()) ? createEqualCondition(predicateCompareRightValue, column) : Optional.<Condition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return createInCondition((PredicateInRightValue) predicateSegment.getRightValue(), column);
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            return createBetweenCondition((PredicateBetweenRightValue) predicateSegment.getRightValue(), column);
        }
        return Optional.absent();
    }
    
    private Optional<Condition> createEqualCondition(final PredicateCompareRightValue expressionSegment, final Column column) {
        return expressionSegment.getExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new Condition(column, ((SimpleExpressionSegment) expressionSegment.getExpression()).getSQLExpression())) : Optional.<Condition>absent();
    }
    
    private Optional<Condition> createInCondition(final PredicateInRightValue expressionSegment, final Column column) {
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        for (ExpressionSegment each : expressionSegment.getSqlExpressions()) {
            if (!(each instanceof SimpleExpressionSegment)) {
                sqlExpressions.clear();
                break;
            } else {
                sqlExpressions.add(((SimpleExpressionSegment) each).getSQLExpression());
            }
        }
        return sqlExpressions.isEmpty() ? Optional.<Condition>absent() : Optional.of(new Condition(column, sqlExpressions));
    }
    
    private Optional<Condition> createBetweenCondition(final PredicateBetweenRightValue expressionSegment, final Column column) {
        return expressionSegment.getBetweenExpression() instanceof SimpleExpressionSegment && expressionSegment.getAndExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new Condition(column, 
                ((SimpleExpressionSegment) expressionSegment.getBetweenExpression()).getSQLExpression(), ((SimpleExpressionSegment) expressionSegment.getAndExpression()).getSQLExpression()))
                : Optional.<Condition>absent();
    }
}
