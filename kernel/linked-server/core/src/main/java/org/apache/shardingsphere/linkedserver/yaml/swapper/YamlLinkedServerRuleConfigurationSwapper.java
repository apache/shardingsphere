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

package org.apache.shardingsphere.linkedserver.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.linkedserver.config.LinkedServerRuleConfiguration;
import org.apache.shardingsphere.linkedserver.config.rule.LinkedServerConfiguration;
import org.apache.shardingsphere.linkedserver.constant.LinkedServerOrder;
import org.apache.shardingsphere.linkedserver.yaml.config.YamlLinkedServerRuleConfiguration;
import org.apache.shardingsphere.linkedserver.yaml.config.rule.YamlLinkedServerConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * YAML linked server rule configuration swapper.
 */
public final class YamlLinkedServerRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlLinkedServerRuleConfiguration, LinkedServerRuleConfiguration> {
    
    @Override
    public YamlLinkedServerRuleConfiguration swapToYamlConfiguration(final LinkedServerRuleConfiguration data) {
        YamlLinkedServerRuleConfiguration result = new YamlLinkedServerRuleConfiguration();
        for (LinkedServerConfiguration each : data.getServers()) {
            result.getServers().put(each.getName(), swapToYamlServerConfiguration(each));
        }
        return result;
    }
    
    private YamlLinkedServerConfiguration swapToYamlServerConfiguration(final LinkedServerConfiguration config) {
        YamlLinkedServerConfiguration result = new YamlLinkedServerConfiguration();
        result.setDatabaseType(config.getDatabaseType());
        result.setTables(new LinkedHashMap<>(config.getTables()));
        return result;
    }
    
    @Override
    public LinkedServerRuleConfiguration swapToObject(final YamlLinkedServerRuleConfiguration yamlConfig) {
        return new LinkedServerRuleConfiguration(swapServers(yamlConfig));
    }
    
    private Collection<LinkedServerConfiguration> swapServers(final YamlLinkedServerRuleConfiguration yamlConfig) {
        Collection<LinkedServerConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlLinkedServerConfiguration> entry : yamlConfig.getServers().entrySet()) {
            result.add(new LinkedServerConfiguration(entry.getKey(), entry.getValue().getDatabaseType(), entry.getValue().getTables()));
        }
        return result;
    }
    
    @Override
    public Class<LinkedServerRuleConfiguration> getTypeClass() {
        return LinkedServerRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "LINKED_SERVER";
    }
    
    @Override
    public int getOrder() {
        return LinkedServerOrder.ORDER;
    }
}
