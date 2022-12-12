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

package org.apache.shardingsphere.singletable.yaml.config.swapper;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.constant.SingleTableOrder;
import org.apache.shardingsphere.singletable.yaml.config.pojo.YamlSingleTableRuleConfiguration;

/**
 * YAML single table rule configuration swapper.
 */
public final class YamlSingleTableRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlSingleTableRuleConfiguration, SingleTableRuleConfiguration> {
    
    @Override
    public YamlSingleTableRuleConfiguration swapToYamlConfiguration(final SingleTableRuleConfiguration data) {
        YamlSingleTableRuleConfiguration result = new YamlSingleTableRuleConfiguration();
        data.getDefaultDataSource().ifPresent(result::setDefaultDataSource);
        return result;
    }
    
    @Override
    public SingleTableRuleConfiguration swapToObject(final YamlSingleTableRuleConfiguration yamlConfig) {
        SingleTableRuleConfiguration result = new SingleTableRuleConfiguration();
        result.setDefaultDataSource(yamlConfig.getDefaultDataSource());
        return result;
    }
    
    @Override
    public Class<SingleTableRuleConfiguration> getTypeClass() {
        return SingleTableRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SINGLE";
    }
    
    @Override
    public int getOrder() {
        return SingleTableOrder.ORDER;
    }
}
