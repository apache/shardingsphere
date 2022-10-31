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

package org.apache.shardingsphere.infra.yaml.config.swapper.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPIRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * YAML rule configuration swapper factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlRuleConfigurationSwapperFactory {
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    /**
     * Get instance map of YAML rule configuration swapper.
     * 
     * @param ruleConfigs rule configurations
     * @return instance map of rule configuration and YAML rule configuration swapper
     */
    @SuppressWarnings("rawtypes")
    public static Map<RuleConfiguration, YamlRuleConfigurationSwapper> getInstanceMapByRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs) {
        return OrderedSPIRegistry.getRegisteredServices(YamlRuleConfigurationSwapper.class, ruleConfigs);
    }
    
    /**
     * Get instance map of YAML rule configuration swapper.
     *
     * @param ruleConfigTypes rule configurations types
     * @return got instance map
     */
    @SuppressWarnings("rawtypes")
    public static Map<Class<?>, YamlRuleConfigurationSwapper> getInstanceMapByRuleConfigurationClasses(final Collection<Class<?>> ruleConfigTypes) {
        return OrderedSPIRegistry.getRegisteredServicesByClass(YamlRuleConfigurationSwapper.class, ruleConfigTypes);
    }
    
    /**
     * Get all instances of all YAML rule configuration swappers.
     *
     * @return got instances
     */
    @SuppressWarnings("rawtypes")
    public static Collection<YamlRuleConfigurationSwapper> getAllInstances() {
        return ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class);
    }
}
