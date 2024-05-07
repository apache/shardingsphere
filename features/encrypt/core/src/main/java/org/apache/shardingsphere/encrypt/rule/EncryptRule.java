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

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptTableNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.MismatchedEncryptAlgorithmTypeException;
import org.apache.shardingsphere.encrypt.rule.attribute.EncryptTableMapperRuleAttribute;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Encrypt rule.
 */
public final class EncryptRule implements DatabaseRule, PartialRuleUpdateSupported<EncryptRuleConfiguration> {
    
    private final String databaseName;
    
    private final AtomicReference<EncryptRuleConfiguration> ruleConfig = new AtomicReference<>();
    
    private final Map<String, EncryptTable> tables;
    
    private final ConcurrentHashMap<String, EncryptAlgorithm> encryptors;
    
    @Getter
    private final RuleAttributes attributes;
    
    public EncryptRule(final String databaseName, final EncryptRuleConfiguration ruleConfig) {
        this.databaseName = databaseName;
        this.ruleConfig.set(ruleConfig);
        tables = new ConcurrentHashMap<>();
        encryptors = createEncryptors(ruleConfig);
        for (EncryptTableRuleConfiguration each : ruleConfig.getTables()) {
            each.getColumns().forEach(this::checkEncryptorType);
            tables.put(each.getName().toLowerCase(), new EncryptTable(each, encryptors));
        }
        attributes = new RuleAttributes(new EncryptTableMapperRuleAttribute(ruleConfig.getTables()));
    }
    
    private ConcurrentHashMap<String, EncryptAlgorithm> createEncryptors(final EncryptRuleConfiguration ruleConfig) {
        ConcurrentHashMap<String, EncryptAlgorithm> result = new ConcurrentHashMap<>(ruleConfig.getEncryptors().size(), 1F);
        for (Entry<String, AlgorithmConfiguration> entry : ruleConfig.getEncryptors().entrySet()) {
            result.put(entry.getKey(), TypedSPILoader.getService(EncryptAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps()));
        }
        return result;
    }
    
    // TODO How to process changed encryptors and tables if check failed? It should check before rule change
    private void checkEncryptorType(final EncryptColumnRuleConfiguration columnRuleConfig) {
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
     * Get all table names.
     *
     * @return all table names
     */
    public Collection<String> getAllTableNames() {
        return tables.keySet();
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
    public EncryptRuleConfiguration getConfiguration() {
        return ruleConfig.get();
    }
    
    @Override
    public void updateConfiguration(final EncryptRuleConfiguration toBeUpdatedRuleConfig) {
        ruleConfig.set(toBeUpdatedRuleConfig);
    }
    
    @Override
    public boolean partialUpdateRule(final EncryptRuleConfiguration toBeUpdatedRuleConfig) {
        Collection<String> toBeAddedTableNames = toBeUpdatedRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
        toBeAddedTableNames.removeAll(tables.keySet());
        if (!toBeAddedTableNames.isEmpty()) {
            toBeAddedTableNames.forEach(each -> addTableRule(each, toBeUpdatedRuleConfig));
            return true;
        }
        Collection<String> toBeRemovedTableNames = new HashSet<>(tables.keySet());
        toBeRemovedTableNames.removeAll(toBeUpdatedRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList()));
        if (!toBeRemovedTableNames.isEmpty()) {
            toBeRemovedTableNames.stream().map(String::toLowerCase).forEach(tables::remove);
            // TODO check and remove unused encryptors
            return true;
        }
        // TODO Process update table
        // TODO Process update encryptors
        return false;
    }
    
    private void addTableRule(final String tableName, final EncryptRuleConfiguration toBeUpdatedRuleConfig) {
        EncryptTableRuleConfiguration tableRuleConfig = getTableRuleConfiguration(tableName, toBeUpdatedRuleConfig);
        for (Entry<String, AlgorithmConfiguration> entry : toBeUpdatedRuleConfig.getEncryptors().entrySet()) {
            encryptors.computeIfAbsent(entry.getKey(), key -> TypedSPILoader.getService(EncryptAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps()));
        }
        tableRuleConfig.getColumns().forEach(this::checkEncryptorType);
        tables.put(tableName.toLowerCase(), new EncryptTable(tableRuleConfig, encryptors));
    }
    
    private EncryptTableRuleConfiguration getTableRuleConfiguration(final String tableName, final EncryptRuleConfiguration toBeUpdatedRuleConfig) {
        Optional<EncryptTableRuleConfiguration> result = toBeUpdatedRuleConfig.getTables().stream().filter(table -> table.getName().equals(tableName)).findFirst();
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
}
