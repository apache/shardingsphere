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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.updater;

import org.apache.shardingsphere.infra.distsql.RDLUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateBindingTablesException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBindingTableRuleNotExistsException;
import org.apache.shardingsphere.proxy.backend.exception.ShardingTableRuleNotExistedException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Create sharding binding table rule statement updater.
 */
public final class CreateShardingBindingTableRuleStatementUpdater implements RDLUpdater<CreateShardingBindingTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        if (null == currentRuleConfig) {
            throw new ShardingBindingTableRuleNotExistsException(schemaName);
        }
        Collection<String> invalidBindingTables = new HashSet<>();
        Collection<String> existLogicTables = getLogicTables(currentRuleConfig);
        Collection<String> bindingTables = sqlStatement.getBindingTables();
        for (String each : bindingTables) {
            if (!existLogicTables.contains(each)) {
                invalidBindingTables.add(each);
            }
        }
        if (!invalidBindingTables.isEmpty()) {
            throw new ShardingTableRuleNotExistedException(schemaName, invalidBindingTables);
        }
        checkToBeCreatedDuplicateBindingTables(sqlStatement, currentRuleConfig);
    }
    
    private Collection<String> getLogicTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new HashSet<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private void checkToBeCreatedDuplicateBindingTables(final CreateShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> toBeCreatedBindingTables = new HashSet<>();
        Collection<String> duplicateBindingTables = sqlStatement.getBindingTables().stream().filter(each -> !toBeCreatedBindingTables.add(each)).collect(Collectors.toSet());
        duplicateBindingTables.addAll(getCurrentBindingTables(currentRuleConfig).stream().filter(each -> !toBeCreatedBindingTables.add(each)).collect(Collectors.toSet()));
        if (!duplicateBindingTables.isEmpty()) {
            throw new DuplicateBindingTablesException(duplicateBindingTables);
        }
    }
    
    private Collection<String> getCurrentBindingTables(final ShardingRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getBindingTableGroups().stream().flatMap(each -> Arrays.stream(each.split(","))).map(String::trim).collect(Collectors.toList());
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final String schemaName, final CreateShardingBindingTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        YamlShardingRuleConfiguration yamlShardingRuleConfiguration = ShardingRuleStatementConverter.convert(sqlStatement);
        currentRuleConfig.getBindingTableGroups().addAll(yamlShardingRuleConfiguration.getBindingTables());
        return false;
    }
    
    @Override
    public String getType() {
        return CreateShardingBindingTableRulesStatement.class.getCanonicalName();
    }
}
