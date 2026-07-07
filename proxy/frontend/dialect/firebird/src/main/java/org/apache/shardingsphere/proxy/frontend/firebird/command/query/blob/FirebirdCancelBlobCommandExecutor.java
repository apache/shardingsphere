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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCancelBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;

import java.util.Collection;
import java.util.Collections;
import java.util.OptionalLong;

/**
 * Cancel blob command executor for Firebird.
 * Cancels and invalidates the blob handle. If this was a newly created blob, the blob is disposed.
 */
@RequiredArgsConstructor
public final class FirebirdCancelBlobCommandExecutor implements CommandExecutor {
    
    private final FirebirdCancelBlobCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        int blobHandle = FirebirdBlobWriteCache.getInstance().getBlobHandle(connectionSession.getConnectionId(), packet.getBlobHandle());
        OptionalLong blobId = FirebirdBlobWriteCache.getInstance().getBlobId(connectionSession.getConnectionId(), blobHandle);
        if (blobId.isPresent()) {
            FirebirdBlobWriteCache.getInstance().removeWrite(connectionSession.getConnectionId(), blobId.getAsLong());
        }
        FirebirdBlobReadCache.getInstance().removeBlob(connectionSession.getConnectionId(), blobHandle);
        return Collections.singleton(new FirebirdGenericResponsePacket());
    }
}
