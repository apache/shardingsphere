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

package io.shardingsphere.transaction.core;

import io.shardingsphere.core.exception.ShardingException;

/**
 * Sharding transaction manager.
 *
 * @author zhaojun
 * @author zhangliang
 * 
 */
public interface ShardingTransactionManager {
    
    /**
     * Begin transaction.
     *
     * @throws ShardingException sharding exception
     */
    void begin() throws ShardingException;
    
    /**
     * Commit transaction.
     *
     * @throws ShardingException sharding exception
     */
    void commit() throws ShardingException;
    
    /**
     * Rollback transaction.
     *
     * @throws ShardingException sharding exception
     */
    void rollback() throws ShardingException;
    
    /**
     * Obtain the status of the transaction associated with the current thread.
     *
     * @return Transaction status. Returns {@code Status.NoTransaction} if no transaction is associated with current thread.
     * @throws ShardingException sharding exception
     */
    int getStatus() throws ShardingException;
}
