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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.DataSourceDisabledEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;
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
public final class SetReadwriteSplittingStatusHandler extends UpdatableRALBackendHandler<SetReadwriteSplittingStatusStatement> {
    
    private static final String DISABLE = "DISABLE";
    
    @Override
    protected void update(final ContextManager contextManager) throws DistSQLException {
        String databaseName = getSqlStatement().getDatabase().isPresent() ? getSqlStatement().getDatabase().get().getIdentifier().getValue() : getConnectionSession().getDatabaseName();
        String toBeUpdatedResource = getSqlStatement().getResourceName();
        checkModeAndPersistRepository(contextManager);
        checkDatabaseName(databaseName);
        checkReadwriteSplittingRule(contextManager, databaseName);
        Map<String, String> replicaResources = getReplicaResources(contextManager, databaseName);
        Map<String, String> disabledResources = getDisabledResources(contextManager, databaseName);
        Map<String, String> autoAwareResources = getAutoAwareResources(contextManager, databaseName);
        boolean isDisable = DISABLE.equals(getSqlStatement().getStatus());
        if (isDisable) {
            checkDisable(contextManager, databaseName, disabledResources.keySet(), toBeUpdatedResource, replicaResources);
        } else {
            checkEnable(contextManager, databaseName, disabledResources, toBeUpdatedResource);
        }
        Collection<String> groupNames = getGroupNames(toBeUpdatedResource, replicaResources, disabledResources, autoAwareResources);
        updateStatus(databaseName, groupNames, toBeUpdatedResource, isDisable);
    }
    
    private void checkReadwriteSplittingRule(final ContextManager contextManager, final String databaseName) {
        Optional<ReadwriteSplittingRule> rule = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        ShardingSpherePreconditions.checkState(rule.isPresent(), () -> new UnsupportedSQLOperationException("The current schema has no read_write splitting rules"));
    }
    
    private void checkModeAndPersistRepository(final ContextManager contextManager) {
        ShardingSpherePreconditions.checkState(contextManager.getInstanceContext().isCluster(), () -> new UnsupportedSQLOperationException("Mode must be `Cluster`"));
    }
    
    private void checkDatabaseName(final String databaseName) {
        if (Strings.isNullOrEmpty(databaseName)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
    }
    
    private Map<String, String> getReplicaResources(final ContextManager contextManager, final String databaseName) {
        Map<String, Map<String, String>> readwriteSplittingRules = getExportedReadwriteSplittingRules(contextManager, databaseName);
        Map<String, String> result = new HashMap<>();
        readwriteSplittingRules.entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).forEach(entry -> addReplicaResource(result, entry));
        return result;
    }
    
    private Map<String, String> getAutoAwareResources(final ContextManager contextManager, final String databaseName) {
        Map<String, Map<String, String>> readwriteSplittingRules = getExportedReadwriteSplittingRules(contextManager, databaseName);
        Map<String, String> result = new HashMap<>();
        readwriteSplittingRules.values().stream().filter(each -> each.containsKey(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME)).forEach(each -> Splitter.on(",")
                .splitToList(each.get(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES)).forEach(each1 -> put(result, each1, each.get(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME))));
        return result;
    }
    
    private Map<String, String> getDisabledResources(final ContextManager contextManager, final String databaseName) {
        MetaDataPersistService persistService = contextManager.getMetaDataContexts().getPersistService();
        return getDisabledStorageNodes(databaseName, persistService).stream()
                .collect(Collectors.toMap(QualifiedDatabase::getDataSourceName, QualifiedDatabase::getGroupName, (value1, value2) -> String.join(",", value1, value2)));
    }
    
    private void checkEnable(final ContextManager contextManager, final String databaseName, final Map<String, String> disabledResources, final String toBeEnabledResource) throws DistSQLException {
        checkResourceExists(contextManager, databaseName, toBeEnabledResource);
        checkIsNotDisabled(disabledResources.keySet(), toBeEnabledResource);
    }
    
    private void checkResourceExists(final ContextManager contextManager, final String databaseName, final String toBeDisabledResource) throws DistSQLException {
        Collection<String> notExistedResources = contextManager
                .getMetaDataContexts().getMetaData().getDatabase(databaseName).getResource().getNotExistedResources(Collections.singleton(toBeDisabledResource));
        ShardingSpherePreconditions.checkState(notExistedResources.isEmpty(), () -> new RequiredResourceMissedException(databaseName, Collections.singleton(toBeDisabledResource)));
    }
    
    private void checkIsNotDisabled(final Collection<String> disabledResources, final String toBeEnabledResource) {
        ShardingSpherePreconditions.checkState(disabledResources.contains(toBeEnabledResource), () -> new UnsupportedSQLOperationException(String.format("`%s` is not disabled", toBeEnabledResource)));
    }
    
    private void checkDisable(final ContextManager contextManager, final String databaseName, final Collection<String> disabledResources, final String toBeDisabledResource,
                              final Map<String, String> replicaResources) throws DistSQLException {
        checkResourceExists(contextManager, databaseName, toBeDisabledResource);
        checkIsDisabled(replicaResources, disabledResources, toBeDisabledResource);
        checkIsReplicaResource(replicaResources, toBeDisabledResource);
        checkIsLastResource(replicaResources, toBeDisabledResource);
    }
    
