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

import com.google.common.base.Strings;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.ral.query.DatabaseAwareQueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.event.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Show status from readwrite-splitting rules executor.
 */
@Setter
public final class ShowStatusFromReadwriteSplittingRulesExecutor implements DatabaseAwareQueryableRALExecutor<ShowStatusFromReadwriteSplittingRulesStatement> {
    
    private ShardingSphereDatabase currentDatabase;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("storage_unit", "status");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowStatusFromReadwriteSplittingRulesStatement sqlStatement, final ShardingSphereMetaData metaData) {
        Collection<String> allReadResources = getAllReadResources(currentDatabase, sqlStatement.getGroupName());
        Map<String, StorageNodeDataSource> persistentReadResources = getPersistentReadResources(
                currentDatabase.getName(), ProxyContext.getInstance().getContextManager().getMetaDataContexts().getPersistService());
        return buildRows(allReadResources, persistentReadResources);
    }
    
    private Collection<String> getAllReadResources(final ShardingSphereDatabase database, final String groupName) {
        Collection<String> exportKeys = Arrays.asList(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE);
        Map<String, Object> exportMap = database.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> new RuleExportEngine(each).containExportableKey(exportKeys)).findFirst().map(each -> new RuleExportEngine(each).export(exportKeys)).orElse(Collections.emptyMap());
        Map<String, Map<String, String>> allReadwriteRuleMap = exportMap.values().stream().map(each -> ((Map<String, Map<String, String>>) each).entrySet())
                .flatMap(Collection::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
        if (!Strings.isNullOrEmpty(groupName)) {
            allReadwriteRuleMap = allReadwriteRuleMap.entrySet().stream().filter(each -> groupName.equalsIgnoreCase(each.getKey()))
                    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (oldValue, currentValue) -> currentValue, LinkedHashMap::new));
        }
        return allReadwriteRuleMap.values().stream().map(each -> each.get(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES)).filter(each -> null != each && !each.isEmpty())
                .map(this::deconstructString).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private Map<String, StorageNodeDataSource> getPersistentReadResources(final String databaseName, final MetaDataBasedPersistService persistService) {
        if (null == persistService || null == persistService.getRepository() || !(persistService.getRepository() instanceof ClusterPersistRepository)) {
            return Collections.emptyMap();
        }
        Map<String, StorageNodeDataSource> storageNodes = new StorageNodeStatusService((ClusterPersistRepository) persistService.getRepository()).loadStorageNodes();
        Map<String, StorageNodeDataSource> result = new HashMap<>();
        storageNodes.entrySet().stream().filter(entry -> StorageNodeRole.MEMBER == entry.getValue().getRole()).forEach(entry -> {
            QualifiedDatabase qualifiedDatabase = new QualifiedDatabase(entry.getKey());
            if (databaseName.equalsIgnoreCase(qualifiedDatabase.getDatabaseName())) {
                result.put(qualifiedDatabase.getDataSourceName(), entry.getValue());
            }
        });
        return result;
    }
    
    private Collection<LocalDataQueryResultRow> buildRows(final Collection<String> readResources, final Map<String, StorageNodeDataSource> persistentReadResources) {
        Map<DataSourceState, Map<String, StorageNodeDataSource>> persistentReadResourceGroup = persistentReadResources.entrySet().stream()
                .collect(Collectors.groupingBy(each -> each.getValue().getStatus(), Collectors.toMap(Entry::getKey, Entry::getValue)));
        Map<String, StorageNodeDataSource> disabledReadResources = persistentReadResourceGroup.getOrDefault(DataSourceState.DISABLED, Collections.emptyMap());
        Map<String, StorageNodeDataSource> enabledReadResources = persistentReadResourceGroup.getOrDefault(DataSourceState.ENABLED, Collections.emptyMap());
        readResources.removeIf(disabledReadResources::containsKey);
        readResources.addAll(enabledReadResources.keySet());
        readResources.addAll(disabledReadResources.keySet());
        return readResources.stream().map(each -> buildRow(each, disabledReadResources.get(each))).collect(Collectors.toList());
    }
    
    private List<String> deconstructString(final String str) {
        return new LinkedList<>(Arrays.asList(str.split(",")));
    }
    
    private LocalDataQueryResultRow buildRow(final String resource, final StorageNodeDataSource storageNodeDataSource) {
        return null == storageNodeDataSource ? new LocalDataQueryResultRow(resource, DataSourceState.ENABLED.name()) : new LocalDataQueryResultRow(resource, storageNodeDataSource.getStatus().name());
    }
    
    @Override
    public Class<ShowStatusFromReadwriteSplittingRulesStatement> getType() {
        return ShowStatusFromReadwriteSplittingRulesStatement.class;
    }
}
