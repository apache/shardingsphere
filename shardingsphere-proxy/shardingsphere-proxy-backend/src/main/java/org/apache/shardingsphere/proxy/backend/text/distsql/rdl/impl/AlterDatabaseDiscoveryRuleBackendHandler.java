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
import org.apache.shardingsphere.dbdiscovery.common.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.common.yaml.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DatabaseDiscoveryRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidDatabaseDiscoveryTypesException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter database discovery rule backend handler.
 */
public final class AlterDatabaseDiscoveryRuleBackendHandler extends SchemaRequiredBackendHandler<AlterDatabaseDiscoveryRuleStatement> {

    static {
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }

    public AlterDatabaseDiscoveryRuleBackendHandler(final AlterDatabaseDiscoveryRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterDatabaseDiscoveryRuleStatement statement) {
        Collection<String> alteredRuleNames = getAlteredRuleNames(statement);
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = getDatabaseDiscoveryRuleConfiguration(schemaName, alteredRuleNames);
        check(schemaName, statement, databaseDiscoveryRuleConfiguration, alteredRuleNames);
        YamlDatabaseDiscoveryRuleConfiguration yamlDatabaseDiscoveryRuleConfiguration = alter(databaseDiscoveryRuleConfiguration, statement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(yamlDatabaseDiscoveryRuleConfiguration));
        post(schemaName, rules);
        return new UpdateResponseHeader(statement);
    }

    private Collection<String> getAlteredRuleNames(final AlterDatabaseDiscoveryRuleStatement statement) {
        return statement.getDatabaseDiscoveryRules().stream().map(DatabaseDiscoveryRuleSegment::getName).collect(Collectors.toList());
    }

    private DatabaseDiscoveryRuleConfiguration getDatabaseDiscoveryRuleConfiguration(final String schemaName, final Collection<String> alteredRuleNames) {
        Optional<DatabaseDiscoveryRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof DatabaseDiscoveryRuleConfiguration).map(each -> (DatabaseDiscoveryRuleConfiguration) each).findFirst();
        if (!ruleConfig.isPresent()) {
            throw new DatabaseDiscoveryRulesNotExistedException(schemaName, alteredRuleNames);
        }
        return ruleConfig.get();
    }
    
    private void check(final String schemaName, final AlterDatabaseDiscoveryRuleStatement sqlStatement,
                       final DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration, final Collection<String> alteredRuleNames) {
        checkAlteredRules(schemaName, databaseDiscoveryRuleConfiguration, alteredRuleNames);
        checkResources(schemaName, sqlStatement);
        checkDiscoveryType(sqlStatement);
    }

    private void checkAlteredRules(final String schemaName, final DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration, final Collection<String> alteredRuleNames) {
        Set<String> existRuleNames = databaseDiscoveryRuleConfiguration.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistRuleNames = alteredRuleNames.stream()
                .filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistRuleNames.isEmpty()) {
            throw new DatabaseDiscoveryRulesNotExistedException(schemaName, notExistRuleNames);
        }
    }

    private void checkResources(final String schemaName, final AlterDatabaseDiscoveryRuleStatement statement) {
        Collection<String> resources = new LinkedHashSet<>();
        statement.getDatabaseDiscoveryRules().forEach(each -> resources.addAll(each.getDataSources()));
        Collection<String> notExistResources = resources.stream().filter(each -> !this.isValidResource(schemaName, each)).collect(Collectors.toList());
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }

    private void checkDiscoveryType(final AlterDatabaseDiscoveryRuleStatement statement) {
        Collection<String> invalidDiscoveryTypes = statement.getDatabaseDiscoveryRules().stream().map(each -> each.getDiscoveryTypeName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidDiscoveryTypes.isEmpty()) {
            throw new InvalidDatabaseDiscoveryTypesException(invalidDiscoveryTypes);
        }
    }

    private boolean isValidResource(final String schemaName, final String resourceName) {
        return Objects.nonNull(ProxyContext.getInstance().getMetaData(schemaName).getResource())
                && ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources().containsKey(resourceName);
    }

    private YamlDatabaseDiscoveryRuleConfiguration alter(final DatabaseDiscoveryRuleConfiguration ruleConfig, final AlterDatabaseDiscoveryRuleStatement statement) {
        YamlDatabaseDiscoveryRuleConfiguration alterYamlDatabaseDiscoveryRuleConfiguration = DatabaseDiscoveryRuleStatementConverter.convert(statement.getDatabaseDiscoveryRules());
        YamlDatabaseDiscoveryRuleConfiguration result = new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singletonList(ruleConfig)).stream()
                .map(each -> (YamlDatabaseDiscoveryRuleConfiguration) each).findFirst().get();
        alterYamlDatabaseDiscoveryRuleConfiguration.getDataSources().keySet()
                .forEach(each -> result.getDiscoveryTypes().remove(result.getDataSources().get(each).getDiscoveryTypeName()));
        result.getDataSources().putAll(alterYamlDatabaseDiscoveryRuleConfiguration.getDataSources());
        result.getDiscoveryTypes().putAll(alterYamlDatabaseDiscoveryRuleConfiguration.getDiscoveryTypes());
        return result;
    }

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
