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

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.EncryptRulesNotExistedException;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop encrypt rule backend handler.
 */
public final class DropEncryptRuleBackendHandler extends RDLBackendHandler<DropEncryptRuleStatement> {
    
    public DropEncryptRuleBackendHandler(final DropEncryptRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final DropEncryptRuleStatement sqlStatement) {
        Optional<EncryptRuleConfiguration> ruleConfig = getEncryptRuleConfiguration(schemaName);
        if (!ruleConfig.isPresent()) {
            throw new EncryptRulesNotExistedException(schemaName, sqlStatement.getTables());
        }
        check(schemaName, ruleConfig.get(), sqlStatement.getTables());
    }
    
    @Override
    public void doExecute(final String schemaName, final DropEncryptRuleStatement sqlStatement) {
        EncryptRuleConfiguration encryptRuleConfiguration = getEncryptRuleConfiguration(schemaName).get();
        sqlStatement.getTables().forEach(each -> {
            EncryptTableRuleConfiguration encryptTableRuleConfiguration = encryptRuleConfiguration.getTables()
                    .stream().filter(tableRule -> tableRule.getName().equals(each)).findAny().get();
            encryptRuleConfiguration.getTables().remove(encryptTableRuleConfiguration);
            encryptTableRuleConfiguration.getColumns().forEach(column -> encryptRuleConfiguration.getEncryptors().remove(column.getEncryptorName()));
        });
        if (encryptRuleConfiguration.getTables().isEmpty()) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().remove(encryptRuleConfiguration);
        }
    }
    
    private void check(final String schemaName, final EncryptRuleConfiguration ruleConfig, final Collection<String> droppedTables) {
        Collection<String> encryptTables = ruleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedTables = droppedTables.stream().filter(each -> !encryptTables.contains(each)).collect(Collectors.toList());
        if (!notExistedTables.isEmpty()) {
            throw new EncryptRulesNotExistedException(schemaName, notExistedTables);
        }
    }
}
