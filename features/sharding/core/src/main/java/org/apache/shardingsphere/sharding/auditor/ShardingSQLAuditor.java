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

package org.apache.shardingsphere.sharding.auditor;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Sharding SQL auditor.
 */
public final class ShardingSQLAuditor implements SQLAuditor<ShardingRule> {
    
    @HighFrequencyInvocation
    @Override
    public void audit(final QueryContext queryContext, final ShardingSphereDatabase database, final ShardingRule rule) {
        Collection<ShardingAuditStrategyConfiguration> auditStrategies = getShardingAuditStrategies(queryContext.getSqlStatementContext(), rule);
        if (auditStrategies.isEmpty()) {
            return;
        }
        Collection<String> disableAuditNames = queryContext.getHintValueContext().getDisableAuditNames();
        for (ShardingAuditStrategyConfiguration each : auditStrategies) {
            audit(queryContext, database, rule, each, disableAuditNames);
        }
    }
    
    private void audit(final QueryContext queryContext, final ShardingSphereDatabase database, final ShardingRule rule,
                       final ShardingAuditStrategyConfiguration auditStrategy, final Collection<String> disableAuditNames) {
        for (String each : auditStrategy.getAuditorNames()) {
            if (!auditStrategy.isAllowHintDisable() || !disableAuditNames.contains(each.toLowerCase())) {
                rule.getAuditors().get(each).check(queryContext.getSqlStatementContext(), queryContext.getParameters(), queryContext.getMetaData().getGlobalRuleMetaData(), database);
            }
        }
    }
    
    private Collection<ShardingAuditStrategyConfiguration> getShardingAuditStrategies(final SQLStatementContext sqlStatementContext, final ShardingRule rule) {
        Collection<ShardingAuditStrategyConfiguration> result = new LinkedList<>();
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            rule.findShardingTable(each).ifPresent(optional -> result.add(rule.getAuditStrategyConfiguration(optional)));
        }
        return result;
    }
    
    @Override
    public int getOrder() {
        return ShardingOrder.ORDER;
    }
    
    @Override
    public Class<ShardingRule> getTypeClass() {
        return ShardingRule.class;
    }
}
