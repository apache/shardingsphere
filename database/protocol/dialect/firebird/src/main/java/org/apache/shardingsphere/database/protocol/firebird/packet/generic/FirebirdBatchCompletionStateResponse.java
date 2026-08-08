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

package org.apache.shardingsphere.database.protocol.firebird.packet.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch completion state response (op_batch_cs) for Firebird.
 *
 * <p>Failures are reported as detailed errors (record number plus status vector). The simplified-errors list
 * (p_batch_errors, record numbers without a status vector) is always empty: the proxy holds the full status vector for
 * the failure, so it never needs the record-number-only fallback that Firebird uses to cap server-side memory.</p>
 */
public class FirebirdBatchCompletionStateResponse extends FirebirdPacket {
    
    private int statementHandle;
    
    private long recordsCount;
    
    private int[] updateCounts = new int[0];
    
    private final List<DetailedError> detailedErrors = new ArrayList<>();
    
    /**
     * Set statement handle.
     *
     * @param objectHandle statement handle
     * @return this response
     */
    public FirebirdBatchCompletionStateResponse setHandle(final int objectHandle) {
        statementHandle = objectHandle;
        return this;
    }
    
    /**
     * Set records count.
     *
     * @param recordsCount records count
     * @return this response
     */
    public FirebirdBatchCompletionStateResponse setRecordsCount(final long recordsCount) {
        this.recordsCount = recordsCount;
        return this;
    }
    
    /**
     * Set update counts.
     *
     * @param updateCounts update counts
     * @return this response
     */
    public FirebirdBatchCompletionStateResponse setUpdateCounts(final int[] updateCounts) {
        this.updateCounts = null == updateCounts ? new int[0] : updateCounts;
        return this;
    }
    
    /**
     * Add detailed error for a failed batch message.
     *
     * @param element zero-based index of the failed batch message
     * @param statusVector status vector describing the failure
     * @return this response
     */
    public FirebirdBatchCompletionStateResponse addDetailedError(final int element, final FirebirdStatusVector statusVector) {
        detailedErrors.add(new DetailedError(element, statusVector));
        return this;
    }
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.BATCH_CS.getValue());
        payload.writeInt4(statementHandle);
        payload.writeInt4(Math.toIntExact(recordsCount));
        payload.writeInt4(updateCounts.length);
        payload.writeInt4(detailedErrors.size());
        payload.writeInt4(0);
        for (int c : updateCounts) {
            payload.writeInt4(c);
        }
        for (DetailedError each : detailedErrors) {
            payload.writeInt4(each.element);
            each.statusVector.write(payload);
        }
    }
    
    /**
     * Detailed error reporting a failed batch message.
     */
    @RequiredArgsConstructor
    @Getter
    public static final class DetailedError {
        
        private final int element;
        
        private final FirebirdStatusVector statusVector;
    }
}
