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

package org.apache.shardingsphere.core.parse.filler.sharding.dml;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.filler.common.dml.PredicateUtils;
import org.apache.shardingsphere.core.parse.filler.encrypt.dml.EncryptOrPredicateFiller;
import org.apache.shardingsphere.core.parse.filler.sharding.dml.select.ShardingRowNumberPredicateFiller;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.condition.ParseCondition;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Or predicate filler for sharding.
 *
 * @author duhongjun
 * @author zhangliang
 * @author panjuan
 */
@Setter
public final class ShardingOrPredicateFiller implements SQLSegmentFiller<OrPredicateSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private final ShardingRowNumberPredicateFiller shardingRowNumberPredicateFiller = new ShardingRowNumberPredicateFiller();
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        sqlStatement.getRouteCondition().getOrConditions().addAll(buildConditions(sqlSegment, sqlStatement).getOrConditions());
        if (sqlStatement instanceof SelectStatement) {
            shardingRowNumberPredicateFiller.fill(sqlSegment, sqlStatement);
        }
    }
    
    /**
     * Build condition.
     *
     * @param sqlSegment SQL segment
     * @param sqlStatement SQL statement
     * @return or condition
     */
    public ParseCondition buildConditions(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        ParseCondition result = createParseCondition(sqlSegment, sqlStatement);
        createEncryptOrPredicateFiller().fill(sqlSegment, sqlStatement);
        return result;
    }
    
    private EncryptOrPredicateFiller createEncryptOrPredicateFiller() {
        EncryptOrPredicateFiller result = new EncryptOrPredicateFiller();
        result.setEncryptorEngine(shardingRule.getEncryptRule().getEncryptorEngine());
        result.setShardingTableMetaData(shardingTableMetaData);
        return result;
    }
    
    private ParseCondition createParseCondition(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        ParseCondition result = new ParseCondition();
        for (AndPredicate each : sqlSegment.getAndPredicates()) {
            AndCondition andCondition = new AndCondition();
            for (PredicateSegment predicate : each.getPredicates()) {
                Optional<String> tableName = PredicateUtils.findTableName(predicate, sqlStatement, shardingTableMetaData);
                if (!tableName.isPresent() || !shardingRule.isShardingColumn(predicate.getColumn().getName(), tableName.get())) {
                    continue;
                }
                Optional<Condition> condition = createCondition(predicate, new Column(predicate.getColumn().getName(), tableName.get()));
                if (condition.isPresent()) {
                    andCondition.getConditions().add(condition.get());
                }
            }
            if (andCondition.getConditions().isEmpty()) {
                result.getOrConditions().clear();
                return result;
            }
            result.getOrConditions().add(andCondition);
        }
        return result;
    }
    
    private Optional<Condition> createCondition(final PredicateSegment predicateSegment, final Column column) {
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue compareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return isOperatorSupportedWithSharding(compareRightValue.getOperator()) 
                    ? PredicateUtils.createCompareCondition(compareRightValue, column, predicateSegment) : Optional.<Condition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return PredicateUtils.createInCondition((PredicateInRightValue) predicateSegment.getRightValue(), column, predicateSegment);
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            return PredicateUtils.createBetweenCondition((PredicateBetweenRightValue) predicateSegment.getRightValue(), column, predicateSegment);
        }
        return Optional.absent();
    }
    
    private boolean isOperatorSupportedWithSharding(final String operator) {
        return "=".equals(operator);
    }
}
