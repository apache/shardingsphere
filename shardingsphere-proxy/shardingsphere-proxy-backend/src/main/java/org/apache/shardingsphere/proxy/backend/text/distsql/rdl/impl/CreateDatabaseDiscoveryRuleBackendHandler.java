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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateRuleNamesException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidDatabaseDiscoveryTypesException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create database discovery rule backend handler.
 */
public final class CreateDatabaseDiscoveryRuleBackendHandler extends RDLBackendHandler<CreateDatabaseDiscoveryRuleStatement> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }
    
    public CreateDatabaseDiscoveryRuleBackendHandler(final CreateDatabaseDiscoveryRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        checkDuplicateRuleNames(schemaName, sqlStatement);
        checkResources(schemaName, sqlStatement);
        checkDiscoverTypes(sqlStatement);
    }
    
    @Override
    public void doExecute(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        YamlDatabaseDiscoveryRuleConfiguration yamlDatabaseDiscoveryRuleConfig = DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getRules());
        DatabaseDiscoveryRuleConfiguration createdDatabaseDiscoveryRuleConfiguration = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singleton(yamlDatabaseDiscoveryRuleConfig))
                .stream().filter(each -> each instanceof DatabaseDiscoveryRuleConfiguration).findAny().map(each -> (DatabaseDiscoveryRuleConfiguration) each).get();
        if (getDatabaseDiscoveryRuleConfiguration(schemaName).isPresent()) {
            DatabaseDiscoveryRuleConfiguration existDatabaseDiscoveryRuleConfiguration = getDatabaseDiscoveryRuleConfiguration(schemaName).get();
            existDatabaseDiscoveryRuleConfiguration.getDataSources().addAll(createdDatabaseDiscoveryRuleConfiguration.getDataSources());
            existDatabaseDiscoveryRuleConfiguration.getDiscoveryTypes().putAll(createdDatabaseDiscoveryRuleConfiguration.getDiscoveryTypes());
        } else {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(createdDatabaseDiscoveryRuleConfiguration);
        }
    }
    
    private void checkDuplicateRuleNames(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        Optional<DatabaseDiscoveryRuleConfiguration> optional = getDatabaseDiscoveryRuleConfiguration(schemaName);
        if (optional.isPresent()) {
            Collection<String> existRuleNames = getRuleNames(optional.get());
            Collection<String> duplicateRuleNames = sqlStatement.getRules().stream()
                    .map(DatabaseDiscoveryRuleSegment::getName).filter(existRuleNames::contains).collect(Collectors.toSet());
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
    
    private void checkResources(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> resources.addAll(each.getDataSources()));
        Collection<String> notExistResources = getInvalidResources(schemaName, resources);
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private void checkDiscoverTypes(final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> invalidDiscoveryTypes = sqlStatement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getDiscoveryTypeName).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidDiscoveryTypes.isEmpty()) {
            throw new InvalidDatabaseDiscoveryTypesException(invalidDiscoveryTypes);
        }
    }
    
    private Collection<String> getRuleNames(final DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration) {
        return databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toList());
    }
}
