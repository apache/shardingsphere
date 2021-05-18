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
import org.apache.shardingsphere.dbdiscovery.common.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.common.yaml.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DatabaseDiscoveryRuleExistsException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidDatabaseDiscoveryTypesException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create database discovery rule backend handler.
 */
public final class CreateDatabaseDiscoveryRuleBackendHandler extends SchemaRequiredBackendHandler<CreateDatabaseDiscoveryRuleStatement> {

    public CreateDatabaseDiscoveryRuleBackendHandler(final CreateDatabaseDiscoveryRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        check(schemaName, sqlStatement);
        YamlDatabaseDiscoveryRuleConfiguration config = DatabaseDiscoveryRuleStatementConverter.convert(sqlStatement.getDatabaseDiscoveryRules());
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(config));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final CreateDatabaseDiscoveryRuleStatement sqlStatement) {
        if (ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream().anyMatch(each -> each instanceof DatabaseDiscoveryRuleConfiguration)) {
            throw new DatabaseDiscoveryRuleExistsException(schemaName);
        }
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getDatabaseDiscoveryRules().forEach(each -> resources.addAll(each.getDataSources()));
        Collection<String> notExistResources = resources.stream().filter(each -> !this.isValidResource(schemaName, each)).collect(Collectors.toList());
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(notExistResources);
        }
        Collection<String> invalidDiscoveryTypes = sqlStatement.getDatabaseDiscoveryRules().stream().map(each -> each.getDiscoveryTypeName()).distinct()
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

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
