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
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleField;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleField.Type;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleKeyListNameGenerator;
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
@RepositoryTupleEntity("sharding")
@Getter
@Setter
public final class YamlShardingRuleConfiguration implements YamlRuleConfiguration {
    
    @RepositoryTupleField(type = Type.TABLE)
    private Map<String, YamlTableRuleConfiguration> tables = new LinkedHashMap<>();
    
    @RepositoryTupleField(type = Type.TABLE)
    private Map<String, YamlShardingAutoTableRuleConfiguration> autoTables = new LinkedHashMap<>();
    
    @RepositoryTupleField(type = Type.TABLE)
    @RepositoryTupleKeyListNameGenerator(ShardingBindingTableRepositoryTupleKeyListNameGenerator.class)
    private Collection<String> bindingTables = new LinkedList<>();
    
    @RepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlShardingStrategyConfiguration defaultDatabaseStrategy;
    
    @RepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlShardingStrategyConfiguration defaultTableStrategy;
    
    @RepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlKeyGenerateStrategyConfiguration defaultKeyGenerateStrategy;
    
    @RepositoryTupleField(type = Type.DEFAULT_STRATEGY)
    private YamlShardingAuditStrategyConfiguration defaultAuditStrategy;
    
    @RepositoryTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
    
    @RepositoryTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> keyGenerators = new LinkedHashMap<>();
    
    @RepositoryTupleField(type = Type.ALGORITHM)
    private Map<String, YamlAlgorithmConfiguration> auditors = new LinkedHashMap<>();
    
    @RepositoryTupleField(type = Type.OTHER)
    private String defaultShardingColumn;
    
    @RepositoryTupleField(type = Type.OTHER)
    private YamlShardingCacheConfiguration shardingCache;
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationType() {
        return ShardingRuleConfiguration.class;
    }
}
