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

import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.replica.api.config.ReplicaActualTableRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaLogicTableRuleConfiguration;
import org.apache.shardingsphere.replica.yaml.config.YamlReplicaActualTableRuleConfiguration;
import org.apache.shardingsphere.replica.yaml.config.YamlReplicaLogicTableRuleConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Replica logic table rule configuration YAML swapper.
 */
public final class ReplicaLogicTableRuleConfigurationYamlSwapper implements YamlSwapper<YamlReplicaLogicTableRuleConfiguration, ReplicaLogicTableRuleConfiguration> {
    
    private final ReplicaActualTableRuleConfigurationYamlSwapper actualTableRuleConfigurationYamlSwapper = new ReplicaActualTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlReplicaLogicTableRuleConfiguration swapToYamlConfiguration(final ReplicaLogicTableRuleConfiguration data) {
        Collection<YamlReplicaActualTableRuleConfiguration> replicaGroups = data.getReplicaGroups().stream()
                .map(actualTableRuleConfigurationYamlSwapper::swapToYamlConfiguration)
                .collect(Collectors.toList());
        YamlReplicaLogicTableRuleConfiguration result = new YamlReplicaLogicTableRuleConfiguration();
        result.setLogicTable(data.getLogicTable());
        result.setReplicaGroups(replicaGroups);
        return result;
    }
    
    @Override
    public ReplicaLogicTableRuleConfiguration swapToObject(final YamlReplicaLogicTableRuleConfiguration yamlConfiguration) {
        Collection<ReplicaActualTableRuleConfiguration> replicaGroups = yamlConfiguration.getReplicaGroups().stream()
                .map(actualTableRuleConfigurationYamlSwapper::swapToObject)
                .collect(Collectors.toList());
        return new ReplicaLogicTableRuleConfiguration(yamlConfiguration.getLogicTable(), replicaGroups);
    }
}
