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

import org.apache.shardingsphere.infra.distsql.update.RDLCreateUpdater;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.exception.ShardingBindingTableRuleNotExistsException;
import org.apache.shardingsphere.sharding.distsql.handler.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.distsql.parser.segment.BindingTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Create sharding binding table rule statement updater.
 */
public final class CreateShardingBindingTableRuleStatementUpdater implements RDLCreateUpdater<CreateShardingBindingTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeCreatedBindingTables(schemaName, sqlStatement, currentRuleConfig);
        checkToBeCreatedDuplicateBindingTables(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws ShardingBindingTableRuleNotExistsException {
        if (null == currentRuleConfig) {
            throw new ShardingBindingTableRuleNotExistsException(schemaName);
        }
    }
    
    private void checkToBeCreatedBindingTables(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement, 
                                               final ShardingRuleConfiguration currentRuleConfig) throws ShardingTableRuleNotExistedException {
        Collection<String> notExistedBindingTables = new HashSet<>();
        Collection<String> currentLogicTables = getCurrentLogicTables(currentRuleConfig);
        for (String each : sqlStatement.getBindingTables()) {
            if (!currentLogicTables.contains(each)) {
                notExistedBindingTables.add(each);
            }
        }
        if (!notExistedBindingTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, notExistedBindingTables);
        }
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
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement) {
        return null;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement, 
                                               final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        for (BindingTableRuleSegment each : sqlStatement.getRules()) {
            currentRuleConfig.getBindingTableGroups().add(each.getTableGroups());
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
