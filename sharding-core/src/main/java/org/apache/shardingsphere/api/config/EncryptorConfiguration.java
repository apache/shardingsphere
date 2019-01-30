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

package org.apache.shardingsphere.api.config;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.encrypt.ShardingEncryptorFactory;
import org.apache.shardingsphere.core.encrypt.encryptor.ShardingEncryptor;
import org.apache.shardingsphere.core.routing.strategy.ShardingEncryptorStrategy;

import java.util.Properties;

/**
 * Encryptor configuration.
 *
 * @author panjuan
 */
@AllArgsConstructor
@Getter
@Setter
public final class EncryptorConfiguration {
    
    private String type;
    
    private String columns;
    
    private String assistedQueryColumns;
    
    private Properties props;
    
    /**
     * Get sharding encryptor strategy.
     *
     * @return sharding encryptor strategy
     */
    public ShardingEncryptorStrategy getShardingEncryptorStrategy() {
        if (Strings.isNullOrEmpty(type) || Strings.isNullOrEmpty(columns)) {
            return null;
        }
        ShardingEncryptor shardingEncryptor = ShardingEncryptorFactory.newInstance(type);
        shardingEncryptor.setProperties(props);
        return getShardingEncryptorStrategy(shardingEncryptor);
    }
    
    private ShardingEncryptorStrategy getShardingEncryptorStrategy(final ShardingEncryptor shardingEncryptor) {
        return Strings.isNullOrEmpty(assistedQueryColumns) ? new ShardingEncryptorStrategy(Splitter.on(",").trimResults().splitToList(columns), shardingEncryptor) 
                : new ShardingEncryptorStrategy(Splitter.on(",").trimResults().splitToList(columns), Splitter.on(",").trimResults().splitToList(assistedQueryColumns), shardingEncryptor);
    }
}
