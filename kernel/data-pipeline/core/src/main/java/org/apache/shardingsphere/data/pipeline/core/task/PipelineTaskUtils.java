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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.channel.AckCallbacks;
import org.apache.shardingsphere.data.pipeline.common.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pipeline task utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineTaskUtils {
    
    /**
     * Generate inventory task id.
     *
     * @param inventoryDumperConfig inventory dumper configuration
     * @return inventory task id
     */
    public static String generateInventoryTaskId(final InventoryDumperConfiguration inventoryDumperConfig) {
        String result = String.format("%s.%s", inventoryDumperConfig.getDataSourceName(), inventoryDumperConfig.getActualTableName());
        return null == inventoryDumperConfig.getShardingItem() ? result : result + "#" + inventoryDumperConfig.getShardingItem();
    }
    
    /**
     * Create incremental task progress.
     *
     * @param position ingest position
     * @param jobItemProgress job item progress
     * @return incremental task progress
     */
    public static IncrementalTaskProgress createIncrementalTaskProgress(final IngestPosition position, final InventoryIncrementalJobItemProgress jobItemProgress) {
        IncrementalTaskProgress result = new IncrementalTaskProgress(position);
        if (null != jobItemProgress && null != jobItemProgress.getIncremental()) {
            Optional.ofNullable(jobItemProgress.getIncremental().getIncrementalTaskProgress())
                    .ifPresent(optional -> result.setIncrementalTaskDelay(jobItemProgress.getIncremental().getIncrementalTaskProgress().getIncrementalTaskDelay()));
        }
        return result;
    }
    
    /**
     * Create channel for inventory task.
     *
     * @param pipelineChannelCreator channel creator
     * @param averageElementSize average element size
     * @param position ingest position
     * @return channel
     */
    public static PipelineChannel createInventoryChannel(final PipelineChannelCreator pipelineChannelCreator, final int averageElementSize, final AtomicReference<IngestPosition> position) {
        return pipelineChannelCreator.createPipelineChannel(1, averageElementSize, records -> AckCallbacks.inventoryCallback(records, position));
    }
    
    /**
     * Create incremental channel.
     *
     * @param concurrency output concurrency
     * @param pipelineChannelCreator channel creator
     * @param progress incremental task progress
     * @return channel
     */
    public static PipelineChannel createIncrementalChannel(final int concurrency, final PipelineChannelCreator pipelineChannelCreator, final IncrementalTaskProgress progress) {
        return pipelineChannelCreator.createPipelineChannel(concurrency, 5, records -> AckCallbacks.incrementalCallback(records, progress));
    }
}
