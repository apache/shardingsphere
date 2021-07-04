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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.distsql.update.RDLCreateUpdater;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create database discovery rule statement updater.
 */
public final class CreateDatabaseDiscoveryRuleStatementUpdater implements RDLCreateUpdater<CreateDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, 
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws DistSQLException {
        checkDuplicateRuleNames(schemaName, sqlStatement, currentRuleConfig);
        checkToBeCreatedResources(schemaName, sqlStatement, resource);
        checkToBeCreatedDiscoverTypes(sqlStatement);
    }
    
    private void checkDuplicateRuleNames(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, 
                                         final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DuplicateRuleException {
        if (null != currentRuleConfig) {
            Collection<String> existRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toList());
            Collection<String> duplicateRuleNames = sqlStatement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getName).filter(existRuleNames::contains).collect(Collectors.toSet());
            duplicateRuleNames.addAll(getToBeCreatedDuplicateRuleNames(sqlStatement));
            if (!duplicateRuleNames.isEmpty()) {
                throw new DuplicateRuleException("database discovery", schemaName, duplicateRuleNames);
            }
        }
    }
    
    private Collection<String> getToBeCreatedDuplicateRuleNames(final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream()
                .collect(Collectors.toMap(DatabaseDiscoveryRuleSegment::getName, e -> 1, Integer::sum))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    private void checkToBeCreatedResources(final String schemaName, 
                                           final CreateDatabaseDiscoveryRuleStatement sqlStatement, final ShardingSphereResource resource) throws RequiredResourceMissedException {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> resources.addAll(each.getDataSources()));
        Collection<String> notExistResources = resource.getNotExistedResources(resources);
        if (!notExistResources.isEmpty()) {
            throw new RequiredResourceMissedException(schemaName, notExistResources);
        }
    }
    
    private void checkToBeCreatedDiscoverTypes(final CreateDatabaseDiscoveryRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> notExistedDiscoveryTypes = sqlStatement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getDiscoveryTypeName).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedDiscoveryTypes.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("database discover", notExistedDiscoveryTypes);
        }
    }
    
    @Override
    public DatabaseDiscoveryRuleConfiguration buildToBeCreatedRuleConfiguration(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        return DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDataSources().addAll(toBeCreatedRuleConfig.getDataSources());
            currentRuleConfig.getDiscoveryTypes().putAll(toBeCreatedRuleConfig.getDiscoveryTypes());
        }
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateDatabaseDiscoveryRuleStatement.class.getCanonicalName();
    }
}
