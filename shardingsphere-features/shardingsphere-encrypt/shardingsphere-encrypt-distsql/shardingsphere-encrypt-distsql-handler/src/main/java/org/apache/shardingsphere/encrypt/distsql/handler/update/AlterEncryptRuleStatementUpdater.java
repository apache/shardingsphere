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
import org.apache.shardingsphere.encrypt.distsql.handler.converter.EncryptRuleStatementConverter;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.factory.EncryptAlgorithmFactory;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alter encrypt rule statement updater.
 */
public final class AlterEncryptRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterEncryptRuleStatement, EncryptRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) throws DistSQLException {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeAlteredRules(databaseName, sqlStatement, currentRuleConfig);
        checkToBeAlteredEncryptors(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final EncryptRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Encrypt", databaseName);
        }
    }
    
    private void checkToBeAlteredRules(final String databaseName, final AlterEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentEncryptTableNames = currentRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistEncryptTableNames = getToBeAlteredEncryptTableNames(sqlStatement).stream().filter(each -> !currentEncryptTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistEncryptTableNames.isEmpty()) {
            throw new RequiredRuleMissedException("Encrypt", databaseName, notExistEncryptTableNames);
        }
        checkDataType(sqlStatement);
    }
    
    private Collection<String> getToBeAlteredEncryptTableNames(final AlterEncryptRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(EncryptRuleSegment::getTableName).collect(Collectors.toList());
    }
    
    private void checkDataType(final AlterEncryptRuleStatement sqlStatement) throws DistSQLException {
        Collection<String> invalidRules = sqlStatement.getRules().stream()
                .map(each -> getInvalidColumns(each.getTableName(), each.getColumns())).flatMap(Collection::stream).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidRules.isEmpty(), () -> new InvalidRuleConfigurationException("encrypt", invalidRules, Collections.singleton("incomplete data type")));
    }
    
    private Collection<String> getInvalidColumns(final String tableName, final Collection<EncryptColumnSegment> columns) {
        return columns.stream().filter(each -> !each.isCorrectDataType()).map(each -> String.format("%s.%s", tableName, each.getName())).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredEncryptors(final AlterEncryptRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> encryptors = new LinkedHashSet<>();
        for (EncryptRuleSegment each : sqlStatement.getRules()) {
            encryptors.addAll(each.getColumns().stream().map(column -> column.getEncryptor().getName()).collect(Collectors.toSet()));
        }
        Collection<String> invalidEncryptors = encryptors.stream().filter(each -> !EncryptAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidEncryptors.isEmpty(), () -> new InvalidAlgorithmConfigurationException("encryptor", invalidEncryptors));
    }
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterEncryptRuleStatement sqlStatement) {
        return EncryptRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final EncryptRuleConfiguration toBeAlteredRuleConfig) {
        dropRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
    }
    
    private void dropRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final EncryptRuleConfiguration toBeAlteredRuleConfig) {
        for (EncryptTableRuleConfiguration each : toBeAlteredRuleConfig.getTables()) {
            Optional<EncryptTableRuleConfiguration> toBeRemovedTableRuleConfig = currentRuleConfig.getTables().stream().filter(tableRule -> tableRule.getName().equals(each.getName())).findAny();
            Preconditions.checkState(toBeRemovedTableRuleConfig.isPresent());
            currentRuleConfig.getTables().remove(toBeRemovedTableRuleConfig.get());
            toBeRemovedTableRuleConfig.get().getColumns().forEach(column -> currentRuleConfig.getEncryptors().remove(column.getEncryptorName()));
        }
    }
    
    private void addRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final EncryptRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getTables().addAll(toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getEncryptors().putAll(toBeAlteredRuleConfig.getEncryptors());
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getRuleConfigurationClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterEncryptRuleStatement.class.getName();
    }
}
