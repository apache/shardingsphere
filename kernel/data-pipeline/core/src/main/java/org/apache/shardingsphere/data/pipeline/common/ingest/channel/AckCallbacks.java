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

package org.apache.shardingsphere.data.pipeline.common.ingest.channel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ack callback utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AckCallbacks {
    
    /**
     * Ack callback for inventory dump.
     *
     * @param records record list
     * @param position ingest position
     */
    public static void inventoryCallback(final List<Record> records, final AtomicReference<IngestPosition> position) {
        Record lastRecord = records.get(records.size() - 1);
        position.set(lastRecord.getPosition());
    }
    
    /**
     * Ack callback for incremental dump.
     *
     * @param records record list
     * @param progress incremental task progress
     */
    public static void incrementalCallback(final List<Record> records, final IncrementalTaskProgress progress) {
        Record lastHandledRecord = records.get(records.size() - 1);
        if (!(lastHandledRecord.getPosition() instanceof PlaceholderPosition)) {
            progress.setPosition(lastHandledRecord.getPosition());
            progress.getIncrementalTaskDelay().setLastEventTimestamps(lastHandledRecord.getCommitTime());
        }
        progress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
    }
}
