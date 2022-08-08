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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.yaml.RuleAlteredJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rule altered job.
 */
@Slf4j
@RequiredArgsConstructor
public final class RuleAlteredJob implements SimpleJob, PipelineJob {
    
    private final GovernanceRepositoryAPI governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
    
    private volatile String jobId;
    
    private final PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
    
    // Shared by all sharding items
    private final RuleAlteredJobPreparer jobPreparer = new RuleAlteredJobPreparer();
    
    @Getter
    private final Map<Integer, PipelineTasksRunner> tasksRunnerMap = new ConcurrentHashMap<>();
    
    private volatile boolean stopping;
    
    @Setter
    private OneOffJobBootstrap oneOffJobBootstrap;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        log.info("Execute job {}-{}", shardingContext.getJobName(), shardingContext.getShardingItem());
        if (stopping) {
            log.info("stopping true, ignore");
            return;
        }
        jobId = shardingContext.getJobName();
        RuleAlteredJobConfiguration jobConfig = RuleAlteredJobConfigurationSwapper.swapToObject(shardingContext.getJobParameter());
        JobProgress initProgress = governanceRepositoryAPI.getJobProgress(shardingContext.getJobName(), shardingContext.getShardingItem());
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(jobConfig, shardingContext.getShardingItem(), initProgress, dataSourceManager, jobPreparer);
        int shardingItem = jobContext.getShardingItem();
        if (tasksRunnerMap.containsKey(shardingItem)) {
            // If the following log is output, it is possible that the elasticjob task was not closed correctly
            log.warn("schedulerMap contains shardingItem {}, ignore", shardingItem);
            return;
        }
        log.info("start RuleAlteredJobScheduler, jobId={}, shardingItem={}", jobId, shardingItem);
        RuleAlteredJobScheduler jobScheduler = new RuleAlteredJobScheduler(jobContext);
        jobScheduler.start();
        tasksRunnerMap.put(shardingItem, jobScheduler);
        PipelineJobProgressPersistService.addJobProgressPersistContext(jobId, shardingItem);
    }
    
    @Override
    public Optional<PipelineTasksRunner> getTasksRunner(final int shardingItem) {
        return Optional.ofNullable(tasksRunnerMap.get(shardingItem));
    }
    
    /**
     * Stop job.
     */
    public void stop() {
        stopping = true;
        dataSourceManager.close();
        if (null != oneOffJobBootstrap) {
            oneOffJobBootstrap.shutdown();
        }
        if (null == jobId) {
            log.info("stop, jobId is null, ignore");
            return;
        }
        log.info("stop job scheduler, jobId={}", jobId);
        for (PipelineTasksRunner each : tasksRunnerMap.values()) {
            each.stop();
        }
        tasksRunnerMap.clear();
        PipelineJobProgressPersistService.removeJobProgressPersistContext(jobId);
    }
}
