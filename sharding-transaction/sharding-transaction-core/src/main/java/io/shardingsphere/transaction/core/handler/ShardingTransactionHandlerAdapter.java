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

package io.shardingsphere.transaction.core.handler;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.spi.ShardingTransactionHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Abstract class for sharding transaction handler.
 *
 * @author zhaojun
 *
 */
public abstract class ShardingTransactionHandlerAdapter implements ShardingTransactionHandler {
    
    /**
     * Default implement for do in transaction.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doInTransaction(final TransactionOperationType transactionOperationType) {
        switch (transactionOperationType) {
            case BEGIN:
                getShardingTransactionManager().begin();
                break;
            case COMMIT:
                getShardingTransactionManager().commit();
                break;
            case ROLLBACK:
                getShardingTransactionManager().rollback();
                break;
            default:
        }
    }
    
    /**
     * Default implement for register transaction resource.
     */
    @Override
    public void registerTransactionalResource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        // adapter
    }
    
    /**
     * Default implement for clear transactional resource.
     */
    @Override
    public void clearTransactionalResource(final Map<String, DataSource> dataSourceMap) {
        // adapter
    }
    
    /**
     * Default implement for create connection.
     */
    @Override
    public Connection createConnection(final String dataSourceName, final DataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }
}
