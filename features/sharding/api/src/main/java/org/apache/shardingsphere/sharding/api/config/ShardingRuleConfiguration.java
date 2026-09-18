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

package org.apache.shardingsphere.sharding.api.config;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.config.rule.function.DistributedRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sharding rule configuration.
 */
@Getter
@Setter
public final class ShardingRuleConfiguration implements DatabaseRuleConfiguration, DistributedRuleConfiguration {
    
    @NotNull
    @Valid
    private Collection<@NotNull ShardingTableRuleConfiguration> tables = new LinkedList<>();
    
    @NotNull
    @Valid
    private Collection<@NotNull ShardingAutoTableRuleConfiguration> autoTables = new LinkedList<>();
    
    @NotNull
    @Valid
    private Collection<@NotNull ShardingTableReferenceRuleConfiguration> bindingTableGroups = new LinkedList<>();
    
    @Valid
    private ShardingStrategyConfiguration defaultDatabaseShardingStrategy;
    
    @Valid
    private ShardingStrategyConfiguration defaultTableShardingStrategy;
    
    @Valid
    private KeyGenerateStrategyConfiguration defaultKeyGenerateStrategy;
    
    @Valid
    private ShardingAuditStrategyConfiguration defaultAuditStrategy;
    
    private String defaultShardingColumn;
    
    @NotNull
    @Valid
    private Map<@NotBlank String, @NotNull KeyGenerateStrategiesConfiguration> keyGenerateStrategies = new LinkedHashMap<>();
    
    @NotNull
    private Map<@NotBlank String, @NotNull AlgorithmConfiguration> shardingAlgorithms = new LinkedHashMap<>();
    
    @NotNull
    private Map<@NotBlank String, @NotNull AlgorithmConfiguration> keyGenerators = new LinkedHashMap<>();
    
    @NotNull
    private Map<@NotBlank String, @NotNull AlgorithmConfiguration> auditors = new LinkedHashMap<>();
    
    @Override
    public Collection<String> getLogicTableNames() {
        return new CaseInsensitiveSet<>(tables.stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toList()));
    }
}
