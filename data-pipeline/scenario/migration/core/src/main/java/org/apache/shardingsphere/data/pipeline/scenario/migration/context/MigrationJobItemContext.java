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

package org.apache.shardingsphere.data.pipeline.scenario.migration.context;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.type.PipelineDataSourceSink;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobUpdateProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Migration job item context.
 */
@Getter
@Setter
public final class MigrationJobItemContext implements TransmissionJobItemContext {
    
    private final String jobId;
    
    private final int shardingItem;
    
    private final String dataSourceName;
    
    private volatile boolean stopping;
    
    private volatile JobStatus status = JobStatus.RUNNING;
    
    private final TransmissionJobItemProgress initProgress;
    
    private final MigrationTaskConfiguration taskConfig;
    
    private final Collection<PipelineTask> inventoryTasks = new LinkedList<>();
    
    private final Collection<PipelineTask> incrementalTasks = new LinkedList<>();
    
    private final AtomicLong processedRecordsCount = new AtomicLong(0L);
    
    private final AtomicLong inventoryRecordsCount = new AtomicLong(0L);
    
    private final MigrationJobConfiguration jobConfig;
    
    private final TransmissionProcessContext jobProcessContext;
    
    private final PipelineDataSourceManager dataSourceManager;
    
    private final LazyInitializer<PipelineDataSource> sourceDataSourceLazyInitializer = new LazyInitializer<PipelineDataSource>() {
        
        @Override
        protected PipelineDataSource initialize() {
            return dataSourceManager.getDataSource(taskConfig.getDumperContext().getCommonContext().getDataSourceConfig());
        }
    };
    
    private final LazyInitializer<PipelineTableMetaDataLoader> sourceMetaDataLoaderLazyInitializer = new LazyInitializer<PipelineTableMetaDataLoader>() {
        
        @Override
        protected PipelineTableMetaDataLoader initialize() throws ConcurrentException {
            return new StandardPipelineTableMetaDataLoader(sourceDataSourceLazyInitializer.get());
        }
    };
    
    public MigrationJobItemContext(final MigrationJobConfiguration jobConfig, final int shardingItem, final TransmissionJobItemProgress initProgress,
                                   final TransmissionProcessContext jobProcessContext, final MigrationTaskConfiguration taskConfig, final PipelineDataSourceManager dataSourceManager) {
        this.jobConfig = jobConfig;
        jobId = jobConfig.getJobId();
        this.shardingItem = shardingItem;
        dataSourceName = taskConfig.getDataSourceName();
        this.initProgress = initProgress;
        if (null != initProgress) {
            processedRecordsCount.set(initProgress.getProcessedRecordsCount());
            inventoryRecordsCount.set(initProgress.getInventoryRecordsCount());
        }
        this.jobProcessContext = jobProcessContext;
        this.taskConfig = taskConfig;
        this.dataSourceManager = dataSourceManager;
    }
    
    /**
     * Get source data source.
     *
     * @return source data source
     */
    @SneakyThrows(ConcurrentException.class)
    public PipelineDataSource getSourceDataSource() {
        return sourceDataSourceLazyInitializer.get();
    }
    
    @Override
    @SneakyThrows(ConcurrentException.class)
    public PipelineTableMetaDataLoader getSourceMetaDataLoader() {
        return sourceMetaDataLoaderLazyInitializer.get();
    }
    
    @Override
    public PipelineSink getSink() {
        return new PipelineDataSourceSink(taskConfig.getImporterConfig(), dataSourceManager);
    }
    
    /**
     * Is source and target database the same or not.
     *
     * @return true if source and target database the same, otherwise false
     */
    public boolean isSourceTargetDatabaseTheSame() {
        return jobConfig.getSourceDatabaseType() == jobConfig.getTargetDatabaseType();
    }
    
    @Override
    public void onProgressUpdated(final PipelineJobUpdateProgress updateProgress) {
        processedRecordsCount.addAndGet(updateProgress.getProcessedRecordsCount());
        PipelineJobProgressPersistService.notifyPersist(jobId, shardingItem);
    }
    
    @Override
    public long getProcessedRecordsCount() {
        return processedRecordsCount.get();
    }
    
    @Override
    public void updateInventoryRecordsCount(final long recordsCount) {
        inventoryRecordsCount.addAndGet(recordsCount);
    }
    
    @Override
    public long getInventoryRecordsCount() {
        return inventoryRecordsCount.get();
    }
}
