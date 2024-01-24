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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.readwritesplitting;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.type.ral.update.DatabaseAwareUpdatableRALExecutor;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.metadata.persist.MetaDataBasedPersistService;
import org.apache.shardingsphere.mode.event.storage.DataSourceDisabledEvent;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.event.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service.StorageNodeStatusService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.status.AlterReadwriteSplittingStorageUnitStatusStatement;
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
 * Alter readwrite-splitting storage unit status executor.
 */
// TODO move to readwritesplitting module
@Setter
public final class AlterReadwriteSplittingStorageUnitStatusExecutor implements DatabaseAwareUpdatableRALExecutor<AlterReadwriteSplittingStorageUnitStatusStatement> {
    
    private static final String DISABLE = "DISABLE";
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final AlterReadwriteSplittingStorageUnitStatusStatement sqlStatement) {
        String toBeUpdatedStorageUnit = sqlStatement.getStorageUnitName();
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        checkModeAndPersistRepository(contextManager);
        checkReadwriteSplittingRule();
        Map<String, String> replicaStorageUnits = getReplicaResources();
        Map<String, String> disabledStorageUnits = getDisabledResources(contextManager);
        Map<String, String> autoAwareResources = getAutoAwareResources();
        boolean isDisable = DISABLE.equals(sqlStatement.getStatus());
        if (isDisable) {
            checkDisable(contextManager, database.getName(), disabledStorageUnits.keySet(), toBeUpdatedStorageUnit, replicaStorageUnits);
        } else {
            checkEnable(contextManager, database.getName(), disabledStorageUnits, toBeUpdatedStorageUnit);
        }
        Collection<String> groupNames = getGroupNames(toBeUpdatedStorageUnit, replicaStorageUnits, disabledStorageUnits, autoAwareResources);
        String groupName = sqlStatement.getGroupName();
        if (Strings.isNullOrEmpty(groupName)) {
            updateStatus(database.getName(), groupNames, toBeUpdatedStorageUnit, isDisable);
        } else {
            checkGroupName(groupNames, groupName);
            updateStatus(database.getName(), Collections.singleton(groupName), toBeUpdatedStorageUnit, isDisable);
        }
    }
    
    private void checkModeAndPersistRepository(final ContextManager contextManager) {
        ShardingSpherePreconditions.checkState(contextManager.getInstanceContext().isCluster(), () -> new UnsupportedSQLOperationException("Mode must be `Cluster`"));
    }
    
    private void checkReadwriteSplittingRule() {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        ShardingSpherePreconditions.checkState(rule.isPresent(), () -> new UnsupportedSQLOperationException("The current database has no read_write splitting rules"));
    }
    
    private Map<String, String> getReplicaResources() {
        Map<String, String> result = new HashMap<>();
        getExportedReadwriteSplittingRules().entrySet().stream().filter(entry -> !entry.getValue().isEmpty()).forEach(entry -> addReplicaResource(result, entry));
        return result;
    }
    
    private Map<String, Map<String, String>> getExportedReadwriteSplittingRules() {
        Map<String, Map<String, String>> result = new HashMap<>();
        database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class)
                .filter(each -> new RuleExportEngine(each)
                        .containExportableKey(Arrays.asList(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE)))
                .map(each -> new RuleExportEngine(each).export(Arrays.asList(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE)))
                .ifPresent(optional -> {
                    result.putAll((Map) optional.getOrDefault(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, Collections.emptyMap()));
                    result.putAll((Map) optional.getOrDefault(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, Collections.emptyMap()));
                });
        return result;
    }
    
    private Map<String, String> getDisabledResources(final ContextManager contextManager) {
        return getDisabledStorageNodes(contextManager.getMetaDataContexts().getPersistService()).stream()
                .collect(Collectors.toMap(QualifiedDatabase::getDataSourceName, QualifiedDatabase::getGroupName, (value1, value2) -> String.join(",", value1, value2)));
    }
    
    private Map<String, String> getAutoAwareResources() {
        Map<String, String> result = new HashMap<>();
        getExportedReadwriteSplittingRules().values().stream().filter(each -> each.containsKey(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME)).forEach(each -> Splitter.on(",")
                .splitToList(each.get(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES)).forEach(each1 -> put(result, each1, each.get(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME))));
        return result;
    }
    
    private void checkGroupName(final Collection<String> groupNames, final String groupName) {
        ShardingSpherePreconditions.checkState(groupNames.contains(groupName),
                () -> new UnsupportedSQLOperationException(String.format("The current database does not exist the group `%s`", groupName)));
    }
    
    private void checkEnable(final ContextManager contextManager, final String databaseName, final Map<String, String> disabledResources, final String toBeEnabledResource) {
        checkResourceExists(contextManager, databaseName, toBeEnabledResource);
        checkIsNotDisabled(disabledResources.keySet(), toBeEnabledResource);
    }
    
    private void checkIsNotDisabled(final Collection<String> disabledResources, final String toBeEnabledResource) {
        ShardingSpherePreconditions.checkState(disabledResources.contains(toBeEnabledResource), () -> new UnsupportedSQLOperationException(String.format("`%s` is not disabled", toBeEnabledResource)));
    }
    
    private void checkResourceExists(final ContextManager contextManager, final String databaseName, final String toBeDisabledResource) {
        Collection<String> notExistedResources = contextManager
                .getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getNotExistedDataSources(Collections.singleton(toBeDisabledResource));
        ShardingSpherePreconditions.checkState(notExistedResources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, Collections.singleton(toBeDisabledResource)));
    }
    
    private void checkDisable(final ContextManager contextManager, final String databaseName, final Collection<String> disabledStorageUnits, final String toBeDisabledStorageUnit,
                              final Map<String, String> replicaResources) {
        checkResourceExists(contextManager, databaseName, toBeDisabledStorageUnit);
        checkIsDisabled(replicaResources, disabledStorageUnits, toBeDisabledStorageUnit);
        checkIsReplicaResource(replicaResources, toBeDisabledStorageUnit);
    }
    
    private void checkIsDisabled(final Map<String, String> replicaResources, final Collection<String> disabledStorageUnits, final String toBeDisabledStorageUnit) {
        String toBeDisableResourceRuleNames = replicaResources.get(toBeDisabledStorageUnit);
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(toBeDisableResourceRuleNames) || !disabledStorageUnits.contains(toBeDisabledStorageUnit),
                () -> new UnsupportedSQLOperationException(String.format("`%s` has been disabled", toBeDisabledStorageUnit)));
    }
    
    private void checkIsReplicaResource(final Map<String, String> replicaStorageUnits, final String toBeDisabledStorageUnit) {
        ShardingSpherePreconditions.checkState(replicaStorageUnits.containsKey(toBeDisabledStorageUnit),
                () -> new UnsupportedSQLOperationException(String.format("`%s` is not used as a read storage unit by any read-write separation rules,cannot be disabled", toBeDisabledStorageUnit)));
    }
    
    private Collection<String> getGroupNames(final String toBeDisableStorageUnit, final Map<String, String> replicaStorageUnits,
                                             final Map<String, String> disabledStorageUnits, final Map<String, String> autoAwareResources) {
        String groupNames = autoAwareResources.getOrDefault(toBeDisableStorageUnit, replicaStorageUnits.getOrDefault(toBeDisableStorageUnit, disabledStorageUnits.get(toBeDisableStorageUnit)));
        return Splitter.on(",").splitToList(groupNames);
    }
    
    private void updateStatus(final String databaseName, final Collection<String> groupNames, final String toBeDisableStorageUnit, final boolean isDisable) {
        groupNames.forEach(each -> {
            StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, isDisable ? DataSourceState.DISABLED : DataSourceState.ENABLED);
            ProxyContext.getInstance().getContextManager().getInstanceContext().getEventBusContext()
                    .post(new DataSourceDisabledEvent(databaseName, each, toBeDisableStorageUnit, storageNodeDataSource));
        });
    }
    
    private Collection<QualifiedDatabase> getDisabledStorageNodes(final MetaDataBasedPersistService persistService) {
        Map<String, StorageNodeDataSource> storageNodes = new StorageNodeStatusService(persistService.getRepository()).loadStorageNodes();
        return storageNodes.entrySet().stream().filter(each -> DataSourceState.DISABLED == each.getValue().getStatus())
                .map(each -> new QualifiedDatabase(each.getKey())).filter(each -> database.getName().equalsIgnoreCase(each.getDatabaseName())).collect(Collectors.toList());
    }
    
    private void addReplicaResource(final Map<String, String> replicaStorageUnits, final Entry<String, Map<String, String>> readwriteSplittingRule) {
        readwriteSplittingRule.getValue().entrySet().stream().filter(entry -> ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES.equals(entry.getKey()))
                .map(entry -> Arrays.asList(entry.getValue().split(","))).flatMap(Collection::stream).forEach(each -> put(replicaStorageUnits, each, readwriteSplittingRule.getKey()));
    }
    
    private void put(final Map<String, String> map, final String key, final String value) {
        map.put(key, map.containsKey(key) ? String.join(",", map.get(key), value) : value);
    }
    
    @Override
    public Class<AlterReadwriteSplittingStorageUnitStatusStatement> getType() {
        return AlterReadwriteSplittingStorageUnitStatusStatement.class;
    }
}
