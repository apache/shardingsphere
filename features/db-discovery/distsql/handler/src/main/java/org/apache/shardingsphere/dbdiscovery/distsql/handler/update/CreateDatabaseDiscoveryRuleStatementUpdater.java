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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryConstructionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.factory.DatabaseDiscoveryProviderAlgorithmFactory;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

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
    
    private static final String RULE_TYPE = "Database discovery";
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkDuplicateRuleNames(databaseName, sqlStatement, currentRuleConfig);
        checkResources(databaseName, sqlStatement, database.getResourceMetaData());
        checkDiscoverTypeAndHeartbeat(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkDuplicateRuleNames(final String databaseName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            return;
        }
        Collection<String> existRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName).collect(Collectors.toList());
        Collection<String> duplicateRuleNames = sqlStatement.getRules().stream().map(AbstractDatabaseDiscoverySegment::getName).filter(existRuleNames::contains).collect(Collectors.toSet());
        duplicateRuleNames.addAll(getToBeCreatedDuplicateRuleNames(sqlStatement));
        ShardingSpherePreconditions.checkState(duplicateRuleNames.isEmpty(), () -> new DuplicateRuleException(RULE_TYPE.toLowerCase(), databaseName, duplicateRuleNames));
    }
    
    private Collection<String> getToBeCreatedDuplicateRuleNames(final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().collect(Collectors.toMap(AbstractDatabaseDiscoverySegment::getName, each -> 1, Integer::sum))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private void checkResources(final String databaseName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final ShardingSphereResourceMetaData resourceMetaData) {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> resources.addAll(each.getDataSources()));
        Collection<String> notExistResources = resourceMetaData.getNotExistedResources(resources);
        ShardingSpherePreconditions.checkState(notExistResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistResources));
    }
    
    private void checkDiscoverTypeAndHeartbeat(final String databaseName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        Map<String, List<AbstractDatabaseDiscoverySegment>> segmentMap = sqlStatement.getRules().stream().collect(Collectors.groupingBy(each -> each.getClass().getSimpleName()));
        Collection<String> invalidInput = segmentMap.getOrDefault(DatabaseDiscoveryDefinitionSegment.class.getSimpleName(), Collections.emptyList()).stream()
                .map(each -> ((DatabaseDiscoveryDefinitionSegment) each).getDiscoveryType().getName()).distinct()
                .filter(each -> !DatabaseDiscoveryProviderAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidInput.isEmpty(), () -> new InvalidAlgorithmConfigurationException(RULE_TYPE.toLowerCase(), invalidInput));
        segmentMap.getOrDefault(DatabaseDiscoveryConstructionSegment.class.getSimpleName(), Collections.emptyList()).stream().map(each -> (DatabaseDiscoveryConstructionSegment) each)
                .forEach(each -> {
                    if (null == currentRuleConfig || !currentRuleConfig.getDiscoveryTypes().containsKey(each.getDiscoveryTypeName())) {
                        invalidInput.add(each.getDiscoveryTypeName());
                    }
                    if (null == currentRuleConfig || !currentRuleConfig.getDiscoveryHeartbeats().containsKey(each.getDiscoveryHeartbeatName())) {
                        invalidInput.add(each.getDiscoveryHeartbeatName());
                    }
                });
        ShardingSpherePreconditions.checkState(invalidInput.isEmpty(), () -> new MissingRequiredAlgorithmException(RULE_TYPE, databaseName, invalidInput));
    }
    
    @Override
    public DatabaseDiscoveryRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        return DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDataSources().addAll(toBeCreatedRuleConfig.getDataSources());
            currentRuleConfig.getDiscoveryTypes().putAll(toBeCreatedRuleConfig.getDiscoveryTypes());
            currentRuleConfig.getDiscoveryHeartbeats().putAll(toBeCreatedRuleConfig.getDiscoveryHeartbeats());
        }
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
