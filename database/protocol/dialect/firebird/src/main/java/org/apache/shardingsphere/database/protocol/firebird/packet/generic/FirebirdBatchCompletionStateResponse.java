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

import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

public class FirebirdBatchCompletionStateResponse extends FirebirdPacket {
    
    private int statementHandle;
    
    private long recordsCount;
    
    private long batchVectors;
    
    private long batchErrors;
    
    private int[] updateCounts = new int[0];
    
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
    
    @Override
    protected void write(final FirebirdPacketPayload payload) {
        payload.writeInt4(FirebirdCommandPacketType.BATCH_CS.getValue());
        payload.writeInt4(statementHandle);
        payload.writeInt4(Math.toIntExact(recordsCount));
        payload.writeInt4(updateCounts.length);
        batchVectors = 0;
        batchErrors = 0;
        payload.writeInt4((int) batchVectors);
        payload.writeInt4((int) batchErrors);
        for (int c : updateCounts) {
            payload.writeInt4(c);
        }
    }
}
