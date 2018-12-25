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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.handler.ShardingTransactionHandlerAdapter;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;
import io.shardingsphere.transaction.spi.xa.XATransactionManager;
import io.shardingsphere.transaction.xa.convert.datasource.ShardingXADataSource;
import io.shardingsphere.transaction.xa.convert.datasource.ShardingXADataSourceUtil;
import io.shardingsphere.transaction.xa.convert.swap.DataSourceSwapperRegistry;
import io.shardingsphere.transaction.xa.manager.XATransactionManagerSPILoader;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import javax.transaction.Transaction;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XA sharding transaction handler.
 *
 * @author zhaojun
 */
@Slf4j
public final class XAShardingTransactionHandler extends ShardingTransactionHandlerAdapter {
    
    private static final Map<String, ShardingXADataSource> SHARDING_XA_DATA_SOURCE_MAP = new ConcurrentHashMap<>();
    
    private DatabaseType databaseType;
    
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
    public void registerTransactionDataSource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        removeTransactionDataSource();
        this.databaseType = databaseType;
        try {
            for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
                DataSourceParameter parameter = DataSourceSwapperRegistry.getSwapper(entry.getValue().getClass()).swap(entry.getValue());
                ShardingXADataSource shardingXADataSource = ShardingXADataSourceUtil.createShardingXADataSource(databaseType, entry.getKey(), parameter);
                SHARDING_XA_DATA_SOURCE_MAP.put(entry.getKey(), shardingXADataSource);
                xaTransactionManager.registerRecoveryResource(entry.getKey(), shardingXADataSource.getXaDataSource());
            }
        } catch (final Exception ex) {
            log.error("Failed to register transaction datasource of XAShardingTransactionHandler");
        }
        xaTransactionManager.startup();
    }
    
    private void removeTransactionDataSource() {
        if (!SHARDING_XA_DATA_SOURCE_MAP.isEmpty()) {
            for (ShardingXADataSource each : SHARDING_XA_DATA_SOURCE_MAP.values()) {
                xaTransactionManager.removeRecoveryResource(each.getDatasourceName(), each.getXaDataSource());
            }
        }
        SHARDING_XA_DATA_SOURCE_MAP.clear();
    }
    
    @Override
    public synchronized void synchronizeTransactionResource(final String datasourceName, final Connection connection, final Object... properties) {
        try {
            Transaction transaction = xaTransactionManager.getUnderlyingTransactionManager().getTransaction();
            transaction.enlistResource(xaTransactionManager.getRecoveryXAResource(datasourceName));
        } catch (final Exception ex) {
            throw new ShardingException(ex);
        }
    }
}
