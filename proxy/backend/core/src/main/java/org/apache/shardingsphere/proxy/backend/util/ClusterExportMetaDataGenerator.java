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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedSnapshotInfo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Cluster export metadata generator.
 */
@RequiredArgsConstructor
public final class ClusterExportMetaDataGenerator {
    
    private final ContextManager contextManager;
    
    /**
     * Generate JSON format.
     *
     * @return export data
     */
    public String generateJsonFormat() {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        ExportedMetaData exportedMetaData = new ExportedMetaData();
        exportedMetaData.setDatabases(generatorDatabasesExportData());
        exportedMetaData.setProps(generatePropsData(metaData.getProps().getProps()));
        exportedMetaData.setRules(generateRulesData(metaData.getGlobalRuleMetaData().getConfigurations()));
        ExportedClusterInfo exportedClusterInfo = new ExportedClusterInfo();
        exportedClusterInfo.setMetaData(exportedMetaData);
        generateSnapshotInfo(metaData, exportedClusterInfo);
        return JsonUtils.toJsonString(exportedClusterInfo);
    }
    
    private Map<String, String> generatorDatabasesExportData() {
        Map<String, String> result = new LinkedHashMap<>(contextManager.getMetaDataContexts().getMetaData().getAllDatabases().size(), 1F);
        for (ShardingSphereDatabase each : contextManager.getMetaDataContexts().getMetaData().getAllDatabases()) {
            if (each.getResourceMetaData().getAllInstanceDataSourceNames().isEmpty()) {
                continue;
            }
            result.put(each.getName(), new DatabaseExportMetaDataGenerator(each).generateYAMLFormat());
        }
        return result;
    }
    
    private String generatePropsData(final Properties props) {
        return props.isEmpty() ? "" : YamlEngine.marshal(Collections.singletonMap("props", props));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private String generateRulesData(final Collection<RuleConfiguration> rules) {
        if (rules.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("rules:").append(System.lineSeparator());
        for (Entry<RuleConfiguration, YamlRuleConfigurationSwapper> entry : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, rules).entrySet()) {
            result.append(YamlEngine.marshal(Collections.singletonList(entry.getValue().swapToYamlConfiguration(entry.getKey()))));
        }
        return result.toString();
    }
    
    private void generateSnapshotInfo(final ShardingSphereMetaData metaData, final ExportedClusterInfo exportedClusterInfo) {
        GlobalClockRule globalClockRule = metaData.getGlobalRuleMetaData().getSingleRule(GlobalClockRule.class);
        if (globalClockRule.getConfiguration().isEnabled()) {
            ExportedSnapshotInfo snapshotInfo = new ExportedSnapshotInfo();
            snapshotInfo.setCsn(String.valueOf(globalClockRule.getGlobalClockProvider().map(GlobalClockProvider::getCurrentTimestamp).orElse(0L)));
            snapshotInfo.setCreateTime(LocalDateTime.now());
            exportedClusterInfo.setSnapshotInfo(snapshotInfo);
        }
    }
}
