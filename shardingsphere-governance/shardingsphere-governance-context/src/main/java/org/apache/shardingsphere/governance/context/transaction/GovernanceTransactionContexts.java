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
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceChangeCompletedEvent;
import org.apache.shardingsphere.governance.core.registry.config.event.datasource.DataSourceDeletedEvent;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Governance transaction contexts.
 */
public final class GovernanceTransactionContexts implements TransactionContexts {
    
    private volatile TransactionContexts contexts;
    
    private final String xaTransactionMangerType;
    
    public GovernanceTransactionContexts(final TransactionContexts contexts, final String xaTransactionMangerType) {
        this.contexts = contexts;
        this.xaTransactionMangerType = xaTransactionMangerType;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Renew transaction manager engine contexts.
     *
     * @param event data source change completed event
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangeCompletedEvent event) throws Exception {
        closeStaleEngine(event.getSchemaName());
        Map<String, ShardingTransactionManagerEngine> existedEngines = contexts.getEngines();
        existedEngines.put(event.getSchemaName(), createNewEngine(event.getDatabaseType(), event.getDataSources()));
        renewContexts(existedEngines);
    }
    
    /**
     * Renew transaction manager engine context.
     * 
     * @param event data source deleted event.
     * @throws Exception exception
     */
    @Subscribe
    public synchronized void renew(final DataSourceDeletedEvent event) throws Exception {
        closeStaleEngine(event.getSchemaName());
        renewContexts(contexts.getEngines());
    }
    
    private void closeStaleEngine(final String schemaName) throws Exception {
        ShardingTransactionManagerEngine staleEngine = contexts.getEngines().remove(schemaName);
        if (null != staleEngine) {
            staleEngine.close();
        }
    }
    
    private ShardingTransactionManagerEngine createNewEngine(final DatabaseType databaseType, final Map<String, DataSource> dataSources) {
        ShardingTransactionManagerEngine result = new ShardingTransactionManagerEngine();
        result.init(databaseType, dataSources, xaTransactionMangerType);
        return result;
    }
    
    private void renewContexts(final Map<String, ShardingTransactionManagerEngine> engines) {
        contexts = new StandardTransactionContexts(engines);
    }
    
    @Override
    public Map<String, ShardingTransactionManagerEngine> getEngines() {
        return contexts.getEngines();
    }
    
    @Override
    public void close() throws Exception {
        contexts.close();
    }
}
