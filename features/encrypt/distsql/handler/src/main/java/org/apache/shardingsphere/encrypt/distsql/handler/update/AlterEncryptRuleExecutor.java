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
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.handler.converter.EncryptRuleStatementConverter;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alter encrypt rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(EncryptRule.class)
@Setter
public final class AlterEncryptRuleExecutor implements DatabaseRuleAlterExecutor<AlterEncryptRuleStatement, EncryptRule, EncryptRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private EncryptRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterEncryptRuleStatement sqlStatement) {
        checkToBeAlteredRules(sqlStatement);
        checkColumnNames(sqlStatement);
        checkToBeAlteredEncryptors(sqlStatement);
    }
    
    private void checkToBeAlteredRules(final AlterEncryptRuleStatement sqlStatement) {
        Collection<String> currentEncryptTableNames = rule.getConfiguration().getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistEncryptTableNames = getToBeAlteredEncryptTableNames(sqlStatement).stream().filter(each -> !currentEncryptTableNames.contains(each)).collect(Collectors.toList());
        if (!notExistEncryptTableNames.isEmpty()) {
            throw new MissingRequiredRuleException("Encrypt", database.getName(), notExistEncryptTableNames);
        }
    }
    
    private Collection<String> getToBeAlteredEncryptTableNames(final AlterEncryptRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(EncryptRuleSegment::getTableName).collect(Collectors.toList());
    }
    
    private void checkColumnNames(final AlterEncryptRuleStatement sqlStatement) {
        for (EncryptRuleSegment each : sqlStatement.getRules()) {
            ShardingSpherePreconditions.checkState(isColumnNameNotConflicts(each),
                    () -> new InvalidRuleConfigurationException("encrypt", "assisted query column or like query column conflicts with logic column"));
        }
    }
    
    private boolean isColumnNameNotConflicts(final EncryptRuleSegment rule) {
        return rule.getColumns().stream().noneMatch(each -> null != each.getLikeQuery() && each.getName().equals(each.getLikeQuery().getName())
                || null != each.getAssistedQuery() && each.getName().equals(each.getAssistedQuery().getName()));
    }
    
    private void checkToBeAlteredEncryptors(final AlterEncryptRuleStatement sqlStatement) {
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
    public EncryptRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterEncryptRuleStatement sqlStatement) {
        return EncryptRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public EncryptRuleConfiguration buildToBeDroppedRuleConfiguration(final EncryptRuleConfiguration toBeAlteredRuleConfig) {
        Map<String, AlgorithmConfiguration> toBeDroppedEncryptors = new HashMap<>();
        Collection<String> unusedEncryptor = UnusedAlgorithmFinder.findUnusedEncryptor(rule.getConfiguration());
        unusedEncryptor.forEach(each -> toBeDroppedEncryptors.put(each, rule.getConfiguration().getEncryptors().get(each)));
        return new EncryptRuleConfiguration(Collections.emptyList(), toBeDroppedEncryptors);
    }
    
    private void dropRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final EncryptRuleConfiguration toBeAlteredRuleConfig) {
        for (EncryptTableRuleConfiguration each : toBeAlteredRuleConfig.getTables()) {
            Optional<EncryptTableRuleConfiguration> toBeRemovedTableRuleConfig = currentRuleConfig.getTables().stream().filter(tableRule -> tableRule.getName().equals(each.getName())).findAny();
            Preconditions.checkState(toBeRemovedTableRuleConfig.isPresent());
            currentRuleConfig.getTables().remove(toBeRemovedTableRuleConfig.get());
        }
    }
    
    private void addRuleConfiguration(final EncryptRuleConfiguration currentRuleConfig, final EncryptRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getTables().addAll(toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getEncryptors().putAll(toBeAlteredRuleConfig.getEncryptors());
    }
    
    private void dropUnusedEncryptor(final EncryptRuleConfiguration currentRuleConfig) {
        Collection<String> inUsedEncryptors = currentRuleConfig.getTables().stream().flatMap(each -> each.getColumns().stream()).map(optional -> optional.getCipher().getEncryptorName())
                .collect(Collectors.toSet());
        inUsedEncryptors.addAll(currentRuleConfig.getTables().stream().flatMap(each -> each.getColumns().stream())
                .map(optional -> optional.getAssistedQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).orElse(""))
                .collect(Collectors.toSet()));
        inUsedEncryptors.addAll(currentRuleConfig.getTables().stream().flatMap(each -> each.getColumns().stream())
                .map(optional -> optional.getLikeQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).orElse(""))
                .collect(Collectors.toSet()));
        Collection<String> unusedEncryptors = currentRuleConfig.getEncryptors().keySet().stream().filter(each -> !inUsedEncryptors.contains(each)).collect(Collectors.toSet());
        unusedEncryptors.forEach(each -> currentRuleConfig.getEncryptors().remove(each));
    }
    
    @Override
    public Class<EncryptRule> getRuleClass() {
        return EncryptRule.class;
    }
    
    @Override
    public Class<AlterEncryptRuleStatement> getType() {
        return AlterEncryptRuleStatement.class;
    }
}
