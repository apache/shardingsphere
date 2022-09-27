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

import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.listener.PipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;

import java.util.Collection;

/**
 * Inventory incremental job item context.
 */
public interface InventoryIncrementalJobItemContext extends PipelineJobItemContext, PipelineJobProgressListener {
    
    @Override
    InventoryIncrementalProcessContext getJobProcessContext();
    
    /**
     * Get inventory tasks.
     *
     * @return inventory tasks
     */
    Collection<InventoryTask> getInventoryTasks();
    
    /**
     * Get incremental tasks.
     *
     * @return incremental tasks
     */
    Collection<IncrementalTask> getIncrementalTasks();
    
    /**
     * Get init progress.
     *
     * @return init progress
     */
    InventoryIncrementalJobItemProgress getInitProgress();
    
    /**
     * Get source meta data loader.
     *
     * @return source meta data loader
     */
    PipelineTableMetaDataLoader getSourceMetaDataLoader();
    
    /**
     * Get data source manager.
     *
     * @return data source manager
     */
    PipelineDataSourceManager getDataSourceManager();
    
    /**
     * Get processed record count.
     *
     * @return processed record count.
     */
    long getProcessedRecordsCount();
}
