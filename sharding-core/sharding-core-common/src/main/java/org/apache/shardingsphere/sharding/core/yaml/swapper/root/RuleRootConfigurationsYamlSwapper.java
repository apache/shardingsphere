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

package org.apache.shardingsphere.sharding.core.yaml.swapper.root;

import org.apache.shardingsphere.sharding.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.config.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;

import java.util.Collection;

/**
 * Rule root configurations YAML swapper.
 */
public final class RuleRootConfigurationsYamlSwapper implements YamlSwapper<YamlRootRuleConfigurations, Collection<RuleConfiguration>> {
    
    private final YamlRuleConfigurationSwapperEngine swapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    @Override
    public YamlRootRuleConfigurations swap(final Collection<RuleConfiguration> data) {
        YamlRootRuleConfigurations result = new YamlRootRuleConfigurations();
        result.setRules(swapperEngine.swapToYAMLConfigurations(data));
        return result;
    }
    
    @Override
    public Collection<RuleConfiguration> swap(final YamlRootRuleConfigurations configurations) {
        return swapperEngine.swapToRuleConfigurations(configurations.getRules());
    }
}
