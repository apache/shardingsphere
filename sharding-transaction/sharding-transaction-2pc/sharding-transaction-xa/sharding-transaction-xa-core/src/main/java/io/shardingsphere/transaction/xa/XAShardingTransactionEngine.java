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

package io.shardingsphere.transaction.xa;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.spi.ShardingTransactionEngine;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.datasource.ShardingXADataSource;
import io.shardingsphere.transaction.xa.manager.XATransactionManagerSPILoader;
import io.shardingsphere.transaction.xa.spi.XATransactionManager;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import javax.transaction.Status;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding transaction engine for XA.
 *
 * @author zhaojun
 */
public final class XAShardingTransactionEngine implements ShardingTransactionEngine {
    
    private final Map<String, ShardingXADataSource> cachedShardingXADataSourceMap = new HashMap<>();
    
    private final XATransactionManager xaTransactionManager = XATransactionManagerSPILoader.getInstance().getTransactionManager();
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @Override
    public void registerTransactionalResources(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = entry.getValue();
            if (dataSource instanceof AtomikosDataSourceBean) {
                continue;
            }
            String resourceName = entry.getKey();
            ShardingXADataSource shardingXADataSource = new ShardingXADataSource(databaseType, resourceName, entry.getValue());
            cachedShardingXADataSourceMap.put(resourceName, shardingXADataSource);
            xaTransactionManager.registerRecoveryResource(resourceName, shardingXADataSource.getXaDataSource());
        }
        xaTransactionManager.startup();
    }
    
    @Override
    public void clearTransactionalResources() {
        for (ShardingXADataSource each : cachedShardingXADataSourceMap.values()) {
            xaTransactionManager.removeRecoveryResource(each.getResourceName(), each.getXaDataSource());
        }
        cachedShardingXADataSourceMap.clear();
    }
    
    @SneakyThrows
    @Override
    public boolean isInTransaction() {
        return Status.STATUS_NO_TRANSACTION != xaTransactionManager.getUnderlyingTransactionManager().getTransaction().getStatus();
    }
    
    @SneakyThrows
    @Override
    public Connection getConnection(final String dataSourceName) {
        ShardingXAConnection shardingXAConnection = cachedShardingXADataSourceMap.get(dataSourceName).getXAConnection();
        xaTransactionManager.enlistResource(shardingXAConnection.getXAResource());
        return shardingXAConnection.getConnection();
    }
    
    @Override
    public void begin() {
        xaTransactionManager.begin();
    }
    
    @Override
    public void commit() {
        xaTransactionManager.commit();
    }
    
    @Override
    public void rollback() {
        xaTransactionManager.rollback();
    }
}
