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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.handler.converter.EncryptRuleStatementConverter;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.factory.EncryptAlgorithmFactory;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Create encrypt rule statement updater.
 */
public final class CreateEncryptRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateEncryptRuleStatement, EncryptRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkDuplicateRuleNames(database.getName(), sqlStatement, currentRuleConfig);
        checkDataType(sqlStatement);
        checkToBeCreatedEncryptors(sqlStatement);
        // TODO check resource
    }
    
    private void checkDuplicateRuleNames(final String databaseName, final CreateEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) throws DuplicateRuleException {
        if (null != currentRuleConfig) {
            Collection<String> currentRuleNames = currentRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
            Collection<String> toBeCreatedDuplicateRuleNames = sqlStatement.getRules().stream().map(EncryptRuleSegment::getTableName).filter(currentRuleNames::contains).collect(Collectors.toList());
            if (!toBeCreatedDuplicateRuleNames.isEmpty()) {
                throw new DuplicateRuleException("encrypt", databaseName, toBeCreatedDuplicateRuleNames);
            }
        }
    }
    
    private void checkDataType(final CreateEncryptRuleStatement sqlStatement) throws DistSQLException {
        Collection<String> invalidRules = sqlStatement.getRules().stream()
                .map(each -> getInvalidColumns(each.getTableName(), each.getColumns())).flatMap(Collection::stream).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidRules.isEmpty(), () -> new InvalidRuleConfigurationException("encrypt", invalidRules, Collections.singleton("incomplete data type")));
    }
    
    private Collection<String> getInvalidColumns(final String tableName, final Collection<EncryptColumnSegment> columns) {
        return columns.stream().filter(each -> !each.isCorrectDataType()).map(each -> String.format("%s.%s", tableName, each.getName())).collect(Collectors.toList());
    }
    
    private void checkToBeCreatedEncryptors(final CreateEncryptRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> encryptors = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> encryptors.addAll(each.getColumns().stream().map(column -> column.getEncryptor().getName()).collect(Collectors.toSet())));
        Collection<String> notExistedEncryptors = encryptors.stream().filter(each -> !EncryptAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedEncryptors.isEmpty(), () -> new InvalidAlgorithmConfigurationException("encryptor", notExistedEncryptors));
    }
    
    @Override
    public EncryptRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateEncryptRuleStatement sqlStatement) {
        return EncryptRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final EncryptRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getTables().addAll(toBeCreatedRuleConfig.getTables());
            currentRuleConfig.getEncryptors().putAll(toBeCreatedRuleConfig.getEncryptors());
        }
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getRuleConfigurationClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateEncryptRuleStatement.class.getName();
    }
}
