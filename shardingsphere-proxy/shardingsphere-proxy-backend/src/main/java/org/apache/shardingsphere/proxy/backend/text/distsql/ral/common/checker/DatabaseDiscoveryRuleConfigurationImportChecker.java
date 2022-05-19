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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.factory.DatabaseDiscoveryProviderAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereDatabaseMetaData;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Database discovery rule configuration import checker.
 */
public final class DatabaseDiscoveryRuleConfigurationImportChecker {
    
    private static final String DB_DISCOVERY = "Database discovery";
    
    /**
     * Check database discovery rule configuration.
     *
     * @param databaseMetaData database meta data
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    public void check(final ShardingSphereDatabaseMetaData databaseMetaData, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == databaseMetaData || null == currentRuleConfig) {
            return;
        }
        String databaseName = databaseMetaData.getDatabase().getName();
        checkResources(databaseName, databaseMetaData, currentRuleConfig);
        checkDiscoverTypeAndHeartbeat(databaseName, currentRuleConfig);
    }
    
    private void checkResources(final String databaseName, final ShardingSphereDatabaseMetaData databaseMetaData, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> requireResources = new LinkedHashSet<>();
        currentRuleConfig.getDataSources().forEach(each -> requireResources.addAll(each.getDataSourceNames()));
        Collection<String> notExistResources = databaseMetaData.getResource().getNotExistedResources(requireResources);
        DistSQLException.predictionThrow(notExistResources.isEmpty(), () -> new RequiredResourceMissedException(databaseName, notExistResources));
    }
    
    private void checkDiscoverTypeAndHeartbeat(final String databaseName, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> invalidInput = currentRuleConfig.getDiscoveryTypes().values().stream().map(ShardingSphereAlgorithmConfiguration::getType)
                .filter(each -> !DatabaseDiscoveryProviderAlgorithmFactory.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidInput.isEmpty(), () -> new InvalidAlgorithmConfigurationException(DB_DISCOVERY.toLowerCase(), invalidInput));
        currentRuleConfig.getDataSources().forEach(each -> {
            if (!currentRuleConfig.getDiscoveryTypes().containsKey(each.getDiscoveryTypeName())) {
                invalidInput.add(each.getDiscoveryTypeName());
            }
            if (!currentRuleConfig.getDiscoveryHeartbeats().containsKey(each.getDiscoveryHeartbeatName())) {
                invalidInput.add(each.getDiscoveryHeartbeatName());
            }
        });
        DistSQLException.predictionThrow(invalidInput.isEmpty(), () -> new RequiredAlgorithmMissedException(DB_DISCOVERY, databaseName, invalidInput));
    }
}
