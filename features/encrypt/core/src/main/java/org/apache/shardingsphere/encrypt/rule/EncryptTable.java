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
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLogicColumnNotFoundException;

import java.util.Collection;
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
    
    public EncryptTable(final EncryptTableRuleConfiguration config) {
        columns = createEncryptColumns(config);
    }
    
    private Map<String, EncryptColumn> createEncryptColumns(final EncryptTableRuleConfiguration config) {
        Map<String, EncryptColumn> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (EncryptColumnRuleConfiguration each : config.getColumns()) {
            result.put(each.getName(), createEncryptColumn(each));
        }
        return result;
    }
    
    private EncryptColumn createEncryptColumn(final EncryptColumnRuleConfiguration config) {
        EncryptColumnItem cipherColumnItem = new EncryptColumnItem(config.getCipher().getName(), config.getCipher().getEncryptorName());
        EncryptColumn result = new EncryptColumn(config.getName(), cipherColumnItem);
        if (config.getAssistedQuery().isPresent()) {
            EncryptColumnItem assistedQueryColumn = new EncryptColumnItem(config.getAssistedQuery().get().getName(), config.getAssistedQuery().get().getEncryptorName());
            result.setAssistedQuery(assistedQueryColumn);
        }
        if (config.getLikeQuery().isPresent()) {
            EncryptColumnItem likeQueryColumn = new EncryptColumnItem(config.getLikeQuery().get().getName(), config.getLikeQuery().get().getEncryptorName());
            result.setLikeQuery(likeQueryColumn);
        }
        return result;
    }
    
    /**
     * Find encrypt algorithm name.
     *
     * @param logicColumn column name
     * @return encrypt algorithm name
     */
    public Optional<String> findEncryptorName(final String logicColumn) {
        return columns.containsKey(logicColumn) ? Optional.of(columns.get(logicColumn).getCipher().getEncryptorName()) : Optional.empty();
    }
    
    /**
     * Find assisted query encrypt algorithm name.
     *
     * @param logicColumn column name
     * @return assist encrypt algorithm name
     */
    public Optional<String> findAssistedQueryEncryptorName(final String logicColumn) {
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getAssistedQuery().map(EncryptColumnItem::getEncryptorName) : Optional.empty();
    }
    
    /**
     * Find like query encrypt algorithm name.
     *
     * @param logicColumn column name
     * @return like encrypt algorithm name
     */
    public Optional<String> findLikeQueryEncryptorName(final String logicColumn) {
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getLikeQuery().map(EncryptColumnItem::getEncryptorName) : Optional.empty();
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
     * @throws EncryptLogicColumnNotFoundException encrypt logic column not found exception
     */
    public String getLogicColumnByCipherColumn(final String cipherColumn) {
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            if (entry.getValue().getCipher().getName().equalsIgnoreCase(cipherColumn)) {
                return entry.getKey();
            }
        }
        throw new EncryptLogicColumnNotFoundException(cipherColumn);
    }
    
    /**
     * Is cipher column or not.
     *
     * @param columnName column name
     * @return cipher column or not
     */
    public boolean isCipherColumn(final String columnName) {
        return columns.values().stream().anyMatch(each -> each.getCipher().getName().equalsIgnoreCase(columnName));
    }
    
    /**
     * Get cipher column.
     *
     * @param logicColumn logic column name
     * @return cipher column
     */
    public String getCipherColumn(final String logicColumn) {
        return columns.get(logicColumn).getCipher().getName();
    }
    
    /**
     * Get assisted query columns.
     *
     * @return assisted query columns
     */
    public Collection<String> getAssistedQueryColumns() {
        Collection<String> result = new LinkedList<>();
        for (EncryptColumn each : columns.values()) {
            if (each.getAssistedQuery().isPresent()) {
                result.add(each.getAssistedQuery().get().getName());
            }
        }
        return result;
    }
    
    /**
     * Get like query columns.
     *
     * @return like query columns
     */
    public Collection<String> getLikeQueryColumns() {
        Collection<String> result = new LinkedList<>();
        for (EncryptColumn each : columns.values()) {
            if (each.getLikeQuery().isPresent()) {
                result.add(each.getLikeQuery().get().getName());
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
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getAssistedQuery().map(EncryptColumnItem::getName) : Optional.empty();
    }
    
    /**
     * Find like query column.
     *
     * @param logicColumn column name
     * @return like query column
     */
    public Optional<String> findLikeQueryColumn(final String logicColumn) {
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getLikeQuery().map(EncryptColumnItem::getName) : Optional.empty();
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns() {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getCipher().getName());
        }
        return result;
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
