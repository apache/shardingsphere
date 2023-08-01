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

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableReferenceRuleStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop sharding table reference rule statement updater.
 */
public final class DropShardingTableReferenceRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingTableReferenceRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeDroppedShardingTableReferenceRules(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkState(null != currentRuleConfig && !currentRuleConfig.getBindingTableGroups().isEmpty(),
                () -> new MissingRequiredRuleException("Sharding table reference", databaseName));
    }
    
    private void checkToBeDroppedShardingTableReferenceRules(final String databaseName,
                                                             final DropShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> currentRuleNames = getCurrentShardingTableReferenceRuleNames(currentRuleConfig);
        Collection<String> notExistedRuleNames = sqlStatement.getNames().stream().filter(each -> !containsIgnoreCase(currentRuleNames, each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedRuleNames.isEmpty(), () -> new MissingRequiredRuleException("Sharding table reference", databaseName, notExistedRuleNames));
    }
    
    private Collection<String> getCurrentShardingTableReferenceRuleNames(final ShardingRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getBindingTableGroups().stream().map(ShardingTableReferenceRuleConfiguration::getName).collect(Collectors.toList());
    }
    
    private boolean containsIgnoreCase(final Collection<String> ruleNames, final String name) {
        return ruleNames.stream().anyMatch(each -> each.equalsIgnoreCase(name));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final DropShardingTableReferenceRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration(each, ""));
        }
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBindingTableGroups().removeIf(each -> sqlStatement.getNames().stream().anyMatch(each.getName()::equalsIgnoreCase));
        return false;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(getCurrentShardingTableReferenceRuleNames(currentRuleConfig), sqlStatement.getNames()).isEmpty();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingTableReferenceRuleStatement.class.getName();
    }
}
