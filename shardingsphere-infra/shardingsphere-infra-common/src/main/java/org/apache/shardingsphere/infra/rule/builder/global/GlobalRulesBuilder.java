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

package org.apache.shardingsphere.infra.rule.builder.global;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.DefaultSchemaRuleConfigurationBuilder;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Global rules builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalRulesBuilder {
    
    static {
        ShardingSphereServiceLoader.register(GlobalRuleBuilder.class);
        ShardingSphereServiceLoader.register(DefaultGlobalRuleConfigurationBuilder.class);
    }
    
    /**
     * Build rules.
     *
     * @param globalRuleConfigs rule configurations
     * @param mataDataMap mata data map
     * @return built rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildRules(final Collection<RuleConfiguration> globalRuleConfigs, final Map<String, ShardingSphereMetaData> mataDataMap) {
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
        Map<GlobalRuleBuilder, DefaultSchemaRuleConfigurationBuilder> defaultBuilders =
                OrderedSPIRegistry.getRegisteredServices(DefaultSchemaRuleConfigurationBuilder.class, getMissedKernelGlobalRuleBuilders(builders.values()));
        // TODO consider about order for new put items
        for (Entry<GlobalRuleBuilder, DefaultSchemaRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            builders.put(entry.getValue().build(), entry.getKey());
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<GlobalRuleBuilder> getMissedKernelGlobalRuleBuilders(final Collection<GlobalRuleBuilder> configuredBuilders) {
        Collection<Class<GlobalRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<GlobalRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return OrderedSPIRegistry.getRegisteredServices(GlobalRuleBuilder.class).stream().filter(each -> !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
}
