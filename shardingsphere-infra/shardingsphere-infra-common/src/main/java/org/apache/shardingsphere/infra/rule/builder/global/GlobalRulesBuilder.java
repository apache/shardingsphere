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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

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
    
    /**
     * Build rules.
     *
     * @param globalRuleConfigs global rule configurations
     * @param databases databases
     * @param instanceContext instance context
     * @return built rules
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Collection<ShardingSphereRule> buildRules(final Collection<RuleConfiguration> globalRuleConfigs,
                                                            final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (Entry<RuleConfiguration, GlobalRuleBuilder> entry : getRuleBuilderMap(globalRuleConfigs).entrySet()) {
            result.add(entry.getValue().build(entry.getKey(), databases, instanceContext));
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, GlobalRuleBuilder> getRuleBuilderMap(final Collection<RuleConfiguration> globalRuleConfigs) {
        Map<RuleConfiguration, GlobalRuleBuilder> result = new LinkedHashMap<>();
        result.putAll(GlobalRuleBuilderFactory.getInstanceMap(globalRuleConfigs));
        result.putAll(getMissedDefaultRuleBuilderMap(result));
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    private static Map<RuleConfiguration, GlobalRuleBuilder> getMissedDefaultRuleBuilderMap(final Map<RuleConfiguration, GlobalRuleBuilder> builders) {
        Map<RuleConfiguration, GlobalRuleBuilder> result = new LinkedHashMap<>();
        Map<GlobalRuleBuilder, DefaultGlobalRuleConfigurationBuilder> defaultBuilders = DefaultGlobalRuleConfigurationBuilderFactory.getInstance(getMissedDefaultRuleBuilders(builders.values()));
        for (Entry<GlobalRuleBuilder, DefaultGlobalRuleConfigurationBuilder> entry : defaultBuilders.entrySet()) {
            result.put(entry.getValue().build(), entry.getKey());
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Collection<GlobalRuleBuilder> getMissedDefaultRuleBuilders(final Collection<GlobalRuleBuilder> configuredBuilders) {
        Collection<Class<GlobalRuleBuilder>> configuredBuilderClasses = configuredBuilders.stream().map(each -> (Class<GlobalRuleBuilder>) each.getClass()).collect(Collectors.toSet());
        return GlobalRuleBuilderFactory.getInstances(configuredBuilderClasses);
    }
}
