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

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.transaction.event.WeakXaTransactionEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Weak-XA Transaction Listener.
 *
 * @author zhaojun
 */
public class WeakXaTransactionListener {
    
    /**
     * Weak-Xa Transaction Event Listener.
     *
     * @param weakXaTransactionEvent WeakXaTransactionEvent
     * @throws SQLException SQLException
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final WeakXaTransactionEvent weakXaTransactionEvent) throws SQLException {
        switch (weakXaTransactionEvent.getTclType()) {
            case BEGIN:
                doBegin(weakXaTransactionEvent);
                break;
            case COMMIT:
                doCommit(weakXaTransactionEvent);
                break;
            case ROLLBACK:
                doRollback(weakXaTransactionEvent);
                break;
            default:
        }
    }
    
    private void doBegin(final WeakXaTransactionEvent weakXaTransactionEvent) throws SQLException {
        for (Connection each : weakXaTransactionEvent.getCachedConnections().values()) {
            each.setAutoCommit(weakXaTransactionEvent.isAutoCommit());
        }
    }
    
    private void doCommit(final WeakXaTransactionEvent weakXaTransactionEvent) throws SQLException {
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
    
    private void doRollback(final WeakXaTransactionEvent weakXaTransactionEvent) throws SQLException {
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
        SQLException ex = new SQLException();
        for (SQLException each : exceptions) {
            ex.setNextException(each);
        }
        throw ex;
    }
}
