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
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;

import java.util.Collection;
import java.util.HashSet;

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
                    fill(predicate, sqlStatement);
                }
            }
        }
    }

    private void fill(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        Optional<String> tableName = PredicateUtils.findTableName(predicateSegment, sqlStatement, shardingTableMetaData);
        if (!tableName.isPresent() || !isNeedEncrypt(predicateSegment, tableName.get())) {
            return;
        }
        Column column = new Column(predicateSegment.getColumn().getName(), tableName.get());
        Optional<Condition> condition = createCondition(predicateSegment, column);
        if (condition.isPresent()) {
            AndCondition andCondition;
            if (sqlStatement.getEncryptCondition().getOrConditions().isEmpty()) {
                andCondition = new AndCondition();
                sqlStatement.getEncryptCondition().getOrConditions().add(andCondition);
            } else {
                andCondition = sqlStatement.getEncryptCondition().getOrConditions().get(0);
            }
            andCondition.getConditions().add(condition.get());
            sqlStatement.getSQLTokens().add(new EncryptColumnToken(predicateSegment.getColumn().getStartIndex(), predicateSegment.getStopIndex(), column, true));
        }
    }

    private Optional<Condition> createCondition(final PredicateSegment predicateSegment, final Column column) {
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue compareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return isOperatorSupportedWithEncrypt(compareRightValue.getOperator()) ? PredicateUtils.createCompareCondition(compareRightValue, column) : Optional.<Condition>absent();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            return PredicateUtils.createInCondition((PredicateInRightValue) predicateSegment.getRightValue(), column);
        }
        return Optional.absent();
    }
    
    private boolean isNeedEncrypt(final PredicateSegment predicate, final String tableName) {
        // TODO panjuan: spilt EncryptRule and EncryptorEngine, cannot pass EncryptorEngine to parse module
        encryptorEngine = null == encryptorEngine ? encryptRule.getEncryptorEngine() : encryptorEngine;
        return encryptorEngine.getShardingEncryptor(tableName, predicate.getColumn().getName()).isPresent();
    }
    
    private boolean isOperatorSupportedWithEncrypt(final String operator) {
        return "=".equals(operator) || "<>".equals(operator) || "!=".equals(operator);
    }
}
