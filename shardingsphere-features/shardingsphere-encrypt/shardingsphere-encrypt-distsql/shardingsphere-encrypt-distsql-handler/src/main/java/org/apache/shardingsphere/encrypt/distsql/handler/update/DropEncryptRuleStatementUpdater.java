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

package org.apache.shardingsphere.encrypt.distsql.handler.update;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop encrypt rule statement updater.
 */
public final class DropEncryptRuleStatementUpdater implements RuleDefinitionDropUpdater<DropEncryptRuleStatement, EncryptRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropEncryptRuleStatement sqlStatement, 
                                  final EncryptRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeDroppedEncryptTableNames(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final EncryptRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Encrypt", schemaName);
        }
    }
    
    private void checkToBeDroppedEncryptTableNames(final String schemaName, final DropEncryptRuleStatement sqlStatement, 
                                                   final EncryptRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        Collection<String> currentEncryptTableNames = currentRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedTableNames = sqlStatement.getTables().stream().filter(each -> !currentEncryptTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new RequiredRuleMissedException("Encrypt", schemaName, notExistedTableNames);
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getTables()) {
            dropRule(currentRuleConfig, each);
        }
        return currentRuleConfig.getTables().isEmpty();
    }
    
    private void dropRule(final EncryptRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<EncryptTableRuleConfiguration> encryptTableRuleConfig = currentRuleConfig.getTables().stream().filter(tableRule -> tableRule.getName().equals(ruleName)).findAny();
        Preconditions.checkState(encryptTableRuleConfig.isPresent());
        currentRuleConfig.getTables().remove(encryptTableRuleConfig.get());
        encryptTableRuleConfig.get().getColumns().stream().filter(column -> !isEncryptorInUse(currentRuleConfig, column.getEncryptorName()))
                .forEach(column -> currentRuleConfig.getEncryptors().remove(column.getEncryptorName()));
    }
    
    private boolean isEncryptorInUse(final EncryptRuleConfiguration currentRuleConfig, final String toBeDroppedEncryptorName) {
        for (EncryptTableRuleConfiguration each : currentRuleConfig.getTables()) {
            if (each.getColumns().stream().filter(column -> column.getEncryptorName().equals(toBeDroppedEncryptorName)).findAny().isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getRuleConfigurationClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropEncryptRuleStatement.class.getCanonicalName();
    }
}
