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

package org.apache.shardingsphere.core.rule;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.shardingsphere.api.config.encrypt.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptorRuleConfiguration;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptorServiceLoader;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encrypt rule.
 *
 * @author panjuan
 */
public final class EncryptRule implements BaseRule {
    
    private final Map<String, ShardingEncryptor> encryptors = new LinkedHashMap<>();
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    @Getter
    private EncryptRuleConfiguration ruleConfiguration;
    
    public EncryptRule() {
        ruleConfiguration = new EncryptRuleConfiguration();
    }
    
    public EncryptRule(final EncryptRuleConfiguration encryptRuleConfig) {
        this.ruleConfiguration = encryptRuleConfig;
        Preconditions.checkArgument(isValidRuleConfiguration(), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        initEncryptors(encryptRuleConfig.getEncryptors());
        initTables(encryptRuleConfig.getTables());
    }
    
    private boolean isValidRuleConfiguration() {
        return (ruleConfiguration.getEncryptors().isEmpty() && ruleConfiguration.getTables().isEmpty()) || isValidTableConfiguration();
    }
    
    private boolean isValidTableConfiguration() {
        for (EncryptTableRuleConfiguration table : ruleConfiguration.getTables().values()) {
            for (EncryptColumnRuleConfiguration column : table.getColumns().values()) {
                if (!isValidColumnConfiguration(column)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidColumnConfiguration(final EncryptColumnRuleConfiguration column) {
        return !Strings.isNullOrEmpty(column.getEncryptor()) && !Strings.isNullOrEmpty(column.getCipherColumn()) && ruleConfiguration.getEncryptors().containsKey(column.getEncryptor());
    }
    
    private void initEncryptors(final Map<String, EncryptorRuleConfiguration> encryptors) {
        ShardingEncryptorServiceLoader serviceLoader = new ShardingEncryptorServiceLoader();
        for (Entry<String, EncryptorRuleConfiguration> entry : encryptors.entrySet()) {
            this.encryptors.put(entry.getKey(), createShardingEncryptor(serviceLoader, entry.getValue()));
        }
    }
    
    private ShardingEncryptor createShardingEncryptor(final ShardingEncryptorServiceLoader serviceLoader, final EncryptorRuleConfiguration encryptorRuleConfig) {
        ShardingEncryptor result = serviceLoader.newService(encryptorRuleConfig.getType(), encryptorRuleConfig.getProperties());
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
        return Optional.fromNullable(tables.get(logicTable));
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
        return originColumnName.isPresent() && tables.containsKey(logicTable) ? tables.get(logicTable).findPlainColumn(originColumnName.get()) : Optional.<String>absent();
    }

    private Optional<String> findOriginColumnName(final String logicTable, final String logicColumn) {
        for (String each : tables.get(logicTable).getLogicColumns()) {
            if (logicColumn.equalsIgnoreCase(each)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
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
        return tables.containsKey(logicTable) ? tables.get(logicTable).findAssistedQueryColumn(logicColumn) : Optional.<String>absent();
    }
    
    /**
     * Get assisted query columns.
     * 
     * @param logicTable logic table
     * @return assisted query columns
     */
    public Collection<String> getAssistedQueryColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getAssistedQueryColumns() : Collections.<String>emptyList();
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
        return tables.containsKey(logicTable) ? tables.get(logicTable).getPlainColumns() : Collections.<String>emptyList();
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @param logicTable logic table 
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getLogicAndCipherColumns() : Collections.<String, String>emptyMap();
    }
    
    /**
     * Get logic and plain columns.
     *
     * @param logicTable logic table 
     * @return logic and plain columns
     */
    public Map<String, String> getLogicAndPlainColumns(final String logicTable) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).getLogicAndPlainColumns() : Collections.<String, String>emptyMap();
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
        final Optional<ShardingEncryptor> shardingEncryptor = findShardingEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(shardingEncryptor.isPresent() && shardingEncryptor.get() instanceof ShardingQueryAssistedEncryptor,
                String.format("Can not find ShardingQueryAssistedEncryptor by %s.%s.", logicTable, logicColumn));
        return Lists.transform(originalValues, new Function<Object, Object>() {
            
            @Override
            public Object apply(final Object input) {
                return null == input ? null : ((ShardingQueryAssistedEncryptor) shardingEncryptor.get()).queryAssistedEncrypt(input.toString());
            }
        });
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
        final Optional<ShardingEncryptor> shardingEncryptor = findShardingEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(shardingEncryptor.isPresent(), String.format("Can not find ShardingQueryAssistedEncryptor by %s.%s.", logicTable, logicColumn));
        return Lists.transform(originalValues, new Function<Object, Object>() {
            
            @Override
            public Object apply(final Object input) {
                return null == input ? null : String.valueOf(shardingEncryptor.get().encrypt(input.toString()));
            }
        });
    }
    
    /**
     * Find sharding encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return sharding encryptor
     */
    public Optional<ShardingEncryptor> findShardingEncryptor(final String logicTable, final String logicColumn) {
        if (!tables.containsKey(logicTable)) {
            return Optional.absent();
        }
        Optional<String> encryptor = tables.get(logicTable).findShardingEncryptor(logicColumn);
        return encryptor.isPresent() ? Optional.of(encryptors.get(encryptor.get())) : Optional.<ShardingEncryptor>absent();
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
