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

package org.apache.shardingsphere.core.encrypt;

import com.google.common.base.Optional;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding encryptor engine.
 *
 * @author panjuan
 */
public final class ShardingEncryptorEngine {
    
    private final Map<String, ShardingEncryptorStrategy> shardingEncryptorStrategies = new LinkedHashMap<>();
    
    public ShardingEncryptorEngine(final Map<String, ShardingEncryptorStrategy> shardingEncryptorStrategies) {
        for (Entry<String, ShardingEncryptorStrategy> entry : shardingEncryptorStrategies.entrySet()) {
            if (null != entry.getValue()) {
                this.shardingEncryptorStrategies.put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    public ShardingEncryptorEngine(final Map<String, ShardingEncryptorStrategy> shardingEncryptorStrategies, final ShardingEncryptorStrategy defaultEncryptorStrategy) {
        for (Entry<String, ShardingEncryptorStrategy> entry : shardingEncryptorStrategies.entrySet()) {
            if (null != entry.getValue()) {
                this.shardingEncryptorStrategies.put(entry.getKey(), entry.getValue());
            } else {
                this.shardingEncryptorStrategies.put(entry.getKey(), defaultEncryptorStrategy);
            }
        }
    }
    
    
    /**
     * Get sharding encryptor.
     * 
     * @param logicTableName logic table name
     * @param columnName column name
     * @return optional of sharding encryptor
     */
    public Optional<ShardingEncryptor> getShardingEncryptor(final String logicTableName, final String columnName) {
        if (shardingEncryptorStrategies.keySet().contains(logicTableName) && shardingEncryptorStrategies.get(logicTableName).getColumns().contains(columnName)) {
            return Optional.of(shardingEncryptorStrategies.get(logicTableName).getShardingEncryptor());
        }
        return Optional.absent();
    }
    
    /**
     * Is has sharding encryptor strategy or not.
     * 
     * @param logicTableName logic table name
     * @return sharding encryptor strategy or not
     */
    public boolean isHasShardingEncryptorStrategy(final String logicTableName) {
        return shardingEncryptorStrategies.keySet().contains(logicTableName);
    }
    
    /**
     * Get assisted query column.
     * 
     * @param logicTableName logic table name
     * @param columnName column name
     * @return assisted query column
     */
    public Optional<String> getAssistedQueryColumn(final String logicTableName, final String columnName) {
        return shardingEncryptorStrategies.get(logicTableName).getAssistedQueryColumn(columnName);
    }
}
