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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Set readwrite-splitting status handler.
 */
public final class SetReadwriteSplittingStatusHandler extends UpdatableRALBackendHandler<SetReadwriteSplittingStatusStatement, SetReadwriteSplittingStatusHandler> {
    
    private static final String DISABLE = "DISABLE";
    
    private ConnectionSession connectionSession;
    
    @Override
    public SetReadwriteSplittingStatusHandler init(final HandlerParameter<SetReadwriteSplittingStatusStatement> parameter) {
        initStatement(parameter.getStatement());
        connectionSession = parameter.getConnectionSession();
        return this;
    }
    
    @Override
    protected void update(final ContextManager contextManager, final SetReadwriteSplittingStatusStatement sqlStatement) throws DistSQLException {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : connectionSession.getSchemaName();
        String toBeUpdatedResource = sqlStatement.getResourceName();
        checkModeAndPersistRepository(contextManager);
        checkSchema(schemaName);
        checkReadwriteSplittingRule(contextManager, schemaName);
        Map<String, String> replicaResources = getReplicaResources(contextManager, schemaName);
        Map<String, String> disabledResources = getDisabledResources(contextManager, schemaName);
        boolean isDisable = DISABLE.equals(sqlStatement.getStatus());
        if (isDisable) {
            checkDisable(contextManager, schemaName, disabledResources.keySet(), toBeUpdatedResource, replicaResources);
        } else {
            checkEnable(contextManager, schemaName, disabledResources, toBeUpdatedResource);
        }
        Collection<String> groupNames = getGroupNames(toBeUpdatedResource, replicaResources, disabledResources);
        updateStatus(schemaName, groupNames, toBeUpdatedResource, isDisable);
    }
    
    private ReadwriteSplittingRuleConfiguration checkReadwriteSplittingRule(final ContextManager contextManager, final String schemaName) {
        Optional<ReadwriteSplittingRuleConfiguration> result = contextManager.getMetaDataContexts().getMetaData(schemaName)
                .getRuleMetaData().findRuleConfiguration(ReadwriteSplittingRuleConfiguration.class).stream().findAny();
        if (!result.isPresent()) {
            throw new UnsupportedOperationException("The current schema has no read_write splitting rules");
        }
        return result.get();
    }
    
    private void checkModeAndPersistRepository(final ContextManager contextManager) {
        if (!"Cluster".equals(contextManager.getInstanceContext().getModeConfiguration().getType())) {
            throw new UnsupportedOperationException("Mode must be `Cluster`.");
        }
        if (!contextManager.getMetaDataContexts().getMetaDataPersistService().isPresent()) {
            throw new UnsupportedOperationException("Persistence must be configured");
        }
    }
    
    private void checkSchema(final String schemaName) {
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
    }
    
