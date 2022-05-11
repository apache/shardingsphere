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

package org.apache.shardingsphere.infra.rule.builder.schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.function.DistributedRuleConfiguration;
import org.apache.shardingsphere.infra.config.function.EnhancedRuleConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Schema rules builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaRulesBuilder {
    
    /**
     * Build rules.
     *
     * @param databaseName database name
     * @param databaseConfig schema configuration
     * @param props configuration properties
     * @return built rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildRules(final String databaseName, final DatabaseConfiguration databaseConfig, final ConfigurationProperties props) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, SchemaRuleBuilder> entry : getRuleBuilderMap(databaseConfig).entrySet()) {
            result.add(entry.getValue().build(entry.getKey(), databaseName, databaseConfig.getDataSources(), result, props));
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getRuleBuilderMap(final DatabaseConfiguration databaseConfig) {
        Map<RuleConfiguration, SchemaRuleBuilder> result = new LinkedHashMap<>();
        result.putAll(getDistributedRuleBuilderMap(databaseConfig.getRuleConfigurations()));
        result.putAll(getEnhancedRuleBuilderMap(databaseConfig.getRuleConfigurations()));
        result.putAll(getMissedDefaultRuleBuilderMap(result.values()));
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getDistributedRuleBuilderMap(final Collection<RuleConfiguration> ruleConfigs) {
        Collection<RuleConfiguration> distributedRuleConfigs = ruleConfigs.stream().filter(each -> isAssignableFrom(each, DistributedRuleConfiguration.class)).collect(Collectors.toList());
        return SchemaRuleBuilderFactory.getInstanceMap(distributedRuleConfigs, Comparator.reverseOrder());
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getEnhancedRuleBuilderMap(final Collection<RuleConfiguration> ruleConfigs) {
        Collection<RuleConfiguration> enhancedRuleConfigs = ruleConfigs.stream().filter(each -> isAssignableFrom(each, EnhancedRuleConfiguration.class)).collect(Collectors.toList());
        return SchemaRuleBuilderFactory.getInstanceMap(enhancedRuleConfigs);
    }
    
    private static boolean isAssignableFrom(final RuleConfiguration ruleConfig, final Class<? extends RuleConfiguration> ruleConfigClass) {
        return Arrays.stream(ruleConfig.getClass().getInterfaces()).anyMatch(ruleConfigClass::isAssignableFrom);
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getMissedDefaultRuleBuilderMap(final Collection<SchemaRuleBuilder> configuredBuilders) {
        Map<RuleConfiguration, SchemaRuleBuilder> result = new LinkedHashMap<>();
        Map<SchemaRuleBuilder, DefaultSchemaRuleConfigurationBuilder> defaultBuilders = DefaultSchemaRuleConfigurationBuilderFactory.getInstances(getMissedDefaultRuleBuilders(configuredBuilders));
        // TODO consider about order for new put items
        for (Entry<SchemaRuleBuilder, DefaultSchemaRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            result.put(entry.getValue().build(), entry.getKey());
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<SchemaRuleBuilder> getMissedDefaultRuleBuilders(final Collection<SchemaRuleBuilder> configuredBuilders) {
        Collection<Class<SchemaRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<SchemaRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return SchemaRuleBuilderFactory.getInstances().stream().filter(each -> !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
}
