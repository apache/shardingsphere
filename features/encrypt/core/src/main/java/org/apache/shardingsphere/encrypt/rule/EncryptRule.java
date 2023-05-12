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
import org.apache.shardingsphere.encrypt.api.context.EncryptContext;
import org.apache.shardingsphere.encrypt.api.encrypt.assisted.AssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.like.LikeEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.api.encrypt.standard.StandardEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;
import org.apache.shardingsphere.encrypt.exception.algorithm.MismatchedEncryptAlgorithmTypeException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptAssistedQueryEncryptorNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptEncryptorNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLikeQueryEncryptorNotFoundException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ColumnContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Encrypt rule.
 */
public final class EncryptRule implements DatabaseRule, TableContainedRule, ColumnContainedRule {
    
    @Getter
    private final RuleConfiguration configuration;
    
    @SuppressWarnings("rawtypes")
    private final Map<String, StandardEncryptAlgorithm> standardEncryptors = new LinkedHashMap<>();
    
    @SuppressWarnings("rawtypes")
    private final Map<String, LikeEncryptAlgorithm> likeEncryptors = new LinkedHashMap<>();
    
    @SuppressWarnings("rawtypes")
    private final Map<String, AssistedEncryptAlgorithm> assistedEncryptors = new LinkedHashMap<>();
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    public EncryptRule(final EncryptRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        ruleConfig.getEncryptors().forEach((key, value) -> putAllEncryptors(key, TypedSPILoader.getService(EncryptAlgorithm.class, value.getType(), value.getProps())));
        for (EncryptTableRuleConfiguration each : ruleConfig.getTables()) {
            each.getColumns().forEach(this::checkEncryptAlgorithmType);
            tables.put(each.getName().toLowerCase(), new EncryptTable(each));
        }
    }
    
