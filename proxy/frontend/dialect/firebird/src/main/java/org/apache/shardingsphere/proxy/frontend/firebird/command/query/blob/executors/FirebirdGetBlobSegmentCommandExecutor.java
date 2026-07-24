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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.executors;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdBlobRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.util.Collection;
import java.util.Collections;

/**
 * Get blob segment command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdGetBlobSegmentCommandExecutor implements CommandExecutor {
    
    private static final int STATE_END_OF_BLOB = 2;
    
    private final FirebirdGetBlobSegmentCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        int blobHandle = FirebirdBlobRegistry.getInstance().resolveBlobHandle(connectionSession.getConnectionId(), packet.getBlobHandle());
        byte[] payloadSegment = FirebirdBlobRegistry.getInstance().readSegment(connectionSession.getConnectionId(), blobHandle, packet.getSegmentLength());
        int responseHandle = FirebirdBlobRegistry.getInstance().isEof(connectionSession.getConnectionId(), blobHandle) ? STATE_END_OF_BLOB : 0;
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(responseHandle).setData(new FirebirdGetBlobSegmentResponsePacket(payloadSegment)));
    }
}
