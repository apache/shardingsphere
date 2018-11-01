/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.core.datasource;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.transaction.ShardingTransactionEvent;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.spi.transaction.ShardingTransactionManager;

import java.util.HashMap;
import java.util.Map;

public final class FixedXAShardingTransactionManager implements ShardingTransactionManager {
    
    private static final Map<String, Object> INVOKES = new HashMap<>();
    
    /**
     * Get invoke map.
     *
     * @return map
     */
    public static Map<String, Object> getInvokes() {
        return INVOKES;
    }
    
    @Override
    public void begin(final ShardingTransactionEvent transactionEvent) throws ShardingException {
        INVOKES.put("begin", transactionEvent);
    }
    
    @Override
    public void commit(final ShardingTransactionEvent transactionEvent) throws ShardingException {
        INVOKES.put("commit", transactionEvent);
    }
    
    @Override
    public void rollback(final ShardingTransactionEvent transactionEvent) throws ShardingException {
        INVOKES.put("rollback", transactionEvent);
    }
    
    @Override
    public int getStatus() throws ShardingException {
        return 0;
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
}
