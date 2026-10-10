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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for Firebird BLOB reads buffered by the proxy.
 *
 * <p>Read direction counterpart of the write cache: open_blob puts the whole BLOB content here,
 * and get_segment hands it out to the client chunk by chunk using a cursor over the original content.
 * Entries are keyed by connection id and blob handle, so concurrent connections and BLOBs do not interfere.</p>
 *
 * <p>A cursor lives until close_blob or cancel_blob releases its handle, so a fully read BLOB can be repositioned
 * by seek_blob and read again, as Firebird clears the end of file state when a BLOB is positioned.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobReadCache {
    
    private static final FirebirdBlobReadCache INSTANCE = new FirebirdBlobReadCache();
    
    private static final int SEEK_MODE_RELATIVE = 1;
    
    private static final int SEEK_MODE_FROM_TAIL = 2;
    
    private final Map<Integer, Map<Integer, BlobReadCursor>> cursors = new ConcurrentHashMap<>(16);
    
    public static FirebirdBlobReadCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection for BLOB reads.
     *
     * @param connectionId connection id
     */
    public void registerConnection(final int connectionId) {
        cursors.put(connectionId, new ConcurrentHashMap<>(4));
    }
    
    /**
     * Unregister connection for BLOB reads.
     *
     * @param connectionId connection id
     */
    public void unregisterConnection(final int connectionId) {
        cursors.remove(connectionId);
    }
    
    /**
     * Register an opened BLOB with its full content.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param content blob content
     * @param streamBlob whether the BLOB is a stream BLOB
     */
    public void registerBlob(final int connectionId, final int blobHandle, final byte[] content, final boolean streamBlob) {
        getCursorMap(connectionId).put(blobHandle, new BlobReadCursor(content, streamBlob));
    }
    
    /**
     * Read segment data by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param maximumLength maximum segment length
     * @return optional segment data
     */
    public Optional<BlobSegment> readSegment(final int connectionId, final int blobHandle, final int maximumLength) {
        BlobReadCursor cursor = getCursorMap(connectionId).get(blobHandle);
        if (null == cursor || 0 == cursor.getRemainingSize()) {
            return Optional.empty();
        }
        int segmentLength = Math.min(maximumLength, cursor.getRemainingSize());
        byte[] data = Arrays.copyOfRange(cursor.content, cursor.offset, cursor.offset + segmentLength);
        cursor.offset += segmentLength;
        return Optional.of(new BlobSegment(data, 0 == cursor.getRemainingSize()));
    }
    
    /**
     * Get total BLOB size by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional total BLOB size
     */
    @HighFrequencyInvocation
    public OptionalInt getTotalSize(final int connectionId, final int blobHandle) {
        BlobReadCursor cursor = getCursorMap(connectionId).get(blobHandle);
        return null == cursor ? OptionalInt.empty() : OptionalInt.of(cursor.content.length);
    }
    
    /**
     * Judge whether an opened BLOB is a stream BLOB.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional stream BLOB state
     */
    @HighFrequencyInvocation
    public Optional<Boolean> isStreamBlob(final int connectionId, final int blobHandle) {
        BlobReadCursor cursor = getCursorMap(connectionId).get(blobHandle);
        return null == cursor ? Optional.empty() : Optional.of(cursor.streamBlob);
    }
    
    /**
     * Position an opened BLOB for the following reads.
     *
     * <p>Mode 1 positions relative to the current position and mode 2 relative to the end of the BLOB,
     * every other mode positions from the start. The resulting position is limited to the BLOB content.</p>
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param seekMode seek mode
     * @param offset offset to position by
     * @return optional resulting position
     */
    @HighFrequencyInvocation
    public OptionalInt seek(final int connectionId, final int blobHandle, final int seekMode, final int offset) {
        BlobReadCursor cursor = getCursorMap(connectionId).get(blobHandle);
        return null == cursor ? OptionalInt.empty() : OptionalInt.of(cursor.seek(seekMode, offset));
    }
    
    /**
     * Remove BLOB by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     */
    public void removeBlob(final int connectionId, final int blobHandle) {
        getCursorMap(connectionId).remove(blobHandle);
    }
    
    private Map<Integer, BlobReadCursor> getCursorMap(final int connectionId) {
        Map<Integer, BlobReadCursor> result = cursors.get(connectionId);
        return null == result ? cursors.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4)) : result;
    }
    
    @RequiredArgsConstructor
    private static final class BlobReadCursor {
        
        private final byte[] content;
        
        private final boolean streamBlob;
        
        private int offset;
        
        private int getRemainingSize() {
            return content.length - offset;
        }
        
        @HighFrequencyInvocation
        private int seek(final int seekMode, final int seekOffset) {
            int position = getSeekPosition(seekMode, seekOffset);
            offset = Math.min(Math.max(position, 0), content.length);
            return offset;
        }
        
        private int getSeekPosition(final int seekMode, final int seekOffset) {
            if (SEEK_MODE_RELATIVE == seekMode) {
                return offset + seekOffset;
            }
            return SEEK_MODE_FROM_TAIL == seekMode ? content.length + seekOffset : seekOffset;
        }
    }
    
    /**
     * Firebird BLOB segment.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static final class BlobSegment {
        
        private final byte[] data;
        
        private final boolean complete;
    }
}
