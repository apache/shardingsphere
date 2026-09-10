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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrIdException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdOpenBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;

import java.util.Collection;
import java.util.Collections;

/**
 * Open blob command executor for Firebird.
 *
 * <p>A zero BLOB id is the NULL BLOB quad, which Firebird opens as an empty BLOB instead of rejecting it,
 * so it is registered with empty content rather than reported as an invalid BLOB id.</p>
 */
@RequiredArgsConstructor
public final class FirebirdOpenBlobCommandExecutor implements CommandExecutor {
    
    private final FirebirdOpenBlobCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        long blobId = packet.getBlobId();
        if (0L == blobId) {
            return registerBlob(blobId, new byte[0]);
        }
        byte[] blobContent = FirebirdBlobBinaryProtocolValue.getBlobContent(connectionSession.getConnectionId(), blobId);
        ShardingSpherePreconditions.checkNotNull(blobContent, () -> new InvalidSegstrIdException(blobId));
        return registerBlob(blobId, blobContent);
    }
    
    private Collection<DatabasePacket> registerBlob(final long blobId, final byte[] blobContent) {
        int blobHandle = FirebirdBlobHandleGenerator.getInstance().nextBlobHandle(connectionSession.getConnectionId());
        FirebirdBlobReadCache.getInstance().registerBlob(connectionSession.getConnectionId(), blobHandle, blobContent);
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(blobHandle).setId(blobId));
    }
}
