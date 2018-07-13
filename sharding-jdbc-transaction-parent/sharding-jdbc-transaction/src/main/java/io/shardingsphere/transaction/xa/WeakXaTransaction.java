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

package io.shardingsphere.transaction.xa;

import io.shardingsphere.core.transaction.spi.Transaction;
import io.shardingsphere.core.util.EventBusInstance;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Weak XA transaction implement for Transaction spi.
 *
 * @author zhaojun
 */
@AllArgsConstructor
public class WeakXaTransaction implements Transaction {
    
    static {
        EventBusInstance.getInstance().register(new WeakXaTransactionListener(new WeakXaTransaction()));
    }
    
    private final boolean autoCommit = true;
    
    private final Map<String, Connection> cachedConnections;
    
    @Override
    public void begin() throws SQLException {
        for (Connection each : cachedConnections.values()) {
            each.setAutoCommit(autoCommit);
        }
    }
    
    @Override
    public void commit() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
            try {
                each.commit();
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(exceptions);
    }
    
    @Override
    public void rollback() throws SQLException {
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Connection each : cachedConnections.values()) {
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
        SQLException ex = new SQLException();
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }
}
