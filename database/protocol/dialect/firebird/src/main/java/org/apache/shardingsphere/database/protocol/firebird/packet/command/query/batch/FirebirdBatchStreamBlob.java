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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;

/**
 * BLOB read from the BLOB buffer of a Firebird batch.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
public final class FirebirdBatchStreamBlob {
    
    private final long batchBlobId;
    
    private final byte[] bpb;
    
    private final boolean segmented;
    
    private final ByteArrayOutputStream data = new ByteArrayOutputStream();
    
    /**
     * Append BLOB data that Firebird puts into the BLOB at once: one segment, or the stream data up to the next BLOB header.
     *
     * @param bytes BLOB data
     */
    void appendData(final byte[] bytes) {
        data.write(bytes, 0, bytes.length);
    }
}
