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

package org.apache.shardingsphere.sharding.yaml.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleField;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleEntity;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleField.Type;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleKeyListNameGenerator;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingBindingTableRepositoryTupleKeyListNameGenerator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding rule configuration for YAML.
 */
@RuleRepositoryTupleEntity("sharding")
@Getter
@Setter
public final class YamlShardingRuleConfiguration implements YamlRuleConfiguration {
    
    @RuleRepositoryTupleField(type = Type.TABLE)
    private Map<String, YamlTableRuleConfiguration> tables = new LinkedHashMap<>();
    
    @RuleRepositoryTupleField(type = Type.TABLE)
    private Map<String, YamlShardingAutoTableRuleConfiguration> autoTables = new LinkedHashMap<>();
    
    @RuleRepositoryTupleField(type = Type.TABLE)
    @RuleRepositoryTupleKeyListNameGenerator(ShardingBindingTableRepositoryTupleKeyListNameGenerator.class)
    private Collection<String> bindingTables = new LinkedList<>();
    
    @RuleRepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlShardingStrategyConfiguration defaultDatabaseStrategy;
    
    @RuleRepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlShardingStrategyConfiguration defaultTableStrategy;
    
    @RuleRepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlKeyGenerateStrategyConfiguration defaultKeyGenerateStrategy;
    
    @RuleRepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlShardingAuditStrategyConfiguration defaultAuditStrategy;
    
    @RuleRepositoryTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
    
    @RuleRepositoryTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> keyGenerators = new LinkedHashMap<>();
    
    @RuleRepositoryTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> auditors = new LinkedHashMap<>();
    
    @RuleRepositoryTupleField(type = Type.OTHER)
    private String defaultShardingColumn;
    
    @RuleRepositoryTupleField(type = Type.OTHER)
    private YamlShardingCacheConfiguration shardingCache;
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationType() {
        return ShardingRuleConfiguration.class;
    }
}
