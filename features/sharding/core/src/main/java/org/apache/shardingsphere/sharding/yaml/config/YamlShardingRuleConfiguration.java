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
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterPersistField;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterRuleEntity;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterRuleEntity.Type;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.annotation.RegistryCenterTupleKeyNameGenerator;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.metadata.nodepath.ShardingRuleNodePathProvider;
import org.apache.shardingsphere.sharding.yaml.config.cache.YamlShardingCacheConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingBindingTableRegistryCenterTupleKeyNameGenerator;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding rule configuration for YAML.
 */
@RegistryCenterRuleEntity(Type.DATABASE)
@Getter
@Setter
public final class YamlShardingRuleConfiguration implements YamlRuleConfiguration {
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.TABLES, order = 500)
    private Map<String, YamlTableRuleConfiguration> tables = new LinkedHashMap<>();
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.AUTO_TABLES, order = 501)
    private Map<String, YamlShardingAutoTableRuleConfiguration> autoTables = new LinkedHashMap<>();
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.BINDING_TABLES, order = 502)
    @RegistryCenterTupleKeyNameGenerator(ShardingBindingTableRegistryCenterTupleKeyNameGenerator.class)
    private Collection<String> bindingTables = new LinkedList<>();
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.DEFAULT_DATABASE_STRATEGY, order = 100)
    private YamlShardingStrategyConfiguration defaultDatabaseStrategy;
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.DEFAULT_TABLE_STRATEGY, order = 101)
    private YamlShardingStrategyConfiguration defaultTableStrategy;
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.DEFAULT_KEY_GENERATE_STRATEGY, order = 102)
    private YamlKeyGenerateStrategyConfiguration defaultKeyGenerateStrategy;
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.DEFAULT_AUDIT_STRATEGY, order = 103)
    private YamlShardingAuditStrategyConfiguration defaultAuditStrategy;
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.ALGORITHMS, order = 0)
    private Map<String, YamlAlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.KEY_GENERATORS, order = 1)
    private Map<String, YamlAlgorithmConfiguration> keyGenerators = new LinkedHashMap<>();
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.AUDITORS, order = 2)
    private Map<String, YamlAlgorithmConfiguration> auditors = new LinkedHashMap<>();
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.DEFAULT_SHARDING_COLUMN, order = 200)
    private String defaultShardingColumn;
    
    @RegistryCenterPersistField(value = ShardingRuleNodePathProvider.SHARDING_CACHE, order = 201)
    private YamlShardingCacheConfiguration shardingCache;
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationType() {
        return ShardingRuleConfiguration.class;
    }
}
