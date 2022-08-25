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

import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.exception.EncryptLogicColumnNotFoundException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Encrypt table.
 */
public final class EncryptTable {
    
    private final Map<String, EncryptColumn> columns;
    
    private final Boolean queryWithCipherColumn;
    
    public EncryptTable(final EncryptTableRuleConfiguration config) {
        columns = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (EncryptColumnRuleConfiguration each : config.getColumns()) {
            columns.put(each.getLogicColumn(), new EncryptColumn(each.getCipherColumn(), each.getAssistedQueryColumn(), each.getPlainColumn(), each.getEncryptorName(),
                    each.getAssistedQueryEncryptorName(), each.getQueryWithCipherColumn()));
        }
        queryWithCipherColumn = config.getQueryWithCipherColumn();
    }
    
    /**
     * Find encrypt algorithm name.
     *
     * @param logicColumn column name
     * @return encrypt algorithm name
     */
    public Optional<String> findEncryptorName(final String logicColumn) {
        return columns.containsKey(logicColumn) ? Optional.of(columns.get(logicColumn).getEncryptorName()) : Optional.empty();
    }
    
    /**
     * Find assisted query encrypt algorithm name.
     *
     * @param logicColumn column name
     * @return assist encrypt algorithm name
     */
    public Optional<String> findAssistedQueryEncryptorName(final String logicColumn) {
        return columns.containsKey(logicColumn) ? Optional.ofNullable(columns.get(logicColumn).getAssistedQueryEncryptorName()) : Optional.empty();
    }
    
    /**
     * Get logic columns.
     *
     * @return logic column
     */
    public Collection<String> getLogicColumns() {
        return columns.keySet();
    }
    
    /**
     * Get logic column by cipher column.
     * 
     * @param cipherColumn cipher column
     * @return logic column
     */
    public String getLogicColumnByCipherColumn(final String cipherColumn) {
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            if (entry.getValue().getCipherColumn().equals(cipherColumn)) {
                return entry.getKey();
            }
        }
        throw new EncryptLogicColumnNotFoundException(cipherColumn);
    }
    
    /**
     * Get logic column by plain column.
     *
     * @param plainColumn plain column
     * @return logic column
     */
    public String getLogicColumnByPlainColumn(final String plainColumn) {
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            if (entry.getValue().getPlainColumn().isPresent() && entry.getValue().getPlainColumn().get().equals(plainColumn)) {
                return entry.getKey();
            }
        }
        throw new EncryptLogicColumnNotFoundException(plainColumn);
    }
    
    /**
     * Is cipher column or not.
     *
     * @param columnName column name
     * @return cipher column or not
     */
    public boolean isCipherColumn(final String columnName) {
        return columns.values().stream().anyMatch(each -> each.getCipherColumn().equalsIgnoreCase(columnName));
    }
    
    /**
     * Get cipher column.
     *
     * @param logicColumn logic column name
     * @return cipher column
     */
    public String getCipherColumn(final String logicColumn) {
        return columns.get(logicColumn).getCipherColumn();
    }
    
    /**
     * Get assisted query columns.
     *
     * @return assisted query columns
     */
    public Collection<String> getAssistedQueryColumns() {
        Collection<String> result = new LinkedList<>();
        for (EncryptColumn each : columns.values()) {
            if (each.getAssistedQueryColumn().isPresent()) {
                result.add(each.getAssistedQueryColumn().get());
            }
        }
        return result;
    }
    
    /**
     * Find assisted query column.
     *
     * @param logicColumn column name
     * @return assisted query column
     */
    public Optional<String> findAssistedQueryColumn(final String logicColumn) {
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getAssistedQueryColumn() : Optional.empty();
    }
    
    /**
     * Get plain columns.
     *
     * @return plain columns
     */
    public Collection<String> getPlainColumns() {
        Collection<String> result = new LinkedList<>();
        for (EncryptColumn each : columns.values()) {
            if (each.getPlainColumn().isPresent()) {
                result.add(each.getPlainColumn().get());
            }
        }
        return result;
    }
    
    /**
     * Find plain column.
     *
     * @param logicColumn logic column name
     * @return plain column
     */
    public Optional<String> findPlainColumn(final String logicColumn) {
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getPlainColumn() : Optional.empty();
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns() {
        Map<String, String> result = new HashMap<>(columns.size(), 1);
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getCipherColumn());
        }
        return result;
    }
    
    /**
     * Get query with cipher column.
     *
     * @param logicColumn logic column
     * @return query with cipher column
     */
    public Optional<Boolean> getQueryWithCipherColumn(final String logicColumn) {
        return Optional.ofNullable(findEncryptColumn(logicColumn).map(EncryptColumn::getQueryWithCipherColumn).orElse(queryWithCipherColumn));
    }
    
    /**
     * Find encrypt column.
     * 
     * @param logicColumn logic column
     * @return encrypt column
     */
    public Optional<EncryptColumn> findEncryptColumn(final String logicColumn) {
        return Optional.ofNullable(columns.get(logicColumn));
    }
    
}
