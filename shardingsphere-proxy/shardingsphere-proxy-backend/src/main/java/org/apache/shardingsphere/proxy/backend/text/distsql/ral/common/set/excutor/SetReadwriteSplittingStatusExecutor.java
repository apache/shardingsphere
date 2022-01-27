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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Set readwrite-splitting status executor.
 */
@AllArgsConstructor
public final class SetReadwriteSplittingStatusExecutor implements SetStatementExecutor {
    
    private static final String DISABLE = "DISABLE";
    
    private final SetReadwriteSplittingStatusStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : connectionSession.getSchemaName();
        String resourceName = sqlStatement.getResourceName();
        checkSchema(schemaName);
        boolean isDisable = DISABLE.equals(sqlStatement.getStatus());
        if (isDisable) {
            checkDisablingIsValid(schemaName, resourceName);
        } else {
            checkEnablingIsValid(schemaName, resourceName);
        }
        ShardingSphereEventBus.getInstance().post(new DataSourceDisabledEvent(schemaName, resourceName, isDisable));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void checkSchema(final String schemaName) {
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
    }
    
    private void checkEnablingIsValid(final String schemaName, final String toBeEnabledResource) throws DistSQLException {
        checkResourceExists(schemaName, toBeEnabledResource);
        Collection<String> disabledResources = getDisabledResources(schemaName);
        if (!disabledResources.contains(toBeEnabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` is not disabled", toBeEnabledResource));
        }
    }
    
    private void checkDisablingIsValid(final String schemaName, final String toBeDisabledResource) throws DistSQLException {
        checkResourceExists(schemaName, toBeDisabledResource);
        Collection<String> disabledResources = getDisabledResources(schemaName);
        if (disabledResources.contains(toBeDisabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` has been disabled", toBeDisabledResource));
        }
        Map<String, Map<String, String>> readwriteSplittingRules = getExportedReadwriteSplittingRules(schemaName);
        Map<String, String> primaryResources = new HashMap<>();
        Map<String, String> replicaResources = new HashMap<>();
        readwriteSplittingRules.entrySet().stream().filter(entry -> !entry.getValue().isEmpty())
                .peek(entry -> addPrimaryResource(primaryResources, entry)).forEach(entry -> addReplicaResource(replicaResources, entry));
        if (primaryResources.containsKey(toBeDisabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` is the primary resource in the `%s` rule, cannot be disabled",
                    toBeDisabledResource, primaryResources.get(toBeDisabledResource)));
        }
        if (!replicaResources.containsKey(toBeDisabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` is not used by any readwrite-splitting rule, cannot be disabled", toBeDisabledResource));
        }
        Set<String> canBeDisabledResources = getCanBeDisabledResources(replicaResources, disabledResources);
        if (!canBeDisabledResources.contains(toBeDisabledResource)) {
            throw new UnsupportedOperationException(String.format("`%s` is the last read resource in `%s`, cannot be disabled", toBeDisabledResource, replicaResources.get(toBeDisabledResource)));
        }
    }
    
    private Collection<String> getDisabledResources(final String schemaName) {
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        List<String> result = new ArrayList<>();
        persistService.ifPresent(op -> result.addAll(getStorageNodeStatusData(op).stream().filter(each -> isCurrentSchema(schemaName, each)).map(this::getResourceName).collect(Collectors.toSet())));
        return result;
    }
    
    private Collection<String> getStorageNodeStatusData(final MetaDataPersistService persistService) {
        return persistService.getRepository().getChildrenKeys(StorageStatusNode.getStatusPath(StorageNodeStatus.DISABLE));
    }
    
    private Map<String, Map<String, String>> getExportedReadwriteSplittingRules(final String schemaName) {
        Map<String, Map<String, String>> result = new HashMap<>();
        ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().findRules(ReadwriteSplittingRule.class).stream().findAny()
                .filter(each -> each.containExportableKey(Arrays.asList(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, ExportableConstants.EXPORTABLE_KEY_DATA_SOURCE)))
                .map(each -> each.export(Arrays.asList(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, ExportableConstants.EXPORTABLE_KEY_DATA_SOURCE)))
                .ifPresent(each -> {
                    result.putAll((Map) each.getOrDefault(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE, Collections.emptyMap()));
                    result.putAll((Map) each.getOrDefault(ExportableConstants.EXPORTABLE_KEY_DATA_SOURCE, Collections.emptyMap()));
                });
        return result;
    }
    
    private Set<String> getCanBeDisabledResources(final Map<String, String> replicaResources, final Collection<String> haveBeenDisabledResources) {
        Set<String> onlyOneResourceRules = replicaResources.values().stream().map(schemaName -> Arrays.stream(schemaName.split(",")).collect(Collectors.toMap(each -> each, each -> 1)).entrySet())
                .flatMap(Collection::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue, Integer::sum)).entrySet().stream()
                .filter(entry -> entry.getValue() <= 1).map(Entry::getKey).collect(Collectors.toSet());
        return replicaResources.entrySet().stream().filter(entry -> !haveBeenDisabledResources.contains(entry.getKey()))
                .filter(entry -> onlyOneResourceRules.stream().noneMatch(each -> Arrays.asList(entry.getValue().split(",")).contains(each))).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private void checkResourceExists(final String schemaName, final String toBeDisabledResource) throws DistSQLException {
        Collection<String> notExistedResources = ProxyContext.getInstance().getMetaData(schemaName).getResource().getNotExistedResources(Collections.singleton(toBeDisabledResource));
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), new RequiredResourceMissedException(schemaName, Collections.singleton(toBeDisabledResource)));
    }
    
    private void addPrimaryResource(final Map<String, String> primaryResources, final Entry<String, Map<String, String>> entry) {
        entry.getValue().entrySet().stream().filter(entry1 -> ExportableConstants.PRIMARY_DATA_SOURCE_NAME.equals(entry1.getKey()))
                .forEach(entry1 -> put(primaryResources, entry1.getValue(), entry.getKey()));
    }
    
    private void addReplicaResource(final Map<String, String> replicaResources, final Entry<String, Map<String, String>> entry) {
        entry.getValue().entrySet().stream().filter(entry1 -> ExportableConstants.REPLICA_DATA_SOURCE_NAMES.equals(entry1.getKey()))
                .map(entry1 -> Arrays.asList(entry1.getValue().split(","))).flatMap(Collection::stream).forEach(each -> put(replicaResources, each, entry.getKey()));
    }
    
    private boolean isCurrentSchema(final String schemaName, final String nodeData) {
        return schemaName.equals(nodeData.split("\\.")[0]);
    }
    
    private String getResourceName(final String nodeData) {
        return nodeData.split("\\.")[1];
    }
    
    private void put(final Map<String, String> map, final String key, final String value) {
        if (map.containsKey(key)) {
            map.put(key, String.join(",", map.get(key), value));
        } else {
            map.put(key, value);
        }
    }
}
