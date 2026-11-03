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
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

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
    public Collection<DatabasePacket> execute() {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(connectionSession.getConnectionId());
        ConnectionSession transactionSession = FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, transactionId);
        transactionSession.setAutoCommit(packet.isAutoCommit());
        transactionSession.setReadOnly(packet.isReadOnly());
        transactionSession.setIsolationLevel(packet.getIsolationLevel());
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(transactionId));
    }
    
}
