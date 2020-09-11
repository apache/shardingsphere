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

package org.apache.shardingsphere.replica.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replica.api.config.ReplicaLogicTableRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;
import org.apache.shardingsphere.replica.constant.ReplicaOrder;
import org.apache.shardingsphere.replica.yaml.config.YamlReplicaLogicTableRuleConfiguration;
import org.apache.shardingsphere.replica.yaml.config.YamlReplicaRuleConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Replica rule configuration YAML swapper.
 */
public final class ReplicaRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlReplicaRuleConfiguration, ReplicaRuleConfiguration> {
    
    private final ReplicaLogicTableRuleConfigurationYamlSwapper logicTableRuleConfigurationYamlSwapper = new ReplicaLogicTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlReplicaRuleConfiguration swapToYamlConfiguration(final ReplicaRuleConfiguration data) {
        Collection<YamlReplicaLogicTableRuleConfiguration> yamlTables = data.getTables().stream()
                .map(logicTableRuleConfigurationYamlSwapper::swapToYamlConfiguration)
                .collect(Collectors.toList());
        YamlReplicaRuleConfiguration result = new YamlReplicaRuleConfiguration();
        result.setTables(yamlTables);
        return result;
    }
    
    @Override
    public ReplicaRuleConfiguration swapToObject(final YamlReplicaRuleConfiguration yamlConfiguration) {
        Collection<ReplicaLogicTableRuleConfiguration> tables = yamlConfiguration.getTables().stream()
                .map(logicTableRuleConfigurationYamlSwapper::swapToObject)
                .collect(Collectors.toList());
        return new ReplicaRuleConfiguration(tables);
    }
    
    @Override
    public Class<ReplicaRuleConfiguration> getTypeClass() {
        return ReplicaRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "REPLICA";
    }
    
    @Override
    public int getOrder() {
        return ReplicaOrder.ORDER;
    }
}
