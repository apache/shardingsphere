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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Firebird start transaction command executor.
 */
@RequiredArgsConstructor
public final class FirebirdStartTransactionCommandExecutor implements CommandExecutor {
    
    private final FirebirdStartTransactionPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    @HighFrequencyInvocation
    public Collection<DatabasePacket> execute() throws SQLException {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession.getConnectionId());
        ConnectionSession transactionSession = FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, transactionId);
        transactionSession.setAutoCommit(false);
        transactionSession.setReadOnly(packet.isReadOnly());
        transactionSession.setIsolationLevel(packet.getIsolationLevel());
        try {
            registerRawTransactionOptions(transactionSession);
        } catch (final SQLException | IllegalArgumentException ex) {
            rollbackOnFailure(transactionSession.getDatabaseConnectionManager(), ex);
            unregisterOnFailure(transactionId, ex);
            throw ex;
        }
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(transactionId));
    }
    
    private void registerRawTransactionOptions(final ConnectionSession transactionSession) throws SQLException {
        byte[] rawTpb = packet.getRawTpb();
        if (0 == rawTpb.length) {
            return;
        }
        ProxyDatabaseConnectionManager manager = transactionSession.getDatabaseConnectionManager();
        manager.handleAutoCommit();
        FirebirdRawTransactionOptionsHandler handler = new FirebirdRawTransactionOptionsHandler(rawTpb);
        manager.getConnectionInitializers().add(handler::initialize);
        manager.getConnectionReleaseProcessors().add(handler::release);
        String databaseName = transactionSession.getCurrentDatabaseName();
        for (String each : ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits().keySet()) {
            manager.getConnections(databaseName, each, 0, 1, ConnectionMode.CONNECTION_STRICTLY);
        }
    }
    
    private void rollbackOnFailure(final ProxyDatabaseConnectionManager manager, final Exception cause) {
        try {
            new ProxyBackendTransactionManager(manager).rollback();
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
    }
    
    private void unregisterOnFailure(final int transactionId, final Exception cause) {
        FirebirdTransactionIdGenerator.getInstance().closeTransaction(connectionSession.getConnectionId(), transactionId);
        try {
            FirebirdTransactionSessionRegistry.getInstance().unregister(connectionSession, transactionId);
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
    }
    
}
