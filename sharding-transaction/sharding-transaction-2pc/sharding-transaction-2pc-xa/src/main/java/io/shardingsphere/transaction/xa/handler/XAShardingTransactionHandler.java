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

import com.atomikos.jdbc.AtomikosDataSourceBean;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.handler.ShardingTransactionHandlerAdapter;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.spi.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.datasource.ShardingXADataSource;
import io.shardingsphere.transaction.xa.manager.XATransactionManagerSPILoader;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * XA sharding transaction handler.
 *
 * @author zhaojun
 */
@Slf4j
public final class XAShardingTransactionHandler extends ShardingTransactionHandlerAdapter {
    
    private final Map<String, ShardingXADataSource> cachedShardingXADataSourceMap = new HashMap<>();
    
    private final XATransactionManager xaTransactionManager = XATransactionManagerSPILoader.getInstance().getTransactionManager();
    
    @Override
    public ShardingTransactionManager getShardingTransactionManager() {
        return xaTransactionManager;
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @Override
    public void registerTransactionalResource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            DataSource dataSource = entry.getValue();
            if (dataSource instanceof AtomikosDataSourceBean) {
                continue;
            }
            ShardingXADataSource shardingXADataSource = new ShardingXADataSource(databaseType, entry.getKey(), entry.getValue());
            cachedShardingXADataSourceMap.put(entry.getKey(), shardingXADataSource);
            xaTransactionManager.registerRecoveryResource(entry.getKey(), shardingXADataSource.getXaDataSource());
        }
        xaTransactionManager.startup();
    }
    
    @Override
    public void clearTransactionalResource() {
        if (!cachedShardingXADataSourceMap.isEmpty()) {
            for (ShardingXADataSource each : cachedShardingXADataSourceMap.values()) {
                xaTransactionManager.removeRecoveryResource(each.getResourceName(), each.getXaDataSource());
            }
        }
        cachedShardingXADataSourceMap.clear();
    }
    
    @Override
    public Connection createConnection(final String dataSourceName, final DataSource dataSource) {
        Connection result;
        ShardingXADataSource shardingXADataSource = cachedShardingXADataSourceMap.get(dataSourceName);
        try {
            Transaction transaction = xaTransactionManager.getUnderlyingTransactionManager().getTransaction();
            if (null != transaction && Status.STATUS_NO_TRANSACTION != transaction.getStatus()) {
                ShardingXAConnection shardingXAConnection = shardingXADataSource.getXAConnection();
                transaction.enlistResource(shardingXAConnection.getXAResource());
                result = shardingXAConnection.getConnection();
            } else {
                result = shardingXADataSource.getConnectionFromOriginalDataSource();
            }
        } catch (final SQLException | RollbackException | SystemException ex) {
            log.error("Failed to synchronize transactional resource");
            throw new ShardingException(ex);
        }
        return result;
    }
}
    
