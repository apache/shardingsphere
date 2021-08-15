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

package org.apache.shardingsphere.infra.context.manager;

import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

import java.util.Optional;

/**
 * Context manager.
 */
public interface ContextManager {
    
    /**
     * Initialize context manager.
     * 
     * @param metaDataContexts meta data contexts
     * @param transactionContexts transaction contexts
     */
    void init(MetaDataContexts metaDataContexts, TransactionContexts transactionContexts);
    
    /**
     * Get meta data contexts.
     * 
     * @return meta data contexts
     */
    MetaDataContexts getMetaDataContexts();
    
    /**
     * Renew meta data contexts.
     *
     * @param metaDataContexts meta data contexts
     */
    void renewMetaDataContexts(MetaDataContexts metaDataContexts);
    
    /**
     * Get transaction contexts.
     *
     * @return transaction contexts
     */
    TransactionContexts getTransactionContexts();
    
    /**
     * Renew transaction contexts.
     *
     * @param transactionContexts transaction contexts
     */
    void renewTransactionContexts(TransactionContexts transactionContexts);
    
    /**
     * Get lock.
     *
     * @return lock
     */
    Optional<ShardingSphereLock> getLock();
}
