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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Unused algorithm finder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnusedAlgorithmFinder {
    
    /**
     * Find unused sharding algorithms.
     *
     * @param ruleConfig sharding rule configuration
     * @return found unused sharding algorithms
     */
    public static Collection<String> findUnusedShardingAlgorithm(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> inUsedAlgorithms = ruleConfig.getTables().stream().map(each -> Arrays.asList(each.getTableShardingStrategy(), each.getDatabaseShardingStrategy()))
                .flatMap(Collection::stream).filter(Objects::nonNull).map(ShardingStrategyConfiguration::getShardingAlgorithmName).collect(Collectors.toSet());
        inUsedAlgorithms.addAll(ruleConfig.getTables().stream().filter(each -> null != each.getDatabaseShardingStrategy())
                .map(each -> each.getDatabaseShardingStrategy().getShardingAlgorithmName()).collect(Collectors.toSet()));
        inUsedAlgorithms.addAll(ruleConfig.getTables().stream().filter(each -> null != each.getTableShardingStrategy())
                .map(each -> each.getTableShardingStrategy().getShardingAlgorithmName()).collect(Collectors.toSet()));
        inUsedAlgorithms.addAll(ruleConfig.getAutoTables().stream().filter(each -> null != each.getShardingStrategy())
                .map(each -> each.getShardingStrategy().getShardingAlgorithmName()).collect(Collectors.toSet()));
        if (null != ruleConfig.getDefaultDatabaseShardingStrategy()) {
            inUsedAlgorithms.add(ruleConfig.getDefaultDatabaseShardingStrategy().getShardingAlgorithmName());
        }
        if (null != ruleConfig.getDefaultTableShardingStrategy()) {
            inUsedAlgorithms.add(ruleConfig.getDefaultTableShardingStrategy().getShardingAlgorithmName());
        }
        return ruleConfig.getShardingAlgorithms().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
    }
    
    /**
     * Find unused key generators.
     *
     * @param ruleConfig sharding rule configuration
     * @return found unused key generators
     */
    public static Collection<String> findUnusedKeyGenerator(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> inUsedKeyGenerators = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getKeyGenerateStrategy).filter(Objects::nonNull)
                .map(KeyGenerateStrategyConfiguration::getKeyGeneratorName).collect(Collectors.toSet());
        inUsedKeyGenerators.addAll(ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getKeyGenerateStrategy).filter(Objects::nonNull)
                .map(KeyGenerateStrategyConfiguration::getKeyGeneratorName).collect(Collectors.toSet()));
        if (null != ruleConfig.getDefaultKeyGenerateStrategy()) {
            inUsedKeyGenerators.add(ruleConfig.getDefaultKeyGenerateStrategy().getKeyGeneratorName());
        }
        return ruleConfig.getKeyGenerators().keySet().stream().filter(each -> !inUsedKeyGenerators.contains(each)).collect(Collectors.toSet());
    }
    
    /**
     * Find unused auditors.
     *
     * @param ruleConfig sharding rule configuration
     * @return found unused auditors
     */
    public static Collection<String> findUnusedAuditor(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> inUsedAuditors = ruleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getAuditStrategy).filter(Objects::nonNull)
                .flatMap(each -> each.getAuditorNames().stream()).collect(Collectors.toSet());
        inUsedAuditors.addAll(ruleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getAuditStrategy).filter(Objects::nonNull)
                .flatMap(each -> each.getAuditorNames().stream()).collect(Collectors.toSet()));
        if (null != ruleConfig.getDefaultAuditStrategy()) {
            inUsedAuditors.addAll(ruleConfig.getDefaultAuditStrategy().getAuditorNames());
        }
        return ruleConfig.getAuditors().keySet().stream().filter(each -> !inUsedAuditors.contains(each)).collect(Collectors.toSet());
    }
}
