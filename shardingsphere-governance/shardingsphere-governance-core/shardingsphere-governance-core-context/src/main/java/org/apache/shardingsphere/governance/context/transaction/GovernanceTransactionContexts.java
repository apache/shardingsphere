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

package org.apache.shardingsphere.governance.context.transaction;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.event.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

import java.util.Map;

/**
 * Governance transaction contexts.
 */
public final class GovernanceTransactionContexts implements TransactionContexts {
    
    private final TransactionContexts contexts;
    
    public GovernanceTransactionContexts(final TransactionContexts contexts) {
        this.contexts = contexts;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    @Override
    public Map<String, ShardingTransactionManagerEngine> getEngines() {
        return contexts.getEngines();
    }
    
    @Override
    public ShardingTransactionManagerEngine getDefaultTransactionManagerEngine() {
        return contexts.getDefaultTransactionManagerEngine();
    }
    
    @Override
    public void close() throws Exception {
        contexts.close();
    }
    
    /**
     * Renew transaction manager engine contexts.
     *
     * @param event data source change completed event
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangeCompletedEvent event) throws Exception {
        ShardingTransactionManagerEngine oldEngine = contexts.getEngines().remove(event.getSchemaName());
        if (null != oldEngine) {
            oldEngine.close();
        }
        ShardingTransactionManagerEngine newEngine = new ShardingTransactionManagerEngine();
        newEngine.init(event.getDatabaseType(), event.getDataSources());
        contexts.getEngines().put(event.getSchemaName(), newEngine);
    }
}
