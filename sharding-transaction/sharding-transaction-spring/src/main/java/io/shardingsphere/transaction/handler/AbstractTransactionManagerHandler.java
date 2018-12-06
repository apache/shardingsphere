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

package io.shardingsphere.transaction.handler;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.exception.ShardingException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abstract transaction manager handler.
 *
 * @author yangyi
 */
public abstract class AbstractTransactionManagerHandler implements TransactionManagerHandler {
    
    private static final String SET_TRANSACTION_TYPE_SQL = "SET TRANSACTION_TYPE=%s";
    
    @Override
    public final void switchTransactionType(final TransactionType transactionType) {
        Connection connection = getConnectionFromTransactionManager();
        try (Statement statement = connection.createStatement()) {
            statement.execute(String.format(SET_TRANSACTION_TYPE_SQL, transactionType.name()));
        } catch (final SQLException ex) {
            throw new ShardingException("Switch transaction type for sharding-proxy failed: ", ex);
        }
    }
    
    /**
     * Get physical connection which transaction manager will use.
     *
     * @return connection to Sharding-Proxy
     */
    protected abstract Connection getConnectionFromTransactionManager();
}
