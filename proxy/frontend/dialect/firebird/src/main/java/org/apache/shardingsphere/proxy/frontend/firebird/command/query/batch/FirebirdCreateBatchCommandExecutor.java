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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCreateCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Create Batch command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdCreateBatchCommandExecutor implements CommandExecutor {
    
    private final FirebirdBatchCreateCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        int statementId = packet.getStatementHandle();
        FirebirdBatchRegistry.getInstance().unregisterBatchStatement(connectionSession.getConnectionId(), statementId);
        FirebirdBatchRegistry.getInstance().registerBatchStatement(connectionSession.getConnectionId(), statementId,
                new FirebirdBatchStatement(packet.getStatementHandle()));
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(statementId));
    }
}
