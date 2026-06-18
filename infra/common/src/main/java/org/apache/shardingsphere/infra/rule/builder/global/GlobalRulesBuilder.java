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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.Collections;
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
    
    /**
     * Build rules.
     *
     * @param globalRuleConfigs global rule configurations
     * @param databases databases
     * @param props props
     * @return built rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildRules(final Collection<RuleConfiguration> globalRuleConfigs,
                                                            final Collection<ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, GlobalRuleBuilder> entry : getRuleBuilderMap(globalRuleConfigs).entrySet()) {
            result.add(entry.getValue().build(entry.getKey(), databases, props));
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, GlobalRuleBuilder> getRuleBuilderMap(final Collection<RuleConfiguration> globalRuleConfigs) {
        Map<RuleConfiguration, GlobalRuleBuilder> result = new LinkedHashMap<>();
        result.putAll(OrderedSPILoader.getServices(GlobalRuleBuilder.class, globalRuleConfigs));
        result.putAll(getMissedDefaultRuleBuilderMap(result));
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, GlobalRuleBuilder> getMissedDefaultRuleBuilderMap(final Map<RuleConfiguration, GlobalRuleBuilder> builders) {
        Map<GlobalRuleBuilder, DefaultGlobalRuleConfigurationBuilder> defaultBuilders = OrderedSPILoader.getServices(
                DefaultGlobalRuleConfigurationBuilder.class, getMissedDefaultRuleBuilders(builders.values()));
        return defaultBuilders.entrySet().stream().collect(
                Collectors.toMap(entry -> entry.getValue().build(), Entry::getKey, (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<GlobalRuleBuilder> getMissedDefaultRuleBuilders(final Collection<GlobalRuleBuilder> configuredBuilders) {
        Collection<Class<GlobalRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<GlobalRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return OrderedSPILoader.getServices(GlobalRuleBuilder.class).stream().filter(each -> !configuredBuilderClasses.contains(each.getClass())).collect(Collectors.toList());
    }
    
    /**
     * Build single rule.
     *
     * @param globalRuleConfig global rule configuration
     * @param databases databases
     * @param props props
     * @return built rule
     */
    @SuppressWarnings("unchecked")
    public static Collection<ShardingSphereRule> buildSingleRules(final RuleConfiguration globalRuleConfig, final Collection<ShardingSphereDatabase> databases, final ConfigurationProperties props) {
        return OrderedSPILoader.getServices(GlobalRuleBuilder.class, Collections.singleton(globalRuleConfig)).entrySet()
                .stream().map(each -> each.getValue().build(each.getKey(), databases, props)).collect(Collectors.toList());
    }
}
