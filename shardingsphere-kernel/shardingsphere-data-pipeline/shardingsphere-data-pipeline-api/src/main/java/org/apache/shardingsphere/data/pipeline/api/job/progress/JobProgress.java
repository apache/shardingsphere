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

package org.apache.shardingsphere.data.pipeline.api.job.progress;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;

import java.util.Map;
import java.util.Optional;

/**
 * Job progress.
 */
@Getter
@Setter
// TODO now rename
public final class JobProgress implements PipelineJobProgress {
    
    private JobStatus status = JobStatus.RUNNING;
    
    private String sourceDatabaseType;
    
    private boolean active;
    
    private JobInventoryTaskProgress jobInventoryTask;
    
    private JobIncrementalTaskProgress jobIncrementalTask;
    
    /**
     * get incremental position.
     * @param dataSourceName dataSource
     * @return incremental position
     */
    public Optional<IngestPosition<?>> getIncrementalPosition(final String dataSourceName) {
        return jobIncrementalTask.getIncrementalPosition(dataSourceName);
    }
    
    /**
     * Get inventory position.
     *
     * @param tableName table name
     * @return inventory position
     */
    public Map<String, IngestPosition<?>> getInventoryPosition(final String tableName) {
        return jobInventoryTask.getInventoryPosition(tableName);
    }
    
    /**
     * Get data source.
     *
     * @return data source
     */
    public String getDataSource() {
        return jobIncrementalTask.getIncrementalTaskProgressMap().keySet().stream().findAny().orElse("");
    }
    
    /**
     * Get inventory finished percentage.
     *
     * @return finished percentage
     */
    public int getInventoryFinishedPercentage() {
        return jobInventoryTask.getInventoryFinishedPercentage();
    }
    
    /**
     * Get incremental latest active time milliseconds.
     *
     * @return latest active time, <code>0</code> is there is no activity
     */
    public long getIncrementalLatestActiveTimeMillis() {
        return null == jobIncrementalTask ? 0L : jobIncrementalTask.getIncrementalLatestActiveTimeMillis();
    }
}
