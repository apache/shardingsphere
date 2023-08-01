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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.audit.exception.SQLAuditException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.List;

/**
 * DML sharding conditions sharding audit algorithm.
 */
public final class DMLShardingConditionsShardingAuditAlgorithm implements ShardingAuditAlgorithm {
    
    @Override
    public void check(final SQLStatementContext sqlStatementContext, final List<Object> params,
                      final Grantee grantee, final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            ShardingRule rule = database.getRuleMetaData().getSingleRule(ShardingRule.class);
            if (sqlStatementContext.getTablesContext().getTableNames().stream().anyMatch(rule::isShardingTable)) {
                ShardingSpherePreconditions.checkState(!new ShardingConditionEngine(globalRuleMetaData, database, rule).createShardingConditions(sqlStatementContext, params).isEmpty(),
                        () -> new SQLAuditException("Not allow DML operation without sharding conditions"));
            }
        }
    }
    
    @Override
    public String getType() {
        return "DML_SHARDING_CONDITIONS";
    }
}
