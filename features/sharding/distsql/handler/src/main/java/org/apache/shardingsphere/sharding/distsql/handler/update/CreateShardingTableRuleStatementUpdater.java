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

import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Create sharding table rule statement updater.
 */
public final class CreateShardingTableRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    private boolean ifNotExists;
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        ifNotExists = sqlStatement.isIfNotExists();
        ShardingTableRuleStatementChecker.checkCreation(database, sqlStatement.getRules(), ifNotExists, currentRuleConfig);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final CreateShardingTableRuleStatement sqlStatement) {
        return ShardingTableRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        if (ifNotExists) {
            Collection<String> currentTables = new LinkedList<>();
            currentRuleConfig.getTables().forEach(each -> currentTables.add(each.getLogicTable()));
            currentRuleConfig.getAutoTables().forEach(each -> currentTables.add(each.getLogicTable()));
            toBeCreatedRuleConfig.getTables().removeIf(each -> currentTables.contains(each.getLogicTable()));
            toBeCreatedRuleConfig.getAutoTables().removeIf(each -> currentTables.contains(each.getLogicTable()));
            if (toBeCreatedRuleConfig.getTables().isEmpty() && toBeCreatedRuleConfig.getAutoTables().isEmpty()) {
                return;
            }
        }
        currentRuleConfig.getTables().addAll(toBeCreatedRuleConfig.getTables());
        currentRuleConfig.getAutoTables().addAll(toBeCreatedRuleConfig.getAutoTables());
        currentRuleConfig.getShardingAlgorithms().putAll(toBeCreatedRuleConfig.getShardingAlgorithms());
        currentRuleConfig.getKeyGenerators().putAll(toBeCreatedRuleConfig.getKeyGenerators());
        currentRuleConfig.getAuditors().putAll(toBeCreatedRuleConfig.getAuditors());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingTableRuleStatement.class.getName();
    }
}
