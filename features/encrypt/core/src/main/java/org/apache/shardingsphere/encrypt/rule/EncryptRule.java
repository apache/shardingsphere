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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptTableNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.MismatchedEncryptAlgorithmTypeException;
import org.apache.shardingsphere.encrypt.rule.attribute.EncryptTableMapperRuleAttribute;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.rule.PartialRuleUpdateSupported;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    
    private final Map<String, EncryptAlgorithm> encryptors;
    
    private final Map<String, EncryptTable> tables = new CaseInsensitiveMap<>(Collections.emptyMap(), new ConcurrentHashMap<>());
    
    private final AtomicReference<RuleAttributes> attributes = new AtomicReference<>();
    
    public EncryptRule(final String databaseName, final EncryptRuleConfiguration ruleConfig) {
        this.databaseName = databaseName;
        this.ruleConfig.set(ruleConfig);
        encryptors = createEncryptors(ruleConfig);
        for (EncryptTableRuleConfiguration each : ruleConfig.getTables()) {
            each.getColumns().forEach(this::checkEncryptorType);
            tables.put(each.getName(), new EncryptTable(each, encryptors));
        }
        attributes.set(buildRuleAttributes());
    }
    
    private RuleAttributes buildRuleAttributes() {
        List<RuleAttribute> ruleAttributes = new LinkedList<>();
        ruleAttributes.add(new EncryptTableMapperRuleAttribute(tables.keySet()));
        return new RuleAttributes(ruleAttributes.toArray(new RuleAttribute[]{}));
    }
    
    private Map<String, EncryptAlgorithm> createEncryptors(final EncryptRuleConfiguration ruleConfig) {
        Map<String, EncryptAlgorithm> result = new CaseInsensitiveMap<>(Collections.emptyMap(), new ConcurrentHashMap<>(ruleConfig.getEncryptors().size(), 1F));
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
    @HighFrequencyInvocation
    public Optional<EncryptTable> findEncryptTable(final String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }
    
    /**
     * Get encrypt table.
     *
     * @param tableName table name
     * @return encrypt table
     */
    @HighFrequencyInvocation
    public EncryptTable getEncryptTable(final String tableName) {
        return findEncryptTable(tableName).orElseThrow(() -> new EncryptTableNotFoundException(tableName));
    }
    
    /**
     * Find query encryptor.
     *
     * @param tableName table name
     * @param columnName column name
     * @return query encryptor
     */
    @HighFrequencyInvocation
    public Optional<EncryptAlgorithm> findQueryEncryptor(final String tableName, final String columnName) {
        return findEncryptTable(tableName).flatMap(optional -> optional.findQueryEncryptor(columnName));
    }
    
    @Override
    public RuleAttributes getAttributes() {
        return attributes.get();
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
    public boolean partialUpdate(final EncryptRuleConfiguration toBeUpdatedRuleConfig) {
        if (handleAddedEncryptors(toBeUpdatedRuleConfig) || handleRemovedEncryptors(toBeUpdatedRuleConfig)) {
            return false;
        }
        Collection<String> toBeUpdatedTablesNames = toBeUpdatedRuleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toCollection(CaseInsensitiveSet::new));
        Collection<String> toBeRemovedTableNames = tables.keySet().stream().filter(each -> !toBeUpdatedTablesNames.contains(each)).collect(Collectors.toList());
        if (!toBeRemovedTableNames.isEmpty()) {
            toBeRemovedTableNames.forEach(tables::remove);
        }
        for (EncryptTableRuleConfiguration encryptTableRuleConfiguration : toBeUpdatedRuleConfig.getTables()) {
            encryptTableRuleConfiguration.getColumns().forEach(this::checkEncryptorType);
            tables.put(encryptTableRuleConfiguration.getName(), new EncryptTable(encryptTableRuleConfiguration, encryptors));
            attributes.set(buildRuleAttributes());
        }
        return true;
    }
    
    private boolean handleAddedEncryptors(final EncryptRuleConfiguration toBeUpdatedRuleConfig) {
        return toBeUpdatedRuleConfig.getEncryptors().entrySet().stream()
                .filter(entry -> !encryptors.containsKey(entry.getKey()))
                .peek(entry -> encryptors.computeIfAbsent(entry.getKey(), key -> TypedSPILoader.getService(EncryptAlgorithm.class, entry.getValue().getType(), entry.getValue().getProps())))
                .findAny().isPresent();
    }
    
    private boolean handleRemovedEncryptors(final EncryptRuleConfiguration toBeUpdatedRuleConfig) {
        return encryptors.entrySet().stream()
                .filter(entry -> !toBeUpdatedRuleConfig.getEncryptors().containsKey(entry.getKey()))
                .peek(entry -> encryptors.remove(entry.getKey())).findAny().isPresent();
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
}
