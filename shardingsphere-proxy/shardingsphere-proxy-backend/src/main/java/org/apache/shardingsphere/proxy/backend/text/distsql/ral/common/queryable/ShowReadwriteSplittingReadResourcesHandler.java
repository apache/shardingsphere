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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;

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
 * Show readwrite-splitting read resources executor.
 */
public final class ShowReadwriteSplittingReadResourcesHandler extends QueryableRALBackendHandler<ShowReadwriteSplittingReadResourcesStatement, ShowReadwriteSplittingReadResourcesHandler> {
    
    private static final String RESOURCE = "resource";
    
    private static final String STATUS = "status";
    
    private static final String DELAY_TIME = "delay_time(ms)";
    
    private ConnectionSession connectionSession;
    
    @Override
    public ShowReadwriteSplittingReadResourcesHandler init(final HandlerParameter<ShowReadwriteSplittingReadResourcesStatement> parameter) {
        connectionSession = parameter.getConnectionSession();
        return super.init(parameter);
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(RESOURCE, STATUS, DELAY_TIME);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : connectionSession.getSchemaName();
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
        MetaDataContexts metaDataContexts = contextManager.getMetaDataContexts();
        ShardingSphereMetaData metaData = metaDataContexts.getMetaData(schemaName);
        Collection<String> allReadResources = getAllReadResources(metaData);
        Map<String, StorageNodeDataSource> persistentReadResources = getPersistentReadResources(schemaName, metaDataContexts.getMetaDataPersistService().orElse(null));
        return buildRows(allReadResources, persistentReadResources);
    }
    
    private Collection<String> getAllReadResources(final ShardingSphereMetaData metaData) {
        Collection<String> result = new LinkedHashSet<>();
        Map<String, Map<String, String>> readResourceData = getReadResourceData(metaData);
        readResourceData.forEach((key, value) -> {
            String resources = value.getOrDefault(ExportableConstants.REPLICA_DATA_SOURCE_NAMES, "");
            result.addAll(deconstructString(resources));
        });
        return result;
    }
    
    private Map<String, Map<String, String>> getReadResourceData(final ShardingSphereMetaData metaData) {
        return metaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof ExportableRule)
                .map(each -> ((ExportableRule) each).export(ExportableConstants.EXPORTABLE_KEY_DATA_SOURCE))
                .map(each -> (Map<String, Map<String, String>>) each.orElse(Collections.emptyMap()))
                .map(Map::entrySet).flatMap(Collection::stream).filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (v1, v2) -> v2, LinkedHashMap::new));
    }
    
    private Map<String, StorageNodeDataSource> getPersistentReadResources(final String schemaName, final MetaDataPersistService persistService) {
        if (null == persistService || null == persistService.getRepository() || !(persistService.getRepository() instanceof ClusterPersistRepository)) {
            return Collections.emptyMap();
        }
        Map<String, StorageNodeDataSource> storageNodes = new StorageNodeStatusService((ClusterPersistRepository) persistService.getRepository()).loadStorageNodes();
        Map<String, StorageNodeDataSource> result = new HashMap<>();
        storageNodes.entrySet().stream().filter(entry -> "member".equalsIgnoreCase(entry.getValue().getRole())).forEach(entry -> {
            QualifiedSchema qualifiedSchema = new QualifiedSchema(entry.getKey());
            if (schemaName.equalsIgnoreCase(qualifiedSchema.getSchemaName())) {
                result.put(qualifiedSchema.getDataSourceName(), entry.getValue());
            }
        });
        return result;
    }
    
    private Collection<List<Object>> buildRows(final Collection<String> allReadResources, final Map<String, StorageNodeDataSource> disabledResources) {
        return allReadResources.stream().map(each -> buildRow(each, disabledResources.get(each))).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private LinkedList<String> deconstructString(final String str) {
        return new LinkedList<>(Arrays.asList(str.split(",")));
    }
    
    private List<Object> buildRow(final String resource, final StorageNodeDataSource storageNodeDataSource) {
        if (null == storageNodeDataSource) {
            return Arrays.asList(resource, StorageNodeStatus.ENABLED.name().toLowerCase(), "0");
        } else {
            Long replicationDelayTime = storageNodeDataSource.getReplicationDelayMilliseconds();
            String status = StorageNodeStatus.valueOf(storageNodeDataSource.getStatus().toUpperCase()).name().toLowerCase();
            return Arrays.asList(resource, status, null != replicationDelayTime ? Long.toString(replicationDelayTime) : "0");
        }
    }
}
