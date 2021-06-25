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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.updater;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.infra.distsql.RDLUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateRuleNamesException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidDatabaseDiscoveryTypesException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create database discovery rule statement updater.
 */
public final class CreateDatabaseDiscoveryRuleStatementUpdater implements RDLUpdater<CreateDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, 
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkDuplicateRuleNames(schemaName, sqlStatement, currentRuleConfig);
        checkResources(schemaName, sqlStatement, resource);
        checkDiscoverTypes(sqlStatement);
    }
    
    private void checkDuplicateRuleNames(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        if (null != currentRuleConfig) {
            Collection<String> existRuleNames = getRuleNames(currentRuleConfig);
            Collection<String> duplicateRuleNames = sqlStatement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getName).filter(existRuleNames::contains).collect(Collectors.toSet());
            duplicateRuleNames.addAll(getDuplicateRuleNames(sqlStatement));
            if (!duplicateRuleNames.isEmpty()) {
                throw new DuplicateRuleNamesException(schemaName, duplicateRuleNames);
            }
        }
    }
    
    private Collection<String> getDuplicateRuleNames(final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream()
                .collect(Collectors.toMap(DatabaseDiscoveryRuleSegment::getName, e -> 1, Integer::sum))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    private void checkResources(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final ShardingSphereResource resource) {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> resources.addAll(each.getDataSources()));
        Collection<String> notExistResources = resource.getNotExistedResources(resources);
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private void checkDiscoverTypes(final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> invalidDiscoveryTypes = sqlStatement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getDiscoveryTypeName).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!invalidDiscoveryTypes.isEmpty()) {
            throw new InvalidDatabaseDiscoveryTypesException(invalidDiscoveryTypes);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        YamlDatabaseDiscoveryRuleConfiguration yamlDatabaseDiscoveryRuleConfig = DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getRules());
        DatabaseDiscoveryRuleConfiguration createdDatabaseDiscoveryRuleConfig = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singleton(yamlDatabaseDiscoveryRuleConfig))
                .stream().filter(each -> each instanceof DatabaseDiscoveryRuleConfiguration).findAny().map(each -> (DatabaseDiscoveryRuleConfiguration) each).get();
        if (null == currentRuleConfig) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(createdDatabaseDiscoveryRuleConfig);
        } else {
            currentRuleConfig.getDataSources().addAll(createdDatabaseDiscoveryRuleConfig.getDataSources());
            currentRuleConfig.getDiscoveryTypes().putAll(createdDatabaseDiscoveryRuleConfig.getDiscoveryTypes());
        }
    }
    
    private Collection<String> getRuleNames(final DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration) {
        return databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toList());
    }
    
    @Override
    public String getType() {
        return CreateDatabaseDiscoveryRuleStatement.class.getCanonicalName();
    }
}
