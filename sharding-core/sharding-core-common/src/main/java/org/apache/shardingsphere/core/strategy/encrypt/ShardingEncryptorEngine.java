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

import com.google.common.base.Optional;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding encryptor engine.
 *
 * @author panjuan
 */
public final class ShardingEncryptorEngine {
    
    
    private final Collection<ShardingEncryptorStrategy> shardingEncryptorStrategies = new LinkedList<>();
    
    public ShardingEncryptorEngine(final EncryptRuleConfiguration encryptRuleConfiguration) {
        for (EncryptorRuleConfiguration each : encryptRuleConfiguration.getEncryptorRuleConfigs()) {
            shardingEncryptorStrategies.add(new ShardingEncryptorStrategy(each));
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
        for (ShardingEncryptorStrategy each : shardingEncryptorStrategies) {
            Optional<ShardingEncryptor> result = each.getShardingEncryptor(logicTableName, columnName);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    /**
     * Is has sharding query assisted encryptor or not.
     * 
     * @param logicTableName logic table name
     * @return has sharding query assisted encryptor or not
     */
    public boolean isHasShardingQueryAssistedEncryptor(final String logicTableName) {
        for (ShardingEncryptorStrategy each : shardingEncryptorStrategies) {
            if (each.isHasShardingQueryAssistedEncryptor(logicTableName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get assisted query column.
     * 
     * @param logicTableName logic table name
     * @param columnName column name
     * @return assisted query column
     */
    public Optional<String> getAssistedQueryColumn(final String logicTableName, final String columnName) {
        for (ShardingEncryptorStrategy each : shardingEncryptorStrategies) {
            Optional<String> result = each.getAssistedQueryColumn(logicTableName, columnName);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    /**
     * Get assisted query column count.
     * 
     * @param logicTableName logic table name
     * @return assisted query column count
     */
    public Optional<Integer> getAssistedQueryColumnCount(final String logicTableName) {
        if (!shardingEncryptorStrategies.containsKey(logicTableName)) {
            return Optional.absent();
        }
        return Optional.of(shardingEncryptorStrategies.get(logicTableName).getAssistedQueryColumns().size());
    }
}
