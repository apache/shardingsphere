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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker;

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Shadow rule configuration import checker.
 */
public final class ShadowRuleConfigurationImportChecker {
    
    /**
     * Check shadow rule configuration.
     *
     * @param database database
     * @param currentRuleConfig current rule configuration
     */
    public void check(final ShardingSphereDatabase database, final ShadowRuleConfiguration currentRuleConfig) {
        if (null == database || null == currentRuleConfig) {
            return;
        }
        String databaseName = database.getName();
        checkDataSources(databaseName, database, currentRuleConfig);
        checkTables(currentRuleConfig, databaseName);
        checkShadowAlgorithms(currentRuleConfig);
    }
    
    private void checkDataSources(final String databaseName, final ShardingSphereDatabase database, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> requiredResource = getRequiredResources(currentRuleConfig);
        Collection<String> notExistedResources = database.getResourceMetaData().getNotExistedDataSources(requiredResource);
        Collection<String> logicResources = getLogicDataSources(database);
        notExistedResources.removeIf(logicResources::contains);
        ShardingSpherePreconditions.checkState(notExistedResources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedResources));
    }
    
    private Collection<String> getRequiredResources(final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        currentRuleConfig.getDataSources().forEach(each -> {
            if (null != each.getShadowDataSourceName()) {
                result.add(each.getShadowDataSourceName());
            }
            if (null != each.getProductionDataSourceName()) {
                result.add(each.getProductionDataSourceName());
            }
        });
        return result;
    }
    
    private Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findRules(DataSourceContainedRule.class).stream()
                .map(each -> each.getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private void checkTables(final ShadowRuleConfiguration currentRuleConfig, final String databaseName) {
        Collection<String> tableNames = currentRuleConfig.getTables().keySet();
        Collection<String> duplicatedTables = tableNames.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedTables.isEmpty(), () -> new DuplicateRuleException("SHADOW", databaseName, duplicatedTables));
    }
    
    private void checkShadowAlgorithms(final ShadowRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getShadowAlgorithms().values().forEach(each -> TypedSPILoader.checkService(ShadowAlgorithm.class, each.getType(), each.getProps()));
    }
}
