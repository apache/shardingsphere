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

package org.apache.shardingsphere.scaling.core.job.task.incremental;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.distribution.DistributionChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.config.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.executor.dumper.DumperFactory;
import org.apache.shardingsphere.scaling.core.executor.engine.ExecuteCallback;
import org.apache.shardingsphere.scaling.core.executor.importer.Importer;
import org.apache.shardingsphere.scaling.core.executor.importer.ImporterFactory;
import org.apache.shardingsphere.scaling.core.executor.importer.ImporterListener;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.schedule.core.executor.AbstractLifecycleExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Incremental task.
 */
@Slf4j
@ToString(exclude = {"dataSourceManager", "dumper", "progress"})
public final class IncrementalTask extends AbstractLifecycleExecutor implements ScalingTask {
    
    @Getter
    private final String taskId;
    
    private final int concurrency;
    
    private final DumperConfiguration dumperConfig;
    
    private final ImporterConfiguration importerConfig;
    
    private final DataSourceManager dataSourceManager;
    
    private Dumper dumper;
    
    @Getter
    private final IncrementalTaskProgress progress;
    
    public IncrementalTask(final int concurrency, final DumperConfiguration dumperConfig, final ImporterConfiguration importerConfig) {
        this.concurrency = concurrency;
        this.dumperConfig = dumperConfig;
        this.importerConfig = importerConfig;
        dataSourceManager = new DataSourceManager();
        taskId = dumperConfig.getDataSourceName();
        progress = new IncrementalTaskProgress();
        progress.setPosition(dumperConfig.getPosition());
    }
    
    @Override
    public void start() {
        progress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
        dumper = DumperFactory.newInstanceLogDumper(dumperConfig, progress.getPosition());
        Collection<Importer> importers = instanceImporters();
        instanceChannel(importers);
        Future<?> future = ScalingContext.getInstance().getIncrementalDumperExecuteEngine().submitAll(importers, getExecuteCallback());
        dumper.start();
        waitForResult(future);
        dataSourceManager.close();
    }
    
    private List<Importer> instanceImporters() {
        List<Importer> result = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            result.add(ImporterFactory.newInstance(importerConfig, dataSourceManager));
        }
        return result;
    }
    
    private void instanceChannel(final Collection<Importer> importers) {
        DistributionChannel channel = new DistributionChannel(importers.size(), records -> {
            Record lastHandledRecord = records.get(records.size() - 1);
            if (!(lastHandledRecord.getPosition() instanceof PlaceholderPosition)) {
                progress.setPosition(lastHandledRecord.getPosition());
                progress.getIncrementalTaskDelay().setLastEventTimestamps(lastHandledRecord.getCommitTime());
            }
        });
        dumper.setChannel(channel);
        ImporterListener importerListener = records -> progress.getIncrementalTaskDelay().setLatestActiveTimeMillis(System.currentTimeMillis());
        for (Importer each : importers) {
            each.setChannel(channel);
            each.setImporterListener(importerListener);
        }
    }
    
    private ExecuteCallback getExecuteCallback() {
        return new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("get an error when migrating the increment data", throwable);
                dumper.stop();
            }
        };
    }
    
    private void waitForResult(final Future<?> future) {
        try {
            future.get();
        } catch (final InterruptedException ignored) {
        } catch (final ExecutionException ex) {
            throw new ScalingTaskExecuteException(String.format("Task %s execute failed ", taskId), ex.getCause());
        }
    }
    
    @Override
    public void stop() {
        if (null != dumper) {
            dumper.stop();
            dumper = null;
        }
    }
}
