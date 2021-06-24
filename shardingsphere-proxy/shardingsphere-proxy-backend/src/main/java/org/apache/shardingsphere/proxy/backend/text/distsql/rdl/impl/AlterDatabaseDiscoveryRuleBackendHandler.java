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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.yaml.converter.DatabaseDiscoveryRuleStatementConverter;
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
    public void check(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        checkToBeAlteredRules(schemaName, getRuleConfiguration(schemaName, sqlStatement), sqlStatement);
        checkToBeAlteredResources(schemaName, sqlStatement);
        checkToBeAlteredDiscoveryType(sqlStatement);
    }
    
    private void checkToBeAlteredRules(final String schemaName, final DatabaseDiscoveryRuleConfiguration ruleConfig, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> currentRuleNames = ruleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistedRuleNames = getToBeAlteredRuleNames(sqlStatement).stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new DatabaseDiscoveryRulesNotExistedException(schemaName, notExistedRuleNames);
        }
    }
    
    private Collection<String> getToBeAlteredRuleNames(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getName).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredResources(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> notExistedResources = getNotExistedResources(schemaName, getToBeAlteredResourceNames(sqlStatement));
        if (!notExistedResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistedResources);
        }
    }
    
    private Collection<String> getToBeAlteredResourceNames(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> result = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> result.addAll(each.getDataSources()));
        return result;
    }
    
    private void checkToBeAlteredDiscoveryType(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Collection<String> notExistedDiscoveryTypes = getToBeAlteredDiscoveryTypeNames(sqlStatement).stream()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedDiscoveryTypes.isEmpty()) {
            throw new InvalidDatabaseDiscoveryTypesException(notExistedDiscoveryTypes);
        }
    }
    
    private Collection<String> getToBeAlteredDiscoveryTypeNames(final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(DatabaseDiscoveryRuleSegment::getDiscoveryTypeName).collect(Collectors.toSet());
    }
    
    private DatabaseDiscoveryRuleConfiguration getRuleConfiguration(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Optional<DatabaseDiscoveryRuleConfiguration> result = findCurrentRuleConfiguration(schemaName, DatabaseDiscoveryRuleConfiguration.class);
        if (!result.isPresent()) {
            throw new DatabaseDiscoveryRulesNotExistedException(schemaName, getToBeAlteredRuleNames(sqlStatement));
        }
        return result.get();
    }
    
    @Override
    public void doExecute(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        DatabaseDiscoveryRuleConfiguration currentRuleConfig = getCurrentRuleConfiguration(schemaName, DatabaseDiscoveryRuleConfiguration.class);
        dropRuleConfiguration(currentRuleConfig, sqlStatement);
        addRuleConfiguration(currentRuleConfig, sqlStatement);
    }
    
    private void dropRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        getToBeAlteredRuleNames(sqlStatement).forEach(each -> dropDataSourceRuleConfiguration(currentRuleConfig, each));
    }
    
    private void dropDataSourceRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String toBeDroppedRuleNames) {
        Optional<DatabaseDiscoveryDataSourceRuleConfiguration> toBeDroppedDataSourceRuleConfig
                = currentRuleConfig.getDataSources().stream().filter(each -> each.getName().equals(toBeDroppedRuleNames)).findAny();
        Preconditions.checkState(toBeDroppedDataSourceRuleConfig.isPresent());
        currentRuleConfig.getDataSources().remove(toBeDroppedDataSourceRuleConfig.get());
        currentRuleConfig.getDiscoveryTypes().remove(toBeDroppedDataSourceRuleConfig.get().getDiscoveryTypeName());
    }
    
    private void addRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final AlterDatabaseDiscoveryRuleStatement sqlStatement) {
        Optional<DatabaseDiscoveryRuleConfiguration> toBeAlteredRuleConfig = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singleton(DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getRules()))).stream()
                .map(each -> (DatabaseDiscoveryRuleConfiguration) each).findFirst();
        Preconditions.checkState(toBeAlteredRuleConfig.isPresent());
        currentRuleConfig.getDataSources().addAll(toBeAlteredRuleConfig.get().getDataSources());
        currentRuleConfig.getDiscoveryTypes().putAll(toBeAlteredRuleConfig.get().getDiscoveryTypes());
    }
}
