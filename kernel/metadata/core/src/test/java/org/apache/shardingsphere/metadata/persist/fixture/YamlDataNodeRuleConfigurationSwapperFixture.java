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

package org.apache.shardingsphere.metadata.persist.fixture;

import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlDataNodeRuleConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class YamlDataNodeRuleConfigurationSwapperFixture implements YamlDataNodeRuleConfigurationSwapper<YamlDataNodeRuleConfigurationFixture> {
    
    @Override
    public String getRuleTagName() {
        return "SINGLE";
    }
    
    @Override
    public int getOrder() {
        return 30;
    }
    
    @Override
    public Class<YamlDataNodeRuleConfigurationFixture> getTypeClass() {
        return YamlDataNodeRuleConfigurationFixture.class;
    }
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final YamlDataNodeRuleConfigurationFixture data) {
        return Collections.singletonList(new YamlDataNode(data.getKey(), YamlEngine.marshal(swapToYamlConfiguration(data))));
    }
    
    private YamlDataNodeRuleConfigurationFixture swapToYamlConfiguration(final YamlDataNodeRuleConfigurationFixture data) {
        return data;
    }
    
    @Override
    public Optional<YamlDataNodeRuleConfigurationFixture> swapToObject(final Collection<YamlDataNode> dataNodes) {
        if(null == dataNodes || dataNodes.isEmpty()){
            return Optional.empty();
        }
        YamlDataNode dataNode = dataNodes.iterator().next();
        YamlDataNodeRuleConfigurationFixture configurationFixture = new YamlDataNodeRuleConfigurationFixture();
        configurationFixture.setKey(dataNode.getKey());
        configurationFixture.setValue(dataNode.getValue());
        return Optional.of(configurationFixture);
    }
}
