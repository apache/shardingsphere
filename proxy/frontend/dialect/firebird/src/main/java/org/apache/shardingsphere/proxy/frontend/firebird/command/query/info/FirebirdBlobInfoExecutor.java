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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Blob info command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdBlobInfoExecutor implements CommandExecutor {
    
    private final FirebirdInfoPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(new FirebirdBlobInfoReturnPacket(packet.getInfoItems(), getBlobLength())));
    }
    
    private int getBlobLength() {
        int connectionId = connectionSession.getConnectionId();
        int blobHandle = FirebirdBlobWriteCache.getInstance().getBlobHandle(connectionId, packet.getHandle());
        Optional<byte[]> readSegment = FirebirdBlobReadCache.getInstance().getSegment(connectionId, blobHandle);
        if (readSegment.isPresent()) {
            return readSegment.get().length;
        }
        OptionalLong blobId = FirebirdBlobWriteCache.getInstance().getBlobId(connectionId, blobHandle);
        if (!blobId.isPresent()) {
            return 0;
        }
        Optional<byte[]> uploadData = FirebirdBlobWriteCache.getInstance().getBlobData(connectionId, blobId.getAsLong());
        return uploadData.map(data -> data.length).orElse(0);
    }
}
