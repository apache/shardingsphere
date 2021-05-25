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

import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.converter.EncryptRuleStatementConverter;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.EncryptRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidEncryptorsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Alter encrypt rule backend handler.
 */
public final class AlterEncryptRuleBackendHandler extends SchemaRequiredBackendHandler<AlterEncryptRuleStatement> {

    public AlterEncryptRuleBackendHandler(final AlterEncryptRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public ResponseHeader execute(final String schemaName, final AlterEncryptRuleStatement sqlStatement) {
        Optional<EncryptRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof EncryptRuleConfiguration).map(each -> (EncryptRuleConfiguration) each).findFirst();
        if (!ruleConfig.isPresent()) {
            throw new EncryptRulesNotExistedException(schemaName, getAlteredTables(sqlStatement));
        }
        check(schemaName, sqlStatement, ruleConfig.get());
        YamlEncryptRuleConfiguration alterConfig = alter(ruleConfig.get(), sqlStatement);
        Collection<RuleConfiguration> rules = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(Collections.singleton(alterConfig));
        post(schemaName, rules);
        return new UpdateResponseHeader(sqlStatement);
    }

    private Collection<String> getAlteredTables(final AlterEncryptRuleStatement sqlStatement) {
        return sqlStatement.getEncryptRules().stream().map(EncryptRuleSegment::getTableName).collect(Collectors.toList());
    }

    private void check(final String schemaName, final AlterEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration encryptRuleConfiguration) {
        checkAlteredTables(schemaName, encryptRuleConfiguration, sqlStatement);
        checkEncryptors(sqlStatement);
    }

    private void checkAlteredTables(final String schemaName, final EncryptRuleConfiguration encryptRuleConfiguration,
                                    final AlterEncryptRuleStatement sqlStatement) {
        Collection<String> existTables = getExistTables(encryptRuleConfiguration);
        Collection<String> notExistTables = getAlteredTables(sqlStatement).stream()
                .filter(each -> !existTables.contains(each)).collect(Collectors.toList());
        if (!notExistTables.isEmpty()) {
            throw new EncryptRulesNotExistedException(schemaName, notExistTables);
        }
    }

    private Collection<String> getExistTables(final EncryptRuleConfiguration encryptRuleConfiguration) {
        return encryptRuleConfiguration.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
    }

    private void checkEncryptors(final AlterEncryptRuleStatement sqlStatement) {
        Collection<String> encryptors = new LinkedHashSet<>();
        sqlStatement.getEncryptRules().stream().forEach(each -> encryptors.addAll(each.getColumns().stream()
                .map(column -> column.getEncryptor().getAlgorithmName()).collect(Collectors.toSet())));
        Collection<String> invalidEncryptors = encryptors.stream().filter(each -> !TypedSPIRegistry.findRegisteredService(EncryptAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidEncryptors.isEmpty()) {
            throw new InvalidEncryptorsException(invalidEncryptors);
        }
    }
    
    private YamlEncryptRuleConfiguration alter(final EncryptRuleConfiguration encryptRuleConfiguration, final AlterEncryptRuleStatement sqlStatement) {
        YamlEncryptRuleConfiguration alterYamlEncryptRuleConfiguration
                = EncryptRuleStatementConverter.convert(sqlStatement.getEncryptRules());
        YamlEncryptRuleConfiguration result = new YamlRuleConfigurationSwapperEngine()
                .swapToYamlRuleConfigurations(Collections.singletonList(encryptRuleConfiguration)).stream()
                .map(each -> (YamlEncryptRuleConfiguration) each).findFirst().get();
        result.getTables().putAll(alterYamlEncryptRuleConfiguration.getTables());
        result.getEncryptors().putAll(alterYamlEncryptRuleConfiguration.getEncryptors());
        return result;
    }

    private void post(final String schemaName, final Collection<RuleConfiguration> rules) {
        ShardingSphereEventBus.getInstance().post(new RuleConfigurationsAlteredEvent(schemaName, rules));
    }
}
