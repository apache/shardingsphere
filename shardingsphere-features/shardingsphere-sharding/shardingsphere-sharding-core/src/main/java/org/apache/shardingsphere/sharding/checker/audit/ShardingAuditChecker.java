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

package org.apache.shardingsphere.sharding.checker.audit;

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.check.SQLCheckResult;
import org.apache.shardingsphere.infra.executor.check.SQLChecker;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.constant.ShardingOrder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Sharding audit checker.
 */
public final class ShardingAuditChecker implements SQLChecker<ShardingRule> {
    
    @Override
    public boolean check(final String databaseName, final Grantee grantee, final ShardingRule rule) {
        return true;
    }
    
    @Override
    public SQLCheckResult check(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final Grantee grantee,
                                final String currentDatabase, final Map<String, ShardingSphereDatabase> databases, final ShardingRule rule) {
        Collection<ShardingAuditStrategyConfiguration> auditStrategies = getShardingAuditStrategies(sqlStatementContext, rule);
        if (auditStrategies.isEmpty()) {
            return new SQLCheckResult(true, "");
        }
        Collection<String> disableAuditNames = sqlStatementContext instanceof CommonSQLStatementContext
                ? ((CommonSQLStatementContext<?>) sqlStatementContext).getSqlHintExtractor().findDisableAuditNames()
                : Collections.emptyList();
        for (ShardingAuditStrategyConfiguration auditStrategy : auditStrategies) {
            for (String auditorName : auditStrategy.getAuditorNames()) {
                if (auditStrategy.isAllowHintDisable() && disableAuditNames.contains(auditorName.toLowerCase())) {
                    continue;
                }
                SQLCheckResult result = rule.getAuditors().get(auditorName).check(sqlStatementContext, parameters, grantee, databases.get(currentDatabase.toLowerCase()));
                if (!result.isPassed()) {
                    return result;
                }
            }
        }
        return new SQLCheckResult(true, "");
    }
    
    @Override
    public boolean check(final Grantee grantee, final ShardingRule rule) {
        return true;
    }
    
    @Override
    public boolean check(final Grantee grantee, final BiPredicate<Object, Object> validator, final Object cipher, final ShardingRule rule) {
        return true;
    }
    
    private Collection<ShardingAuditStrategyConfiguration> getShardingAuditStrategies(final SQLStatementContext<?> sqlStatementContext, final ShardingRule rule) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Collection<ShardingAuditStrategyConfiguration> result = new ArrayList<>(tableNames.size());
        for (String each : tableNames) {
            rule.findTableRule(each).ifPresent(tableRule -> result.add(rule.getAuditStrategyConfiguration(tableRule)));
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
