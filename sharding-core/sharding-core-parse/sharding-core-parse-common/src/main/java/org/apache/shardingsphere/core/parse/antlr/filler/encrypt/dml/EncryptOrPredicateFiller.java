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

package org.apache.shardingsphere.core.parse.antlr.filler.encrypt.dml;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.EncryptRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.common.dml.PredicateUtils;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.AndPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Or predicate filler for encrypt.
 *
 * @author duhongjun
 */
@Setter
public final class EncryptOrPredicateFiller implements SQLSegmentFiller<OrPredicateSegment>, EncryptRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private EncryptRule encryptRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Deprecated // TODO should use encryptRule, to be refactored
    private ShardingEncryptorEngine encryptorEngine;
    
    @Override
    public void fill(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        Collection<Integer> stopIndexes = new HashSet<>();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (stopIndexes.add(predicate.getStopIndex())) {
                    Optional<String> tableName = PredicateUtils.findTableName(predicate, sqlStatement, shardingTableMetaData);
                    if (tableName.isPresent() && isNeedEncrypt(predicate, tableName.get())) {
                        fill(predicate, tableName.get(), sqlStatement);
                    }
                }
            }
        }
    }
    
    private void fill(final PredicateSegment predicateSegment, final String tableName, final SQLStatement sqlStatement) {
        AndCondition andCondition;
        if (sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().isEmpty()) {
            andCondition = new AndCondition();
            sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().add(andCondition);
        } else {
            andCondition = sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0);
        }
        Optional<Condition> condition = createCondition(predicateSegment, sqlStatement);
        if (condition.isPresent()) {
            andCondition.getConditions().add(condition.get());
            sqlStatement.getSQLTokens().add(
                    new EncryptColumnToken(predicateSegment.getColumn().getStartIndex(), predicateSegment.getStopIndex(), new Column(predicateSegment.getColumn().getName(), tableName), true));
        }
    }
    
    private Optional<Condition> createCondition(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        Optional<String> tableName = PredicateUtils.findTableName(predicateSegment, sqlStatement, shardingTableMetaData);
        if (!tableName.isPresent() || !isNeedEncrypt(predicateSegment, tableName.get())) {
            return Optional.absent();
        }
        Column column = new Column(predicateSegment.getColumn().getName(), tableName.get());
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue predicateCompareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return "=".equals(predicateCompareRightValue.getOperator()) || "<>".equals(predicateCompareRightValue.getOperator()) || "!=".equals(predicateCompareRightValue.getOperator())
                    ? createEqualCondition(predicateCompareRightValue, column) : Optional.<Condition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return createInCondition((PredicateInRightValue) predicateSegment.getRightValue(), column);
        }
        return Optional.absent();
    }
    
    private boolean isNeedEncrypt(final PredicateSegment predicate, final String tableName) {
        // TODO panjuan: spilt EncryptRule and EncryptorEngine, cannot pass EncryptorEngine to parse module
        encryptorEngine = null == encryptorEngine ? encryptRule.getEncryptorEngine() : encryptorEngine;
        return encryptorEngine.getShardingEncryptor(tableName, predicate.getColumn().getName()).isPresent();
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
}
