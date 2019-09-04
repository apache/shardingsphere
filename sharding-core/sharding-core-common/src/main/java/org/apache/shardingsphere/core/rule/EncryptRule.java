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
import org.apache.shardingsphere.core.spi.algorithm.encrypt.ShardingEncryptorServiceLoader;
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
@Getter
public final class EncryptRule implements BaseRule {
    
    private final Map<String, ShardingEncryptor> encryptors = new LinkedHashMap<>();
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    private EncryptRuleConfiguration ruleConfiguration;
    
    public EncryptRule() {
        ruleConfiguration = new EncryptRuleConfiguration();
    }
    
    public EncryptRule(final EncryptRuleConfiguration encryptRuleConfiguration) {
        this.ruleConfiguration = encryptRuleConfiguration;
        Preconditions.checkArgument(isValidEncryptRuleConfig(), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        initEncryptors(encryptRuleConfiguration.getEncryptors());
        initTables(encryptRuleConfiguration.getTables());
    }
    
    private boolean isValidEncryptRuleConfig() {
        return (ruleConfiguration.getEncryptors().isEmpty() && ruleConfiguration.getTables().isEmpty()) || isValidEncryptTableConfig();
    }
    
    private boolean isValidEncryptTableConfig() {
        for (EncryptTableRuleConfiguration table : ruleConfiguration.getTables().values()) {
            for (EncryptColumnRuleConfiguration column : table.getColumns().values()) {
                if (!isValidColumnConfig(column)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidColumnConfig(final EncryptColumnRuleConfiguration column) {
        return !Strings.isNullOrEmpty(column.getEncryptor()) && !Strings.isNullOrEmpty(column.getCipherColumn()) && ruleConfiguration.getEncryptors().keySet().contains(column.getEncryptor());
    }
    
    private void initEncryptors(final Map<String, EncryptorRuleConfiguration> encryptors) {
        ShardingEncryptorServiceLoader serviceLoader = new ShardingEncryptorServiceLoader();
        for (Entry<String, EncryptorRuleConfiguration> each : encryptors.entrySet()) {
            this.encryptors.put(each.getKey(), createShardingEncryptor(serviceLoader, each.getValue()));
        }
    }
    
    private ShardingEncryptor createShardingEncryptor(final ShardingEncryptorServiceLoader serviceLoader, final EncryptorRuleConfiguration encryptorRuleConfiguration) {
        ShardingEncryptor encryptor = serviceLoader.newService(encryptorRuleConfiguration.getType(), encryptorRuleConfiguration.getProperties());
        encryptor.init();
        return encryptor;
    }
    
    private void initTables(final Map<String, EncryptTableRuleConfiguration> tables) {
        for (Entry<String, EncryptTableRuleConfiguration> entry : tables.entrySet()) {
            this.tables.put(entry.getKey(), new EncryptTable(entry.getValue()));
        }
    }
    
    /**
     * Is logic column or not.
     *
     * @param logicTable logic table
     * @param columnName column name
     * @return is logic column or not
     */
    public boolean isLogicColumn(final String logicTable, final String columnName) {
        return tables.get(logicTable).getLogicColumns().contains(columnName);
    }
    
    /**
     * Get logic column.
     *
     * @param logicTable logic table
     * @param cipherColumn cipher column
     * @return logic column
     */
    public String getLogicColumn(final String logicTable, final String cipherColumn) {
        return tables.get(logicTable).getLogicColumn(cipherColumn);
    }
    
    /**
     * Get logic columns.
     *
     * @param logicTable logic table
     * @return logic columns
     */
    public Collection<String> getLogicColumns(final String logicTable) {
        if (!tables.containsKey(logicTable)) {
            return Collections.emptyList();
        }
        return tables.get(logicTable).getLogicColumns();
    }
    
    /**
     * Get plain column.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return plain column
     */
    public Optional<String> getPlainColumn(final String logicTable, final String logicColumn) {
        if (!tables.containsKey(logicTable)) {
            return Optional.absent();
        }
        return tables.get(logicTable).getPlainColumn(logicColumn);
    }
    
    private Collection<String> getPlainColumns(final String logicTable) {
        if (!tables.containsKey(logicTable)) {
            return Collections.emptyList();
        }
        return tables.get(logicTable).getPlainColumns();
    }
    
    /**
     * Contains plain column or not.
     *
     * @param logicTable logic table name
     * @return contains plain column or not
     */
    public boolean containsPlainColumn(final String logicTable) {
        return tables.containsKey(logicTable) && tables.get(logicTable).isHasPlainColumn();
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
        return tables.keySet().contains(tableName) && tables.get(tableName).getCipherColumns().contains(columnName);
    }
    
    /**
     * Get assisted query column.
     *
     * @param logicTable logic table name
     * @param logicColumn column name
     * @return assisted query column
     */
    public Optional<String> getAssistedQueryColumn(final String logicTable, final String logicColumn) {
        if (!tables.containsKey(logicTable)) {
            return Optional.absent();
        }
        return tables.get(logicTable).getAssistedQueryColumn(logicColumn);
    }
    
    /**
     * Get assisted query columns.
     * 
     * @param logicTable logic table
     * @return assisted query columns
     */
    public Collection<String> getAssistedQueryColumns(final String logicTable) {
        if (!tables.containsKey(logicTable)) {
            return Collections.emptyList();
        }
        return tables.get(logicTable).getAssistedQueryColumns();
    }
    
    /**
     * Contains query assisted column or not.
     *
     * @param logicTable logic table name
     * @return contains query assisted column or not
     */
    public boolean containsQueryAssistedColumn(final String logicTable) {
        return tables.containsKey(logicTable) && tables.get(logicTable).isHasQueryAssistedColumn();
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
    
    /**
     * Get assisted query and plain column count.
     *
     * @param logicTable logic table name
     * @return assisted query and plain column count
     */
    public Integer getAssistedQueryAndPlainColumnCount(final String logicTable) {
        return getAssistedQueryColumns(logicTable).size() + getPlainColumns(logicTable).size();
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @param logicTable logic table 
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns(final String logicTable) {
        if (!tables.containsKey(logicTable)) {
            return Collections.emptyMap();
        }
        return tables.get(logicTable).getLogicAndCipherColumns();
    }
    
    /**
     * Get logic and plain columns.
     *
     * @param logicTable logic table 
     * @return logic and plain columns
     */
    public Map<String, String> getLogicAndPlainColumns(final String logicTable) {
        if (!tables.containsKey(logicTable)) {
            return Collections.emptyMap();
        }
        return tables.get(logicTable).getLogicAndPlainColumns();
    }
    
    /**
     * Get encrypt assisted column values.
     *
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalColumnValues original column values
     * @return assisted column values
     */
    public List<Object> getEncryptAssistedColumnValues(final String logicTable, final String logicColumn, final List<Object> originalColumnValues) {
        final Optional<ShardingEncryptor> shardingEncryptor = getShardingEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(shardingEncryptor.isPresent() && shardingEncryptor.get() instanceof ShardingQueryAssistedEncryptor,
                String.format("Can not find ShardingQueryAssistedEncryptor by %s.%s.", logicTable, logicColumn));
        return Lists.transform(originalColumnValues, new Function<Object, Object>() {
            
            @Override
            public Object apply(final Object input) {
                return ((ShardingQueryAssistedEncryptor) shardingEncryptor.get()).queryAssistedEncrypt(input.toString());
            }
        });
    }
    
    /**
     * get encrypt column values.
     *
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalColumnValues original column values
     * @return encrypt column values
     */
    public List<Object> getEncryptColumnValues(final String logicTable, final String logicColumn, final List<Object> originalColumnValues) {
        final Optional<ShardingEncryptor> shardingEncryptor = getShardingEncryptor(logicTable, logicColumn);
        Preconditions.checkArgument(shardingEncryptor.isPresent(), String.format("Can not find ShardingQueryAssistedEncryptor by %s.%s.", logicTable, logicColumn));
        return Lists.transform(originalColumnValues, new Function<Object, Object>() {
            
            @Override
            public Object apply(final Object input) {
                return String.valueOf(shardingEncryptor.get().encrypt(input.toString()));
            }
        });
    }
    
    /**
     * Get sharding encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn column name
     * @return optional of sharding encryptor
     */
    public Optional<ShardingEncryptor> getShardingEncryptor(final String logicTable, final String logicColumn) {
        if (!tables.containsKey(logicTable)) {
            return Optional.absent();
        }
        Optional<String> encryptor = tables.get(logicTable).getShardingEncryptor(logicColumn);
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
