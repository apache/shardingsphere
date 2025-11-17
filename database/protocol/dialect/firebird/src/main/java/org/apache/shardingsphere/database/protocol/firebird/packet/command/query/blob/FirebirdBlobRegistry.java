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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.blob;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.nio.charset.Charset;

/**
 * Registry for the BLOB segment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobRegistry {
    
    @Getter
    @Setter
    private static byte[] segment;
    
    /**
     * Clear the stored segment.
     */
    public static void clearSegment() {
        segment = null;
    }
    
    /**
     * Wrap the current {@code segment} into a temporary {@link FirebirdPacketPayload}.
     * <p>
     * Layout: {@code [int length][bytes][zero padding to 4-byte alignment]}.
     * If no segment is stored, this method returns {@code null}.
     * </p>
     *
     * @param alloc byte buffer allocator to create the backing buffer
     * @param cs character set used by {@link FirebirdPacketPayload}
     * @return payload containing the length-prefixed segment or {@code null} if segment is absent
     */
    public static FirebirdPacketPayload buildSegmentPayload(final ByteBufAllocator alloc, final Charset cs) {
        if (segment == null) {
            return null;
        }
        int len = segment.length;
        // Padding to the next multiple of 4 (result is in the range 0..3).
        int pad = (4 - (len % 4)) & 0x3;
        ByteBuf buf = alloc.buffer(4 + len + pad);
        buf.writeInt(len);
        buf.writeBytes(segment);
        if (pad > 0) {
            buf.writeZero(pad);
        }
        return new FirebirdPacketPayload(buf, cs);
    }
}
