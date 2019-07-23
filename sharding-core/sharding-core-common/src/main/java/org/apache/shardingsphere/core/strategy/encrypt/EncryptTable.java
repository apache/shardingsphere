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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

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
     * Get sharding encryptor.
     *
     * @param logicColumn column name
     * @return optional of sharding encryptor
     */
    public Optional<String> getShardingEncryptor(final String logicColumn) { 
        return columns.containsKey(logicColumn) ? Optional.of(columns.get(logicColumn).getEncryptor()) : Optional.<String>absent();
    }
    
    /**
     * Is has sharding query assisted encryptor or not.
     *
     * @return has sharding query assisted encryptor or not
     */
    public boolean isHasShardingQueryAssistedEncryptor() {
        for (EncryptColumn each : columns.values()) {
            if (each.getAssistedQueryColumn().isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get assisted query column.
     *
     * @param logicColumn column name
     * @return assisted query column
     */
    public Optional<String> getAssistedQueryColumn(final String logicColumn) {
        if (!columns.containsKey(logicColumn)) {
            return Optional.absent();
        }
        return columns.get(logicColumn).getAssistedQueryColumn();
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
     * Get plain column.
     * 
     * @param logicColumn logic column name
     * @return plain column
     */
    public Optional<String> getPlainColumn(final String logicColumn) {
        return columns.get(logicColumn).getPlainColumn();
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
}
