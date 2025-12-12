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
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.InUsedStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.StorageUnitsOperateException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.resoure.UnregisterStorageUnitRuleAttribute;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Unregister storage unit executor.
 */
@Setter
public final class UnregisterStorageUnitExecutor implements DistSQLUpdateExecutor<UnregisterStorageUnitStatement>, DistSQLExecutorDatabaseAware {
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final UnregisterStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        if (!sqlStatement.isIfExists()) {
            checkExisted(sqlStatement.getStorageUnitNames());
        }
        checkInUsed(sqlStatement);
        try {
            contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().unregisterStorageUnits(database, sqlStatement.getStorageUnitNames());
        } catch (final ShardingSphereServerException ex) {
            throw new StorageUnitsOperateException("unregister", sqlStatement.getStorageUnitNames(), ex);
        }
    }
    
    private void checkExisted(final Collection<String> storageUnitNames) {
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        Collection<String> notExistedStorageUnits = storageUnitNames.stream().filter(each -> !storageUnits.containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedStorageUnits, () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedStorageUnits));
    }
    
    private void checkInUsed(final UnregisterStorageUnitStatement sqlStatement) {
        Map<String, Collection<Class<? extends ShardingSphereRule>>> inUsedStorageUnits = database.getRuleMetaData().getInUsedStorageUnitNameAndRulesMap();
        Collection<String> inUsedStorageUnitNames = inUsedStorageUnits.keySet();
        inUsedStorageUnitNames.retainAll(sqlStatement.getStorageUnitNames());
        if (inUsedStorageUnitNames.isEmpty()) {
            return;
        }
        Collection<Class<? extends ShardingSphereRule>> ignoreUsageCheckRules = getIgnoreUsageCheckRules(sqlStatement);
        String firstResource = inUsedStorageUnitNames.iterator().next();
        ShardingSpherePreconditions.checkNotEmpty(ignoreUsageCheckRules, () -> new InUsedStorageUnitException(firstResource, inUsedStorageUnits.get(firstResource)));
        checkInUsedIgnoreTables(new HashSet<>(inUsedStorageUnitNames), inUsedStorageUnits, ignoreUsageCheckRules);
    }
    
    private Collection<Class<? extends ShardingSphereRule>> getIgnoreUsageCheckRules(final UnregisterStorageUnitStatement sqlStatement) {
        Collection<Class<? extends ShardingSphereRule>> result = new LinkedList<>();
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            Optional<UnregisterStorageUnitRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(UnregisterStorageUnitRuleAttribute.class);
            if (ruleAttribute.isPresent() && ruleAttribute.get().ignoreUsageCheck(sqlStatement.isIgnoreSingleTables(), sqlStatement.isIgnoreBroadcastTables())) {
                result.add(each.getClass());
            }
        }
        return result;
    }
    
    private void checkInUsedIgnoreTables(final Collection<String> inUsedResourceNames, final Map<String, Collection<Class<? extends ShardingSphereRule>>> inUsedStorageUnits,
                                         final Collection<Class<? extends ShardingSphereRule>> ignoreShardingSphereRules) {
        for (String each : inUsedResourceNames) {
            Collection<Class<? extends ShardingSphereRule>> inUsedRules = inUsedStorageUnits.get(each);
            ignoreShardingSphereRules.forEach(inUsedRules::remove);
            ShardingSpherePreconditions.checkMustEmpty(inUsedRules, () -> new InUsedStorageUnitException(each, inUsedRules));
        }
    }
    
    @Override
    public Class<UnregisterStorageUnitStatement> getType() {
        return UnregisterStorageUnitStatement.class;
    }
}
