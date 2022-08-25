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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop sharding broadcast table rule statement updater.
 */
public final class DropShardingBroadcastTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingBroadcastTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String databaseName = database.getName();
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkBroadcastTableRuleExist(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkBroadcastTableRuleExist(final String databaseName,
                                              final DropShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (sqlStatement.isIfExists()) {
            return;
        }
        if (!sqlStatement.getRules().isEmpty()) {
            Collection<String> currentRules = currentRuleConfig.getBroadcastTables();
            Collection<String> notExistRules = sqlStatement.getRules().stream().filter(each -> !containsIgnoreCase(currentRules, each)).collect(Collectors.toList());
            DistSQLException.predictionThrow(notExistRules.isEmpty(), () -> new RequiredRuleMissedException("Broadcast", databaseName, notExistRules));
        }
    }
    
    private boolean containsIgnoreCase(final Collection<String> collection, final String str) {
        return collection.stream().anyMatch(each -> each.equalsIgnoreCase(str));
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig, () -> new RequiredRuleMissedException("Broadcast", databaseName));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.getRules().isEmpty()) {
            return true;
        }
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(currentRuleConfig.getBroadcastTables(), sqlStatement.getRules()).isEmpty();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (sqlStatement.getRules().isEmpty()) {
            currentRuleConfig.getBroadcastTables().clear();
        } else {
            currentRuleConfig.getBroadcastTables().removeIf(each -> containsIgnoreCase(sqlStatement.getRules(), each));
        }
        return false;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingBroadcastTableRulesStatement.class.getName();
    }
}
