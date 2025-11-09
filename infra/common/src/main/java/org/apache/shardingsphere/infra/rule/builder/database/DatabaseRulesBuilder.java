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

package org.apache.shardingsphere.infra.rule.builder.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.config.rule.function.DistributedRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.function.EnhancedRuleConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Database rules builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseRulesBuilder {
    
    /**
     * Build database rules.
     *
     * @param databaseName database name
     * @param protocolType protocol type
     * @param databaseConfig database configuration
     * @param computeNodeInstanceContext compute node instance context
     * @param resourceMetaData resource meta data
     * @return built rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> build(final String databaseName, final DatabaseType protocolType, final DatabaseConfiguration databaseConfig,
                                                       final ComputeNodeInstanceContext computeNodeInstanceContext, final ResourceMetaData resourceMetaData) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, DatabaseRuleBuilder> entry : getRuleBuilderMap(databaseConfig).entrySet()) {
            DatabaseRuleConfigurationChecker configChecker = OrderedSPILoader.getServicesByClass(
                    DatabaseRuleConfigurationChecker.class, Collections.singleton(entry.getKey().getClass())).get(entry.getKey().getClass());
            if (null != configChecker) {
                configChecker.check(databaseName, entry.getKey(), resourceMetaData.getDataSourceMap(), result);
            }
            result.add(entry.getValue().build(entry.getKey(), databaseName, protocolType, resourceMetaData, result, computeNodeInstanceContext));
        }
        return result;
    }
    
    /**
     * Build database rule.
     *
     * @param databaseName database name
     * @param protocolType protocol type
     * @param rules rules
     * @param ruleConfig rule configuration
     * @param computeNodeInstanceContext compute node instance context
     * @param resourceMetaData resource meta data
     * @return built rule
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ShardingSphereRule build(final String databaseName, final DatabaseType protocolType, final Collection<ShardingSphereRule> rules, final RuleConfiguration ruleConfig,
                                           final ComputeNodeInstanceContext computeNodeInstanceContext, final ResourceMetaData resourceMetaData) {
        DatabaseRuleBuilder databaseRuleBuilder = OrderedSPILoader.getServices(DatabaseRuleBuilder.class, Collections.singleton(ruleConfig)).get(ruleConfig);
        DatabaseRuleConfigurationChecker configChecker =
                OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class, Collections.singleton(ruleConfig.getClass())).get(ruleConfig.getClass());
        if (null != configChecker) {
            configChecker.check(databaseName, ruleConfig, resourceMetaData.getDataSourceMap(), rules);
        }
        return databaseRuleBuilder.build(ruleConfig, databaseName, protocolType, resourceMetaData, rules, computeNodeInstanceContext);
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, DatabaseRuleBuilder> getRuleBuilderMap(final DatabaseConfiguration databaseConfig) {
        Map<RuleConfiguration, DatabaseRuleBuilder> result = new LinkedHashMap<>();
        result.putAll(getDistributedRuleBuilderMap(databaseConfig.getRuleConfigurations()));
        result.putAll(getEnhancedRuleBuilderMap(databaseConfig.getRuleConfigurations()));
        result.putAll(getMissedDefaultRuleBuilderMap(result.values()));
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, DatabaseRuleBuilder> getDistributedRuleBuilderMap(final Collection<RuleConfiguration> ruleConfigs) {
        Collection<RuleConfiguration> distributedRuleConfigs = ruleConfigs.stream().filter(each -> isAssignableFrom(each, DistributedRuleConfiguration.class)).collect(Collectors.toList());
        return OrderedSPILoader.getServices(DatabaseRuleBuilder.class, distributedRuleConfigs, Comparator.reverseOrder());
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, DatabaseRuleBuilder> getEnhancedRuleBuilderMap(final Collection<RuleConfiguration> ruleConfigs) {
        Collection<RuleConfiguration> enhancedRuleConfigs = ruleConfigs.stream().filter(each -> isAssignableFrom(each, EnhancedRuleConfiguration.class)).collect(Collectors.toList());
        return OrderedSPILoader.getServices(DatabaseRuleBuilder.class, enhancedRuleConfigs);
    }
    
    private static boolean isAssignableFrom(final RuleConfiguration ruleConfig, final Class<? extends RuleConfiguration> ruleConfigClass) {
        return Arrays.stream(ruleConfig.getClass().getInterfaces()).anyMatch(ruleConfigClass::isAssignableFrom);
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, DatabaseRuleBuilder> getMissedDefaultRuleBuilderMap(final Collection<DatabaseRuleBuilder> configuredBuilders) {
        Map<DatabaseRuleBuilder, DefaultDatabaseRuleConfigurationBuilder> defaultBuilders =
                OrderedSPILoader.getServices(DefaultDatabaseRuleConfigurationBuilder.class, getMissedDefaultRuleBuilders(configuredBuilders));
        Map<RuleConfiguration, DatabaseRuleBuilder> result = new LinkedHashMap<>(defaultBuilders.size(), 1F);
        // TODO consider about order for new put items
        for (Entry<DatabaseRuleBuilder, DefaultDatabaseRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            result.put(entry.getValue().build(), entry.getKey());
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<DatabaseRuleBuilder> getMissedDefaultRuleBuilders(final Collection<DatabaseRuleBuilder> configuredBuilders) {
        Collection<Class<DatabaseRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<DatabaseRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return OrderedSPILoader.getServices(DatabaseRuleBuilder.class).stream().filter(each -> !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
}
