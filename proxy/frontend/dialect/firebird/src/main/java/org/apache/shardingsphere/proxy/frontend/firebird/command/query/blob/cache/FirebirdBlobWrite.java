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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.lang.ref.SoftReference;
import java.util.Optional;

/**
 * Firebird BLOB data written by the client and buffered by the proxy.
 *
 * <p>Firebird keeps the BLOB ID of a temporary BLOB used by a statement valid until the transaction ends, but stores its data in the database instead of memory.
 * That is why the proxy keeps the data of a used BLOB only softly reachable: it stays available for reuse until the garbage collector needs the memory.</p>
 */
@RequiredArgsConstructor
public final class FirebirdBlobWrite {
    
    @Getter
    private final int blobHandle;
    
    @Getter
    private final long blobId;
    
    @Getter
    private final int transactionId;
    
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    
    private byte[] content;
    
    private SoftReference<byte[]> usedContent;
    
    @Getter
    private boolean closed;
    
    /**
     * Append a BLOB data segment.
     *
     * @param segment BLOB data segment bytes
     */
    public void append(final byte[] segment) {
        buffer.write(segment, 0, segment.length);
    }
    
    /**
     * Get size of BLOB data.
     *
     * @return size of BLOB data in bytes
     */
    public int getSize() {
        return closed ? content.length : buffer.size();
    }
    
    /**
     * Get BLOB data.
     *
     * <p>The data of a closed write is shared by every caller without copying and must not be modified.</p>
     *
     * @return BLOB data, or empty if the garbage collector released the data of a used write
     */
    public Optional<byte[]> getBytes() {
        if (!closed) {
            return Optional.of(buffer.toByteArray());
        }
        return Optional.ofNullable(null == content ? usedContent.get() : content);
    }
    
    /**
     * Mark this BLOB write as closed and keep its data as one exact-size array.
     */
    public void markClosed() {
        content = buffer.toByteArray();
        buffer = null;
        closed = true;
    }
    
    /**
     * Mark this closed BLOB write as used by a statement and keep its data only softly reachable.
     */
    void markUsed() {
        if (null != content) {
            usedContent = new SoftReference<>(content);
            content = null;
        }
    }
}
