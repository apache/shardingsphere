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

package org.apache.shardingsphere.data.pipeline.core.task;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannelAckCallback;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;

import java.util.List;

/**
 * Incremental task acknowledged callback.
 */
@RequiredArgsConstructor
public final class IncrementalTaskAckCallback implements PipelineChannelAckCallback {
    
    private final IncrementalTaskProgress progress;
    
    @Override
    public void onAck(final List<Record> records) {
        Record lastHandledRecord = records.get(records.size() - 1);
        if (!(lastHandledRecord.getPosition() instanceof IngestPlaceholderPosition)) {
            progress.setPosition(lastHandledRecord.getPosition());
            progress.getIncrementalTaskDelay().setLastEventTimestamps(lastHandledRecord.getCommitTime());
        }
        if (!records.isEmpty() && records.stream().anyMatch(DataRecord.class::isInstance)) {
            progress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
        }
    }
}
