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

package org.apache.shardingsphere.mask.yaml.swapper.rule;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * YAML mask table rule configuration swapper.
 */
public final class YamlMaskTableRuleConfigurationSwapper implements YamlConfigurationSwapper<YamlMaskTableRuleConfiguration, MaskTableRuleConfiguration> {
    
    private final YamlMaskColumnRuleConfigurationSwapper columnSwapper = new YamlMaskColumnRuleConfigurationSwapper();
    
    @Override
    public YamlMaskTableRuleConfiguration swapToYamlConfiguration(final MaskTableRuleConfiguration data) {
        YamlMaskTableRuleConfiguration result = new YamlMaskTableRuleConfiguration();
        for (MaskColumnRuleConfiguration each : data.getColumns()) {
            result.getColumns().put(each.getLogicColumn(), columnSwapper.swapToYamlConfiguration(each));
        }
        result.setName(data.getName());
        return result;
    }
    
    @Override
    public MaskTableRuleConfiguration swapToObject(final YamlMaskTableRuleConfiguration yamlConfig) {
        Collection<MaskColumnRuleConfiguration> columns = new LinkedList<>();
        for (Entry<String, YamlMaskColumnRuleConfiguration> entry : yamlConfig.getColumns().entrySet()) {
            YamlMaskColumnRuleConfiguration yamlMaskColumnRuleConfig = entry.getValue();
            yamlMaskColumnRuleConfig.setLogicColumn(entry.getKey());
            columns.add(columnSwapper.swapToObject(yamlMaskColumnRuleConfig));
        }
        return new MaskTableRuleConfiguration(yamlConfig.getName(), columns);
    }
}
