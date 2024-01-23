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

package org.apache.shardingsphere.data.pipeline.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNodeWatcher;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pipeline context manager lifecycle listener.
 */
@Slf4j
public final class PipelineContextManagerLifecycleListener implements ContextManagerLifecycleListener {
    
    @Override
    public void onInitialized(final String databaseName, final ContextManager contextManager) {
        ModeConfiguration modeConfig = contextManager.getInstanceContext().getModeConfiguration();
        if (!contextManager.getInstanceContext().isCluster()) {
            log.info("mode type is not Cluster, mode type='{}', ignore", modeConfig.getType());
            return;
        }
        // TODO When StandalonePersistRepository is equivalent with ClusterPersistRepository, use STANDALONE mode in pipeline IT and remove this check.
        if (DefaultDatabase.LOGIC_NAME.equals(databaseName)) {
            return;
        }
        PipelineContextKey contextKey = new PipelineContextKey(databaseName, contextManager.getInstanceContext().getInstance().getMetaData().getType());
        PipelineContextManager.putContext(contextKey, new PipelineContext(modeConfig, contextManager));
        PipelineMetaDataNodeWatcher.getInstance(contextKey);
        ElasticJobServiceLoader.registerTypedService(ElasticJobListener.class);
        try {
            dispatchEnablePipelineJobStartEvent(contextKey);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("Dispatch enable pipeline job start event failed", ex);
        }
    }
    
    private void dispatchEnablePipelineJobStartEvent(final PipelineContextKey contextKey) {
        JobConfigurationAPI jobConfigAPI = PipelineAPIFactory.getJobConfigurationAPI(contextKey);
        List<JobBriefInfo> allJobsBriefInfo = PipelineAPIFactory.getJobStatisticsAPI(contextKey).getAllJobsBriefInfo()
                .stream().filter(each -> !each.getJobName().startsWith("_")).collect(Collectors.toList());
        for (JobBriefInfo each : allJobsBriefInfo) {
            PipelineJobType jobType = PipelineJobIdUtils.parseJobType(each.getJobName());
            PipelineJobInfo jobInfo = jobType.getJobInfo(each.getJobName());
            if (null == jobInfo || null == jobInfo.getJobMetaData()) {
                continue;
            }
            if (!jobInfo.getJobMetaData().isActive()) {
                return;
            }
            JobConfigurationPOJO jobConfig = jobConfigAPI.getJobConfiguration(each.getJobName());
            jobConfigAPI.updateJobConfiguration(jobConfig);
        }
    }
    
    @Override
    public void onDestroyed(final String databaseName, final InstanceType instanceType) {
        PipelineContextManager.removeContext(new PipelineContextKey(databaseName, instanceType));
    }
}
