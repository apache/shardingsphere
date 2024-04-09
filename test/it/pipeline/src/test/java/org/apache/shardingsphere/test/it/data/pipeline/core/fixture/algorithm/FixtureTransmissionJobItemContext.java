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

package org.apache.shardingsphere.test.it.data.pipeline.core.fixture.algorithm;

import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;

import java.util.Collection;

public final class FixtureTransmissionJobItemContext implements TransmissionJobItemContext {
    
    @Override
    public void onProgressUpdated(final PipelineJobProgressUpdatedParameter param) {
    }
    
    @Override
    public TransmissionProcessContext getJobProcessContext() {
        return null;
    }
    
    @Override
    public Collection<PipelineTask> getInventoryTasks() {
        return null;
    }
    
    @Override
    public Collection<PipelineTask> getIncrementalTasks() {
        return null;
    }
    
    @Override
    public TransmissionJobItemProgress getInitProgress() {
        return null;
    }
    
    @Override
    public PipelineTableMetaDataLoader getSourceMetaDataLoader() {
        return null;
    }
    
    @Override
    public PipelineSink getSink() {
        return null;
    }
    
    @Override
    public long getProcessedRecordsCount() {
        return 0;
    }
    
    @Override
    public void updateInventoryRecordsCount(final long recordsCount) {
    }
    
    @Override
    public long getInventoryRecordsCount() {
        return 0;
    }
    
    @Override
    public String getJobId() {
        return null;
    }
    
    @Override
    public int getShardingItem() {
        return 0;
    }
    
    @Override
    public String getDataSourceName() {
        return null;
    }
    
    @Override
    public JobStatus getStatus() {
        return null;
    }
    
    @Override
    public void setStatus(final JobStatus status) {
    }
    
    @Override
    public PipelineJobConfiguration getJobConfig() {
        return null;
    }
    
    @Override
    public void setStopping(final boolean stopping) {
    }
    
    @Override
    public boolean isStopping() {
        return false;
    }
}
