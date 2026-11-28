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

package org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.end;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdSingleHandleCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.commonresponses.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.blobs.FirebirdBlobWriteCache;

import java.util.Collection;
import java.util.Collections;
import java.util.OptionalLong;

/**
 * End blob command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdEndBlobCommandExecutor implements CommandExecutor {
    
    private final FirebirdSingleHandleCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private final FirebirdCommandPacketType commandPacketType;
    
    @Override
    public Collection<DatabasePacket> execute() {
        int blobHandle = FirebirdBlobHandleGenerator.getInstance().resolveBlobHandle(connectionSession.getConnectionId(), packet.getObjectHandle());
        if (FirebirdCommandPacketType.CLOSE_BLOB == commandPacketType) {
            return closeBlob(blobHandle);
        }
        return cancelBlob(blobHandle);
    }
    
    private Collection<DatabasePacket> closeBlob(final int blobHandle) {
        long blobId = FirebirdBlobWriteCache.getInstance().getBlobId(connectionSession.getConnectionId(), blobHandle).orElse(0L);
        FirebirdBlobWriteCache.getInstance().closeWrite(connectionSession.getConnectionId(), blobHandle);
        releaseBlobHandle(blobHandle);
        return Collections.singleton(new FirebirdGenericResponsePacket().setWriteZeroStatementId(true).setId(blobId));
    }
    
    private Collection<DatabasePacket> cancelBlob(final int blobHandle) {
        OptionalLong blobId = FirebirdBlobWriteCache.getInstance().getBlobId(connectionSession.getConnectionId(), blobHandle);
        if (blobId.isPresent()) {
            FirebirdBlobWriteCache.getInstance().removeWrite(connectionSession.getConnectionId(), blobId.getAsLong());
        }
        releaseBlobHandle(blobHandle);
        return Collections.singleton(new FirebirdGenericResponsePacket());
    }
    
    private void releaseBlobHandle(final int blobHandle) {
        FirebirdBlobReadCache.getInstance().removeBlob(connectionSession.getConnectionId(), blobHandle);
        FirebirdBlobHandleGenerator.getInstance().releaseBlobHandle(connectionSession.getConnectionId(), blobHandle);
    }
}
