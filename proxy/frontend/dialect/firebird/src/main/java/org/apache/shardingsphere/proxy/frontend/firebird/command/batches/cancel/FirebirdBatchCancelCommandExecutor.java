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

package org.apache.shardingsphere.proxy.frontend.firebird.command.batches.cancel;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.batches.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdSingleHandleCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonresponses.generic.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonresponses.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.batches.FirebirdBatchStatementManager;

import java.util.Collection;
import java.util.Collections;

/**
 * Batch cancel command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdBatchCancelCommandExecutor implements CommandExecutor {
    
    private final FirebirdSingleHandleCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        FirebirdBatchStatementManager batchStatementManager = FirebirdBatchStatementManager.getInstance();
        FirebirdBatchStatement batchStatement = batchStatementManager.getBatchStatement(connectionSession.getConnectionId(), packet.getObjectHandle());
        // TODO restore checkNotNull when FirebirdFrontendEngine stops marking the transaction as failed for InvalidBatchHandleException
        if (null == batchStatement) {
            return Collections.singleton(FirebirdErrorPacketFactory.newInstance(new InvalidBatchHandleException(packet.getObjectHandle())));
        }
        batchStatementManager.resetBatchStatement(batchStatement);
        return Collections.singleton(new FirebirdGenericResponsePacket());
    }
}
