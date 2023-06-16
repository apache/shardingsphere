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
import org.apache.shardingsphere.broadcast.metadata.converter.BroadcastNodeConverter;
import org.apache.shardingsphere.broadcast.yaml.config.YamlBroadcastRuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;

/**
 * TODO Rename to YamlBroadcastRuleConfigurationSwapper when metadata structure adjustment completed.
 * New YAML broadcast rule configuration swapper.
 */
public final class NewYamlBroadcastRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<BroadcastRuleConfiguration> {
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final BroadcastRuleConfiguration data) {
        if (data.getTables().isEmpty()) {
            return Collections.emptyList();
        }
        YamlBroadcastRuleConfiguration yamlBroadcastRuleConfiguration = new YamlBroadcastRuleConfiguration();
        yamlBroadcastRuleConfiguration.getTables().addAll(data.getTables());
        return Collections.singleton(new YamlDataNode(BroadcastNodeConverter.getTablesPath(), YamlEngine.marshal(yamlBroadcastRuleConfiguration)));
    }
    
    @Override
    public BroadcastRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        BroadcastRuleConfiguration result = new BroadcastRuleConfiguration();
        if (!dataNodes.isEmpty()) {
            YamlBroadcastRuleConfiguration yamlBroadcastRuleConfiguration = YamlEngine.unmarshal(dataNodes.iterator().next().getValue(), YamlBroadcastRuleConfiguration.class);
            result.setTables(yamlBroadcastRuleConfiguration.getTables());
        }
        return result;
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
