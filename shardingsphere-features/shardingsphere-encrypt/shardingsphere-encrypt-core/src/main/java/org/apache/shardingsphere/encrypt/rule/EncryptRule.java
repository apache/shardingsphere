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
import lombok.Getter;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.QueryAssistedEncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.DataTypeLoader;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
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
public final class EncryptRule implements SchemaRule, TableContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(EncryptAlgorithm.class);
    }
    
    @SuppressWarnings("rawtypes")
    private final Map<String, EncryptAlgorithm> encryptors = new LinkedHashMap<>();
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    @Getter
    private final boolean queryWithCipherColumn;
    
    public EncryptRule(final EncryptRuleConfiguration config, final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkArgument(isValidRuleConfiguration(config), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        config.getEncryptors().forEach((key, value) -> encryptors.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, EncryptAlgorithm.class)));
        Map<String, Integer> dataTypes = containsConfigDataTypeColumn(config.getTables()) ? getDataTypes(dataSourceMap) : Collections.emptyMap();
        config.getTables().forEach(each -> tables.put(each.getName(), new EncryptTable(each, dataTypes)));
        queryWithCipherColumn = config.isQueryWithCipherColumn();
    }
    
    public EncryptRule(final AlgorithmProvidedEncryptRuleConfiguration config, final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkArgument(isValidRuleConfigurationWithAlgorithmProvided(config), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        encryptors.putAll(config.getEncryptors());
        Map<String, Integer> dataTypes = containsConfigDataTypeColumn(config.getTables()) ? getDataTypes(dataSourceMap) : Collections.emptyMap();
        config.getTables().forEach(each -> tables.put(each.getName(), new EncryptTable(each, dataTypes)));
        queryWithCipherColumn = config.isQueryWithCipherColumn();
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
    
    private Map<String, Integer> getDataTypes(final Map<String, DataSource> dataSourceMap) {
        Optional<DataSource> dataSource = dataSourceMap.values().stream().findAny();
        if (dataSource.isPresent()) {
            try {
                return DataTypeLoader.load(dataSource.get().getConnection().getMetaData());
            } catch (SQLException ex) {
                throw new ShardingSphereConfigurationException("Can not load data types: %s", ex.getMessage());
            }
        }
        return Collections.emptyMap();
    }
    
    private boolean containsConfigDataTypeColumn(final Collection<EncryptTableRuleConfiguration> tableRuleConfigurations) {
        for (EncryptTableRuleConfiguration each : tableRuleConfigurations) {
            for (EncryptColumnRuleConfiguration column : each.getColumns()) {
                if (null != column.getLogicDataType() && !column.getLogicDataType().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
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
     * Find encrypt column.
     * 
     * @param logicTable logic table
     * @param columnName column name
     * @return encrypt column
     */
    public Optional<EncryptColumn> findEncryptColumn(final String logicTable, final String columnName) {
        return findEncryptTable(logicTable).flatMap(encryptTable -> encryptTable.findEncryptColumn(columnName));
    }
    
    /**
     * Find encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return encryptor
     */
    @SuppressWarnings("rawtypes")
    public Optional<EncryptAlgorithm> findEncryptor(final String logicTable, final String logicColumn) {
        return tables.containsKey(logicTable) ? tables.get(logicTable).findEncryptorName(logicColumn).map(encryptors::get) : Optional.empty();
    }
    
    /**
     * get encrypt values.
     *
     * @param schemaName schema name
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return encrypt values
     */
    @SuppressWarnings("rawtypes")
    public List<Object> getEncryptValues(final String schemaName, final String logicTable, final String logicColumn, final List<Object> originalValues) {
        Optional<EncryptAlgorithm> encryptor = findEncryptor(logicTable, logicColumn);
        EncryptContext encryptContext = EncryptContextBuilder.build(schemaName, logicTable, logicColumn, this);
        Preconditions.checkArgument(encryptor.isPresent(), "Can not find EncryptAlgorithm by %s.%s.", logicTable, logicColumn);
        return getEncryptValues(encryptor.get(), originalValues, encryptContext);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> getEncryptValues(final EncryptAlgorithm encryptor, final List<Object> originalValues, final EncryptContext encryptContext) {
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
        return tables.get(logicTable).getCipherColumn(logicColumn);
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
     * @param schemaName schema name
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return assisted query values
     */
    @SuppressWarnings("rawtypes")
    public List<Object> getEncryptAssistedQueryValues(final String schemaName, final String logicTable, final String logicColumn, final List<Object> originalValues) {
        Optional<EncryptAlgorithm> encryptor = findEncryptor(logicTable, logicColumn);
        EncryptContext encryptContext = EncryptContextBuilder.build(schemaName, logicTable, logicColumn, this);
        Preconditions.checkArgument(encryptor.isPresent() && encryptor.get() instanceof QueryAssistedEncryptAlgorithm,
                "Can not find QueryAssistedEncryptAlgorithm by %s.%s.", logicTable, logicColumn);
        return getEncryptAssistedQueryValues((QueryAssistedEncryptAlgorithm) encryptor.get(), originalValues, encryptContext);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> getEncryptAssistedQueryValues(final QueryAssistedEncryptAlgorithm encryptor, final List<Object> originalValues, final EncryptContext encryptContext) {
        List<Object> result = new LinkedList<>();
        for (Object each : originalValues) {
            result.add(null == each ? null : encryptor.queryAssistedEncrypt(each, encryptContext));
        }
        return result;
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
    
    /**
     * Judge whether table is support QueryWithCipherColumn or not.
     *
     * @param tableName table name
     * @return whether table is support QueryWithCipherColumn or not
     */
    public boolean isQueryWithCipherColumn(final String tableName) {
        return findEncryptTable(tableName).flatMap(EncryptTable::getQueryWithCipherColumn).orElse(queryWithCipherColumn);
    }
    
    private Optional<String> findOriginColumnName(final String logicTable, final String logicColumn) {
        for (String each : tables.get(logicTable).getLogicColumns()) {
            if (logicColumn.equalsIgnoreCase(each)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Collection<String> getTables() {
        return tables.keySet();
    }
    
    @Override
    public String getType() {
        return EncryptRule.class.getSimpleName();
    }
    
    /**
     * Set up encryptor schema.
     * 
     * @param schema schema
     */
    public void setUpEncryptorSchema(final ShardingSphereSchema schema) {
        for (EncryptAlgorithm<?, ?> each : encryptors.values()) {
            if (each instanceof SchemaMetaDataAware) {
                ((SchemaMetaDataAware) each).setSchema(schema);
            }
        }
    }
    
    /**
     * Check whether contains config data type or not.
     * 
     * @param tableName table name
     * @param columnName column name
     * @return boolean whether contains config data type or not
     */
    public boolean containsConfigDataType(final String tableName, final String columnName) {
        return findEncryptTable(tableName).flatMap(encryptTable -> encryptTable.findEncryptColumn(columnName)
                .filter(encryptColumn -> null != encryptColumn.getLogicDataType())).isPresent();
    }
}
