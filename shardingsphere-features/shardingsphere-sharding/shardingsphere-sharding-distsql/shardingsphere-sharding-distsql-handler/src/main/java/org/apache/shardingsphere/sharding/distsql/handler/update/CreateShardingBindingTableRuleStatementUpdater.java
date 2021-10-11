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
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create sharding binding table rule statement updater.
 */
public final class CreateShardingBindingTableRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingBindingTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateShardingBindingTableRulesStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeCreatedBindingTables(schemaName, sqlStatement, currentRuleConfig);
        checkToBeCreatedDuplicateBindingTables(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    private void checkToBeCreatedBindingTables(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement,
                                               final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentLogicTables = getCurrentLogicTables(currentRuleConfig);
        Set<String> notExistedBindingTables = sqlStatement.getBindingTables().stream().filter(each -> !currentLogicTables.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistedBindingTables.isEmpty(), new RequiredRuleMissedException("Sharding", schemaName, notExistedBindingTables));
    }
    
    private Collection<String> getCurrentLogicTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new HashSet<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private void checkToBeCreatedDuplicateBindingTables(final String schemaName, 
                                                        final CreateShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DuplicateRuleException {
        Collection<String> toBeCreatedBindingTables = new HashSet<>();
        Collection<String> duplicateBindingTables = sqlStatement.getBindingTables().stream().filter(each -> !toBeCreatedBindingTables.add(each)).collect(Collectors.toSet());
        duplicateBindingTables.addAll(getCurrentBindingTables(currentRuleConfig).stream().filter(each -> !toBeCreatedBindingTables.add(each)).collect(Collectors.toSet()));
        if (!duplicateBindingTables.isEmpty()) {
            throw new DuplicateRuleException("binding", schemaName, duplicateBindingTables);
        }
    }
    
    private Collection<String> getCurrentBindingTables(final ShardingRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getBindingTableGroups().stream().flatMap(each -> Arrays.stream(each.split(","))).map(String::trim).collect(Collectors.toList());
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingBindingTableRulesStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (BindingTableRuleSegment each : sqlStatement.getRules()) {
            result.getBindingTableGroups().add(each.getTableGroups());
        }
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getBindingTableGroups().addAll(toBeCreatedRuleConfig.getBindingTableGroups());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingBindingTableRulesStatement.class.getCanonicalName();
    }
}
