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

package org.apache.shardingsphere.driver.jdbc.adapter;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteTemplate;
import org.apache.shardingsphere.driver.jdbc.unsupported.AbstractUnsupportedOperationConnection;
import org.apache.shardingsphere.infra.hook.RootInvokeHook;
import org.apache.shardingsphere.infra.hook.SPIRootInvokeHook;
import org.apache.shardingsphere.replicaquery.route.engine.impl.PrimaryVisitedManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Map.Entry;

/**
 * Adapter for {@code Connection}.
 */
public abstract class AbstractConnectionAdapter extends AbstractUnsupportedOperationConnection {
    
    @Getter
    private final Multimap<String, Connection> cachedConnections = LinkedHashMultimap.create();
    
    @Getter
    private final ForceExecuteTemplate<Connection> forceExecuteTemplate = new ForceExecuteTemplate<>();
    
    @Getter
    private final ForceExecuteTemplate<Entry<String, Connection>> forceExecuteTemplateForClose = new ForceExecuteTemplate<>();
    
    private final RootInvokeHook rootInvokeHook = new SPIRootInvokeHook();
    
    private boolean readOnly;
    
    private volatile boolean closed;
    
    private int transactionIsolation = TRANSACTION_READ_UNCOMMITTED;
    
    protected AbstractConnectionAdapter() {
        rootInvokeHook.start();
    }
    
    @Override
    public final void close() throws SQLException {
        closed = true;
        PrimaryVisitedManager.clear();
        int connectionSize = cachedConnections.size();
        try {
            forceExecuteTemplateForClose.execute(cachedConnections.entries(), cachedConnections -> cachedConnections.getValue().close());
        } finally {
            cachedConnections.clear();
            rootInvokeHook.finish(connectionSize);
        }
    }
    
    @Override
    public final boolean isClosed() {
        return closed;
    }
    
    @Override
    public final boolean isReadOnly() {
        return readOnly;
    }
    
    @Override
    public final void setReadOnly(final boolean readOnly) throws SQLException {
        this.readOnly = readOnly;
        recordMethodInvocation(Connection.class, "setReadOnly", new Class[]{boolean.class}, new Object[]{readOnly});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setReadOnly(readOnly));
    }
    
    @Override
    public final int getTransactionIsolation() throws SQLException {
        if (cachedConnections.values().isEmpty()) {
            return transactionIsolation;
        }
        return cachedConnections.values().iterator().next().getTransactionIsolation();
    }
    
    @Override
    public final void setTransactionIsolation(final int level) throws SQLException {
        transactionIsolation = level;
        recordMethodInvocation(Connection.class, "setTransactionIsolation", new Class[]{int.class}, new Object[]{level});
        forceExecuteTemplate.execute(cachedConnections.values(), connection -> connection.setTransactionIsolation(level));
    }

    @Override
    public final boolean isValid(final int timeout) throws SQLException {
        for (Connection connection : cachedConnections.values()) {
            if (!connection.isValid(timeout)) {
                return false;
            }
        }
        return true;
    }
    
    @SuppressWarnings("ReturnOfNull")
    @Override
    public final SQLWarning getWarnings() {
        return null;
    }
    
    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void clearWarnings() {
    }
    
    @Override
    public final int getHoldability() {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public final void setHoldability(final int holdability) {
    }
}
