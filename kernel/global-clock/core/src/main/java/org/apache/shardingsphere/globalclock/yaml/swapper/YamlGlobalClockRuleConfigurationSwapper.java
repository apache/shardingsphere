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

package org.apache.shardingsphere.globalclock.yaml.swapper;

import org.apache.shardingsphere.globalclock.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.rule.constant.GlobalClockOrder;
import org.apache.shardingsphere.globalclock.yaml.config.YamlGlobalClockRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;

/**
 * YAML global clock rule configuration swapper.
 */
public final class YamlGlobalClockRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlGlobalClockRuleConfiguration, GlobalClockRuleConfiguration> {
    
    @Override
    public YamlGlobalClockRuleConfiguration swapToYamlConfiguration(final GlobalClockRuleConfiguration data) {
        YamlGlobalClockRuleConfiguration result = new YamlGlobalClockRuleConfiguration();
        result.setType(data.getType());
        result.setProvider(data.getProvider());
        result.setEnabled(data.isEnabled());
        result.setProps(data.getProps());
        return result;
    }
    
    @Override
    public GlobalClockRuleConfiguration swapToObject(final YamlGlobalClockRuleConfiguration yamlConfig) {
        return new GlobalClockRuleConfiguration(yamlConfig.getType(), yamlConfig.getProvider(), yamlConfig.isEnabled(), yamlConfig.getProps());
    }
    
    @Override
    public String getRuleTagName() {
        return "GLOBAL_CLOCK";
    }
    
    @Override
    public int getOrder() {
        return GlobalClockOrder.ORDER;
    }
    
    @Override
    public Class<GlobalClockRuleConfiguration> getTypeClass() {
        return GlobalClockRuleConfiguration.class;
    }
}
