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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Drop sharding broadcast table rule statement updater.
 */
public final class DropShardingBroadcastTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingBroadcastTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropShardingBroadcastTableRulesStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isContainsExistClause()) {
            return;
        }
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkBroadCastTableRuleExist(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkBroadCastTableRuleExist(final String schemaName, final DropShardingBroadcastTableRulesStatement sqlStatement,
                                              final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (sqlStatement.isContainsExistClause()) {
            return;
        }
        if (!sqlStatement.getRules().isEmpty()) {
            Collection<String> currentRules = currentRuleConfig.getBroadcastTables();
            LinkedList<String> notExistRules = sqlStatement.getRules().stream().filter(each -> !currentRules.contains(each)).collect(Collectors.toCollection(LinkedList::new));
            DistSQLException.predictionThrow(notExistRules.isEmpty(), () -> new RequiredRuleMissedException("Broadcast", schemaName, notExistRules));
        }
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig, () -> new RequiredRuleMissedException("Broadcast", schemaName));
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
            currentRuleConfig.getBroadcastTables().removeIf(sqlStatement.getRules()::contains);
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
