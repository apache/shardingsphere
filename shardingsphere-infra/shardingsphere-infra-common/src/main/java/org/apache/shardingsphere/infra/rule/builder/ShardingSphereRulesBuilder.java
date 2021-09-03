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

package org.apache.shardingsphere.infra.rule.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.function.DistributedRuleConfiguration;
import org.apache.shardingsphere.infra.config.function.EnhancedRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.level.DefaultRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.GlobalRuleBuilder;
import org.apache.shardingsphere.infra.rule.builder.scope.SchemaRuleBuilder;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * ShardingSphere rule builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereRulesBuilder {
    
    static {
        ShardingSphereServiceLoader.register(SchemaRuleBuilder.class);
        ShardingSphereServiceLoader.register(GlobalRuleBuilder.class);
        ShardingSphereServiceLoader.register(DefaultRuleConfigurationBuilder.class);
    }
    
    /**
     * Build schema rules.
     *
     * @param materials rules builder materials
     * @return built schema rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildSchemaRules(final ShardingSphereRulesBuilderMaterials materials) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, SchemaRuleBuilder> entry : getSchemaRuleBuilderMap(materials).entrySet()) {
            result.add(entry.getValue().build(materials, entry.getKey(), result));
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getSchemaRuleBuilderMap(final ShardingSphereRulesBuilderMaterials materials) {
        Map<RuleConfiguration, SchemaRuleBuilder> result = new LinkedHashMap<>();
        result.putAll(getDistributedSchemaRuleBuilderMap(materials.getSchemaRuleConfigs()));
        result.putAll(getEnhancedSchemaRuleBuilderMap(materials.getSchemaRuleConfigs()));
        result.putAll(getMissedDefaultSchemaRuleBuilderMap(result.values()));
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getDistributedSchemaRuleBuilderMap(final Collection<RuleConfiguration> schemaRuleConfigs) {
        Collection<RuleConfiguration> distributedRuleConfigs = schemaRuleConfigs.stream().filter(each -> isAssignableFrom(each, DistributedRuleConfiguration.class)).collect(Collectors.toList());
        return OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class, distributedRuleConfigs, Comparator.reverseOrder());
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getEnhancedSchemaRuleBuilderMap(final Collection<RuleConfiguration> schemaRuleConfigs) {
        Collection<RuleConfiguration> enhancedRuleConfigs = schemaRuleConfigs.stream().filter(each -> isAssignableFrom(each, EnhancedRuleConfiguration.class)).collect(Collectors.toList());
        return OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class, enhancedRuleConfigs);
    }
    
    private static boolean isAssignableFrom(final RuleConfiguration ruleConfig, final Class<? extends RuleConfiguration> ruleConfigClass) {
        return Arrays.stream(ruleConfig.getClass().getInterfaces()).anyMatch(ruleConfigClass::isAssignableFrom);
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, SchemaRuleBuilder> getMissedDefaultSchemaRuleBuilderMap(final Collection<SchemaRuleBuilder> configuredBuilders) {
        Map<RuleConfiguration, SchemaRuleBuilder> result = new LinkedHashMap<>();
        Map<SchemaRuleBuilder, DefaultRuleConfigurationBuilder> defaultBuilders =
                OrderedSPIRegistry.getRegisteredServices(DefaultRuleConfigurationBuilder.class, getMissedDefaultSchemaRuleBuilders(configuredBuilders));
        // TODO consider about order for new put items
        for (Entry<SchemaRuleBuilder, DefaultRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            result.put(entry.getValue().build(), entry.getKey());
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<SchemaRuleBuilder> getMissedDefaultSchemaRuleBuilders(final Collection<SchemaRuleBuilder> configuredBuilders) {
        Collection<Class<SchemaRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<SchemaRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return OrderedSPIRegistry.getRegisteredServices(SchemaRuleBuilder.class).stream().filter(each -> !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
    
    /**
     * Build global rules.
     *
     * @param globalRuleConfigs global rule configurations
     * @param mataDataMap mata data map
     * @return built global rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildGlobalRules(final Collection<RuleConfiguration> globalRuleConfigs, final Map<String, ShardingSphereMetaData> mataDataMap) {
        Map<RuleConfiguration, GlobalRuleBuilder> builders = new LinkedHashMap<>(OrderedSPIRegistry.getRegisteredServices(GlobalRuleBuilder.class, globalRuleConfigs));
        appendDefaultGlobalRuleConfigurationBuilder(builders);
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, GlobalRuleBuilder> entry : builders.entrySet()) {
            result.add(entry.getValue().build(entry.getKey(), mataDataMap));
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static void appendDefaultGlobalRuleConfigurationBuilder(final Map<RuleConfiguration, GlobalRuleBuilder> builders) {
        Map<GlobalRuleBuilder, DefaultRuleConfigurationBuilder> defaultBuilders =
                OrderedSPIRegistry.getRegisteredServices(DefaultRuleConfigurationBuilder.class, getMissedKernelGlobalRuleBuilders(builders.values()));
        // TODO consider about order for new put items
        for (Entry<GlobalRuleBuilder, DefaultRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            builders.put(entry.getValue().build(), entry.getKey());
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<GlobalRuleBuilder> getMissedKernelGlobalRuleBuilders(final Collection<GlobalRuleBuilder> configuredBuilders) {
        Collection<Class<GlobalRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<GlobalRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return OrderedSPIRegistry.getRegisteredServices(GlobalRuleBuilder.class).stream().filter(each -> !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
}
