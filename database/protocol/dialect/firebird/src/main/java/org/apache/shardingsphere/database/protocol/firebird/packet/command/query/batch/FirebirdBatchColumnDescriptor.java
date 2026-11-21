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
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;

/**
 * Firebird batch column descriptor.
 */
@RequiredArgsConstructor
@Getter
public final class FirebirdBatchColumnDescriptor {
    
    private final FirebirdBinaryColumnType type;
    
    private final int length;
    
    private final int scale;
    
    private final int offset;
    
    /**
     * BLOB sub type: {@code 0} for binary data, {@code 1} for text. It is {@code 0} by default if the sub type is not set explicitly.
     */
    @Setter(AccessLevel.PACKAGE)
    private int subType;
    
    /**
     * Whether the field holds a BLOB ID that refers to the batch.
     *
     * <p>BLOBs inside a batch have their own IDs. The driver sends BLOB data separately from the rows and gives each BLOB its own number: 1, 2, 3, and so on.
     * When it executes {@code op_batch_exec}, the server finds the BLOB that corresponds to each number.
     * If {@code true}, the field holds the number of a BLOB inside the batch, and the actual BLOB is found by this number.
     * If {@code false}, the field already holds the ID of an actual BLOB, which is used as is.</p>
     */
    @Setter(AccessLevel.PACKAGE)
    private boolean batchBlobId;
}
