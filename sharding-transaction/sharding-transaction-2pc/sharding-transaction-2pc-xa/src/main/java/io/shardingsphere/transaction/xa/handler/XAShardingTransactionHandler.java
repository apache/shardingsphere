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

package io.shardingsphere.transaction.xa.handler;

import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.context.XATransactionContext;
import io.shardingsphere.transaction.core.handler.ShardingTransactionHandlerAdapter;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.xa.manager.XATransactionManagerSPILoader;

/**
 * XA sharding transaction handler.
 *
 * @author zhaojun
 */
public final class XAShardingTransactionHandler extends ShardingTransactionHandlerAdapter<XATransactionContext> {
    
    @Override
    protected ShardingTransactionManager getShardingTransactionManager() {
        return XATransactionManagerSPILoader.getInstance().getTransactionManager();
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
}
