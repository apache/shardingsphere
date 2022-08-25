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
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.context.EncryptContextBuilder;
import org.apache.shardingsphere.encrypt.factory.EncryptAlgorithmFactory;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

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
public final class EncryptRule implements DatabaseRule, TableContainedRule {
    
    @Getter
    private final RuleConfiguration configuration;
    
    @SuppressWarnings("rawtypes")
    private final Map<String, EncryptAlgorithm> encryptors = new LinkedHashMap<>();
    
    private final Map<String, EncryptTable> tables = new LinkedHashMap<>();
    
    @Getter
    private final boolean queryWithCipherColumn;
    
    public EncryptRule(final EncryptRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        ruleConfig.getEncryptors().forEach((key, value) -> encryptors.put(key, EncryptAlgorithmFactory.newInstance(value)));
        ruleConfig.getTables().forEach(each -> tables.put(each.getName().toLowerCase(), new EncryptTable(each)));
        queryWithCipherColumn = ruleConfig.isQueryWithCipherColumn();
    }
    
    public EncryptRule(final AlgorithmProvidedEncryptRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        encryptors.putAll(ruleConfig.getEncryptors());
        ruleConfig.getTables().forEach(each -> tables.put(each.getName().toLowerCase(), new EncryptTable(each)));
        queryWithCipherColumn = ruleConfig.isQueryWithCipherColumn();
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
     * Find encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return encryptor
     */
    @SuppressWarnings("rawtypes")
    public Optional<EncryptAlgorithm> findEncryptor(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findEncryptorName(logicColumn).map(encryptors::get) : Optional.empty();
    }
    
    /**
     * Find assist encryptor.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return encryptor
     */
    @SuppressWarnings("rawtypes")
    public Optional<EncryptAlgorithm> findAssistedQueryEncryptor(final String logicTable, final String logicColumn) {
        String lowerCaseLogicTable = logicTable.toLowerCase();
        return tables.containsKey(lowerCaseLogicTable) ? tables.get(lowerCaseLogicTable).findAssistedQueryEncryptorName(logicColumn).map(encryptors::get) : Optional.empty();
    }
    
    /**
     * get encrypt values.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param logicTable logic table
     * @param logicColumn logic column
     * @param originalValues original values
     * @return encrypt values
     */
    @SuppressWarnings("rawtypes")
    public List<Object> getEncryptValues(final String databaseName, final String schemaName, final String logicTable, final String logicColumn, final List<Object> originalValues) {
        Optional<EncryptAlgorithm> encryptor = findEncryptor(logicTable, logicColumn);
        EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, logicTable, logicColumn);
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
    @SuppressWarnings("rawtypes")
    public List<Object> getEncryptAssistedQueryValues(final String databaseName, final String schemaName, final String logicTable, final String logicColumn, final List<Object> originalValues) {
        Optional<EncryptAlgorithm> encryptor = findAssistedQueryEncryptor(logicTable, logicColumn);
        EncryptContext encryptContext = EncryptContextBuilder.build(databaseName, schemaName, logicTable, logicColumn);
        Preconditions.checkArgument(encryptor.isPresent(), "Can not find assist encryptor by %s.%s.", logicTable, logicColumn);
        return getEncryptAssistedQueryValues(encryptor.get(), originalValues, encryptContext);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> getEncryptAssistedQueryValues(final EncryptAlgorithm encryptor, final List<Object> originalValues, final EncryptContext encryptContext) {
        List<Object> result = new LinkedList<>();
        for (Object each : originalValues) {
            result.add(null == each ? null : encryptor.encrypt(each, encryptContext));
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
        return originColumnName.isPresent() && tables.containsKey(logicTable.toLowerCase()) ? tables.get(logicTable.toLowerCase()).findPlainColumn(originColumnName.get()) : Optional.empty();
    }
    
    /**
     * Judge whether column is support QueryWithCipherColumn or not.
     *
     * @param logicTable logic table name
     * @param logicColumn logic column name
     * @return whether column is support QueryWithCipherColumn or not
     */
    public boolean isQueryWithCipherColumn(final String logicTable, final String logicColumn) {
        return findEncryptTable(logicTable).flatMap(encryptTable -> encryptTable.getQueryWithCipherColumn(logicColumn)).orElse(queryWithCipherColumn);
    }
    
    private Optional<String> findOriginColumnName(final String logicTable, final String logicColumn) {
        for (String each : tables.get(logicTable.toLowerCase()).getLogicColumns()) {
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
     * Set schema meta data.
     *
     * @param databaseName database name
     * @param schemas schema map
     */
    public void setSchemaMetaData(final String databaseName, final Map<String, ShardingSphereSchema> schemas) {
        for (EncryptAlgorithm<?, ?> each : encryptors.values()) {
            if (each instanceof SchemaMetaDataAware) {
                ((SchemaMetaDataAware) each).setDatabaseName(databaseName);
                ((SchemaMetaDataAware) each).setSchemas(schemas);
            }
        }
    }
}
