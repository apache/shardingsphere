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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.CannotUpdateOldBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidSegstrHandleException;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;

/**
 * Validates a BLOB handle used for a write operation for Firebird.
 *
 * <p>A handle that is still allocated but absent from the write cache belongs to a BLOB opened for reading,
 * which Firebird rejects with a dedicated error instead of reporting the handle as unknown.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FirebirdBlobWriteHandleValidator {
    
    /**
     * Validate that a BLOB handle is open for writing.
     *
     * @param connectionId connection id
     * @param blobHandle blob handle
     * @throws CannotUpdateOldBlobException when the handle is a valid handle opened for reading
     * @throws InvalidSegstrHandleException when the handle is unknown
     */
    static void validate(final int connectionId, final int blobHandle) {
        if (FirebirdBlobWriteCache.getInstance().getBlobId(connectionId, blobHandle).isPresent()) {
            return;
        }
        if (FirebirdBlobHandleGenerator.getInstance().isAllocated(connectionId, blobHandle)) {
            throw new CannotUpdateOldBlobException(blobHandle);
        }
        throw new InvalidSegstrHandleException(blobHandle);
    }
}
