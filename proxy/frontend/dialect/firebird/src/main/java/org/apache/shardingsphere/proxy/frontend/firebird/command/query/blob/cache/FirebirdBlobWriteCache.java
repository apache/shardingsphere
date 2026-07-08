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
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Firebird BLOB.
 *
 * <p>In the Firebird protocol the client transfers a BLOB before the SQL statement: first create_blob,
 * then the data segments, and only then execute with an INSERT/UPDATE that carries a blob id instead of the content.
 * Routing depends on the SQL and the sharding key, so while the BLOB is being transferred the proxy does not know
 * yet which shard it should go to and cannot forward the data to a backend right away. That is why we buffer
 * the BLOB here: the client keeps sending put_segment, and once it calls close the BLOB is fully received
 * and can be substituted into the statement as a regular parameter on execute.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobWriteCache {
    
    public static final int MAX_BLOB_SIZE = 64 * 1024 * 1024;

    private static final FirebirdBlobWriteCache INSTANCE = new FirebirdBlobWriteCache();

    private static final int INVALID_OBJECT_HANDLE = 0xFFFF;
    
    private final Map<Integer, Map<Integer, FirebirdBlobWrite>> writesByHandle = new ConcurrentHashMap<>(16);
    
    private final Map<Integer, Map<Long, FirebirdBlobWrite>> writesById = new ConcurrentHashMap<>(16);
    
    private final Map<Integer, Integer> lastBlobHandle = new ConcurrentHashMap<>(16);
    
    public static FirebirdBlobWriteCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection for BLOB writes.
     *
     * @param connectionId connection id
     */
    public void registerConnection(final int connectionId) {
        writesByHandle.put(connectionId, new ConcurrentHashMap<>(4));
        writesById.put(connectionId, new ConcurrentHashMap<>(4));
    }
    
    /**
     * Unregister connection for BLOB writes.
     *
     * @param connectionId connection id
     */
    public void unregisterConnection(final int connectionId) {
        writesByHandle.remove(connectionId);
        writesById.remove(connectionId);
        lastBlobHandle.remove(connectionId);
    }
    
    /**
     * Register a new BLOB write by handle and id.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param blobId blob id
     */
    public void registerBlob(final int connectionId, final int blobHandle, final long blobId) {
        FirebirdBlobWrite write = new FirebirdBlobWrite(blobHandle, blobId);
        getHandleMap(connectionId).put(blobHandle, write);
        getIdMap(connectionId).put(blobId, write);
        lastBlobHandle.put(connectionId, blobHandle);
    }
    
    /**
     * Get a BLOB handle, mapping the deferred placeholder handle to the last created BLOB handle.
     *
     * <p>In the Firebird lazy (deferred) protocol a pipelined operation such as {@code op_put_segment} that is flushed
     * together with its preceding {@code op_create_blob2} carries the placeholder handle {@code 0xFFFF} (INVALID_OBJECT),
     * which the server resolves to the most recently created object. This mirrors that resolution for BLOB writes.</p>
     *
     * @param connectionId connection id
     * @param blobHandle blob handle received from the client
     * @return resolved blob handle
     */
    public int getBlobHandle(final int connectionId, final int blobHandle) {
        if (INVALID_OBJECT_HANDLE != blobHandle) {
            return blobHandle;
        }
        Integer resolved = lastBlobHandle.get(connectionId);
        return null == resolved ? blobHandle : resolved;
    }
    
    /**
     * Check if appending segment data of the given total length would exceed {@link #MAX_BLOB_SIZE}.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param segmentsLength total length of the segment data to append
     * @return whether the buffered data would exceed the maximum BLOB size
     */
    public boolean exceedsMaxSize(final int connectionId, final int blobHandle, final long segmentsLength) {
        FirebirdBlobWrite write = getHandleMap(connectionId).get(blobHandle);
        return null != write && write.getSize() + segmentsLength > MAX_BLOB_SIZE;
    }

    /**
     * Append segment data by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param segment blob segment
     * @return optional current size of buffered data
     */
    public OptionalInt appendSegment(final int connectionId, final int blobHandle, final byte[] segment) {
        FirebirdBlobWrite write = getHandleMap(connectionId).get(blobHandle);
        if (null == write) {
            return OptionalInt.empty();
        }
        write.append(segment);
        return OptionalInt.of(write.getSize());
    }
    
    /**
     * Close write by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional current size of buffered data
     */
    public OptionalInt closeWrite(final int connectionId, final int blobHandle) {
        FirebirdBlobWrite write = getHandleMap(connectionId).get(blobHandle);
        if (null == write) {
            return OptionalInt.empty();
        }
        write.markClosed();
        return OptionalInt.of(write.getSize());
    }
    
    /**
     * Get blob id by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional blob id
     */
    public OptionalLong getBlobId(final int connectionId, final int blobHandle) {
        FirebirdBlobWrite write = getHandleMap(connectionId).get(blobHandle);
        if (null == write) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(write.getBlobId());
    }
    
    /**
     * Get buffered blob data.
     *
     * @param connectionId connection id
     * @param blobId blob id
     * @return optional buffered bytes
     */
    public Optional<byte[]> getBlobData(final int connectionId, final long blobId) {
        FirebirdBlobWrite write = getIdMap(connectionId).get(blobId);
        if (null == write) {
            return Optional.empty();
        }
        return Optional.of(write.getBytes());
    }
    
    /**
     * Check if write is closed.
     *
     * @param connectionId connection id
     * @param blobId blob id
     * @return whether write is closed
     */
    public boolean isClosed(final int connectionId, final long blobId) {
        FirebirdBlobWrite write = getIdMap(connectionId).get(blobId);
        return null != write && write.isClosed();
    }
    
    /**
     * Remove write by blob id.
     *
     * @param connectionId connection id
     * @param blobId blob id
     */
    public void removeWrite(final int connectionId, final long blobId) {
        FirebirdBlobWrite write = getIdMap(connectionId).remove(blobId);
        if (null != write) {
            getHandleMap(connectionId).remove(write.getBlobHandle());
        }
    }
    
    private Map<Integer, FirebirdBlobWrite> getHandleMap(final int connectionId) {
        return writesByHandle.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4));
    }
    
    private Map<Long, FirebirdBlobWrite> getIdMap(final int connectionId) {
        return writesById.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4));
    }
}
