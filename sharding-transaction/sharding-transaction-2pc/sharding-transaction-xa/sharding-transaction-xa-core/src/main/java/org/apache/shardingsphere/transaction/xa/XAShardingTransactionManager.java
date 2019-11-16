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

package org.apache.shardingsphere.transaction.xa;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import lombok.SneakyThrows;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;
import org.apache.shardingsphere.transaction.xa.jta.connection.XATransactionConnection;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XATransactionDataSource;
import org.apache.shardingsphere.transaction.xa.manager.XATransactionManagerLoader;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;

import javax.sql.DataSource;
import javax.transaction.Status;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sharding transaction manager for XA.
 *
 * @author zhaojun
 */
public final class XAShardingTransactionManager implements ShardingTransactionManager {
    
    private final Map<String, XATransactionDataSource> cachedDataSources = new HashMap<>();
    
    private final XATransactionManager xaTransactionManager = XATransactionManagerLoader.getInstance().getTransactionManager();
    
    private ThreadLocal<List<String>> enlistedXAResource = new ThreadLocal<List<String>>() {
        @Override
        public List<String> initialValue() {
            return new LinkedList<>();
        }
    };
    
    @Override
    public void init(final DatabaseType databaseType, final Collection<ResourceDataSource> resourceDataSources) {
        for (ResourceDataSource each : resourceDataSources) {
            DataSource dataSource = each.getDataSource();
            if (dataSource instanceof AtomikosDataSourceBean) {
                continue;
            }
            XATransactionDataSource xaTransactionDataSource = new XATransactionDataSource(databaseType, each.getUniqueResourceName(), dataSource);
            cachedDataSources.put(each.getOriginalName(), xaTransactionDataSource);
            xaTransactionManager.registerRecoveryResource(each.getUniqueResourceName(), xaTransactionDataSource.getXaDataSource());
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
    
    @Override
    public Connection getConnection(final String dataSourceName) throws SQLException {
        XATransactionConnection transactionConnection = cachedDataSources.get(dataSourceName).getConnection();
        if (!enlistedXAResource.get().contains(dataSourceName)) {
            xaTransactionManager.enlistResource(transactionConnection.getXAResource());
            enlistedXAResource.get().add(dataSourceName);
        }
        return transactionConnection.getTargetConnection();
    }
    
    @SneakyThrows
    @Override
    public void begin() {
        xaTransactionManager.getTransactionManager().begin();
    }
    
    @SneakyThrows
    @Override
    public void commit() {
        try {
            xaTransactionManager.getTransactionManager().commit();
        } finally {
            enlistedXAResource.remove();
        }
    }
    
    @SneakyThrows
    @Override
    public void rollback() {
        try {
            xaTransactionManager.getTransactionManager().rollback();
        } finally {
            enlistedXAResource.remove();
        }
    }
    
    @Override
    public void close() throws Exception {
        for (XATransactionDataSource each : cachedDataSources.values()) {
            xaTransactionManager.removeRecoveryResource(each.getResourceName(), each.getXaDataSource());
        }
        cachedDataSources.clear();
        xaTransactionManager.close();
        enlistedXAResource = null;
    }
}
