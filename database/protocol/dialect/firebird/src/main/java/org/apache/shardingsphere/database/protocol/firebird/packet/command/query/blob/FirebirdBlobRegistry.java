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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Registry for the BLOB segment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobRegistry {
    
    private static final FirebirdBlobRegistry INSTANCE = new FirebirdBlobRegistry();
    
    @Getter
    @Setter
    private static byte[] segment;
    
    private final Map<Integer, Map<Integer, FirebirdOpenBlobState>> openBlobsByHandle = new ConcurrentHashMap<>(16);
    
    /**
     * Get registry instance.
     *
     * @return registry instance
     */
    public static FirebirdBlobRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection for open BLOB state.
     *
     * @param connectionId connection id
     */
    public void registerConnection(final int connectionId) {
        openBlobsByHandle.put(connectionId, new ConcurrentHashMap<>(4));
    }
    
    /**
     * Unregister connection for open BLOB state.
     *
     * @param connectionId connection id
     */
    public void unregisterConnection(final int connectionId) {
        openBlobsByHandle.remove(connectionId);
    }
    
    /**
     * Register opened BLOB content by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param content blob content
     */
    public void openBlob(final int connectionId, final int blobHandle, final byte[] content) {
        // Keep an owned snapshot so per-handle cursor state is isolated from external mutation.
        getOpenBlobMap(connectionId).put(blobHandle, new FirebirdOpenBlobState(null == content ? new byte[0] : content.clone()));
    }
    
    /**
     * Remove opened BLOB state by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     */
    public void closeBlob(final int connectionId, final int blobHandle) {
        getOpenBlobMap(connectionId).remove(blobHandle);
    }
    
    /**
     * Read segment data from the current BLOB position and advance the cursor.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param requestedLength requested segment length
     * @return segment data
     */
    public byte[] readSegment(final int connectionId, final int blobHandle, final int requestedLength) {
        FirebirdOpenBlobState state = getOpenBlobMap(connectionId).get(blobHandle);
        if (null == state || requestedLength <= 0 || state.isEof()) {
            return new byte[0];
        }
        int actualLength = Math.min(requestedLength, state.getRemainingLength());
        byte[] result = Arrays.copyOfRange(state.content, state.position, state.position + actualLength);
        state.position += actualLength;
        return result;
    }
    
    /**
     * Seek to the requested position and return the resulting cursor.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param seekMode seek mode
     * @param offset offset
     * @return resulting cursor position
     * @throws IllegalArgumentException unsupported seek mode
     */
    public int seek(final int connectionId, final int blobHandle, final int seekMode, final int offset) {
        FirebirdOpenBlobState state = getOpenBlobMap(connectionId).get(blobHandle);
        if (null == state) {
            return 0;
        }
        int result;
        switch (seekMode) {
            case 0:
                result = clamp(offset, state.content.length);
                break;
            case 1:
                result = clamp(state.position + offset, state.content.length);
                break;
            case 2:
                result = clamp(state.content.length + offset, state.content.length);
                break;
            default:
                throw new IllegalArgumentException(String.format("No SeekMode with id %d", seekMode));
        }
        state.position = result;
        return result;
    }
    
    /**
     * Get total BLOB length by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return total BLOB length
     */
    public int getBlobLength(final int connectionId, final int blobHandle) {
        FirebirdOpenBlobState state = getOpenBlobMap(connectionId).get(blobHandle);
        return null == state ? 0 : state.content.length;
    }
    
    /**
     * Check if the BLOB cursor reached EOF.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return whether the BLOB cursor reached EOF
     */
    public boolean isEof(final int connectionId, final int blobHandle) {
        FirebirdOpenBlobState state = getOpenBlobMap(connectionId).get(blobHandle);
        return null == state || state.isEof();
    }
    
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
    
    private Map<Integer, FirebirdOpenBlobState> getOpenBlobMap(final int connectionId) {
        return openBlobsByHandle.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4));
    }
    
    private int clamp(final int position, final int length) {
        return Math.max(0, Math.min(position, length));
    }
    
    private static final class FirebirdOpenBlobState {
        
        private final byte[] content;
        
        private int position;
        
        private FirebirdOpenBlobState(final byte[] content) {
            this.content = content;
        }
        
        private int getRemainingLength() {
            return content.length - position;
        }
        
        private boolean isEof() {
            return position >= content.length;
        }
    }
}
