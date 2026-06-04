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

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Firebird batch statement metadata.
 */
@Getter
public final class FirebirdBatchStatement {
    
    private final int statementHandle;
    
    private final List<FirebirdBinaryColumnType> columnTypes;
    
    private final long bufferSize;
    
    private final List<List<Object>> parameterValues = new ArrayList<>();
    
    private long accumulatedSize;
    
    private int framedOffset;
    
    private long framedCount;
    
    public FirebirdBatchStatement(final int statementHandle) {
        this(statementHandle, Collections.emptyList(), 0L);
    }
    
    public FirebirdBatchStatement(final int statementHandle, final List<FirebirdBinaryColumnType> columnTypes, final long bufferSize) {
        this.statementHandle = statementHandle;
        this.columnTypes = columnTypes;
        this.bufferSize = bufferSize;
    }
    
    /**
     * Add batched parameter values.
     * @param values parameter values to add
     */
    public void addParameterValues(final List<Object> values) {
        parameterValues.add(values);
    }
    
    /**
     * Add accumulated batch message size in bytes.
     * @param size size in bytes to add
     */
    public void addSize(final long size) {
        accumulatedSize += size;
    }
    
    /**
     * Remember how far the current batch message has been framed, so the next chunk only parses new messages.
     *
     * @param framedOffset byte offset (from packet start) of the next unparsed message
     * @param framedCount number of messages already framed
     */
    public void setFramingProgress(final int framedOffset, final long framedCount) {
        this.framedOffset = framedOffset;
        this.framedCount = framedCount;
    }
    
    /**
     * Clear framing progress once a batch message packet is fully framed.
     */
    public void clearFramingProgress() {
        framedOffset = 0;
        framedCount = 0;
    }
    
    /**
     * Reset accumulated batch state.
     */
    public void reset() {
        parameterValues.clear();
        accumulatedSize = 0;
        clearFramingProgress();
    }
}
