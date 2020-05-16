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
import org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorRuleConfiguration;
import org.apache.shardingsphere.encrypt.strategy.EncryptTable;
import org.apache.shardingsphere.encrypt.strategy.spi.Encryptor;
import org.apache.shardingsphere.encrypt.strategy.spi.QueryAssistedEncryptor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encrypt rule.
 */
public final class EncryptRule implements ShardingSphereRule {
    
    static {
        ShardingSphereServiceLoader.register(Encryptor.class);
    }
    
    private final Map<String, Encryptor> encryptors = new LinkedHashMap<>();
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    public EncryptRule(final EncryptRuleConfiguration encryptRuleConfiguration) {
        Preconditions.checkArgument(isValidRuleConfiguration(encryptRuleConfiguration), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        initEncryptors(encryptRuleConfiguration.getEncryptors());
        initTables(encryptRuleConfiguration.getTables());
    }
    
    private boolean isValidRuleConfiguration(final EncryptRuleConfiguration encryptRuleConfiguration) {
        return (encryptRuleConfiguration.getEncryptors().isEmpty() && encryptRuleConfiguration.getTables().isEmpty()) || isValidTableConfiguration(encryptRuleConfiguration);
    }
    
    private boolean isValidTableConfiguration(final EncryptRuleConfiguration encryptRuleConfiguration) {
        for (EncryptTableRuleConfiguration table : encryptRuleConfiguration.getTables().values()) {
            for (EncryptColumnRuleConfiguration column : table.getColumns().values()) {
                if (!isValidColumnConfiguration(encryptRuleConfiguration, column)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidColumnConfiguration(final EncryptRuleConfiguration encryptRuleConfiguration, final EncryptColumnRuleConfiguration column) {
        return !Strings.isNullOrEmpty(column.getEncryptor()) && !Strings.isNullOrEmpty(column.getCipherColumn()) && encryptRuleConfiguration.getEncryptors().containsKey(column.getEncryptor());
    }
    
    private void initEncryptors(final Map<String, EncryptorRuleConfiguration> encryptors) {
        encryptors.forEach((key, value) -> this.encryptors.put(key, createEncryptor(value)));
    }
    
    private Encryptor createEncryptor(final EncryptorRuleConfiguration encryptorRuleConfig) {
        Encryptor result = TypedSPIRegistry.getRegisteredService(Encryptor.class, encryptorRuleConfig.getType(), encryptorRuleConfig.getProperties());
        result.init();
        return result;
    }
    
    private void initTables(final Map<String, EncryptTableRuleConfiguration> tables) {
        for (Entry<String, EncryptTableRuleConfiguration> entry : tables.entrySet()) {
            this.tables.put(entry.getKey(), new EncryptTable(entry.getValue()));
        }
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
     * Get logic column of cipher column.
     *
     * @param logicTable logic table
     * @param cipherColumn cipher column
     * @return logic column
     */
    public String getLogicColumnOfCipher(final String logicTable, final String cipherColumn) {
        return tables.get(logicTable).getLogicColumnOfCipher(cipherColumn);
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
     * Is cipher column or not.
     *
     * @param tableName table name
     * @param columnName column name
     * @return cipher column or not
     */
    public boolean isCipherColumn(final String tableName, final String columnName) {
        return tables.containsKey(tableName) && tables.get(tableName).getCipherColumns().contains(columnName);
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
     * Get logic and cipher columns.
     *
     * @param logicTable logic table 
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getLogicAndCipherColumns() : Collections.emptyMap();
    }
    
    /**
     * Get logic and plain columns.
     *
     * @param logicTable logic table 
     * @return logic and plain columns
     */
    public Map<String, String> getLogicAndPlainColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getLogicAndPlainColumns() : Collections.emptyMap();
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
        Optional<Encryptor> encryptor = findEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(encryptor.isPresent() && encryptor.get() instanceof QueryAssistedEncryptor,
                String.format("Can not find QueryAssistedEncryptor by %s.%s.", logicTable, logicColumn));
        return originalValues.stream().map(input -> null == input ? null : ((QueryAssistedEncryptor) encryptor.get()).queryAssistedEncrypt(input.toString())).collect(Collectors.toList());
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
        Optional<Encryptor> encryptor = findEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(encryptor.isPresent(), String.format("Can not find QueryAssistedEncryptor by %s.%s.", logicTable, logicColumn));
        return originalValues.stream().map(input -> null == input ? null : String.valueOf(encryptor.get().encrypt(input.toString()))).collect(Collectors.toList());
    }
    
    /**
     * Find sharding encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return sharding encryptor
     */
    public Optional<Encryptor> findEncryptor(final String logicTable, final String logicColumn) {
        if (!tables.containsKey(logicTable)) {
            return Optional.empty();
        }
        Optional<String> encryptor = tables.get(logicTable).findEncryptor(logicColumn);
        return encryptor.map(encryptors::get);
    }
    
    /**
     * Get encrypt table names.
     *
     * @return encrypt table names
     */
    public Collection<String> getEncryptTableNames() {
        return tables.keySet();
    }
}
