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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.commons.codec.binary.Base64;
import org.apache.shardingsphere.distsql.handler.ral.query.MetaDataRequiredQueryableRALExecutor;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportMetaDataStatement;
import org.apache.shardingsphere.globalclock.core.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.core.rule.GlobalClockRule;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedSnapshotInfo;
import org.apache.shardingsphere.proxy.backend.util.ExportUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Export metadata executor.
 */
public final class ExportMetaDataExecutor implements MetaDataRequiredQueryableRALExecutor<ExportMetaDataStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("id", "create_time", "cluster_info");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ExportMetaDataStatement sqlStatement) {
        String exportedData = generateExportData(metaData);
        if (sqlStatement.getFilePath().isPresent()) {
            String filePath = sqlStatement.getFilePath().get();
            ExportUtils.exportToFile(filePath, exportedData);
            return Collections.singleton(new LocalDataQueryResultRow(ProxyContext.getInstance().getContextManager().getInstanceContext().getInstance().getCurrentInstanceId(), LocalDateTime.now(),
                    String.format("Successfully exported to：'%s'", filePath)));
        }
        return Collections.singleton(new LocalDataQueryResultRow(
                ProxyContext.getInstance().getContextManager().getInstanceContext().getInstance().getCurrentInstanceId(), LocalDateTime.now(), Base64.encodeBase64String(exportedData.getBytes())));
    }
    
    private String generateExportData(final ShardingSphereMetaData metaData) {
        ProxyContext proxyContext = ProxyContext.getInstance();
        ExportedMetaData exportedMetaData = new ExportedMetaData();
        exportedMetaData.setDatabases(getDatabases(proxyContext));
        exportedMetaData.setProps(generatePropsData(metaData.getProps().getProps()));
        exportedMetaData.setRules(generateRulesData(metaData.getGlobalRuleMetaData().getConfigurations()));
        ExportedClusterInfo exportedClusterInfo = new ExportedClusterInfo();
        exportedClusterInfo.setMetaData(exportedMetaData);
        generateSnapshotInfo(metaData, exportedClusterInfo);
        return JsonUtils.toJsonString(exportedClusterInfo);
    }
    
    private Map<String, String> getDatabases(final ProxyContext proxyContext) {
        Map<String, String> result = new LinkedHashMap<>();
        proxyContext.getAllDatabaseNames().forEach(each -> {
            ShardingSphereDatabase database = proxyContext.getDatabase(each);
            if (database.getResourceMetaData().getAllInstanceDataSourceNames().isEmpty()) {
                return;
            }
            result.put(each, ExportUtils.generateExportDatabaseData(database));
        });
        return result;
    }
    
    private String generatePropsData(final Properties props) {
        if (props.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append("props:").append(System.lineSeparator());
        props.forEach((key, value) -> result.append("  ").append(key).append(": ").append(value).append(System.lineSeparator()));
        return result.toString();
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
            GlobalClockProvider globalClockProvider = TypedSPILoader.getService(GlobalClockProvider.class,
                    globalClockRule.getGlobalClockProviderType(), globalClockRule.getConfiguration().getProps());
            long csn = globalClockProvider.getCurrentTimestamp();
            ExportedSnapshotInfo snapshotInfo = new ExportedSnapshotInfo();
            snapshotInfo.setCsn(String.valueOf(csn));
            snapshotInfo.setCreateTime(LocalDateTime.now());
            exportedClusterInfo.setSnapshotInfo(snapshotInfo);
        }
    }
    
    @Override
    public Class<ExportMetaDataStatement> getType() {
        return ExportMetaDataStatement.class;
    }
}
