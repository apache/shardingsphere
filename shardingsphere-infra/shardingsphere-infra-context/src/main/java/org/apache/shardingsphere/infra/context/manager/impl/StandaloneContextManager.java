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

package org.apache.shardingsphere.infra.context.manager.impl;

import lombok.Getter;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import java.util.Optional;

/**
 * Standalone context manager.
 */
@Getter
public final class StandaloneContextManager implements ContextManager {
    
    private volatile StandardMetaDataContexts metaDataContexts;
    
    private volatile StandardTransactionContexts transactionContexts;
    
    @Override
    public void init(final StandardMetaDataContexts metaDataContexts, final StandardTransactionContexts transactionContexts) {
        this.metaDataContexts = metaDataContexts;
        this.transactionContexts = transactionContexts;
    }
    
    @Override
    public synchronized void renewMetaDataContexts(final StandardMetaDataContexts metaDataContexts) {
        this.metaDataContexts = metaDataContexts;
    }
    
    @Override
    public synchronized void renewTransactionContexts(final StandardTransactionContexts transactionContexts) {
        this.transactionContexts = transactionContexts;
    }
    
    @Override
    public Optional<ShardingSphereLock> getLock() {
        return Optional.empty();
    }
}
