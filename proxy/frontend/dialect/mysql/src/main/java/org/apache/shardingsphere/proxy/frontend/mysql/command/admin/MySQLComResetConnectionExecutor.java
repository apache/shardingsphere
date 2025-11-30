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

package org.apache.shardingsphere.proxy.frontend.mysql.command.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.ServerStatusFlagCalculator;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * COM_RESET_CONNECTION command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComResetConnectionExecutor implements CommandExecutor {
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        ProxyBackendTransactionManager transactionManager = new ProxyBackendTransactionManager(connectionSession.getDatabaseConnectionManager());
        transactionManager.rollback();
        connectionSession.setAutoCommit(true);
        connectionSession.setDefaultIsolationLevel(null);
        connectionSession.setIsolationLevel(null);
        connectionSession.getServerPreparedStatementRegistry().clear();
        return Collections.singleton(new MySQLOKPacket(ServerStatusFlagCalculator.calculateFor(connectionSession, true)));
    }
}
