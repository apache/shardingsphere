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

package io.shardingsphere.transaction.api.xa;

import io.shardingsphere.transaction.api.TransactionManager;
import io.shardingsphere.transaction.common.event.TransactionEvent;
import io.shardingsphere.transaction.common.event.WeakXaTransactionEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Weak XA transaction manager.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WeakXaTransactionManager implements TransactionManager {
    
    private static final WeakXaTransactionManager TRANSACTION_MANAGER = new WeakXaTransactionManager();
    
    /**
     * Get singleton instance of {@code WeakXaTransactionManager}.
     *
     * @return weak XA transaction manager
     */
    public static WeakXaTransactionManager getInstance() {
        return TRANSACTION_MANAGER;
    }
    
    @Override
    public void begin(final TransactionEvent transactionEvent) throws SQLException {
        WeakXaTransactionEvent weakXaTransactionEvent = (WeakXaTransactionEvent) transactionEvent;
        for (Connection each : weakXaTransactionEvent.getCachedConnections()) {
            each.setAutoCommit(weakXaTransactionEvent.isAutoCommit());
        }
    }
    
    @Override
    public void commit(final TransactionEvent transactionEvent) throws SQLException {
        WeakXaTransactionEvent weakXaTransactionEvent = (WeakXaTransactionEvent) transactionEvent;
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : weakXaTransactionEvent.getCachedConnections()) {
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
        for (Connection each : weakXaTransactionEvent.getCachedConnections()) {
            try {
                each.rollback();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public int getStatus() {
        return 0;
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
