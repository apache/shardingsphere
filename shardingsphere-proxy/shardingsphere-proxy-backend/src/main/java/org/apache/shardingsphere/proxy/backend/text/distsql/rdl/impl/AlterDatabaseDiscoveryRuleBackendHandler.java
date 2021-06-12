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
import org.apache.shardingsphere.dbdiscovery.yaml.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.DatabaseDiscoveryRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidDatabaseDiscoveryTypesException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter database discovery rule backend handler.
 */
public final class AlterDatabaseDiscoveryRuleBackendHandler extends RDLBackendHandler<AlterDatabaseDiscoveryRuleStatement> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }
    
    public AlterDatabaseDiscoveryRuleBackendHandler(final AlterDatabaseDiscoveryRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Optional<DatabaseDiscoveryRuleConfiguration> ruleConfig = getDatabaseDiscoveryRuleConfiguration(schemaName);
        if (!ruleConfig.isPresent()) {
            throw new DatabaseDiscoveryRulesNotExistedException(schemaName, getAlteredRuleNames(sqlStatement));
        }
        check(schemaName, sqlStatement, ruleConfig.get());
    }
    
    @Override
    public void doExecute(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = getDatabaseDiscoveryRuleConfiguration(schemaName).get();
        DatabaseDiscoveryRuleConfiguration alteredDatabaseDiscoveryRuleConfiguration = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singletonList(DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getRules()))).stream()
                .map(each -> (DatabaseDiscoveryRuleConfiguration) each).findFirst().get();
        drop(sqlStatement, databaseDiscoveryRuleConfiguration);
        databaseDiscoveryRuleConfiguration.getDataSources().addAll(alteredDatabaseDiscoveryRuleConfiguration.getDataSources());
        databaseDiscoveryRuleConfiguration.getDiscoveryTypes().putAll(alteredDatabaseDiscoveryRuleConfiguration.getDiscoveryTypes());
    }
    
    private void drop(final AlterDatabaseDiscoveryRuleStatement sqlStatement,
                      final DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration) {
        getAlteredRuleNames(sqlStatement).forEach(each -> {
            DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfiguration = databaseDiscoveryRuleConfiguration.getDataSources()
                    .stream().filter(dataSource -> dataSource.getName().equals(each)).findAny().get();
            databaseDiscoveryRuleConfiguration.getDataSources().remove(databaseDiscoveryDataSourceRuleConfiguration);
            databaseDiscoveryRuleConfiguration.getDiscoveryTypes().remove(databaseDiscoveryDataSourceRuleConfiguration.getDiscoveryTypeName());
        });
    }
    
    private void check(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement,
                       final DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration) {
        checkAlteredRules(schemaName, databaseDiscoveryRuleConfiguration, sqlStatement);
        checkResources(schemaName, sqlStatement);
        checkDiscoveryType(sqlStatement);
    }
    
    private void checkAlteredRules(final String schemaName, final DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration,
                                   final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Set<String> existRuleNames = databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistRuleNames = getAlteredRuleNames(sqlStatement).stream()
                .filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistRuleNames.isEmpty()) {
            throw new DatabaseDiscoveryRulesNotExistedException(schemaName, notExistRuleNames);
        }
    }
    
    private void checkResources(final String schemaName, final AlterDatabaseDiscoveryRuleStatement statement) {
        Collection<String> resources = new LinkedHashSet<>();
        statement.getRules().forEach(each -> resources.addAll(each.getDataSources()));
        Collection<String> notExistResources = getInvalidResources(schemaName, resources);
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private void checkDiscoveryType(final AlterDatabaseDiscoveryRuleStatement statement) {
        Collection<String> invalidDiscoveryTypes = statement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getDiscoveryTypeName).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidDiscoveryTypes.isEmpty()) {
            throw new InvalidDatabaseDiscoveryTypesException(invalidDiscoveryTypes);
        }
    }
    
    private Collection<String> getAlteredRuleNames(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        return sqlStatement.getRules()
                .stream().map(DatabaseDiscoveryRuleSegment::getName).collect(Collectors.toList());
    }
}