    private void checkIsDisabled(final Map<String, String> replicaResources, final Collection<String> disabledResources, final String toBeDisabledResource) {
        String toBeDisableResourceRuleNames = replicaResources.get(toBeDisabledResource);
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(toBeDisableResourceRuleNames) || !disabledResources.contains(toBeDisabledResource),
                () -> new UnsupportedSQLOperationException(String.format("`%s` has been disabled", toBeDisabledResource)));
    }
    
    private void checkIsReplicaResource(final Map<String, String> replicaResources, final String toBeDisabledResource) {
        ShardingSpherePreconditions.checkState(replicaResources.containsKey(toBeDisabledResource),
                () -> new UnsupportedSQLOperationException(String.format("`%s` is not used as a read resource by any read-write separation rules,cannot be disabled", toBeDisabledResource)));
    }
    
    private void checkIsLastResource(final Map<String, String> replicaResources, final String toBeDisabledResource) {
        Collection<String> onlyOneResourceRules = getOnlyOneResourceRules(replicaResources);
        Collection<String> toBeDisabledResourceRuleNames = Splitter.on(",").trimResults().splitToList(replicaResources.get(toBeDisabledResource));
        onlyOneResourceRules = onlyOneResourceRules.stream().filter(toBeDisabledResourceRuleNames::contains).collect(Collectors.toSet());
        Collection<String> finalOnlyOneResourceRules = onlyOneResourceRules;
        ShardingSpherePreconditions.checkState(onlyOneResourceRules.isEmpty(),
                () -> new UnsupportedSQLOperationException(String.format("`%s` is the last read resource in `%s`, cannot be disabled", toBeDisabledResource, finalOnlyOneResourceRules)));
    }
    
    private Collection<String> getGroupNames(final String toBeDisableResource, final Map<String, String> replicaResources,
                                             final Map<String, String> disabledResources, final Map<String, String> autoAwareResources) {
        String groupNames = autoAwareResources.getOrDefault(toBeDisableResource, replicaResources.getOrDefault(toBeDisableResource, disabledResources.get(toBeDisableResource)));
        return Splitter.on(",").splitToList(groupNames);
    }
    
    private void updateStatus(final String databaseName, final Collection<String> groupNames, final String toBeDisableResource, final boolean isDisable) {
        groupNames.forEach(each -> {
            StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, isDisable ? StorageNodeStatus.DISABLED : StorageNodeStatus.ENABLED);
            ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext().post(new DataSourceDisabledEvent(databaseName, each, toBeDisableResource, storageNodeDataSource));
        });
    }
    
    private Collection<QualifiedDatabase> getDisabledStorageNodes(final String databaseName, final MetaDataPersistService persistService) {
        Map<String, StorageNodeDataSource> storageNodes = new StorageNodeStatusService((ClusterPersistRepository) persistService.getRepository()).loadStorageNodes();
        return storageNodes.entrySet().stream().filter(each -> StorageNodeStatus.DISABLED.name().equalsIgnoreCase(each.getValue().getStatus()))
                .map(each -> new QualifiedDatabase(each.getKey())).filter(each -> databaseName.equalsIgnoreCase(each.getDatabaseName())).collect(Collectors.toList());
    }
    
    private Map<String, Map<String, String>> getExportedReadwriteSplittingRules(final ContextManager contextManager, final String databaseName) {
        Map<String, Map<String, String>> result = new HashMap<>();
        contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class)
                .filter(each -> new RuleExportEngine(each)
                        .containExportableKey(Arrays.asList(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE)))
                .map(each -> new RuleExportEngine(each).export(Arrays.asList(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE)))
                .ifPresent(optional -> {
                    result.putAll((Map) optional.getOrDefault(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, Collections.emptyMap()));
                    result.putAll((Map) optional.getOrDefault(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, Collections.emptyMap()));
                });
        return result;
    }
    
    private Collection<String> getOnlyOneResourceRules(final Map<String, String> replicaResources) {
        return replicaResources.values().stream().map(databaseName -> Arrays.stream(databaseName.split(",")).collect(Collectors.toMap(each -> each, each -> 1)).entrySet())
                .flatMap(Collection::stream).collect(Collectors.toMap(Entry::getKey, Entry::getValue, Integer::sum)).entrySet().stream()
                .filter(entry -> entry.getValue() <= 1).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private void addReplicaResource(final Map<String, String> replicaResources, final Entry<String, Map<String, String>> readwriteSplittingRule) {
        readwriteSplittingRule.getValue().entrySet().stream().filter(entry -> ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES.equals(entry.getKey()))
                .map(entry -> Arrays.asList(entry.getValue().split(","))).flatMap(Collection::stream).forEach(each -> put(replicaResources, each, readwriteSplittingRule.getKey()));
    }
    
    private void put(final Map<String, String> map, final String key, final String value) {
        if (map.containsKey(key)) {
            map.put(key, String.join(",", map.get(key), value));
        } else {
            map.put(key, value);
        }
    }
}
