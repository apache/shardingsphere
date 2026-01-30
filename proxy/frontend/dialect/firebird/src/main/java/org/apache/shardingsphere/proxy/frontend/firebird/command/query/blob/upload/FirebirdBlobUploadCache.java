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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.upload;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Firebird BLOB upload segments.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobUploadCache {

    private static final FirebirdBlobUploadCache INSTANCE = new FirebirdBlobUploadCache();

    private final Map<Integer, Map<Integer, FirebirdBlobUpload>> uploadsByHandle = new ConcurrentHashMap<>(16);

    private final Map<Integer, Map<Long, FirebirdBlobUpload>> uploadsById = new ConcurrentHashMap<>(16);

    public static FirebirdBlobUploadCache getInstance() {
        return INSTANCE;
    }

    /**
     * Register connection for BLOB uploads.
     *
     * @param connectionId connection id
     */
    public void registerConnection(final int connectionId) {
        uploadsByHandle.put(connectionId, new ConcurrentHashMap<>(4));
        uploadsById.put(connectionId, new ConcurrentHashMap<>(4));
    }

    /**
     * Unregister connection for BLOB uploads.
     *
     * @param connectionId connection id
     */
    public void unregisterConnection(final int connectionId) {
        uploadsByHandle.remove(connectionId);
        uploadsById.remove(connectionId);
    }

    /**
     * Register a new BLOB upload by handle and id.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @param blobId blob id
     */
    public void registerBlob(final int connectionId, final int blobHandle, final long blobId) {
        FirebirdBlobUpload upload = new FirebirdBlobUpload(blobHandle, blobId);
        getHandleMap(connectionId).put(blobHandle, upload);
        getIdMap(connectionId).put(blobId, upload);
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
        FirebirdBlobUpload upload = getHandleMap(connectionId).get(blobHandle);
        if (null == upload) {
            return OptionalInt.empty();
        }
        upload.append(segment);
        return OptionalInt.of(upload.getSize());
    }

    /**
     * Close upload by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional current size of buffered data
     */
    public OptionalInt closeUpload(final int connectionId, final int blobHandle) {
        FirebirdBlobUpload upload = getHandleMap(connectionId).get(blobHandle);
        if (null == upload) {
            return OptionalInt.empty();
        }
        upload.markClosed();
        return OptionalInt.of(upload.getSize());
    }

    /**
     * Get blob id by handle.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @return optional blob id
     */
    public OptionalLong getBlobId(final int connectionId, final int blobHandle) {
        FirebirdBlobUpload upload = getHandleMap(connectionId).get(blobHandle);
        if (null == upload) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(upload.getBlobId());
    }

    /**
     * Get buffered blob data.
     *
     * @param connectionId connection id
     * @param blobId blob id
     * @return optional buffered bytes
     */
    public Optional<byte[]> getBlobData(final int connectionId, final long blobId) {
        FirebirdBlobUpload upload = getIdMap(connectionId).get(blobId);
        if (null == upload) {
            return Optional.empty();
        }
        return Optional.of(upload.getBytes());
    }

    /**
     * Check if upload is closed.
     *
     * @param connectionId connection id
     * @param blobId blob id
     * @return whether upload is closed
     */
    public boolean isClosed(final int connectionId, final long blobId) {
        FirebirdBlobUpload upload = getIdMap(connectionId).get(blobId);
        return null != upload && upload.isClosed();
    }

    /**
     * Remove upload by blob id.
     *
     * @param connectionId connection id
     * @param blobId blob id
     */
    public void removeUpload(final int connectionId, final long blobId) {
        FirebirdBlobUpload upload = getIdMap(connectionId).remove(blobId);
        if (null == upload) {
            return;
        }
        getHandleMap(connectionId).remove(upload.getBlobHandle());
    }

    private Map<Integer, FirebirdBlobUpload> getHandleMap(final int connectionId) {
        return uploadsByHandle.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4));
    }

    private Map<Long, FirebirdBlobUpload> getIdMap(final int connectionId) {
        return uploadsById.computeIfAbsent(connectionId, key -> new ConcurrentHashMap<>(4));
    }
}
