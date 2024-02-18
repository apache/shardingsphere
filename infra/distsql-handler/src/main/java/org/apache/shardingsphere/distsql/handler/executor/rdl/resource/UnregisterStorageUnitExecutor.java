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

package org.apache.shardingsphere.distsql.handler.executor.rdl.resource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.StorageUnitInUsedException;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.resource.StorageUnitDefinitionProcessor;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Unregister storage unit executor.
 */
@Setter
@Slf4j
public final class UnregisterStorageUnitExecutor implements DistSQLUpdateExecutor<UnregisterStorageUnitStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final UnregisterStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        if (!sqlStatement.isIfExists()) {
            checkExisted(sqlStatement.getStorageUnitNames());
        }
        checkInUsed(sqlStatement);
        try {
            contextManager.getInstanceContext().getModeContextManager().unregisterStorageUnits(database.getName(), sqlStatement.getStorageUnitNames());
        } catch (final SQLException | ShardingSphereServerException ex) {
            log.error("Unregister storage unit failed", ex);
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
    }
    
    private void checkExisted(final Collection<String> storageUnitNames) {
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        Collection<String> notExistedStorageUnits = storageUnitNames.stream().filter(each -> !storageUnits.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedStorageUnits.isEmpty(), () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedStorageUnits));
    }
    
    private void checkInUsed(final UnregisterStorageUnitStatement sqlStatement) {
        Map<String, Collection<Class<? extends ShardingSphereRule>>> inUsedStorageUnits = database.getRuleMetaData().getInUsedStorageUnitNameAndRulesMap();
        Collection<String> inUsedStorageUnitNames = inUsedStorageUnits.keySet();
        inUsedStorageUnitNames.retainAll(sqlStatement.getStorageUnitNames());
        if (inUsedStorageUnitNames.isEmpty()) {
            return;
        }
        Collection<Class<ShardingSphereRule>> ignoreShardingSphereRules = getIgnoreShardingSphereRules(sqlStatement);
        String firstResource = inUsedStorageUnitNames.iterator().next();
        ShardingSpherePreconditions.checkState(!ignoreShardingSphereRules.isEmpty(), () -> new StorageUnitInUsedException(firstResource, inUsedStorageUnits.get(firstResource)));
        checkInUsedIgnoreTables(new HashSet<>(inUsedStorageUnitNames), inUsedStorageUnits, ignoreShardingSphereRules);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<Class<ShardingSphereRule>> getIgnoreShardingSphereRules(final UnregisterStorageUnitStatement sqlStatement) {
        Collection<Class<ShardingSphereRule>> result = new LinkedList<>();
        for (StorageUnitDefinitionProcessor each : ShardingSphereServiceLoader.getServiceInstances(StorageUnitDefinitionProcessor.class)) {
            if (each.ignoreUsageCheckOnUnregister(sqlStatement)) {
                result.add(each.getRuleClass());
            }
        }
        return result;
    }
    
    private void checkInUsedIgnoreTables(final Collection<String> inUsedResourceNames, final Map<String, Collection<Class<? extends ShardingSphereRule>>> inUsedStorageUnits,
                                         final Collection<Class<ShardingSphereRule>> ignoreShardingSphereRules) {
        for (String each : inUsedResourceNames) {
            Collection<Class<? extends ShardingSphereRule>> inUsedRules = inUsedStorageUnits.get(each);
            ignoreShardingSphereRules.forEach(inUsedRules::remove);
            ShardingSpherePreconditions.checkState(inUsedRules.isEmpty(), () -> new StorageUnitInUsedException(each, inUsedRules));
        }
    }
    
    @Override
    public Class<UnregisterStorageUnitStatement> getType() {
        return UnregisterStorageUnitStatement.class;
    }
}
