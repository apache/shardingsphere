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

package org.apache.shardingsphere.encrypt.rule;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.encrypt.assisted.AssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.like.LikeEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.exception.algorithm.MismatchedEncryptAlgorithmTypeException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptTableNotFoundException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableNamesMapper;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Encrypt rule.
 */
public final class EncryptRule implements DatabaseRule, TableContainedRule {
    
    private final String databaseName;
    
    @Getter
    private final RuleConfiguration configuration;
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    private final TableNamesMapper tableNamesMapper = new TableNamesMapper();
    
    public EncryptRule(final String databaseName, final EncryptRuleConfiguration ruleConfig) {
        this.databaseName = databaseName;
        configuration = ruleConfig;
        @SuppressWarnings("rawtypes")
        Map<String, StandardEncryptAlgorithm> standardEncryptors = new LinkedHashMap<>();
        @SuppressWarnings("rawtypes")
        Map<String, AssistedEncryptAlgorithm> assistedEncryptors = new LinkedHashMap<>();
        @SuppressWarnings("rawtypes")
        Map<String, LikeEncryptAlgorithm> likeEncryptors = new LinkedHashMap<>();
        ruleConfig.getEncryptors().forEach((key, value) -> putAllEncryptors(
                key, TypedSPILoader.getService(EncryptAlgorithm.class, value.getType(), value.getProps()), standardEncryptors, assistedEncryptors, likeEncryptors));
        for (EncryptTableRuleConfiguration each : ruleConfig.getTables()) {
            each.getColumns().forEach(columnRuleConfig -> checkStandardEncryptorType(columnRuleConfig, standardEncryptors));
            each.getColumns().forEach(columnRuleConfig -> checkAssistedQueryEncryptorType(columnRuleConfig, assistedEncryptors));
            each.getColumns().forEach(columnRuleConfig -> checkLikeQueryEncryptorType(columnRuleConfig, likeEncryptors));
            tables.put(each.getName().toLowerCase(), new EncryptTable(each, standardEncryptors, assistedEncryptors, likeEncryptors));
            tableNamesMapper.put(each.getName());
        }
    }
    
    /**
     * Encrypt rule constructor.
     * 
     * @deprecated deprecated by compatible encrypt rule configuration
     */
    @Deprecated
    public EncryptRule(final String databaseName, final CompatibleEncryptRuleConfiguration ruleConfig) {
        this.databaseName = databaseName;
        configuration = ruleConfig;
        @SuppressWarnings("rawtypes")
        Map<String, StandardEncryptAlgorithm> standardEncryptors = new LinkedHashMap<>();
        @SuppressWarnings("rawtypes")
        Map<String, AssistedEncryptAlgorithm> assistedEncryptors = new LinkedHashMap<>();
        @SuppressWarnings("rawtypes")
        Map<String, LikeEncryptAlgorithm> likeEncryptors = new LinkedHashMap<>();
        ruleConfig.getEncryptors().forEach((key, value) -> putAllEncryptors(
                key, TypedSPILoader.getService(EncryptAlgorithm.class, value.getType(), value.getProps()), standardEncryptors, assistedEncryptors, likeEncryptors));
        for (EncryptTableRuleConfiguration each : ruleConfig.getTables()) {
            each.getColumns().forEach(columnRuleConfig -> checkStandardEncryptorType(columnRuleConfig, standardEncryptors));
            each.getColumns().forEach(columnRuleConfig -> checkAssistedQueryEncryptorType(columnRuleConfig, assistedEncryptors));
            each.getColumns().forEach(columnRuleConfig -> checkLikeQueryEncryptorType(columnRuleConfig, likeEncryptors));
            tables.put(each.getName().toLowerCase(), new EncryptTable(each, standardEncryptors, assistedEncryptors, likeEncryptors));
            tableNamesMapper.put(each.getName());
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void putAllEncryptors(final String encryptorName, final EncryptAlgorithm algorithm, final Map<String, StandardEncryptAlgorithm> standardEncryptors,
                                  final Map<String, AssistedEncryptAlgorithm> assistedEncryptors, final Map<String, LikeEncryptAlgorithm> likeEncryptors) {
        if (algorithm instanceof StandardEncryptAlgorithm) {
            standardEncryptors.put(encryptorName, (StandardEncryptAlgorithm) algorithm);
        }
        if (algorithm instanceof AssistedEncryptAlgorithm) {
            assistedEncryptors.put(encryptorName, (AssistedEncryptAlgorithm) algorithm);
        }
        if (algorithm instanceof LikeEncryptAlgorithm) {
            likeEncryptors.put(encryptorName, (LikeEncryptAlgorithm) algorithm);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void checkStandardEncryptorType(final EncryptColumnRuleConfiguration columnRuleConfig, final Map<String, StandardEncryptAlgorithm> standardEncryptors) {
        ShardingSpherePreconditions.checkState(standardEncryptors.containsKey(columnRuleConfig.getCipher().getEncryptorName()),
                () -> new MismatchedEncryptAlgorithmTypeException(databaseName, "Cipher", columnRuleConfig.getCipher().getEncryptorName(), StandardEncryptAlgorithm.class.getSimpleName()));
    }
    
    @SuppressWarnings("rawtypes")
    private void checkAssistedQueryEncryptorType(final EncryptColumnRuleConfiguration columnRuleConfig, final Map<String, AssistedEncryptAlgorithm> assistedEncryptors) {
        columnRuleConfig.getAssistedQuery().ifPresent(optional -> ShardingSpherePreconditions.checkState(assistedEncryptors.containsKey(optional.getEncryptorName()),
                () -> new MismatchedEncryptAlgorithmTypeException(databaseName, "Assisted query", optional.getEncryptorName(), AssistedEncryptAlgorithm.class.getSimpleName())));
    }
    
    @SuppressWarnings("rawtypes")
    private void checkLikeQueryEncryptorType(final EncryptColumnRuleConfiguration columnRuleConfig, final Map<String, LikeEncryptAlgorithm> likeEncryptors) {
        columnRuleConfig.getLikeQuery().ifPresent(optional -> ShardingSpherePreconditions.checkState(likeEncryptors.containsKey(optional.getEncryptorName()),
                () -> new MismatchedEncryptAlgorithmTypeException(databaseName, "Like query", optional.getEncryptorName(), LikeEncryptAlgorithm.class.getSimpleName())));
    }
    
    /**
     * Find encrypt table.
     * 
     * @param tableName table name
     * @return encrypt table
     */
    public Optional<EncryptTable> findEncryptTable(final String tableName) {
        return Optional.ofNullable(tables.get(tableName.toLowerCase()));
    }
    
    /**
     * Get encrypt table.
     *
     * @param tableName table name
     * @return encrypt table
     */
    public EncryptTable getEncryptTable(final String tableName) {
        Optional<EncryptTable> encryptTable = findEncryptTable(tableName);
        ShardingSpherePreconditions.checkState(encryptTable.isPresent(), () -> new EncryptTableNotFoundException(tableName));
        return encryptTable.get();
    }
    
    @Override
    public TableNamesMapper getLogicTableMapper() {
        return tableNamesMapper;
    }
    
    @Override
    public TableNamesMapper getActualTableMapper() {
        return new TableNamesMapper();
    }
    
    @Override
    public TableNamesMapper getDistributedTableMapper() {
        return new TableNamesMapper();
    }
    
    @Override
    public TableNamesMapper getEnhancedTableMapper() {
        return getLogicTableMapper();
    }
    
    @Override
    public String getType() {
        return EncryptRule.class.getSimpleName();
    }
}
