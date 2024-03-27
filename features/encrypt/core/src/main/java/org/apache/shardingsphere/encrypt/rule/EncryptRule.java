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
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.algorithm.MismatchedEncryptAlgorithmTypeException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptTableNotFoundException;
import org.apache.shardingsphere.encrypt.rule.attribute.EncryptTableMapperRuleAttribute;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Encrypt rule.
 */
public final class EncryptRule implements DatabaseRule {
    
    private final String databaseName;
    
    @Getter
    private final EncryptRuleConfiguration configuration;
    
    private final Map<String, EncryptTable> tables;
    
    @Getter
    private final RuleAttributes attributes;
    
    public EncryptRule(final String databaseName, final EncryptRuleConfiguration ruleConfig) {
        this.databaseName = databaseName;
        configuration = ruleConfig;
        tables = new LinkedHashMap<>();
        Map<String, EncryptAlgorithm> encryptors = createEncryptors(ruleConfig);
        for (EncryptTableRuleConfiguration each : ruleConfig.getTables()) {
            each.getColumns().forEach(columnRuleConfig -> checkEncryptorType(columnRuleConfig, encryptors));
            tables.put(each.getName().toLowerCase(), new EncryptTable(each, encryptors));
        }
        attributes = new RuleAttributes(new EncryptTableMapperRuleAttribute(ruleConfig.getTables()));
    }
    
    private Map<String, EncryptAlgorithm> createEncryptors(final EncryptRuleConfiguration ruleConfig) {
        Map<String, EncryptAlgorithm> result = new LinkedHashMap<>(ruleConfig.getEncryptors().size(), 1F);
        for (Entry<String, AlgorithmConfiguration> entry : ruleConfig.getEncryptors().entrySet()) {
            result.put(entry.getKey(), TypedSPILoader.getService(EncryptAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps()));
        }
        return result;
    }
    
    private void checkEncryptorType(final EncryptColumnRuleConfiguration columnRuleConfig, final Map<String, EncryptAlgorithm> encryptors) {
        ShardingSpherePreconditions.checkState(encryptors.containsKey(columnRuleConfig.getCipher().getEncryptorName())
                && encryptors.get(columnRuleConfig.getCipher().getEncryptorName()).getMetaData().isSupportDecrypt(),
                () -> new MismatchedEncryptAlgorithmTypeException(databaseName, "Cipher", columnRuleConfig.getCipher().getEncryptorName(), "decrypt"));
        columnRuleConfig.getAssistedQuery().ifPresent(optional -> ShardingSpherePreconditions.checkState(encryptors.containsKey(optional.getEncryptorName())
                && encryptors.get(optional.getEncryptorName()).getMetaData().isSupportEquivalentFilter(),
                () -> new MismatchedEncryptAlgorithmTypeException(databaseName, "Assisted query", columnRuleConfig.getCipher().getEncryptorName(), "equivalent filter")));
        columnRuleConfig.getLikeQuery().ifPresent(optional -> ShardingSpherePreconditions.checkState(encryptors.containsKey(optional.getEncryptorName())
                && encryptors.get(optional.getEncryptorName()).getMetaData().isSupportLike(),
                () -> new MismatchedEncryptAlgorithmTypeException(databaseName, "Like query", columnRuleConfig.getCipher().getEncryptorName(), "like")));
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
}
