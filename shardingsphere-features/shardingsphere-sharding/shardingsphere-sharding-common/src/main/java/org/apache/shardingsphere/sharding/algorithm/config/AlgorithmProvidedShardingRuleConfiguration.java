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

package org.apache.shardingsphere.sharding.algorithm.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

/**
 * Algorithm provided sharding rule configuration.
 */
@Getter
@Setter
public final class AlgorithmProvidedShardingRuleConfiguration implements RuleConfiguration {
    
    private Collection<ShardingTableRuleConfiguration> tables = new LinkedList<>();
    
    private Collection<ShardingAutoTableRuleConfiguration> autoTables = new LinkedList<>();
    
    private Collection<String> bindingTableGroups = new LinkedList<>();
    
    private Collection<String> broadcastTables = new LinkedList<>();
    
    private ShardingStrategyConfiguration defaultDatabaseShardingStrategy;
    
    private ShardingStrategyConfiguration defaultTableShardingStrategy;
    
    private KeyGenerateStrategyConfiguration defaultKeyGenerateStrategy;
    
    private Map<String, ShardingAlgorithm> shardingAlgorithms = new LinkedHashMap<>();
    
    private Map<String, KeyGenerateAlgorithm> keyGenerators = new LinkedHashMap<>();
    
    public AlgorithmProvidedShardingRuleConfiguration() {
    }
    
    public AlgorithmProvidedShardingRuleConfiguration(final Collection<ShardingTableRuleConfiguration> tables,
                                                      final Collection<ShardingAutoTableRuleConfiguration> autoTables,
                                                      final Collection<String> bindingTableGroups,
                                                      final Collection<String> broadcastTables,
                                                      final ShardingStrategyConfiguration defaultDatabaseShardingStrategy,
                                                      final ShardingStrategyConfiguration defaultTableShardingStrategy,
                                                      final KeyGenerateStrategyConfiguration defaultKeyGenerateStrategy,
                                                      final Map<String, ShardingAlgorithm> shardingAlgorithms,
                                                      final Map<String, KeyGenerateAlgorithm> keyGenerators) {
        this.tables = tables;
        this.autoTables = autoTables;
        this.bindingTableGroups = bindingTableGroups;
        this.broadcastTables = broadcastTables;
        this.defaultDatabaseShardingStrategy = defaultDatabaseShardingStrategy;
        this.defaultTableShardingStrategy = defaultTableShardingStrategy;
        this.defaultKeyGenerateStrategy = defaultKeyGenerateStrategy;
        this.shardingAlgorithms = shardingAlgorithms;
        this.keyGenerators = keyGenerators;
    }
}