    private Map<String, String> getReplicaResources(final ContextManager contextManager, final String schemaName) {
        Map<String, Map<String, String>> readwriteSplittingRules = getExportedReadwriteSplittingRules(contextManager, schemaName);
        Map<String, String> result = new HashMap<>();
        readwriteSplittingRules.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).forEach(entry -> addReplicaResource(result, entry));
        return result;
    }
    
    private Map<String, String> getDisabledResources(final ContextManager contextManager, final String schemaName) {
        Optional<MetaDataPersistService> persistService = contextManager.getMetaDataContexts().getMetaDataPersistService();
        Map<String, String> result = new HashMap<>();
        persistService.ifPresent(op -> {
            Map<String, String> disableNodes = getDisabledStorageNodes(schemaName, op).stream()
                    .collect(Collectors.toMap(QualifiedSchema::getDataSourceName, QualifiedSchema::getGroupName, (value1, value2) -> String.join(",", value1, value2)));
            result.putAll(disableNodes);
        });
        return result;
    }
    
    private void checkEnable(final ContextManager contextManager, final String schemaName, final Map<String, String> disabledResources, final String toBeEnabledResource) throws DistSQLException {
        checkResourceExists(contextManager, schemaName, toBeEnabledResource);
        checkIsNotDisabled(disabledResources.keySet(), toBeEnabledResource);
    }
    
    private void checkResourceExists(final ContextManager contextManager, final String schemaName, final String toBeDisabledResource) throws DistSQLException {
        Collection<String> notExistedResources = contextManager.getMetaDataContexts().getMetaData(schemaName).getResource().getNotExistedResources(Collections.singleton(toBeDisabledResource));
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), () -> new RequiredResourceMissedException(schemaName, Collections.singleton(toBeDisabledResource)));
    }
    
    private void checkIsNotDisabled(final Collection<String> disabledResources, final String toBeEnabledResource) {
        if (!disabledResources.contains(toBeEnabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` is not disabled", toBeEnabledResource));
        }
    }
    
    private void checkDisable(final ContextManager contextManager, final String schemaName, final Collection<String> disabledResources, final String toBeDisabledResource,
                              final Map<String, String> replicaResources) throws DistSQLException {
        checkResourceExists(contextManager, schemaName, toBeDisabledResource);
        checkIsDisabled(replicaResources, disabledResources, toBeDisabledResource);
        checkIsReplicaResource(replicaResources, toBeDisabledResource);
        checkIsLastResource(replicaResources, toBeDisabledResource);
    }
    
    private void checkIsDisabled(final Map<String, String> replicaResources, final Collection<String> disabledResources, final String toBeDisabledResource) {
        String toBeDisableResourceRuleNames = replicaResources.get(toBeDisabledResource);
        if (Strings.isNullOrEmpty(toBeDisableResourceRuleNames) && disabledResources.contains(toBeDisabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` has been disabled", toBeDisabledResource));
        }
    }
    
    private void checkIsReplicaResource(final Map<String, String> replicaResources, final String toBeDisabledResource) {
        if (!replicaResources.containsKey(toBeDisabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` is not used as a read resource by any read-write separation rules,cannot be disabled", toBeDisabledResource));
        }
    }
    
    private void checkIsLastResource(final Map<String, String> replicaResources, final String toBeDisabledResource) {
        Collection<String> onlyOneResourceRules = getOnlyOneResourceRules(replicaResources);
        Collection<String> toBeDisabledResourceRuleNames = Splitter.on(",").trimResults().splitToList(replicaResources.get(toBeDisabledResource));
        onlyOneResourceRules = onlyOneResourceRules.stream().filter(toBeDisabledResourceRuleNames::contains).collect(Collectors.toSet());
        if (!onlyOneResourceRules.isEmpty()) {
            throw new UnsupportedOperationException(String.format("`%s` is the last read resource in `%s`, cannot be disabled", toBeDisabledResource, onlyOneResourceRules));
        }
    }
    
    private Collection<String> getGroupNames(final String toBeDisableResource, final Map<String, String> replicaResources, final Map<String, String> disabledResources) {
        String groupNames = replicaResources.getOrDefault(toBeDisableResource, disabledResources.get(toBeDisableResource));
        return Splitter.on(",").splitToList(groupNames);
    }
    
    private void updateStatus(final String schemaName, final Collection<String> groupNames, final String toBeDisableResource, final boolean isDisable) {
        groupNames.forEach(each -> {
            StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, isDisable ? StorageNodeStatus.DISABLED : StorageNodeStatus.ENABLED);
            ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, each, toBeDisableResource, storageNodeDataSource));
        });
    }
    
    private Collection<QualifiedSchema> getDisabledStorageNodes(final String schemaName, final MetaDataPersistService persistService) {
        Map<String, StorageNodeDataSource> storageNodes = new StorageNodeStatusService((ClusterPersistRepository) persistService.getRepository()).loadStorageNodes();
        return storageNodes.entrySet().stream().filter(each -> StorageNodeStatus.DISABLED.name().equalsIgnoreCase(each.getValue().getStatus()))
                .map(each -> new QualifiedSchema(each.getKey())).filter(each -> schemaName.equalsIgnoreCase(each.getSchemaName()))
                .collect(Collectors.toList());
    }
    
    private Map<String, Map<String, String>> getExportedReadwriteSplittingRules(final ContextManager contextManager, final String schemaName) {
        Map<String, Map<String, String>> result = new HashMap<>();
        contextManager.getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().findRules(ReadwriteSplittingRule.class).stream().findAny()
                .filter(each -> each.containExportableKey(Arrays.asList(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, ExportableConstants.EXPORTABLE_KEY_ENABLED_DATA_SOURCE)))
                .map(each -> each.export(Arrays.asList(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, ExportableConstants.EXPORTABLE_KEY_ENABLED_DATA_SOURCE)))
                .ifPresent(each -> {
                    result.putAll((Map) each.getOrDefault(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, Collections.emptyMap()));
                    result.putAll((Map) each.getOrDefault(ExportableConstants.EXPORTABLE_KEY_ENABLED_DATA_SOURCE, Collections.emptyMap()));
                });
        return result;
    }
    
    private Collection<String> getOnlyOneResourceRules(final Map<String, String> replicaResources) {
        return replicaResources.values().stream().map(schemaName -> Arrays.stream(schemaName.split(",")).collect(Collectors.toMap(each -> each, each -> 1)).entrySet())
                .flatMap(Collection::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue, Integer::sum)).entrySet().stream()
                .filter(entry -> entry.getValue() <= 1).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private void addReplicaResource(final Map<String, String> replicaResources, final Entry<String, Map<String, String>> entry) {
        entry.getValue().entrySet().stream().filter(entry1 -> ExportableConstants.REPLICA_DATA_SOURCE_NAMES.equals(entry1.getKey()))
                .map(entry1 -> Arrays.asList(entry1.getValue().split(","))).flatMap(Collection::stream).forEach(each -> put(replicaResources, each, entry.getKey()));
    }
    
    private void put(final Map<String, String> map, final String key, final String value) {
        if (map.containsKey(key)) {
            map.put(key, String.join(",", map.get(key), value));
        } else {
            map.put(key, value);
        }
    }
}
