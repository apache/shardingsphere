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
import lombok.Getter;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.spi.algorithm.encrypt.ShardingEncryptor;

import java.util.List;

/**
 * Sharding encryptor strategy.
 *
 * @author panjuan
 */
@Getter
public final class ShardingEncryptorStrategy {
    
    private final List<String> columns;
    
    private final List<String> assistedQueryColumns;
    
    private final ShardingEncryptor shardingEncryptor;
    
    public ShardingEncryptorStrategy(final List<String> columns, final List<String> assistedQueryColumns, final ShardingEncryptor shardingEncryptor) {
        if (!assistedQueryColumns.isEmpty() && assistedQueryColumns.size() != columns.size()) {
            throw new ShardingConfigurationException("The size of `columns` and `assistedQueryColumns` is not same.");
        }
        this.columns = columns;
        this.assistedQueryColumns = assistedQueryColumns;
        this.shardingEncryptor = shardingEncryptor;
    }
    
    /**
     * Get assisted query column.
     * @param column column
     * @return assisted query column
     */
    public Optional<String> getAssistedQueryColumn(final String column) {
        return assistedQueryColumns.size() - 1 >= columns.indexOf(column) ? Optional.of(assistedQueryColumns.get(columns.indexOf(column))) : Optional.<String>absent();
    }
}
