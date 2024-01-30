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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.engine.RuleDefinitionExecuteEngine;
import org.apache.shardingsphere.distsql.handler.type.rdl.rule.spi.database.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.handler.converter.EncryptRuleStatementConverter;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Create encrypt rule executor.
 */
public final class CreateEncryptRuleExecutor implements DatabaseRuleCreateExecutor<CreateEncryptRuleStatement, EncryptRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicateRuleNames(database.getName(), sqlStatement, currentRuleConfig);
        }
        checkColumnNames(sqlStatement);
        checkAlgorithmTypes(sqlStatement);
        checkToBeCreatedEncryptors(sqlStatement);
        RuleDefinitionExecuteEngine.checkDataSources(database);
    }
    
    private void checkAlgorithmTypes(final CreateEncryptRuleStatement sqlStatement) {
        sqlStatement.getRules().stream().flatMap(each -> each.getColumns().stream()).forEach(each -> {
            checkStandardAlgorithmType(each.getCipher());
            checkLikeAlgorithmType(each.getLikeQuery());
            checkAssistedAlgorithmType(each.getAssistedQuery());
        });
    }
    
    private void checkStandardAlgorithmType(final EncryptColumnItemSegment itemSegment) {
        if (null == itemSegment || null == itemSegment.getEncryptor()) {
            return;
        }
        EncryptAlgorithm encryptAlgorithm = TypedSPILoader.getService(EncryptAlgorithm.class, itemSegment.getEncryptor().getName(), itemSegment.getEncryptor().getProps());
        ShardingSpherePreconditions.checkState(encryptAlgorithm.getMetaData().isSupportDecrypt(), () -> new InvalidAlgorithmConfigurationException("standard encrypt", encryptAlgorithm.getType()));
    }
    
    private void checkLikeAlgorithmType(final EncryptColumnItemSegment itemSegment) {
        if (null == itemSegment || null == itemSegment.getEncryptor()) {
            return;
        }
        EncryptAlgorithm encryptAlgorithm = TypedSPILoader.getService(EncryptAlgorithm.class, itemSegment.getEncryptor().getName(), itemSegment.getEncryptor().getProps());
        ShardingSpherePreconditions.checkState(encryptAlgorithm.getMetaData().isSupportLike(), () -> new InvalidAlgorithmConfigurationException("like encrypt", encryptAlgorithm.getType()));
    }
    
    private void checkAssistedAlgorithmType(final EncryptColumnItemSegment itemSegment) {
        if (null == itemSegment || null == itemSegment.getEncryptor()) {
            return;
        }
        EncryptAlgorithm encryptAlgorithm = TypedSPILoader.getService(EncryptAlgorithm.class, itemSegment.getEncryptor().getName(), itemSegment.getEncryptor().getProps());
        ShardingSpherePreconditions.checkState(encryptAlgorithm.getMetaData().isSupportEquivalentFilter(),
                () -> new InvalidAlgorithmConfigurationException("assisted encrypt", encryptAlgorithm.getType()));
    }
    
    private void checkDuplicateRuleNames(final String databaseName, final CreateEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement, currentRuleConfig);
        ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("encrypt", databaseName, duplicatedRuleNames));
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateEncryptRuleStatement sqlStatement, final EncryptRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = new LinkedHashSet<>();
        if (null != currentRuleConfig) {
            currentRuleNames = currentRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toSet());
        }
        return sqlStatement.getRules().stream().map(EncryptRuleSegment::getTableName).filter(currentRuleNames::contains).collect(Collectors.toSet());
    }
    
    private void checkColumnNames(final CreateEncryptRuleStatement sqlStatement) {
        for (EncryptRuleSegment each : sqlStatement.getRules()) {
            ShardingSpherePreconditions.checkState(isColumnNameNotConflicts(each),
                    () -> new InvalidRuleConfigurationException("encrypt", "assisted query column or like query column conflicts with logic column"));
        }
    }
    
    private boolean isColumnNameNotConflicts(final EncryptRuleSegment rule) {
        return rule.getColumns().stream().noneMatch(each -> null != each.getLikeQuery() && each.getName().equals(each.getLikeQuery().getName())
                || null != each.getAssistedQuery() && each.getName().equals(each.getAssistedQuery().getName()));
    }
    
    private void checkToBeCreatedEncryptors(final CreateEncryptRuleStatement sqlStatement) {
        Collection<AlgorithmSegment> encryptors = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> each.getColumns().forEach(column -> {
            encryptors.add(column.getCipher().getEncryptor());
            if (null != column.getAssistedQuery()) {
                encryptors.add(column.getAssistedQuery().getEncryptor());
            }
            if (null != column.getLikeQuery()) {
                encryptors.add(column.getLikeQuery().getEncryptor());
            }
        }));
        encryptors.stream().filter(Objects::nonNull).forEach(each -> TypedSPILoader.checkService(EncryptAlgorithm.class, each.getName(), each.getProps()));
    }
    
    @Override
    public EncryptRuleConfiguration buildToBeCreatedRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final CreateEncryptRuleStatement sqlStatement) {
        Collection<EncryptRuleSegment> segments = sqlStatement.getRules();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement, currentRuleConfig);
            segments.removeIf(each -> duplicatedRuleNames.contains(each.getTableName()));
        }
        return EncryptRuleStatementConverter.convert(segments);
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final EncryptRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getTables().addAll(toBeCreatedRuleConfig.getTables());
        currentRuleConfig.getEncryptors().putAll(toBeCreatedRuleConfig.getEncryptors());
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getRuleConfigurationClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public Class<CreateEncryptRuleStatement> getType() {
        return CreateEncryptRuleStatement.class;
    }
}
