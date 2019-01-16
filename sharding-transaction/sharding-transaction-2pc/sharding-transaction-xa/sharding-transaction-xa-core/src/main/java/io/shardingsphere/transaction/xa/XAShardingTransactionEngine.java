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
import io.shardingsphere.transaction.xa.jta.connection.SingleXAConnection;
import io.shardingsphere.transaction.xa.jta.datasource.SingleXADataSource;
import io.shardingsphere.transaction.xa.manager.XATransactionManagerLoader;
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
    
    private final Map<String, SingleXADataSource> cachedSingleXADataSourceMap = new HashMap<>();
    
    private final XATransactionManager xaTransactionManager = XATransactionManagerLoader.getInstance().getTransactionManager();
    
    @Override
    public void init(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = entry.getValue();
            if (dataSource instanceof AtomikosDataSourceBean) {
                continue;
            }
            String resourceName = entry.getKey();
            SingleXADataSource singleXADataSource = new SingleXADataSource(databaseType, resourceName, entry.getValue());
            cachedSingleXADataSourceMap.put(resourceName, singleXADataSource);
            xaTransactionManager.registerRecoveryResource(resourceName, singleXADataSource.getXaDataSource());
        }
        xaTransactionManager.init();
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @SneakyThrows
    @Override
    public boolean isInTransaction() {
        return Status.STATUS_NO_TRANSACTION != xaTransactionManager.getTransactionManager().getStatus();
    }
    
    @SneakyThrows
    @Override
    public Connection getConnection(final String dataSourceName) {
        SingleXAConnection singleXAConnection = cachedSingleXADataSourceMap.get(dataSourceName).getXAConnection();
        xaTransactionManager.enlistResource(singleXAConnection.getXAResource());
        return singleXAConnection.getConnection();
    }
    
    @SneakyThrows
    @Override
    public void begin() {
        xaTransactionManager.getTransactionManager().begin();
    }
    
    @SneakyThrows
    @Override
    public void commit() {
        xaTransactionManager.getTransactionManager().commit();
    }
    
    @SneakyThrows
    @Override
    public void rollback() {
        // TODO mybatis may call rollback twice, need investigate reason here 
        if (Status.STATUS_NO_TRANSACTION != xaTransactionManager.getTransactionManager().getStatus()) {
            xaTransactionManager.getTransactionManager().rollback();
        }
    }
    
    @Override
    public void close() throws Exception {
        for (SingleXADataSource each : cachedSingleXADataSourceMap.values()) {
            xaTransactionManager.removeRecoveryResource(each.getResourceName(), each.getXaDataSource());
        }
        cachedSingleXADataSourceMap.clear();
        xaTransactionManager.close();
    }
}
