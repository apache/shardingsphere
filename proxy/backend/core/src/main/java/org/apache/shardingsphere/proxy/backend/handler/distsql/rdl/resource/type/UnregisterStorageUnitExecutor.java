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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.type;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.StorageUnitInUsedException;
import org.apache.shardingsphere.distsql.handler.type.rdl.resource.ResourceDefinitionExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.rule.SingleRule;

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
public final class UnregisterStorageUnitExecutor implements ResourceDefinitionExecutor<UnregisterStorageUnitStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final UnregisterStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        checkSQLStatement(sqlStatement);
        try {
            contextManager.getInstanceContext().getModeContextManager().unregisterStorageUnits(database.getName(), sqlStatement.getStorageUnitNames());
        } catch (final SQLException | ShardingSphereServerException ex) {
            log.error("Unregister storage unit failed", ex);
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
    }
    
    private void checkSQLStatement(final UnregisterStorageUnitStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkExisted(sqlStatement.getStorageUnitNames());
        }
        checkInUsed(sqlStatement);
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
        if (!inUsedStorageUnitNames.isEmpty()) {
            Collection<Class<? extends ShardingSphereRule>> ignoreShardingSphereRules = getIgnoreShardingSphereRules(sqlStatement);
            if (!ignoreShardingSphereRules.isEmpty()) {
                checkInUsedIgnoreTables(new HashSet<>(inUsedStorageUnitNames), inUsedStorageUnits, ignoreShardingSphereRules);
            } else {
                String firstResource = inUsedStorageUnitNames.iterator().next();
                throw new StorageUnitInUsedException(firstResource, inUsedStorageUnits.get(firstResource));
            }
        }
    }
    
    private Collection<Class<? extends ShardingSphereRule>> getIgnoreShardingSphereRules(final UnregisterStorageUnitStatement sqlStatement) {
        Collection<Class<? extends ShardingSphereRule>> result = new LinkedList<>();
        if (sqlStatement.isIgnoreSingleTables()) {
            result.add(SingleRule.class);
        }
        if (sqlStatement.isIgnoreBroadcastTables()) {
            result.add(BroadcastRule.class);
        }
        return result;
    }
    
    private void checkInUsedIgnoreTables(final Collection<String> inUsedResourceNames,
                                         final Map<String, Collection<Class<? extends ShardingSphereRule>>> inUsedStorageUnits,
                                         final Collection<Class<? extends ShardingSphereRule>> ignoreShardingSphereRules) {
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
