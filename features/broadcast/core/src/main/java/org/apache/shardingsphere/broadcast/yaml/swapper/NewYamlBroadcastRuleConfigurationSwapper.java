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

package org.apache.shardingsphere.broadcast.yaml.swapper;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.constant.BroadcastOrder;
import org.apache.shardingsphere.broadcast.metadata.nodepath.BroadcastRuleNodePathProvider;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO Rename to YamlBroadcastRuleConfigurationSwapper when metadata structure adjustment completed.
 * New YAML broadcast rule configuration swapper.
 */
public final class NewYamlBroadcastRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<BroadcastRuleConfiguration> {
    
    private final RuleNodePath broadcastRuleNodePath = new BroadcastRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final BroadcastRuleConfiguration data) {
        if (data.getTables().isEmpty()) {
            return Collections.emptyList();
        }
        YamlBroadcastRuleConfiguration yamlBroadcastRuleConfiguration = new YamlBroadcastRuleConfiguration();
        yamlBroadcastRuleConfiguration.getTables().addAll(data.getTables());
        return Collections.singleton(new YamlDataNode(BroadcastRuleNodePathProvider.TABLES, YamlEngine.marshal(yamlBroadcastRuleConfiguration)));
    }
    
    @Override
    public Optional<BroadcastRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        List<YamlDataNode> validDataNodes = dataNodes.stream().filter(each -> broadcastRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        for (YamlDataNode each : validDataNodes) {
            if (broadcastRuleNodePath.getRoot().isValidatedPath(each.getKey())) {
                YamlBroadcastRuleConfiguration yamlBroadcastRuleConfiguration = YamlEngine.unmarshal(each.getValue(), YamlBroadcastRuleConfiguration.class);
                return Optional.of(new BroadcastRuleConfiguration(yamlBroadcastRuleConfiguration.getTables()));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Class<BroadcastRuleConfiguration> getTypeClass() {
        return BroadcastRuleConfiguration.class;
    }
    
    @Override
    public int getOrder() {
        return BroadcastOrder.ORDER;
    }
    
    @Override
    public String getRuleTagName() {
        return "BROADCAST";
    }
}
