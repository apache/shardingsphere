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

package org.apache.shardingsphere.infra.yaml.config.swapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.ordered.OrderedSPIRegistry;

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
     * Create new instance of YAML rule configuration swapper.
     * 
     * @param ruleConfigs rule configurations
     * @return new instance map of rule configuration and YAML rule configuration swapper
     */
    @SuppressWarnings("rawtypes")
    public static Map<RuleConfiguration, YamlRuleConfigurationSwapper> newInstanceMapByRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs) {
        return OrderedSPIRegistry.getRegisteredServices(YamlRuleConfigurationSwapper.class, ruleConfigs);
    }
    
    /**
     * Create new instance of YAML rule configuration swapper.
     *
     * @param ruleConfigTypes rule configurations types
     * @return new instance of rule configurations type and YAML rule configuration swapper
     */
    @SuppressWarnings("rawtypes")
    public static Map<Class<?>, YamlRuleConfigurationSwapper> newInstanceMapByRuleConfigurationClasses(final Collection<Class<?>> ruleConfigTypes) {
        return OrderedSPIRegistry.getRegisteredServicesByClass(YamlRuleConfigurationSwapper.class, ruleConfigTypes);
    }
    
    /**
     * Create new instances of all YAML rule configuration swappers.
     *
     * @return new instances of all YAML rule configuration swappers
     */
    @SuppressWarnings("rawtypes")
    public static Collection<YamlRuleConfigurationSwapper> newInstances() {
        return ShardingSphereServiceLoader.getServiceInstances(YamlRuleConfigurationSwapper.class);
    }
}
