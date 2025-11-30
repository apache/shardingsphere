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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.handler.converter.EncryptRuleStatementConverter;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Create encrypt rule executor.
 */
@Setter
public final class CreateEncryptRuleExecutor implements DatabaseRuleCreateExecutor<CreateEncryptRuleStatement, EncryptRule, EncryptRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private EncryptRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateEncryptRuleStatement sqlStatement) {
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicateRuleNames(sqlStatement);
        }
        checkColumnNames(sqlStatement);
        checkAlgorithmTypes(sqlStatement);
        checkToBeCreatedEncryptors(sqlStatement);
        checkDataSources();
    }
    
    private void checkDuplicateRuleNames(final CreateEncryptRuleStatement sqlStatement) {
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement);
        ShardingSpherePreconditions.checkMustEmpty(duplicatedRuleNames, () -> new DuplicateRuleException("encrypt", database.getName(), duplicatedRuleNames));
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateEncryptRuleStatement sqlStatement) {
        return null == rule ? Collections.emptyList() : sqlStatement.getRules().stream().map(EncryptRuleSegment::getTableName).filter(rule.getAllTableNames()::contains).collect(Collectors.toSet());
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
        ShardingSpherePreconditions.checkState(encryptAlgorithm.getMetaData().isSupportDecrypt(), () -> new AlgorithmInitializationException(encryptAlgorithm, "Can not support decrypt"));
    }
    
    private void checkLikeAlgorithmType(final EncryptColumnItemSegment itemSegment) {
        if (null == itemSegment || null == itemSegment.getEncryptor()) {
            return;
        }
        EncryptAlgorithm encryptAlgorithm = TypedSPILoader.getService(EncryptAlgorithm.class, itemSegment.getEncryptor().getName(), itemSegment.getEncryptor().getProps());
        ShardingSpherePreconditions.checkState(encryptAlgorithm.getMetaData().isSupportLike(), () -> new AlgorithmInitializationException(encryptAlgorithm, "Can not support like"));
    }
    
    private void checkAssistedAlgorithmType(final EncryptColumnItemSegment itemSegment) {
        if (null == itemSegment || null == itemSegment.getEncryptor()) {
            return;
        }
        EncryptAlgorithm encryptAlgorithm = TypedSPILoader.getService(EncryptAlgorithm.class, itemSegment.getEncryptor().getName(), itemSegment.getEncryptor().getProps());
        ShardingSpherePreconditions.checkState(encryptAlgorithm.getMetaData().isSupportEquivalentFilter(),
                () -> new AlgorithmInitializationException(encryptAlgorithm, "Can not support assist query"));
    }
    
    private void checkToBeCreatedEncryptors(final CreateEncryptRuleStatement sqlStatement) {
        Collection<AlgorithmSegment> encryptors = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> each.getColumns().forEach(column -> addToEncryptors(column, encryptors)));
        encryptors.stream().filter(Objects::nonNull).forEach(each -> TypedSPILoader.checkService(EncryptAlgorithm.class, each.getName(), each.getProps()));
    }
    
    private void addToEncryptors(final EncryptColumnSegment column, final Collection<AlgorithmSegment> result) {
        result.add(column.getCipher().getEncryptor());
        if (null != column.getAssistedQuery()) {
            result.add(column.getAssistedQuery().getEncryptor());
        }
        if (null != column.getLikeQuery()) {
            result.add(column.getLikeQuery().getEncryptor());
        }
    }
    
    private void checkDataSources() {
        ShardingSpherePreconditions.checkNotEmpty(database.getResourceMetaData().getStorageUnits(), () -> new EmptyStorageUnitException(database.getName()));
    }
    
    @Override
    public EncryptRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateEncryptRuleStatement sqlStatement) {
        Collection<EncryptRuleSegment> segments = sqlStatement.getRules();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement);
            segments.removeIf(each -> duplicatedRuleNames.contains(each.getTableName()));
        }
        return EncryptRuleStatementConverter.convert(segments);
    }
    
    @Override
    public Class<EncryptRule> getRuleClass() {
        return EncryptRule.class;
    }
    
    @Override
    public Class<CreateEncryptRuleStatement> getType() {
        return CreateEncryptRuleStatement.class;
    }
}
