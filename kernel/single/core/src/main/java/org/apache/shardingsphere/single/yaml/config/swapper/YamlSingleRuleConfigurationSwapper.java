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

package org.apache.shardingsphere.single.yaml.config.swapper;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;

/**
 * YAML single rule configuration swapper.
 */
public final class YamlSingleRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlSingleRuleConfiguration, SingleRuleConfiguration> {
    
    @Override
    public YamlSingleRuleConfiguration swapToYamlConfiguration(final SingleRuleConfiguration data) {
        YamlSingleRuleConfiguration result = new YamlSingleRuleConfiguration();
        result.getTables().addAll(data.getTables());
        data.getDefaultDataSource().ifPresent(result::setDefaultDataSource);
        return result;
    }
    
    @Override
    public SingleRuleConfiguration swapToObject(final YamlSingleRuleConfiguration yamlConfig) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        if (null != yamlConfig.getTables()) {
            result.getTables().addAll(yamlConfig.getTables());
        }
        result.setDefaultDataSource(yamlConfig.getDefaultDataSource());
        return result;
    }
    
    @Override
    public Class<SingleRuleConfiguration> getTypeClass() {
        return SingleRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SINGLE";
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
}
