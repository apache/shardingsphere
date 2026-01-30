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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdCloseBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload.FirebirdBlobUploadCache;

import java.util.Collection;
import java.util.Collections;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Close blob command executor for Firebird.
 */
@RequiredArgsConstructor
@Slf4j
public final class FirebirdCloseBlobCommandExecutor implements CommandExecutor {
    
    private final FirebirdCloseBlobCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        OptionalLong blobId = FirebirdBlobUploadCache.getInstance().getBlobId(connectionSession.getConnectionId(), packet.getBlobHandle());
        OptionalInt size = FirebirdBlobUploadCache.getInstance().closeUpload(connectionSession.getConnectionId(), packet.getBlobHandle());
        long responseBlobId = blobId.isPresent() ? blobId.getAsLong() : 0L;
        int bufferSize = size.isPresent() ? size.getAsInt() : 0;
        log.info("Firebird BLOB closed: connectionId={}, blobHandle={}, blobId={}, size={}",
                connectionSession.getConnectionId(), packet.getBlobHandle(), responseBlobId, bufferSize);
        FirebirdGenericResponsePacket response = new FirebirdGenericResponsePacket().setWriteZeroStatementId(true).setId(responseBlobId);
        return Collections.singleton(response);
    }
}
