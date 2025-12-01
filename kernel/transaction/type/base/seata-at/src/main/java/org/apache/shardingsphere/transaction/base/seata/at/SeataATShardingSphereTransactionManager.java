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

package org.apache.shardingsphere.transaction.base.seata.at;

import lombok.SneakyThrows;
import org.apache.seata.config.ConfigurationFactory;
import org.apache.seata.config.FileConfiguration;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.core.exception.TransactionException;
import org.apache.seata.core.rpc.netty.RmNettyRemotingClient;
import org.apache.seata.core.rpc.netty.TmNettyRemotingClient;
import org.apache.seata.rm.RMClient;
import org.apache.seata.rm.datasource.DataSourceProxy;
import org.apache.seata.tm.TMClient;
import org.apache.seata.tm.api.GlobalTransaction;
import org.apache.seata.tm.api.GlobalTransactionContext;
import org.apache.seata.tm.api.GlobalTransactionRole;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.base.seata.at.exception.SeataATApplicationIDNotFoundException;
import org.apache.shardingsphere.transaction.base.seata.at.exception.SeataATDisabledException;
import org.apache.shardingsphere.transaction.exception.TransactionTimeoutException;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata AT transaction manager.
 */
public final class SeataATShardingSphereTransactionManager implements ShardingSphereDistributedTransactionManager {
    
    private final Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    private final String applicationId;
    
    private final String transactionServiceGroup;
    
    private final boolean enableSeataAT;
    
    private final int globalTXTimeout;
    
    public SeataATShardingSphereTransactionManager() {
        FileConfiguration config = new FileConfiguration("seata.conf");
        enableSeataAT = config.getBoolean("shardingsphere.transaction.seata.at.enable", true);
        applicationId = config.getConfig("client.application.id");
        transactionServiceGroup = config.getConfig("client.transaction.service.group", "default");
        globalTXTimeout = config.getInt("shardingsphere.transaction.seata.tx.timeout", 60);
    }
    
    @Override
    public void init(final Map<String, DatabaseType> databaseTypes, final Map<String, DataSource> dataSources, final String providerType) {
        if (enableSeataAT) {
            initSeataRPCClient();
            dataSources.forEach((key, value) -> dataSourceMap.put(key, new DataSourceProxy(value)));
        }
    }
    
    private void initSeataRPCClient() {
        ShardingSpherePreconditions.checkNotNull(applicationId, SeataATApplicationIDNotFoundException::new);
        TMClient.init(applicationId, transactionServiceGroup);
        RMClient.init(applicationId, transactionServiceGroup);
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
    }
    
    @Override
    public boolean isInTransaction() {
        checkSeataATEnabled();
        return null != SeataTransactionHolder.get();
    }
    
    @Override
    public Connection getConnection(final String databaseName, final String dataSourceName) throws SQLException {
        checkSeataATEnabled();
        return dataSourceMap.get(databaseName + "." + dataSourceName).getConnection();
    }
    
    @Override
    public void begin() {
        begin(globalTXTimeout);
    }
    
    @Override
    @SneakyThrows(TransactionException.class)
    public void begin(final int timeout) {
        ShardingSpherePreconditions.checkState(timeout >= 0, TransactionTimeoutException::new);
        checkSeataATEnabled();
        GlobalTransaction globalTransaction = GlobalTransactionContext.getCurrentOrCreate();
        globalTransaction.begin(timeout * 1000);
        SeataTransactionHolder.set(globalTransaction);
    }
    
    @Override
    @SneakyThrows(TransactionException.class)
    public void commit(final boolean rollbackOnly) {
        checkSeataATEnabled();
        GlobalTransaction globalTransaction = SeataTransactionHolder.get();
        try {
            globalTransaction.commit();
        } finally {
            if (GlobalTransactionRole.Participant != globalTransaction.getGlobalTransactionRole()) {
                SeataTransactionHolder.clear();
                RootContext.unbind();
                SeataXIDContext.remove();
            }
        }
    }
    
    @Override
    @SneakyThrows(TransactionException.class)
    public void rollback() {
        checkSeataATEnabled();
        try {
            SeataTransactionHolder.get().rollback();
        } finally {
            SeataTransactionHolder.clear();
            RootContext.unbind();
            SeataXIDContext.remove();
        }
    }
    
    private void checkSeataATEnabled() {
        ShardingSpherePreconditions.checkState(enableSeataAT, SeataATDisabledException::new);
    }
    
    @Override
    public boolean containsProviderType(final String providerType) {
        return true;
    }
    
    @Override
    public void close() {
        dataSourceMap.clear();
        SeataTransactionHolder.clear();
        TmNettyRemotingClient.getInstance().destroy();
        RmNettyRemotingClient.getInstance().destroy();
        ConfigurationFactory.reload();
    }
    
    @Override
    public String getType() {
        return TransactionType.BASE.name();
    }
}
