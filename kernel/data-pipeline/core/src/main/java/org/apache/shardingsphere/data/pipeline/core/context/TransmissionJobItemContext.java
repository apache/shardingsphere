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

package org.apache.shardingsphere.data.pipeline.core.context;

import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;

import java.util.Collection;

/**
 * Transmission job item context.
 */
public interface TransmissionJobItemContext extends PipelineJobItemContext, PipelineJobProgressListener {
    
    @Override
    TransmissionProcessContext getJobProcessContext();
    
    /**
     * Get inventory tasks.
     *
     * @return inventory tasks
     */
    Collection<PipelineTask> getInventoryTasks();
    
    /**
     * Get incremental tasks.
     *
     * @return incremental tasks
     */
    Collection<PipelineTask> getIncrementalTasks();
    
    /**
     * Get init progress.
     *
     * @return init progress
     */
    TransmissionJobItemProgress getInitProgress();
    
    /**
     * Get source meta data loader.
     *
     * @return source meta data loader
     */
    PipelineTableMetaDataLoader getSourceMetaDataLoader();
    
    /**
     * Get sink.
     *
     * @return sink
     */
    PipelineSink getSink();
    
    /**
     * Get processed record count.
     *
     * @return processed record count.
     */
    long getProcessedRecordsCount();
    
    /**
     * Update inventory records count.
     *
     * @param recordsCount records count
     */
    void updateInventoryRecordsCount(long recordsCount);
    
    /**
     * Get inventory records count.
     *
     * @return inventory records count
     */
    long getInventoryRecordsCount();
    
    @Override
    default TransmissionJobItemProgress toProgress() {
        return new TransmissionJobItemProgress(this);
    }
}
