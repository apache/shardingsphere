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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Binder for Firebird BLOB parameters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdBlobParameterBinder {
    
    /**
     * Bind BLOB parameters and update parameter values.
     *
     * @param connectionId connection id
     * @param params parameter values
     * @param parameterTypes parameter types
     * @return updated parameter values
     */
    public static BindResult bindBlobParameters(final int connectionId,
                                                final List<Object> params,
                                                final List<FirebirdBinaryColumnType> parameterTypes) {
        final List<Long> blobIds = new LinkedList<>();
        final int paramCount = Math.min(parameterTypes.size(), params.size());
        for (int index = 0; index < paramCount; index++) {
            if (FirebirdBinaryColumnType.BLOB != parameterTypes.get(index)) {
                continue;
            }
            final Object paramValue = params.get(index);
            if (!(paramValue instanceof Long)) {
                params.set(index, null);
                continue;
            }
            final long blobId = (Long) paramValue;
            if (blobId <= 0L || !FirebirdBlobUploadCache.getInstance().isClosed(connectionId, blobId)) {
                params.set(index, null);
                continue;
            }
            final Optional<byte[]> blobData = FirebirdBlobUploadCache.getInstance().getBlobData(connectionId, blobId);
            if (!blobData.isPresent()) {
                params.set(index, null);
                continue;
            }
            params.set(index, blobData.get());
            blobIds.add(blobId);
        }
        return new BindResult(params, blobIds);
    }
    
    /**
     * Clear cached BLOB uploads by ids.
     *
     * @param connectionId connection id
     * @param blobIds blob ids
     */
    public static void clearBlobUploads(final int connectionId, final List<Long> blobIds) {
        for (Long each : blobIds) {
            FirebirdBlobUploadCache.getInstance().removeUpload(connectionId, each);
        }
    }
    
    /**
     * Check whether parameter types contain BLOB type.
     *
     * @param parameterTypes parameter types
     * @return {@code true} if at least one parameter type is BLOB, otherwise {@code false}
     */
    public static boolean containsBlob(final List<FirebirdBinaryColumnType> parameterTypes) {
        for (FirebirdBinaryColumnType type : parameterTypes) {
            if (type == FirebirdBinaryColumnType.BLOB) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Bind BLOB parameter result.
     */
    @RequiredArgsConstructor
    @Getter
    public static final class BindResult {
        
        private final List<Object> params;
        
        private final List<Long> blobIds;
    }
}
