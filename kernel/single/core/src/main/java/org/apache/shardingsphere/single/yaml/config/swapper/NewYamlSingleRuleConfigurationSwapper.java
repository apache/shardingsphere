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

import org.apache.shardingsphere.infra.converter.GlobalRuleNodeConverter;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * TODO Rename YamlSingleRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * YAML single rule configuration swapper.
 */
public final class NewYamlSingleRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<SingleRuleConfiguration> {
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final SingleRuleConfiguration data) {
        return Collections.singletonList(new YamlDataNode(GlobalRuleNodeConverter.getRootNode(getRuleTagName().toLowerCase()), YamlEngine.marshal(swapToYamlConfiguration(data))));
    }
    
    private YamlSingleRuleConfiguration swapToYamlConfiguration(final SingleRuleConfiguration data) {
        YamlSingleRuleConfiguration result = new YamlSingleRuleConfiguration();
        result.getTables().addAll(data.getTables());
        data.getDefaultDataSource().ifPresent(result::setDefaultDataSource);
        return result;
    }
    
    @Override
    public SingleRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            Optional<String> version = GlobalRuleNodeConverter.getVersion(getRuleTagName().toLowerCase(), each.getKey());
            if (!version.isPresent()) {
                continue;
            }
            return swapToObject(YamlEngine.unmarshal(each.getValue(), YamlSingleRuleConfiguration.class));
        }
        return new SingleRuleConfiguration();
    }
    
    private SingleRuleConfiguration swapToObject(final YamlSingleRuleConfiguration yamlConfig) {
        SingleRuleConfiguration result = new SingleRuleConfiguration();
        result.getTables().addAll(yamlConfig.getTables());
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
