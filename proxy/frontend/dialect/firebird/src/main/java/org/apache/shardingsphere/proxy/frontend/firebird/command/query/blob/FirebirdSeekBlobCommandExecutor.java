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
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrHandleException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrTypeException;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdSeekBlobCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Seek blob command executor for Firebird.
 *
 * <p>Firebird defines seek for stream BLOBs only and returns the resulting position in the BLOB id of the response.
 * A position outside the BLOB is not an error, it is limited to the BLOB content.</p>
 *
 * @see <a href="https://github.com/FirebirdSQL/firebird/blob/v5.0.3/src/jrd/blb.cpp">Firebird blb::BLB_lseek</a>
 */
@RequiredArgsConstructor
public final class FirebirdSeekBlobCommandExecutor implements CommandExecutor {
    
    private final FirebirdSeekBlobCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    @HighFrequencyInvocation
    public Collection<DatabasePacket> execute() {
        int connectionId = connectionSession.getConnectionId();
        int blobHandle = FirebirdBlobHandleGenerator.getInstance().resolveBlobHandle(connectionId, packet.getBlobHandle());
        Optional<Boolean> streamBlob = FirebirdBlobReadCache.getInstance().isStreamBlob(connectionId, blobHandle);
        ShardingSpherePreconditions.checkState(streamBlob.isPresent(), () -> new InvalidSegstrHandleException(blobHandle));
        ShardingSpherePreconditions.checkState(streamBlob.get(), () -> new InvalidSegstrTypeException(blobHandle));
        OptionalInt position = FirebirdBlobReadCache.getInstance().seek(connectionId, blobHandle, packet.getSeekMode(), packet.getOffset());
        return Collections.singleton(new FirebirdGenericResponsePacket().setId(position.getAsInt()));
    }
}
