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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditor;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sharding SQL auditor.
 */
public final class ShardingSQLAuditor implements SQLAuditor<ShardingRule> {
    
    @Override
    public void audit(final SQLStatementContext sqlStatementContext, final List<Object> params, final Grantee grantee, final RuleMetaData globalRuleMetaData,
                      final ShardingSphereDatabase database, final ShardingRule rule, final HintValueContext hintValueContext) {
        Collection<ShardingAuditStrategyConfiguration> auditStrategies = getShardingAuditStrategies(sqlStatementContext, rule);
        if (auditStrategies.isEmpty()) {
            return;
        }
        Collection<String> disableAuditNames = hintValueContext.findDisableAuditNames();
        for (ShardingAuditStrategyConfiguration auditStrategy : auditStrategies) {
            for (String auditorName : auditStrategy.getAuditorNames()) {
                if (!auditStrategy.isAllowHintDisable() || !disableAuditNames.contains(auditorName.toLowerCase())) {
                    rule.getAuditors().get(auditorName).check(sqlStatementContext, params, grantee, globalRuleMetaData, database);
                }
            }
        }
    }
    
    private Collection<ShardingAuditStrategyConfiguration> getShardingAuditStrategies(final SQLStatementContext sqlStatementContext, final ShardingRule rule) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Collection<ShardingAuditStrategyConfiguration> result = new ArrayList<>(tableNames.size());
        for (String each : tableNames) {
            rule.findTableRule(each).ifPresent(optional -> result.add(rule.getAuditStrategyConfiguration(optional)));
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
