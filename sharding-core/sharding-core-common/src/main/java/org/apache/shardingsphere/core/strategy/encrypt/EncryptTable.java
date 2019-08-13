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

package org.apache.shardingsphere.core.strategy.encrypt;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.api.config.encrypt.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.exception.ShardingException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Encryptor strategy.
 *
 * @author panjuan
 */
public final class EncryptTable {
    
    private final Map<String, EncryptColumn> columns = new LinkedHashMap<>();
    
    public EncryptTable(final EncryptTableRuleConfiguration config) { 
        columns.putAll(Maps.transformValues(config.getColumns(), new Function<EncryptColumnRuleConfiguration, EncryptColumn>() {
           
            @Override
            public EncryptColumn apply(final EncryptColumnRuleConfiguration input) {
                return new EncryptColumn(input.getPlainColumn(), input.getCipherColumn(), input.getAssistedQueryColumn(), input.getEncryptor());
            }
        }));
    }
    
    /**
     * Get logic column.
     * 
     * @param cipherColumn cipher column
     * @return logic column
     */
    public String getLogicColumn(final String cipherColumn) {
        for (Entry<String, EncryptColumn> entry : columns.entrySet()) {
            if (entry.getValue().getCipherColumn().equals(cipherColumn)) {
                return entry.getKey();
            }
        }
        throw new ShardingException("Can not find logic column by %s.", cipherColumn);
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
     * Get plain column.
     * 
     * @param logicColumn logic column name
     * @return plain column
     */
    public Optional<String> getPlainColumn(final String logicColumn) {
        return columns.keySet().contains(logicColumn) ? columns.get(logicColumn).getPlainColumn() : Optional.<String>absent();
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
     * Is has plain column or not.
     *
     * @return has plain column or not
     */
    public boolean isHasPlainColumn() {
        for (EncryptColumn each : columns.values()) {
            if (each.getPlainColumn().isPresent()) {
                return true;
            }
        }
        return false;
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
     * Get assisted query column.
     *
     * @param logicColumn column name
     * @return assisted query column
     */
    public Optional<String> getAssistedQueryColumn(final String logicColumn) {
        return columns.containsKey(logicColumn) ? columns.get(logicColumn).getAssistedQueryColumn() : Optional.<String>absent();
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
     * Is has query assisted column or not.
     *
     * @return has query assisted column or not
     */
    public boolean isHasQueryAssistedColumn() {
        for (EncryptColumn each : columns.values()) {
            if (each.getAssistedQueryColumn().isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get sharding encryptor.
     *
     * @param logicColumn column name
     * @return optional of sharding encryptor
     */
    public Optional<String> getShardingEncryptor(final String logicColumn) {
        return columns.containsKey(logicColumn) ? Optional.of(columns.get(logicColumn).getEncryptor()) : Optional.<String>absent();
    }
    
    /**
     * Get logic and cipher columns.
     *
     * @return logic and cipher columns
     */
    public Map<String, String> getLogicAndCipherColumns() {
        return Maps.transformValues(columns, new Function<EncryptColumn, String>() {
            
            @Override
            public String apply(final EncryptColumn input) {
                return input.getCipherColumn();
            }
        });
    }
    
    /**
     * Get logic and plain columns.
     *
     * @return logic and plain columns
     */
    public Map<String, String> getLogicAndPlainColumns() {
        return Maps.transformValues(columns, new Function<EncryptColumn, String>() {
            
            @Override
            public String apply(final EncryptColumn input) {
                if (input.getPlainColumn().isPresent()) {
                    return input.getPlainColumn().get();
                }
                throw new ShardingException("Plain column is null.");
            }
        });
    }
}
