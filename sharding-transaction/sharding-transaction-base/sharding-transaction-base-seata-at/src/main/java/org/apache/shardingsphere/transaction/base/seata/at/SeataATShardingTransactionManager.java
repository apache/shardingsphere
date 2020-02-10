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

import com.google.common.base.Preconditions;
import io.seata.config.FileConfiguration;
import io.seata.core.context.RootContext;
import io.seata.core.rpc.netty.RmRpcClient;
import io.seata.core.rpc.netty.TmRpcClient;
import io.seata.rm.RMClient;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.tm.TMClient;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.spi.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Seata AT sharding transaction manager.
 *
 * @author zhaojun
 */
public final class SeataATShardingTransactionManager implements ShardingTransactionManager {
    
    private final Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    private final FileConfiguration configuration = new FileConfiguration("seata.conf");
    
    @Override
    public void init(final DatabaseType databaseType, final Collection<ResourceDataSource> resourceDataSources) {
        initSeataRPCClient();
        for (ResourceDataSource each : resourceDataSources) {
            dataSourceMap.put(each.getOriginalName(), new DataSourceProxy(each.getDataSource()));
        }
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.BASE;
    }
    
    @Override
    public boolean isInTransaction() {
        return null != RootContext.getXID();
    }
    
    @Override
    public Connection getConnection(final String dataSourceName) throws SQLException {
        return dataSourceMap.get(dataSourceName).getConnection();
    }
    
    @Override
    @SneakyThrows
    public void begin() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        SeataTransactionHolder.get().begin();
        SeataTransactionBroadcaster.collectGlobalTxId();
    }
    
    @Override
    @SneakyThrows
    public void commit() {
        try {
            SeataTransactionHolder.get().commit();
        } finally {
            SeataTransactionBroadcaster.clear();
            SeataTransactionHolder.clear();
        }
    }
    
    @Override
    @SneakyThrows
    public void rollback() {
        try {
            SeataTransactionHolder.get().rollback();
        } finally {
            SeataTransactionBroadcaster.clear();
            SeataTransactionHolder.clear();
        }
    }
    
    @Override
    public void close() {
        dataSourceMap.clear();
        SeataTransactionHolder.clear();
        TmRpcClient.getInstance().destroy();
        RmRpcClient.getInstance().destroy();
    }
    
    private void initSeataRPCClient() {
        String applicationId = configuration.getConfig("client.application.id");
        Preconditions.checkNotNull(applicationId, "please config application id within seata.conf file");
        String transactionServiceGroup = configuration.getConfig("client.transaction.service.group", "default");
        TMClient.init(applicationId, transactionServiceGroup);
        RMClient.init(applicationId, transactionServiceGroup);
    }
}