    /**
     * Encrypt rule constructor.
     * 
     * @deprecated deprecated by compatible encrypt rule configuration
     */
    @Deprecated
    public EncryptRule(final CompatibleEncryptRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        ruleConfig.getEncryptors().forEach((key, value) -> putAllEncryptors(key, TypedSPILoader.getService(EncryptAlgorithm.class, value.getType(), value.getProps())));
        for (EncryptTableRuleConfiguration each : ruleConfig.getTables()) {
            each.getColumns().forEach(this::checkEncryptAlgorithmType);
            tables.put(each.getName().toLowerCase(), new EncryptTable(each));
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void putAllEncryptors(final String encryptorName, final EncryptAlgorithm algorithm) {
        if (algorithm instanceof StandardEncryptAlgorithm) {
            standardEncryptors.put(encryptorName, (StandardEncryptAlgorithm) algorithm);
        }
        if (algorithm instanceof LikeEncryptAlgorithm) {
            likeEncryptors.put(encryptorName, (LikeEncryptAlgorithm) algorithm);
        }
        if (algorithm instanceof AssistedEncryptAlgorithm) {
            assistedEncryptors.put(encryptorName, (AssistedEncryptAlgorithm) algorithm);
        }
    }
    
    private void checkEncryptAlgorithmType(final EncryptColumnRuleConfiguration columnRuleConfig) {
        ShardingSpherePreconditions.checkState(standardEncryptors.containsKey(columnRuleConfig.getCipher().getEncryptorName()),
                () -> new MismatchedEncryptAlgorithmTypeException("Cipher", columnRuleConfig.getCipher().getEncryptorName(), StandardEncryptAlgorithm.class.getSimpleName()));
        columnRuleConfig.getLikeQuery().ifPresent(optional -> ShardingSpherePreconditions.checkState(likeEncryptors.containsKey(optional.getEncryptorName()),
                () -> new MismatchedEncryptAlgorithmTypeException("Like query", optional.getEncryptorName(), LikeEncryptAlgorithm.class.getSimpleName())));
        columnRuleConfig.getAssistedQuery().ifPresent(optional -> ShardingSpherePreconditions.checkState(assistedEncryptors.containsKey(optional.getEncryptorName()),
                () -> new MismatchedEncryptAlgorithmTypeException("Assisted query", optional.getEncryptorName(), AssistedEncryptAlgorithm.class.getSimpleName())));
    }
    
    /**
     * Find encrypt table.
     * 
     * @param logicTable logic table
     * @return encrypt table
     */
    public Optional<EncryptTable> findEncryptTable(final String logicTable) {
        return Optional.ofNullable(tables.get(logicTable.toLowerCase()));
    }
    
    /**
     * Find encrypt column.
     * 
     * @param logicTable logic table
     * @param columnName column name
     * @return encrypt column
     */
    public Optional<EncryptColumn> findEncryptColumn(final String logicTable, final String columnName) {
        return findEncryptTable(logicTable).flatMap(optional -> optional.findEncryptColumn(columnName));
    }
    
    /**
     * Find standard encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return standard encryptor
     */
    @SuppressWarnings("rawtypes")
    public Optional<StandardEncryptAlgorithm> findStandardEncryptor(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findEncryptorName(logicColumn).map(standardEncryptors::get) : Optional.empty();
    }
    
    /**
     * Find assisted encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return assisted encryptor
     */
    @SuppressWarnings("rawtypes")
    public Optional<AssistedEncryptAlgorithm> findAssistedQueryEncryptor(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findAssistedQueryEncryptorName(logicColumn).map(assistedEncryptors::get) : Optional.empty();
    }
    
    /**
     * Find like query encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return like query encryptor
     */
    @SuppressWarnings("rawtypes")
    public Optional<LikeEncryptAlgorithm> findLikeQueryEncryptor(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findLikeQueryEncryptorName(logicColumn).map(likeEncryptors::get) : Optional.empty();
    }
    
    /**
     * Get encrypt values.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return encrypt values
     */
    public List<Object> getEncryptValues(final String databaseName, final String schemaName, final String logicTable, final String logicColumn, final List<Object> originalValues) {
        @SuppressWarnings("rawtypes")
        Optional<StandardEncryptAlgorithm> encryptor = findStandardEncryptor(logicTable, logicColumn);
        EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, logicTable, logicColumn);
        ShardingSpherePreconditions.checkState(encryptor.isPresent(),
                () -> new EncryptEncryptorNotFoundException(String.format("Can not find StandardEncryptAlgorithm by %s.%s.", logicTable, logicColumn)));
        return getEncryptValues(encryptor.get(), originalValues, encryptContext);
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> getEncryptValues(@SuppressWarnings("rawtypes") final StandardEncryptAlgorithm encryptor, final List<Object> originalValues, final EncryptContext encryptContext) {
        List<Object> result = new LinkedList<>();
        for (Object each : originalValues) {
            Object encryptValue = null == each ? null : encryptor.encrypt(each, encryptContext);
            result.add(encryptValue);
        }
        return result;
    }
    
    /**
     * Get cipher column.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return cipher column
     */
    public String getCipherColumn(final String logicTable, final String logicColumn) {
        return tables.get(logicTable.toLowerCase()).getCipherColumn(logicColumn);
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @param logicTable logic table 
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns(final String logicTable) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).getLogicAndCipherColumns() : Collections.emptyMap();
    }
    
    /**
     * Find assisted query column.
     *
     * @param logicTable logic table name
     * @param logicColumn column name
     * @return assisted query column
     */
    public Optional<String> findAssistedQueryColumn(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findAssistedQueryColumn(logicColumn) : Optional.empty();
    }
    
    /**
     * Find like query column.
     *
     * @param logicTable logic table name
     * @param logicColumn column name
     * @return like query column
     */
    public Optional<String> findLikeQueryColumn(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findLikeQueryColumn(logicColumn) : Optional.empty();
    }
    
    /**
     * Get assisted query columns.
     * 
     * @param logicTable logic table
     * @return assisted query columns
     */
    public Collection<String> getAssistedQueryColumns(final String logicTable) {
        return tables.containsKey(logicTable.toLowerCase()) ? tables.get(logicTable.toLowerCase()).getAssistedQueryColumns() : Collections.emptyList();
    }
    
    /**
     * Get encrypt assisted query values.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return assisted query values
     */
    public List<Object> getEncryptAssistedQueryValues(final String databaseName, final String schemaName, final String logicTable, final String logicColumn, final List<Object> originalValues) {
        @SuppressWarnings("rawtypes")
        Optional<AssistedEncryptAlgorithm> assistedQueryEncryptor = findAssistedQueryEncryptor(logicTable, logicColumn);
        EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, logicTable, logicColumn);
        ShardingSpherePreconditions.checkState(assistedQueryEncryptor.isPresent(),
                () -> new EncryptAssistedQueryEncryptorNotFoundException(String.format("Can not find assisted encryptor by %s.%s.", logicTable, logicColumn)));
        return getEncryptAssistedQueryValues(assistedQueryEncryptor.get(), originalValues, encryptContext);
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> getEncryptAssistedQueryValues(@SuppressWarnings("rawtypes") final AssistedEncryptAlgorithm assistedQueryEncryptor,
                                                       final List<Object> originalValues, final EncryptContext encryptContext) {
        List<Object> result = new LinkedList<>();
        for (Object each : originalValues) {
            result.add(null == each ? null : assistedQueryEncryptor.encrypt(each, encryptContext));
        }
        return result;
    }
    
    /**
     * Get encrypt like query values.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return like query values
     */
    public List<Object> getEncryptLikeQueryValues(final String databaseName, final String schemaName, final String logicTable, final String logicColumn, final List<Object> originalValues) {
        @SuppressWarnings("rawtypes")
        Optional<LikeEncryptAlgorithm> likeQueryEncryptor = findLikeQueryEncryptor(logicTable, logicColumn);
        EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, logicTable, logicColumn);
        ShardingSpherePreconditions.checkState(likeQueryEncryptor.isPresent(),
                () -> new EncryptLikeQueryEncryptorNotFoundException(String.format("Can not find like query encryptor by %s.%s.", logicTable, logicColumn)));
        return getEncryptLikeQueryValues(likeQueryEncryptor.get(), originalValues, encryptContext);
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> getEncryptLikeQueryValues(@SuppressWarnings("rawtypes") final LikeEncryptAlgorithm likeQueryEncryptor, final List<Object> originalValues,
                                                   final EncryptContext encryptContext) {
        List<Object> result = new LinkedList<>();
        for (Object each : originalValues) {
            result.add(null == each ? null : likeQueryEncryptor.encrypt(each, encryptContext));
        }
        return result;
    }
    
    @Override
    public Collection<String> getTables() {
        return tables.keySet();
    }
    
    @Override
    public String getType() {
        return EncryptRule.class.getSimpleName();
    }
}
