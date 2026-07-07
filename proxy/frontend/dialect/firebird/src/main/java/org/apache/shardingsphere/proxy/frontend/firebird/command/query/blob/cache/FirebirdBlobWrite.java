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

@RequiredArgsConstructor
public final class FirebirdBlobWrite {

    @Getter
    private final int blobHandle;

    @Getter
    private final long blobId;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

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

    public int getSize() {
        return buffer.size();
    }

    public byte[] getBytes() {
        return buffer.toByteArray();
    }

    /**
     * Mark this BLOB write as closed.
     */
    public void markClosed() {
        closed = true;
    }
}
