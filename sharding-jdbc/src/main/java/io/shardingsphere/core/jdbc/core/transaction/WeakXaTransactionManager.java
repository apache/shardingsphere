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

package io.shardingsphere.core.jdbc.core.transaction;

import io.shardingsphere.core.transaction.event.TransactionEvent;
import io.shardingsphere.core.transaction.event.WeakXaTransactionEvent;
import io.shardingsphere.core.transaction.listener.TransactionListener;
import io.shardingsphere.core.transaction.spi.TransactionManager;
import io.shardingsphere.core.transaction.spi.TransactionEventHolder;
import io.shardingsphere.core.util.EventBusInstance;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Weak XA transaction implement for Transaction spi.
 *
 * @author zhaojun
 */
public final class WeakXaTransactionManager implements TransactionManager {
    
    static {
        EventBusInstance.getInstance().register(new TransactionListener());
        TransactionEventHolder.set(WeakXaTransactionEvent.class);
    }
    
    @Override
    public void begin(final TransactionEvent transactionEvent) throws SQLException {
        WeakXaTransactionEvent weakXaTransactionEvent = (WeakXaTransactionEvent) transactionEvent;
        for (Connection each : weakXaTransactionEvent.getCachedConnections().values()) {
            each.setAutoCommit(weakXaTransactionEvent.isAutoCommit());
        }
    }
    
    @Override
    public void commit(final TransactionEvent transactionEvent) throws SQLException {
        WeakXaTransactionEvent weakXaTransactionEvent = (WeakXaTransactionEvent) transactionEvent;
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : weakXaTransactionEvent.getCachedConnections().values()) {
            try {
                each.commit();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public void rollback(final TransactionEvent transactionEvent) throws SQLException {
        WeakXaTransactionEvent weakXaTransactionEvent = (WeakXaTransactionEvent) transactionEvent;
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : weakXaTransactionEvent.getCachedConnections().values()) {
            try {
                each.rollback();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        SQLException sqlException = new SQLException();
        for (SQLException each : exceptions) {
            sqlException.setNextException(each);
        }
        throw sqlException;
    }
}
