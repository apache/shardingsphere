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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.handler.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Create database discovery rule statement updater.
 */
public final class CreateDatabaseDiscoveryRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkDuplicatedRuleNames(databaseName, sqlStatement, currentRuleConfig);
        checkDataSources(databaseName, sqlStatement, database.getResourceMetaData());
        checkDiscoverTypeAndHeartbeat(sqlStatement);
    }
    
    @Override
    public DatabaseDiscoveryRuleConfiguration buildToBeCreatedRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<AbstractDatabaseDiscoverySegment> segments = sqlStatement.getRules();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement, currentRuleConfig);
            segments.removeIf(each -> duplicatedRuleNames.contains(each.getName()));
        }
        return DatabaseDiscoveryRuleStatementConverter.convert(segments);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getDataSources().addAll(toBeCreatedRuleConfig.getDataSources());
        currentRuleConfig.getDiscoveryTypes().putAll(toBeCreatedRuleConfig.getDiscoveryTypes());
        currentRuleConfig.getDiscoveryHeartbeats().putAll(toBeCreatedRuleConfig.getDiscoveryHeartbeats());
    }
    
    private void checkDuplicatedRuleNames(final String databaseName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        checkDuplicatedRuleNamesWithSelf(databaseName, sqlStatement);
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicatedRuleNamesWithRuleConfiguration(databaseName, sqlStatement, currentRuleConfig);
        }
    }
    
    private void checkDuplicatedRuleNamesWithSelf(final String databaseName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> duplicatedRuleNames = sqlStatement.getRules().stream().collect(Collectors.toMap(AbstractDatabaseDiscoverySegment::getName, each -> 1, Integer::sum))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("database discovery", databaseName, duplicatedRuleNames));
    }
    
    private void checkDuplicatedRuleNamesWithRuleConfiguration(final String databaseName, final CreateDatabaseDiscoveryRuleStatement sqlStatement,
                                                               final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement, currentRuleConfig);
        ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("database discovery", databaseName, duplicatedRuleNames));
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = new LinkedHashSet<>();
        if (null != currentRuleConfig) {
            currentRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName).collect(Collectors.toSet());
        }
        return sqlStatement.getRules().stream().map(AbstractDatabaseDiscoverySegment::getName).filter(currentRuleNames::contains).collect(Collectors.toSet());
    }
    
    private void checkDataSources(final String databaseName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final ShardingSphereResourceMetaData resourceMetaData) {
        Collection<String> dataSources = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> dataSources.addAll(each.getDataSources()));
        Collection<String> notExistedDataSources = resourceMetaData.getNotExistedDataSources(dataSources);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedDataSources));
    }
    
    private void checkDiscoverTypeAndHeartbeat(final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        Map<String, List<AbstractDatabaseDiscoverySegment>> segmentMap = sqlStatement.getRules().stream().collect(Collectors.groupingBy(each -> each.getClass().getSimpleName()));
        Collection<String> invalidInput = segmentMap.getOrDefault(DatabaseDiscoveryDefinitionSegment.class.getSimpleName(), Collections.emptyList()).stream()
                .map(each -> ((DatabaseDiscoveryDefinitionSegment) each).getDiscoveryType().getName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryProviderAlgorithm.class, each).isPresent()).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidInput.isEmpty(), () -> new InvalidAlgorithmConfigurationException("database discovery", invalidInput));
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateDatabaseDiscoveryRuleStatement.class.getName();
    }
}
