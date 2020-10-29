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
import com.google.common.base.Strings;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encrypt rule.
 */
public final class EncryptRule implements ShardingSphereRule {
    
    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }
    
    private final Map<String, EncryptAlgorithm> encryptors = new LinkedHashMap<>();
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    public EncryptRule(final EncryptRuleConfiguration config) {
        Preconditions.checkArgument(isValidRuleConfiguration(config), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        config.getEncryptors().forEach((key, value) -> encryptors.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, EncryptAlgorithm.class)));
        config.getTables().forEach(each -> tables.put(each.getName(), new EncryptTable(each)));
    }
    
    public EncryptRule(final AlgorithmProvidedEncryptRuleConfiguration config) {
        Preconditions.checkArgument(isValidRuleConfigurationWithAlgorithmProvided(config), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        encryptors.putAll(config.getEncryptors());
        config.getTables().forEach(each -> tables.put(each.getName(), new EncryptTable(each)));
    }
    
    private boolean isValidRuleConfiguration(final EncryptRuleConfiguration config) {
        return (config.getEncryptors().isEmpty() && config.getTables().isEmpty()) || isValidTableConfiguration(config);
    }
    
    private boolean isValidTableConfiguration(final EncryptRuleConfiguration config) {
        for (EncryptTableRuleConfiguration table : config.getTables()) {
            for (EncryptColumnRuleConfiguration column : table.getColumns()) {
                if (!isValidColumnConfiguration(config, column)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidColumnConfiguration(final EncryptRuleConfiguration encryptRuleConfig, final EncryptColumnRuleConfiguration column) {
        return !Strings.isNullOrEmpty(column.getEncryptorName()) && !Strings.isNullOrEmpty(column.getCipherColumn()) && containsEncryptors(encryptRuleConfig, column);
    }
    
    private boolean containsEncryptors(final EncryptRuleConfiguration encryptRuleConfig, final EncryptColumnRuleConfiguration column) {
        return encryptRuleConfig.getEncryptors().keySet().stream().anyMatch(each -> each.equals(column.getEncryptorName()));
    }
    
    private boolean isValidRuleConfigurationWithAlgorithmProvided(final AlgorithmProvidedEncryptRuleConfiguration config) {
        return (config.getEncryptors().isEmpty() && config.getTables().isEmpty()) || isValidTableConfigurationWithAlgorithmProvided(config);
    }
    
    private boolean isValidTableConfigurationWithAlgorithmProvided(final AlgorithmProvidedEncryptRuleConfiguration config) {
        for (EncryptTableRuleConfiguration table : config.getTables()) {
            for (EncryptColumnRuleConfiguration column : table.getColumns()) {
                if (!isValidColumnConfigurationWithAlgorithmProvided(config, column)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidColumnConfigurationWithAlgorithmProvided(final AlgorithmProvidedEncryptRuleConfiguration encryptRuleConfig, final EncryptColumnRuleConfiguration column) {
        return !Strings.isNullOrEmpty(column.getEncryptorName()) && !Strings.isNullOrEmpty(column.getCipherColumn())
                && encryptRuleConfig.getEncryptors().containsKey(column.getEncryptorName());
    }
    
    /**
     * Get encrypt table names.
     *
     * @return encrypt table names
     */
    public Collection<String> getEncryptTableNames() {
        return tables.keySet();
    }
    
    /**
     * Find encrypt table.
     * 
     * @param logicTable logic table
     * @return encrypt table
     */
    public Optional<EncryptTable> findEncryptTable(final String logicTable) {
        return Optional.ofNullable(tables.get(logicTable));
    }
    
    /**
     * Find encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return encryptor
     */
    public Optional<EncryptAlgorithm> findEncryptor(final String logicTable, final String logicColumn) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).findEncryptorName(logicColumn).map(encryptors::get) : Optional.empty();
    }
    
    /**
     * get encrypt values.
     *
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return encrypt values
     */
    public List<Object> getEncryptValues(final String logicTable, final String logicColumn, final List<Object> originalValues) {
        Optional<EncryptAlgorithm> encryptor = findEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(encryptor.isPresent(), String.format("Can not find QueryAssistedEncryptAlgorithm by %s.%s.", logicTable, logicColumn));
        return originalValues.stream().map(input -> null == input ? null : String.valueOf(encryptor.get().encrypt(input.toString()))).collect(Collectors.toList());
    }
    
    /**
     * Is cipher column or not.
     *
     * @param tableName table name
     * @param columnName column name
     * @return cipher column or not
     */
    public boolean isCipherColumn(final String tableName, final String columnName) {
        return tables.containsKey(tableName) && tables.get(tableName).isCipherColumn(columnName);
    }
    
    /**
     * Get cipher column.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return cipher column
     */
    public String getCipherColumn(final String logicTable, final String logicColumn) {
        return tables.get(logicTable).getCipherColumn(logicColumn);
    }
    
    /**
     * Get logic column of cipher column.
     *
     * @param logicTable logic table
     * @param cipherColumn cipher column
     * @return logic column
     */
    public String getLogicColumnOfCipher(final String logicTable, final String cipherColumn) {
        return tables.get(logicTable).getLogicColumn(cipherColumn);
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @param logicTable logic table 
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getLogicAndCipherColumns() : Collections.emptyMap();
    }
    
    /**
     * Find assisted query column.
     *
     * @param logicTable logic table name
     * @param logicColumn column name
     * @return assisted query column
     */
    public Optional<String> findAssistedQueryColumn(final String logicTable, final String logicColumn) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).findAssistedQueryColumn(logicColumn) : Optional.empty();
    }
    
    /**
     * Get assisted query columns.
     * 
     * @param logicTable logic table
     * @return assisted query columns
     */
    public Collection<String> getAssistedQueryColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getAssistedQueryColumns() : Collections.emptyList();
    }
    
    /**
     * Get encrypt assisted query values.
     *
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return assisted query values
     */
    public List<Object> getEncryptAssistedQueryValues(final String logicTable, final String logicColumn, final List<Object> originalValues) {
        Optional<EncryptAlgorithm> encryptor = findEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(encryptor.isPresent() && encryptor.get() instanceof QueryAssistedEncryptAlgorithm,
                String.format("Can not find QueryAssistedEncryptAlgorithm by %s.%s.", logicTable, logicColumn));
        return originalValues.stream().map(input -> null == input
                ? null : ((QueryAssistedEncryptAlgorithm) encryptor.get()).queryAssistedEncrypt(input.toString())).collect(Collectors.toList());
    }
    
    /**
     * Get assisted query and plain columns.
     *
     * @param logicTable logic table name
     * @return assisted query and plain columns
     */
    public Collection<String> getAssistedQueryAndPlainColumns(final String logicTable) {
        Collection<String> result = new LinkedList<>();
        result.addAll(getAssistedQueryColumns(logicTable));
        result.addAll(getPlainColumns(logicTable));
        return result;
    }
    
    private Collection<String> getPlainColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getPlainColumns() : Collections.emptyList();
    }
    
    /**
     * Find plain column.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return plain column
     */
    public Optional<String> findPlainColumn(final String logicTable, final String logicColumn) {
        Optional<String> originColumnName = findOriginColumnName(logicTable, logicColumn);
        return originColumnName.isPresent() && tables.containsKey(logicTable) ? tables.get(logicTable).findPlainColumn(originColumnName.get()) : Optional.empty();
    }
    
    private Optional<String> findOriginColumnName(final String logicTable, final String logicColumn) {
        for (String each : tables.get(logicTable).getLogicColumns()) {
            if (logicColumn.equalsIgnoreCase(each)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
