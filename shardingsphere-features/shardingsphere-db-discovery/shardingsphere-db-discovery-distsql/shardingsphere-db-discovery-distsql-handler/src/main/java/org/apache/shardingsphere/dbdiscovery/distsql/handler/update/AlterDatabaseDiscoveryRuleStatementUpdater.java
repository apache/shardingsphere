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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.handler.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryConstructionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.factory.DatabaseDiscoveryProviderAlgorithmFactory;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredAlgorithmMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alter database discovery rule statement updater.
 */
public final class AlterDatabaseDiscoveryRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final AlterDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeAlteredRules(databaseName, sqlStatement, currentRuleConfig);
        checkToBeAlteredResources(databaseName, sqlStatement, database.getResource());
        checkDiscoverTypeAndHeartbeat(sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig, () -> new RequiredRuleMissedException("database discovery", databaseName));
    }
    
    private void checkToBeAlteredRules(final String databaseName,
                                       final AlterDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName).collect(Collectors.toSet());
        Collection<String> notExistedRuleNames = getToBeAlteredRuleNames(sqlStatement).stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(notExistedRuleNames.isEmpty(), () -> new RequiredRuleMissedException("database discovery", databaseName, notExistedRuleNames));
    }
    
    private Collection<String> getToBeAlteredRuleNames(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(AbstractDatabaseDiscoverySegment::getName).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredResources(final String databaseName, final AlterDatabaseDiscoveryRuleStatement sqlStatement, final ShardingSphereResource resource) throws DistSQLException {
        Collection<String> notExistedResources = resource.getNotExistedResources(getToBeAlteredResourceNames(sqlStatement));
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), () -> new RequiredResourceMissedException(databaseName, notExistedResources));
    }
    
    private Collection<String> getToBeAlteredResourceNames(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> result = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> result.addAll(each.getDataSources()));
        return result;
    }
    
    private void checkDiscoverTypeAndHeartbeat(final AlterDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Map<String, List<AbstractDatabaseDiscoverySegment>> segmentMap = sqlStatement.getRules().stream().collect(Collectors.groupingBy(each -> each.getClass().getSimpleName()));
        Collection<String> invalidInput = segmentMap.getOrDefault(DatabaseDiscoveryDefinitionSegment.class.getSimpleName(), Collections.emptyList()).stream()
                .map(each -> ((DatabaseDiscoveryDefinitionSegment) each).getDiscoveryType().getName()).distinct()
                .filter(each -> !DatabaseDiscoveryProviderAlgorithmFactory.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidInput.isEmpty(), () -> new InvalidAlgorithmConfigurationException("database discovery", invalidInput));
        segmentMap.getOrDefault(DatabaseDiscoveryConstructionSegment.class.getSimpleName(), Collections.emptyList()).stream().map(each -> (DatabaseDiscoveryConstructionSegment) each)
                .forEach(each -> {
                    if (!currentRuleConfig.getDiscoveryTypes().containsKey(each.getDiscoveryTypeName())) {
                        invalidInput.add(each.getDiscoveryTypeName());
                    }
                    if (!currentRuleConfig.getDiscoveryHeartbeats().containsKey(each.getDiscoveryHeartbeatName())) {
                        invalidInput.add(each.getDiscoveryHeartbeatName());
                    }
                });
        DistSQLException.predictionThrow(invalidInput.isEmpty(), () -> new RequiredAlgorithmMissedException("database discovery", invalidInput));
    }
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        return DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeAlteredRuleConfig) {
        dropRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        // TODO DistSQL update rule config need to sync scheduler module cron.
    }
    
    private void dropRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeAlteredRuleConfig) {
        for (DatabaseDiscoveryDataSourceRuleConfiguration each : toBeAlteredRuleConfig.getDataSources()) {
            dropDataSourceRuleConfiguration(currentRuleConfig, each.getGroupName());
        }
    }
    
    private void dropDataSourceRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String toBeDroppedRuleNames) {
        Optional<DatabaseDiscoveryDataSourceRuleConfiguration> toBeDroppedDataSourceRuleConfig = currentRuleConfig.getDataSources().stream()
                .filter(each -> each.getGroupName().equals(toBeDroppedRuleNames)).findAny();
        Preconditions.checkState(toBeDroppedDataSourceRuleConfig.isPresent());
        currentRuleConfig.getDataSources().remove(toBeDroppedDataSourceRuleConfig.get());
    }
    
    private void addRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getDataSources().addAll(toBeAlteredRuleConfig.getDataSources());
        currentRuleConfig.getDiscoveryTypes().putAll(toBeAlteredRuleConfig.getDiscoveryTypes());
        currentRuleConfig.getDiscoveryHeartbeats().putAll(toBeAlteredRuleConfig.getDiscoveryHeartbeats());
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterDatabaseDiscoveryRuleStatement.class.getName();
    }
}
