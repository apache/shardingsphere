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
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Firebird BLOB reads buffered by the proxy.
 *
 * <p>Read direction counterpart of the write cache: open_blob puts the whole BLOB content here,
 * and get_segment hands it out to the client chunk by chunk, replacing the stored value with the remainder.
 * Entries are keyed by connection id and blob handle, so concurrent connections and BLOBs do not interfere.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobReadCache {
    
    private static final FirebirdBlobReadCache INSTANCE = new FirebirdBlobReadCache();
    
    private final Map<Integer, Map<Integer, byte[]>> remainingSegments = new ConcurrentHashMap<>(16);
    
    public static FirebirdBlobReadCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection for BLOB reads.
     *
     * @param connectionId connection id
     */
    public void registerConnection(final int connectionId) {
        remainingSegments.put(connectionId, new ConcurrentHashMap<>(4));
    }
    
    /**
     * Unregister connection for BLOB reads.
     *
     * @param connectionId connection id
     */
    public void unregisterConnection(final int connectionId) {
        remainingSegments.remove(connectionId);
    }
    
    /**
     * Register an opened BLOB with its full content.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param content blob content
     */
    public void registerBlob(final int connectionId, final int blobHandle, final byte[] content) {
        getSegmentMap(connectionId).put(blobHandle, content);
    }
    
    /**
     * Get remaining segment data by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional remaining segment data
     */
    public Optional<byte[]> getSegment(final int connectionId, final int blobHandle) {
        return Optional.ofNullable(getSegmentMap(connectionId).get(blobHandle));
    }
    
    /**
     * Replace remaining segment data by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param segment remaining segment data
     */
    public void setSegment(final int connectionId, final int blobHandle, final byte[] segment) {
        getSegmentMap(connectionId).put(blobHandle, segment);
    }
    
    /**
     * Remove BLOB by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     */
    public void removeBlob(final int connectionId, final int blobHandle) {
        getSegmentMap(connectionId).remove(blobHandle);
    }
    
    private Map<Integer, byte[]> getSegmentMap(final int connectionId) {
        return remainingSegments.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4));
    }
}
