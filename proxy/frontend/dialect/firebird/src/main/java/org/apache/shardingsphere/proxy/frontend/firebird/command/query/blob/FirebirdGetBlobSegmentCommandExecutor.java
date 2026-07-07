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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob.FirebirdGetBlobSegmentResponsePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Get blob segment command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdGetBlobSegmentCommandExecutor implements CommandExecutor {

    private static final int SEGMENT_STATE_COMPLETE = 0;

    private static final int SEGMENT_STATE_PARTIAL = 1;

    private static final int SEGMENT_STATE_EOF = 2;

    private final FirebirdGetBlobSegmentCommandPacket packet;

    private final ConnectionSession connectionSession;

    @Override
    public Collection<DatabasePacket> execute() {
        int connectionId = connectionSession.getConnectionId();
        Optional<byte[]> remaining = FirebirdBlobReadCache.getInstance().getSegment(connectionId, packet.getBlobHandle());
        if (!remaining.isPresent() || 0 == remaining.get().length) {
            return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(SEGMENT_STATE_EOF));
        }
        byte[] segment = remaining.get();
        int segmentLength = Math.min(packet.getSegmentLength(), segment.length);
        byte[] payloadSegment = Arrays.copyOf(segment, segmentLength);
        int segmentState;
        if (segmentLength >= segment.length) {
            FirebirdBlobReadCache.getInstance().removeBlob(connectionId, packet.getBlobHandle());
            segmentState = SEGMENT_STATE_COMPLETE;
        } else {
            FirebirdBlobReadCache.getInstance().setSegment(connectionId, packet.getBlobHandle(), Arrays.copyOfRange(segment, segmentLength, segment.length));
            segmentState = SEGMENT_STATE_PARTIAL;
        }
        FirebirdGetBlobSegmentResponsePacket responsePacket = new FirebirdGetBlobSegmentResponsePacket(payloadSegment);
        return Collections.singleton(new FirebirdGenericResponsePacket().setHandle(segmentState).setData(responsePacket));
    }
}