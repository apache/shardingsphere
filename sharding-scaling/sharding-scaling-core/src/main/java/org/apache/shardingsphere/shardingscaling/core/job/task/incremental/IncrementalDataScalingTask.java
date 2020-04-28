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

package org.apache.shardingsphere.shardingscaling.core.job.task.incremental;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.job.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.AbstractShardingScalingExecutor;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.DistributionChannel;
import org.apache.shardingsphere.shardingscaling.core.job.position.LogPosition;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.dumper.Dumper;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.dumper.DumperFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.importer.Importer;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.importer.ImporterFactory;
import org.apache.shardingsphere.shardingscaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.underlying.common.database.metadata.DataSourceMetaData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Incremental data execute task.
 */
@Slf4j
public final class IncrementalDataScalingTask extends AbstractShardingScalingExecutor implements ScalingTask {
    
    private final SyncConfiguration syncConfiguration;
    
    private final DataSourceManager dataSourceManager;
    
    private final String syncTaskId;
    
    private LogPosition logPosition;
    
    private Dumper dumper;
    
    private long delayMillisecond;
    
    public IncrementalDataScalingTask(final SyncConfiguration syncConfiguration, final LogPosition logPosition) {
        this.syncConfiguration = syncConfiguration;
        this.dataSourceManager = new DataSourceManager();
        this.logPosition = logPosition;
        DataSourceMetaData dataSourceMetaData = syncConfiguration.getDumperConfiguration().getDataSourceConfiguration().getDataSourceMetaData();
        syncTaskId = String.format("incremental-%s", null != dataSourceMetaData.getCatalog() ? dataSourceMetaData.getCatalog() : dataSourceMetaData.getSchema());
    }
    
    @Override
    public void start() {
        syncConfiguration.getDumperConfiguration().setTableNameMap(syncConfiguration.getTableNameMap());
        dumper = DumperFactory.newInstanceLogDumper(syncConfiguration.getDumperConfiguration(), logPosition);
        Collection<Importer> importers = instanceImporters();
        instanceChannel(importers);
        Future future = ScalingContext.getInstance().getTaskExecuteEngine().submitAll(importers, new ExecuteCallback() {
        
            @Override
            public void onSuccess() {
            }
        
            @Override
            public void onFailure(final Throwable throwable) {
                dumper.stop();
            }
        });
        dumper.start();
        waitForResult(future);
        dataSourceManager.close();
    }
    
    private List<Importer> instanceImporters() {
        List<Importer> result = new ArrayList<>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            result.add(ImporterFactory.newInstance(syncConfiguration.getImporterConfiguration(), dataSourceManager));
        }
        return result;
    }
    
    private void instanceChannel(final Collection<Importer> importers) {
        DistributionChannel channel = new DistributionChannel(importers.size(), records -> {
            Record lastHandledRecord = records.get(records.size() - 1);
            logPosition = lastHandledRecord.getLogPosition();
            delayMillisecond = System.currentTimeMillis() - lastHandledRecord.getCommitTime();
        });
        dumper.setChannel(channel);
        for (Importer each : importers) {
            each.setChannel(channel);
        }
    }
    
    private void waitForResult(final Future future) {
        try {
            future.get();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            throw new SyncTaskExecuteException(String.format("Task %s execute failed ", syncTaskId), e.getCause());
        }
    }
    
    @Override
    public void stop() {
        if (null != dumper) {
            dumper.stop();
            dumper = null;
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        return new IncrementalDataSyncTaskProgress(syncTaskId, delayMillisecond, logPosition);
    }
}
