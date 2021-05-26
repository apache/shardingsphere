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

import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.converter.EncryptRuleStatementConverter;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.EncryptRuleExistsException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidEncryptorsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create encrypt rule backend handler.
 */
public final class CreateEncryptRuleBackendHandler extends SchemaRequiredBackendHandler<CreateEncryptRuleStatement> {

    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }

    public CreateEncryptRuleBackendHandler(final CreateEncryptRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final CreateEncryptRuleStatement sqlStatement) {
        check(schemaName, sqlStatement);
        YamlEncryptRuleConfiguration config = EncryptRuleStatementConverter.convert(sqlStatement.getEncryptRules());
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(config));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final String schemaName, final CreateEncryptRuleStatement sqlStatement) {
        if (ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream().anyMatch(each -> each instanceof EncryptRuleConfiguration)) {
            throw new EncryptRuleExistsException(schemaName);
        }
        // TODO check resource
        Collection<String> encryptors = new LinkedHashSet<>();
        sqlStatement.getEncryptRules().stream().forEach(each -> encryptors.addAll(each.getColumns().stream()
                .map(column -> column.getEncryptor().getAlgorithmName()).collect(Collectors.toSet())));
        Collection<String> invalidEncryptors = encryptors.stream().filter(each -> !TypedSPIRegistry.findRegisteredService(EncryptAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidEncryptors.isEmpty()) {
            throw new InvalidEncryptorsException(invalidEncryptors);
        }
    }

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
