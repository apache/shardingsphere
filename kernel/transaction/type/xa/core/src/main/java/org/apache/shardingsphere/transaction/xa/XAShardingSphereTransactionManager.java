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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.exception.TransactionTimeoutException;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XATransactionDataSource;
import org.apache.shardingsphere.transaction.xa.jta.datasource.checker.DataSourcePrivilegeChecker;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ShardingSphere Transaction manager for XA.
 */
public final class XAShardingSphereTransactionManager implements ShardingSphereTransactionManager {
    
    private final Map<String, XATransactionDataSource> cachedDataSources = new HashMap<>();
    
    private XATransactionManagerProvider xaTransactionManagerProvider;
    
    @Override
    public void init(final Map<String, DatabaseType> databaseTypes, final Map<String, DataSource> dataSources, final String providerType) {
        dataSources.forEach((key, value) -> DatabaseTypedSPILoader.getService(DataSourcePrivilegeChecker.class, databaseTypes.get(key)).checkPrivilege(value));
        xaTransactionManagerProvider = TypedSPILoader.getService(XATransactionManagerProvider.class, providerType);
        xaTransactionManagerProvider.init();
        Map<String, ResourceDataSource> resourceDataSources = getResourceDataSources(dataSources);
        resourceDataSources.forEach((key, value) -> cachedDataSources.put(value.getOriginalName(), newXATransactionDataSource(databaseTypes.get(key), value)));
    }
    
    private Map<String, ResourceDataSource> getResourceDataSources(final Map<String, DataSource> dataSourceMap) {
        Map<String, ResourceDataSource> result = new LinkedHashMap<>(dataSourceMap.size(), 1F);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), new ResourceDataSource(entry.getKey(), entry.getValue()));
        }
        return result;
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
        ShardingSpherePreconditions.checkState(timeout >= 0, TransactionTimeoutException::new);
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
    public void close() {
        for (XATransactionDataSource each : cachedDataSources.values()) {
            each.close();
        }
        cachedDataSources.clear();
        if (null != xaTransactionManagerProvider) {
            xaTransactionManagerProvider.close();
        }
    }
    
    @Override
    public boolean containsProviderType(final String providerType) {
        return TypedSPILoader.contains(XATransactionManagerProvider.class, providerType);
    }
    
    @Override
    public String getType() {
        return TransactionType.XA.name();
    }
}
