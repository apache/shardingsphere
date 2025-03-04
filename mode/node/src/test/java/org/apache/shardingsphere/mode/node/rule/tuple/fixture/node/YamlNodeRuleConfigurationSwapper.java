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

package org.apache.shardingsphere.mode.node.rule.tuple.fixture.node;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;

public final class YamlNodeRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlNodeRuleConfiguration, NodeRuleConfiguration> {
    
    @Override
    public YamlNodeRuleConfiguration swapToYamlConfiguration(final NodeRuleConfiguration data) {
        return new YamlNodeRuleConfiguration();
    }
    
    @Override
    public NodeRuleConfiguration swapToObject(final YamlNodeRuleConfiguration yamlConfig) {
        return new NodeRuleConfiguration();
    }
    
    @Override
    public String getRuleTagName() {
        return "NODE";
    }
    
    @Override
    public int getOrder() {
        return 12000;
    }
    
    @Override
    public Class<NodeRuleConfiguration> getTypeClass() {
        return NodeRuleConfiguration.class;
    }
}
