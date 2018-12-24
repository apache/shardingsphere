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

import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.DatabaseType;
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
import javax.sql.XAConnection;
import javax.transaction.Transaction;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
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
    
    @Override
    protected ShardingTransactionManager getShardingTransactionManager() {
        return XATransactionManagerSPILoader.getInstance().getTransactionManager();
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @Override
    public void registerTransactionDataSource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        SHARDING_XA_DATA_SOURCE_MAP.clear();
        this.databaseType = databaseType;
        try {
            for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
                DataSourceParameter parameter = DataSourceSwapperRegistry.getSwapper(entry.getValue().getClass()).swap(entry.getValue());
                ShardingXADataSource shardingXADataSource = ShardingXADataSourceUtil.createShardingXADataSource(databaseType, entry.getKey(), parameter);
                SHARDING_XA_DATA_SOURCE_MAP.put(entry.getKey(), shardingXADataSource);
            }
        } catch (final Exception ex) {
            log.error("Failed to register transaction datasource of XAShardingTransactionHandler");
        }
    }
    
    @Override
    public void synchronizeTransactionResource(final String datasourceName, final List<Connection> connections, final Object... properties) throws SQLException {
        try {
            ShardingXADataSource shardingXADataSource = SHARDING_XA_DATA_SOURCE_MAP.get(datasourceName);
            Preconditions.checkNotNull(shardingXADataSource, "Could not find ShardingXADataSource of `%s`", datasourceName);
            for (Connection each : connections) {
                XAConnection xaConnection = shardingXADataSource.wrapPhysicalConnection(each, databaseType);
                Transaction transaction = ((XATransactionManager) getShardingTransactionManager()).getUnderlyingTransactionManager().getTransaction();
                transaction.enlistResource(xaConnection.getXAResource());
            }
        } catch (final Exception ex) {
            throw new SQLException(ex.getMessage());
        }
    }
}
