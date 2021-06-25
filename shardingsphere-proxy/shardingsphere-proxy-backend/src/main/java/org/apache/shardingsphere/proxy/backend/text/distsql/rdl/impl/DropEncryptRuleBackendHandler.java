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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.EncryptRuleNotExistedException;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop encrypt rule backend handler.
 */
public final class DropEncryptRuleBackendHandler extends RDLBackendHandler<DropEncryptRuleStatement, EncryptRuleConfiguration> {
    
    public DropEncryptRuleBackendHandler(final DropEncryptRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void check(final String schemaName, final DropEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            throw new EncryptRuleNotExistedException(schemaName, sqlStatement.getTables());
        }
        check(schemaName, sqlStatement.getTables(), currentRuleConfig);
    }
    
    private void check(final String schemaName, final Collection<String> droppedTables, final EncryptRuleConfiguration currentRuleConfig) {
        Collection<String> encryptTables = currentRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedTables = droppedTables.stream().filter(each -> !encryptTables.contains(each)).collect(Collectors.toList());
        if (!notExistedTables.isEmpty()) {
            throw new EncryptRuleNotExistedException(schemaName, notExistedTables);
        }
    }
    
    @Override
    public void doExecute(final String schemaName, final DropEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        sqlStatement.getTables().forEach(each -> {
            EncryptTableRuleConfiguration encryptTableRuleConfiguration = currentRuleConfig.getTables().stream().filter(tableRule -> tableRule.getName().equals(each)).findAny().get();
            currentRuleConfig.getTables().remove(encryptTableRuleConfiguration);
            encryptTableRuleConfiguration.getColumns().forEach(column -> currentRuleConfig.getEncryptors().remove(column.getEncryptorName()));
        });
        if (currentRuleConfig.getTables().isEmpty()) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().remove(currentRuleConfig);
        }
    }
}
