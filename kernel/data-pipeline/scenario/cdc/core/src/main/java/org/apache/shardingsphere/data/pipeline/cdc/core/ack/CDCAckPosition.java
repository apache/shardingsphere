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

package org.apache.shardingsphere.data.pipeline.cdc.core.ack;

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * CDC ACK position.
 */
public final class CDCAckPosition {
    
    @Getter
    private final Record lastRecord;
    
    private final AtomicInteger dataRecordCount = new AtomicInteger();
    
    public CDCAckPosition(final Record lastRecord, final int dataRecordCount) {
        this.lastRecord = lastRecord;
        this.dataRecordCount.set(dataRecordCount);
    }
    
    /**
     * Get data record count.
     *
     * @return data record count.
     */
    public int getDataRecordCount() {
        return dataRecordCount.get();
    }
}
