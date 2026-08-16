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
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobReadCache {
    
    private static final FirebirdBlobReadCache INSTANCE = new FirebirdBlobReadCache();
    
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
     */
    public void registerBlob(final int connectionId, final int blobHandle, final byte[] content) {
        getCursorMap(connectionId).put(blobHandle, new BlobReadCursor(content));
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
        Map<Integer, BlobReadCursor> cursorMap = getCursorMap(connectionId);
        BlobReadCursor cursor = cursorMap.get(blobHandle);
        if (null == cursor || 0 == cursor.getRemainingSize()) {
            return Optional.empty();
        }
        int segmentLength = Math.min(maximumLength, cursor.getRemainingSize());
        byte[] data = Arrays.copyOfRange(cursor.content, cursor.offset, cursor.offset + segmentLength);
        cursor.offset += segmentLength;
        boolean complete = 0 == cursor.getRemainingSize();
        if (complete) {
            cursorMap.remove(blobHandle);
        }
        return Optional.of(new BlobSegment(data, complete));
    }
    
    /**
     * Get remaining BLOB size by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional remaining BLOB size
     */
    public OptionalInt getRemainingSize(final int connectionId, final int blobHandle) {
        BlobReadCursor cursor = getCursorMap(connectionId).get(blobHandle);
        return null == cursor ? OptionalInt.empty() : OptionalInt.of(cursor.getRemainingSize());
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
        
        private int offset;
        
        private int getRemainingSize() {
            return content.length - offset;
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
