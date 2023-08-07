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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Create sharding table rule statement updater.
 */
public final class CreateShardingTableRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingTableRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingTableRuleStatementChecker.checkCreation(database, sqlStatement.getRules(), sqlStatement.isIfNotExists(), currentRuleConfig);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final CreateShardingTableRuleStatement sqlStatement) {
        Collection<AbstractTableRuleSegment> segments = sqlStatement.getRules();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement, currentRuleConfig);
            segments.removeIf(each -> duplicatedRuleNames.contains(each.getLogicTable()));
        }
        return ShardingTableRuleStatementConverter.convert(segments);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getTables().addAll(toBeCreatedRuleConfig.getTables());
        currentRuleConfig.getAutoTables().addAll(toBeCreatedRuleConfig.getAutoTables());
        currentRuleConfig.getShardingAlgorithms().putAll(toBeCreatedRuleConfig.getShardingAlgorithms());
        currentRuleConfig.getKeyGenerators().putAll(toBeCreatedRuleConfig.getKeyGenerators());
        currentRuleConfig.getAuditors().putAll(toBeCreatedRuleConfig.getAuditors());
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateShardingTableRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> currentShardingTables = null == currentRuleConfig ? Collections.emptyList() : getCurrentShardingTables(currentRuleConfig);
        return sqlStatement.getRules().stream().map(AbstractTableRuleSegment::getLogicTable).filter(currentShardingTables::contains).collect(Collectors.toSet());
    }
    
    private Collection<String> getCurrentShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedList<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public Class<CreateShardingTableRuleStatement> getType() {
        return CreateShardingTableRuleStatement.class;
    }
}
