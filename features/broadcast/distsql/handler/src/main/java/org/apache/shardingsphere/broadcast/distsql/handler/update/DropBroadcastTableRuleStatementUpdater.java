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

package org.apache.shardingsphere.broadcast.distsql.handler.update;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.distsql.parser.statement.DropNewBroadcastTableRuleStatement;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop broadcast table rule statement updater.
 */
public final class DropBroadcastTableRuleStatementUpdater implements RuleDefinitionDropUpdater<DropNewBroadcastTableRuleStatement, BroadcastRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropNewBroadcastTableRuleStatement sqlStatement, final BroadcastRuleConfiguration currentRuleConfig) {
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkBroadcastTableRuleExist(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final BroadcastRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Broadcast", databaseName));
    }
    
    private void checkBroadcastTableRuleExist(final String databaseName, final DropNewBroadcastTableRuleStatement sqlStatement, final BroadcastRuleConfiguration currentRuleConfig) {
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
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropNewBroadcastTableRuleStatement sqlStatement, final BroadcastRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(currentRuleConfig.getBroadcastTables(), sqlStatement.getTables()).isEmpty();
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropNewBroadcastTableRuleStatement sqlStatement, final BroadcastRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBroadcastTables().removeIf(each -> containsIgnoreCase(sqlStatement.getTables(), each));
        return currentRuleConfig.getBroadcastTables().isEmpty();
    }
    
    @Override
    public Class<BroadcastRuleConfiguration> getRuleConfigurationClass() {
        return BroadcastRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropNewBroadcastTableRuleStatement.class.getName();
    }
}
