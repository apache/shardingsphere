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

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XATransactionDataSource;
import org.apache.shardingsphere.transaction.xa.manager.XATransactionManagerProviderFactory;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider;

import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * ShardingSphere Transaction manager for XA.
 */
public final class XAShardingSphereTransactionManager implements ShardingSphereTransactionManager {
    
    private final Map<String, XATransactionDataSource> cachedDataSources = new HashMap<>();
    
    private XATransactionManagerProvider xaTransactionManagerProvider;
    
    @Override
    public void init(final Map<String, DatabaseType> databaseTypes, final Map<String, ResourceDataSource> resourceDataSources, final String providerType) {
        xaTransactionManagerProvider = XATransactionManagerProviderFactory.getInstance(providerType);
        xaTransactionManagerProvider.init();
        resourceDataSources.forEach((key, value) -> cachedDataSources.put(value.getOriginalName(), newXATransactionDataSource(databaseTypes.get(key), value)));
    }
    
    private XATransactionDataSource newXATransactionDataSource(final DatabaseType databaseType, final ResourceDataSource resourceDataSource) {
        String resourceName = resourceDataSource.getUniqueResourceName();
        DataSource dataSource = resourceDataSource.getDataSource();
        return new XATransactionDataSource(databaseType, resourceName, dataSource, xaTransactionManagerProvider);
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @SneakyThrows(SystemException.class)
    @Override
    public boolean isInTransaction() {
        return xaTransactionManagerProvider != null && Status.STATUS_NO_TRANSACTION != xaTransactionManagerProvider.getTransactionManager().getStatus();
    }
    
    @Override
    public Connection getConnection(final String databaseName, final String dataSourceName) throws SQLException {
        try {
            return cachedDataSources.get(databaseName + "." + dataSourceName).getConnection();
        } catch (final SystemException | RollbackException ex) {
            throw new SQLException(ex);
        }
    }
    
    @SneakyThrows({SystemException.class, NotSupportedException.class})
    @Override
    public void begin() {
        xaTransactionManagerProvider.getTransactionManager().begin();
    }
    
    @Override
    @SneakyThrows({SystemException.class, NotSupportedException.class})
    public void begin(final int timeout) {
        if (timeout < 0) {
            throw new NotSupportedException("timeout should more than 0s");
        }
        TransactionManager transactionManager = xaTransactionManagerProvider.getTransactionManager();
        transactionManager.setTransactionTimeout(timeout);
        transactionManager.begin();
    }
    
    @SneakyThrows({SystemException.class, RollbackException.class, HeuristicMixedException.class, HeuristicRollbackException.class})
    @Override
    public void commit(final boolean rollbackOnly) {
        if (rollbackOnly) {
            xaTransactionManagerProvider.getTransactionManager().rollback();
        } else {
            xaTransactionManagerProvider.getTransactionManager().commit();
        }
    }
    
    @SneakyThrows(SystemException.class)
    @Override
    public void rollback() {
        xaTransactionManagerProvider.getTransactionManager().rollback();
    }
    
    @Override
    public void close() throws Exception {
        for (XATransactionDataSource each : cachedDataSources.values()) {
            each.close();
        }
        cachedDataSources.clear();
        if (null != xaTransactionManagerProvider) {
            xaTransactionManagerProvider.close();
        }
    }
}
