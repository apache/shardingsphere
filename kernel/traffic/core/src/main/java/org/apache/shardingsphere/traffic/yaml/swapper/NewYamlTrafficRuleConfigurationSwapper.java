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

package org.apache.shardingsphere.traffic.yaml.swapper;

import org.apache.shardingsphere.infra.config.nodepath.GlobalNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlGlobalRuleConfigurationSwapper;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.constant.TrafficOrder;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.yaml.config.YamlTrafficStrategyConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * TODO Rename YamlTrafficRuleConfigurationSwapper when metadata structure adjustment completed. #25485
 * New YAML traffic rule configuration swapper.
 */
public final class NewYamlTrafficRuleConfigurationSwapper implements NewYamlGlobalRuleConfigurationSwapper<TrafficRuleConfiguration> {
    
    private final YamlTrafficStrategyConfigurationSwapper strategySwapper = new YamlTrafficStrategyConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final TrafficRuleConfiguration data) {
        return Collections.singletonList(new YamlDataNode(getRuleTagName().toLowerCase(), YamlEngine.marshal(swapToYamlConfiguration(data))));
    }
    
    private YamlTrafficRuleConfiguration swapToYamlConfiguration(final TrafficRuleConfiguration data) {
        YamlTrafficRuleConfiguration result = new YamlTrafficRuleConfiguration();
        data.getTrafficStrategies().forEach(each -> result.getTrafficStrategies().put(each.getName(), strategySwapper.swapToYamlConfiguration(each)));
        setYamlAlgorithms(data, result);
        return result;
    }
    
    private void setYamlAlgorithms(final TrafficRuleConfiguration data, final YamlTrafficRuleConfiguration yamlConfig) {
        if (null != data.getTrafficAlgorithms()) {
            data.getTrafficAlgorithms().forEach((key, value) -> yamlConfig.getTrafficAlgorithms().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
        if (null != data.getLoadBalancers()) {
            data.getLoadBalancers().forEach((key, value) -> yamlConfig.getLoadBalancers().put(key, algorithmSwapper.swapToYamlConfiguration(value)));
        }
    }
    
    @Override
    public Optional<TrafficRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            Optional<String> version = GlobalNodePath.getVersion(getRuleTagName().toLowerCase(), each.getKey());
            if (!version.isPresent()) {
                continue;
            }
            return Optional.of(swapToObject(YamlEngine.unmarshal(each.getValue(), YamlTrafficRuleConfiguration.class)));
        }
        return Optional.empty();
    }
    
    private TrafficRuleConfiguration swapToObject(final YamlTrafficRuleConfiguration yamlConfig) {
        
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        for (Entry<String, YamlTrafficStrategyConfiguration> entry : yamlConfig.getTrafficStrategies().entrySet()) {
            YamlTrafficStrategyConfiguration strategyConfig = entry.getValue();
            strategyConfig.setName(entry.getKey());
            result.getTrafficStrategies().add(strategySwapper.swapToObject(strategyConfig));
        }
        setAlgorithms(yamlConfig, result);
        return result;
    }
    
    private void setAlgorithms(final YamlTrafficRuleConfiguration yamlConfig, final TrafficRuleConfiguration ruleConfig) {
        if (null != yamlConfig.getTrafficAlgorithms()) {
            yamlConfig.getTrafficAlgorithms().forEach((key, value) -> ruleConfig.getTrafficAlgorithms().put(key, algorithmSwapper.swapToObject(value)));
        }
        if (null != yamlConfig.getLoadBalancers()) {
            yamlConfig.getLoadBalancers().forEach((key, value) -> ruleConfig.getLoadBalancers().put(key, algorithmSwapper.swapToObject(value)));
        }
    }
    
    @Override
    public Class<TrafficRuleConfiguration> getTypeClass() {
        return TrafficRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "TRAFFIC";
    }
    
    @Override
    public int getOrder() {
        return TrafficOrder.ORDER;
    }
}
