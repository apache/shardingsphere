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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Firebird BLOB data written by the client and buffered by the proxy.
 *
 * <p>Used BLOB data is backed by a temporary file until its transaction ends so statements and open_blob can reuse its ID independently of garbage collection.</p>
 */
@RequiredArgsConstructor
public final class FirebirdBlobWrite {
    
    @Getter
    private final int blobHandle;
    
    @Getter
    private final long blobId;
    
    @Getter
    private final int transactionId;
    
    @Getter
    private final boolean streamBlob;
    
    private ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    
    private byte[] content;
    
    private SoftReference<byte[]> usedContent;
    
    private Path backingFile;
    
    private int size;
    
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
        return closed ? size : buffer.size();
    }
    
    /**
     * Get BLOB data.
     *
     * <p>Callers must not modify the returned data, which is shared while cached.</p>
     *
     * @return BLOB data
     */
    @HighFrequencyInvocation
    @SneakyThrows(IOException.class)
    public Optional<byte[]> getBytes() {
        if (!closed) {
            return Optional.of(buffer.toByteArray());
        }
        if (null != content) {
            return Optional.of(content);
        }
        byte[] result = usedContent.get();
        if (null == result) {
            result = Files.readAllBytes(backingFile);
            usedContent = new SoftReference<>(result);
        }
        return Optional.of(result);
    }
    
    /**
     * Mark this BLOB write as closed and keep its data as one exact-size array.
     */
    @HighFrequencyInvocation
    public void markClosed() {
        content = buffer.toByteArray();
        size = content.length;
        buffer = null;
        closed = true;
    }
    
    @HighFrequencyInvocation
    @SneakyThrows(IOException.class)
    void markUsed() {
        if (null != content) {
            if (null == backingFile) {
                backingFile = Files.createTempFile("shardingsphere-firebird-blob-", ".tmp");
            }
            Files.write(backingFile, content);
            usedContent = new SoftReference<>(content);
            content = null;
        }
    }
    
    @HighFrequencyInvocation
    @SneakyThrows(IOException.class)
    void release() {
        if (null != backingFile) {
            Files.delete(backingFile);
            backingFile = null;
        }
    }
}
