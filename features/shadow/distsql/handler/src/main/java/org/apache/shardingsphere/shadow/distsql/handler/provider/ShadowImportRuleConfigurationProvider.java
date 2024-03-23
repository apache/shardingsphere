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

package org.apache.shardingsphere.shadow.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.engine.update.ral.rule.spi.database.ImportRuleConfigurationProvider;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Shadow import rule configuration provider.
 */
public final class ShadowImportRuleConfigurationProvider implements ImportRuleConfigurationProvider<ShadowRuleConfiguration> {
    
    @Override
    public void check(final ShardingSphereDatabase database, final ShadowRuleConfiguration ruleConfig) {
        if (null == database || null == ruleConfig) {
            return;
        }
        String databaseName = database.getName();
        checkDataSources(databaseName, database, ruleConfig);
        checkTables(ruleConfig, databaseName);
        checkShadowAlgorithms(ruleConfig);
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
        Collection<String> result = new LinkedHashSet<>();
        for (DataSourceMapperRuleAttribute each : database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)) {
            result.addAll(each.getDataSourceMapper().keySet());
        }
        return result;
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
    
    @Override
    public Class<ShadowRuleConfiguration> getType() {
        return ShadowRuleConfiguration.class;
    }
}
