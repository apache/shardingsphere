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

package org.apache.shardingsphere.encrypt.distsql.update;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.exception.EncryptRuleNotExistedException;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.infra.distsql.update.RDLDropUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop encrypt rule statement updater.
 */
public final class DropEncryptRuleStatementUpdater implements RDLDropUpdater<DropEncryptRuleStatement, EncryptRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkToBeDroppedEncryptTableNames(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DropEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            throw new EncryptRuleNotExistedException(schemaName, sqlStatement.getTables());
        }
    }
    
    private void checkToBeDroppedEncryptTableNames(final String schemaName, final DropEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        Collection<String> currentEncryptTableNames = currentRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedTableNames = sqlStatement.getTables().stream().filter(each -> !currentEncryptTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistedTableNames.isEmpty()) {
            throw new EncryptRuleNotExistedException(schemaName, notExistedTableNames);
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final String schemaName, final DropEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getTables()) {
            dropRule(currentRuleConfig, each);
        }
        return currentRuleConfig.getTables().isEmpty();
    }
    
    private void dropRule(final EncryptRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<EncryptTableRuleConfiguration> encryptTableRuleConfig = currentRuleConfig.getTables().stream().filter(tableRule -> tableRule.getName().equals(ruleName)).findAny();
        Preconditions.checkState(encryptTableRuleConfig.isPresent());
        currentRuleConfig.getTables().remove(encryptTableRuleConfig.get());
        encryptTableRuleConfig.get().getColumns().forEach(column -> currentRuleConfig.getEncryptors().remove(column.getEncryptorName()));
    }
    
    @Override
    public String getType() {
        return DropEncryptRuleStatement.class.getCanonicalName();
    }
}
