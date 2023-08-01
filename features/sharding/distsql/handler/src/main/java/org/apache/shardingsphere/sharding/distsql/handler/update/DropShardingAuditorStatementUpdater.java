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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingAuditorStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Drop sharding auditor statement updater.
 */
public final class DropShardingAuditorStatementUpdater implements RuleDefinitionDropUpdater<DropShardingAuditorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropShardingAuditorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        Collection<String> auditorNames = new LinkedList<>(sqlStatement.getNames());
        checkExist(databaseName, auditorNames, currentRuleConfig, sqlStatement);
        checkInUsed(databaseName, auditorNames, currentRuleConfig);
    }
    
    private void checkExist(final String databaseName, final Collection<String> auditorNames, final ShardingRuleConfiguration currentRuleConfig, final DropShardingAuditorStatement sqlStatement) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> notExistAuditors = auditorNames.stream().filter(each -> !currentRuleConfig.getAuditors().containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistAuditors.isEmpty(), () -> new MissingRequiredAlgorithmException("Sharding auditor", databaseName, notExistAuditors));
    }
    
    private void checkInUsed(final String databaseName, final Collection<String> auditorNames, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> usedAuditors = getUsedAuditors(currentRuleConfig);
        Collection<String> inUsedNames = auditorNames.stream().filter(usedAuditors::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(inUsedNames.isEmpty(), () -> new AlgorithmInUsedException("Sharding auditor", databaseName, inUsedNames));
    }
    
    private Collection<String> getUsedAuditors(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().stream().filter(each -> null != each.getAuditStrategy()).forEach(each -> result.addAll(each.getAuditStrategy().getAuditorNames()));
        shardingRuleConfig.getAutoTables().stream().filter(each -> null != each.getAuditStrategy()).forEach(each -> result.addAll(each.getAuditStrategy().getAuditorNames()));
        ShardingAuditStrategyConfiguration auditStrategy = shardingRuleConfig.getDefaultAuditStrategy();
        if (null != auditStrategy && !auditStrategy.getAuditorNames().isEmpty()) {
            result.addAll(auditStrategy.getAuditorNames());
        }
        return result;
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final DropShardingAuditorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getAuditors().put(each, currentRuleConfig.getAuditors().get(each));
        }
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingAuditorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getAuditors().keySet().removeIf(sqlStatement.getNames()::contains);
        return false;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingAuditorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig && !getIdenticalData(currentRuleConfig.getAuditors().keySet(), sqlStatement.getNames()).isEmpty();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingAuditorStatement.class.getName();
    }
}
