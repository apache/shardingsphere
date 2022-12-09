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
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropBroadcastTableRuleStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop broadcast table rule statement updater.
 */
public final class DropBroadcastTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropBroadcastTableRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkBroadcastTableRuleExist(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkBroadcastTableRuleExist(final String databaseName, final DropBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> currentRules = currentRuleConfig.getBroadcastTables();
        Collection<String> notExistRules = sqlStatement.getTables().stream().filter(each -> !containsIgnoreCase(currentRules, each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistRules.isEmpty(), () -> new MissingRequiredRuleException("Broadcast", databaseName, notExistRules));
    }
    
    private boolean containsIgnoreCase(final Collection<String> currentRules, final String ruleName) {
        return currentRules.stream().anyMatch(each -> each.equalsIgnoreCase(ruleName));
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Broadcast", databaseName));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(currentRuleConfig.getBroadcastTables(), sqlStatement.getTables()).isEmpty();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropBroadcastTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBroadcastTables().removeIf(each -> containsIgnoreCase(sqlStatement.getTables(), each));
        return currentRuleConfig.getTables().isEmpty() && currentRuleConfig.getAutoTables().isEmpty() && currentRuleConfig.getBroadcastTables().isEmpty()
                && null == currentRuleConfig.getDefaultDatabaseShardingStrategy() && null == currentRuleConfig.getDefaultTableShardingStrategy();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropBroadcastTableRuleStatement.class.getName();
    }
}
