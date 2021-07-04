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
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.distsql.update.RDLAlterUpdater;
import org.apache.shardingsphere.infra.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Alter encrypt rule statement updater.
 */
public final class AlterEncryptRuleStatementUpdater implements RDLAlterUpdater<AlterEncryptRuleStatement, EncryptRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final AlterEncryptRuleStatement sqlStatement, 
                                  final EncryptRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeAlteredRules(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredEncryptors(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final EncryptRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Encrypt", schemaName);
        }
    }
    
    private void checkToBeAlteredRules(final String schemaName, final AlterEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        Collection<String> currentEncryptTableNames = currentRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistEncryptTableNames = getToBeAlteredEncryptTableNames(sqlStatement).stream().filter(each -> !currentEncryptTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistEncryptTableNames.isEmpty()) {
            throw new RequiredRuleMissedException("Encrypt", schemaName, notExistEncryptTableNames);
        }
    }
    
    private Collection<String> getToBeAlteredEncryptTableNames(final AlterEncryptRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(EncryptRuleSegment::getTableName).collect(Collectors.toList());
    }
    
    private void checkToBeAlteredEncryptors(final AlterEncryptRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> encryptors = new LinkedHashSet<>();
        for (EncryptRuleSegment each : sqlStatement.getRules()) {
            encryptors.addAll(each.getColumns().stream().map(column -> column.getEncryptor().getName()).collect(Collectors.toSet()));
        }
        Collection<String> invalidEncryptors = encryptors.stream().filter(
            each -> !TypedSPIRegistry.findRegisteredService(EncryptAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!invalidEncryptors.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("encryptor", invalidEncryptors);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final AlterEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        dropRuleConfiguration(sqlStatement, currentRuleConfig);
        addRuleConfiguration(sqlStatement, currentRuleConfig);
    }
    
    private void dropRuleConfiguration(final AlterEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        for (String each : getToBeAlteredEncryptTableNames(sqlStatement)) {
            Optional<EncryptTableRuleConfiguration> toBeRemovedEncryptTableRuleConfig = currentRuleConfig.getTables().stream().filter(tableRule -> tableRule.getName().equals(each)).findAny();
            Preconditions.checkState(toBeRemovedEncryptTableRuleConfig.isPresent());
            currentRuleConfig.getTables().remove(toBeRemovedEncryptTableRuleConfig.get());
            toBeRemovedEncryptTableRuleConfig.get().getColumns().forEach(column -> currentRuleConfig.getEncryptors().remove(column.getEncryptorName()));
        }
    }
    
    private void addRuleConfiguration(final AlterEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        EncryptRuleConfiguration toBeAlteredRuleConfig = EncryptRuleStatementConverter.convert(sqlStatement.getRules());
        currentRuleConfig.getTables().addAll(toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getEncryptors().putAll(toBeAlteredRuleConfig.getEncryptors());
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getRuleConfigurationClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterEncryptRuleStatement.class.getCanonicalName();
    }
}
