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

package org.apache.shardingsphere.sharding.algorithm.audit;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sharding.exception.audit.DMLWithoutShardingKeyException;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * DML sharding conditions sharding audit algorithm.
 */
@HighFrequencyInvocation
public final class DMLShardingConditionsShardingAuditAlgorithm implements ShardingAuditAlgorithm {
    
    @Override
    public void check(final SQLStatementContext sqlStatementContext, final List<Object> params, final RuleMetaData globalRuleMetaData, final ShardingSphereDatabase database) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            ShardingRule rule = database.getRuleMetaData().getSingleRule(ShardingRule.class);
            if (sqlStatementContext.getTablesContext().getTableNames().stream().anyMatch(rule::isShardingTable)) {
                if (sqlStatementContext instanceof InsertStatementContext) {
                    InsertStatementContext context = (InsertStatementContext) sqlStatementContext;
                    Collection<Comparable<?>> savedGeneratedValues = context.getGeneratedKeyContext().map(GeneratedKeyContext::getGeneratedValues).orElse(new LinkedList<>());
                    ShardingSpherePreconditions.checkNotEmpty(
                            new ShardingConditionEngine(globalRuleMetaData, database, rule).createShardingConditions(sqlStatementContext, params), DMLWithoutShardingKeyException::new);
                    context.getGeneratedKeyContext().ifPresent(generatedKeyContext -> {
                        generatedKeyContext.getGeneratedValues().clear();
                        generatedKeyContext.getGeneratedValues().addAll(savedGeneratedValues);
                    });
                } else {
                    ShardingSpherePreconditions.checkNotEmpty(
                            new ShardingConditionEngine(globalRuleMetaData, database, rule).createShardingConditions(sqlStatementContext, params), DMLWithoutShardingKeyException::new);
                }
            }
        }
    }
    
    @Override
    public String getType() {
        return "DML_SHARDING_CONDITIONS";
    }
}
