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

package org.apache.shardingsphere.encrypt.strategy;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Encryptor table.
 */
public final class EncryptTable {
    
    private final Map<String, EncryptColumn> columns;
    
    public EncryptTable(final EncryptTableRuleConfiguration config) {
        columns = new LinkedHashMap<>(new LinkedHashMap<>(Maps.transformValues(config.getColumns(), 
            input -> new EncryptColumn(input.getCipherColumn(), input.getAssistedQueryColumn(), input.getPlainColumn(), input.getEncryptor()))));
    }
    
    /**
     * Get logic column of cipher column.
     * 
     * @param cipherColumn cipher column
     * @return logic column
     */
    public String getLogicColumnOfCipher(final String cipherColumn) {
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            if (entry.getValue().getCipherColumn().equals(cipherColumn)) {
                return entry.getKey();
            }
        }
        throw new ShardingSphereException("Can not find logic column by %s.", cipherColumn);
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
     * Find plain column.
     * 
     * @param logicColumn logic column name
     * @return plain column
     */
    public Optional<String> findPlainColumn(final String logicColumn) {
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getPlainColumn() : Optional.empty();
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
     * Get cipher column.
     *
     * @param logicColumn logic column name
     * @return cipher column
     */
    public String getCipherColumn(final String logicColumn) {
        return columns.get(logicColumn).getCipherColumn();
    }
    
    /**
     * Get cipher columns.
     *
     * @return cipher columns
     */
    public Collection<String> getCipherColumns() {
        Collection<String> result = new LinkedList<>();
        for (EncryptColumn each : columns.values()) {
            result.add(each.getCipherColumn());
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
     * Find encryptor.
     *
     * @param logicColumn column name
     * @return encryptor
     */
    public Optional<String> findEncryptor(final String logicColumn) {
        Optional<String> originLogicColumnName = findOriginLogicColumnName(logicColumn);
        return originLogicColumnName.isPresent() && columns.containsKey(originLogicColumnName.get())
                ? Optional.of(columns.get(originLogicColumnName.get()).getEncryptor()) : Optional.empty();
    }
    
    private Optional<String> findOriginLogicColumnName(final String logicColumn) {
        for (String each : columns.keySet()) {
            if (logicColumn.equalsIgnoreCase(each)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns() {
        return Maps.transformValues(columns, EncryptColumn::getCipherColumn);
    }
    
    /**
     * Get logic and plain columns.
     *
     * @return logic and plain columns
     */
    public Map<String, String> getLogicAndPlainColumns() {
        return Maps.transformValues(columns, input -> {
            if (input.getPlainColumn().isPresent()) {
                return input.getPlainColumn().get();
            }
            throw new ShardingSphereException("Plain column is null.");
        });
    }
}
