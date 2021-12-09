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

package org.apache.shardingsphere.schedule.core.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings.JobConfigurationAPIImpl;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;

import java.util.Properties;
import java.util.function.Consumer;

/**
 * Mode schedule context, used for proxy and jdbc.
 */
@Slf4j
public final class ModeScheduleContext {
    
    private final CoordinatorRegistryCenter registryCenter;
    
    private final JobConfigurationAPI jobConfigAPI;
    
    public ModeScheduleContext(final ModeConfiguration modeConfig) {
        CoordinatorRegistryCenter registryCenter = initRegistryCenter(modeConfig);
        this.registryCenter = registryCenter;
        this.jobConfigAPI = null != registryCenter ? new JobConfigurationAPIImpl(registryCenter) : null;
    }
    
    private CoordinatorRegistryCenter initRegistryCenter(final ModeConfiguration modeConfig) {
        if (null == modeConfig) {
            return null;
        }
        String modeType = modeConfig.getType().toUpperCase();
        switch (modeType) {
            // TODO do not hard-code mode type, refactor later
            case "CLUSTER":
                return initRegistryCenterForClusterMode(modeConfig);
            case "STANDALONE":
                return null;
            case "MEMORY":
                return null;
            default:
                // TODO ModeConfiguration.type is not limited, it could be any value
                log.warn("Unknown mode type '{}'", modeType);
                return null;
        }
    }
    
    private CoordinatorRegistryCenter initRegistryCenterForClusterMode(final ModeConfiguration modeConfig) {
        String clusterType = modeConfig.getRepository().getType();
        Properties props = modeConfig.getRepository().getProps();
        // TODO do not hard-code cluster type and property key, refactor later
        if ("ZooKeeper".equalsIgnoreCase(clusterType)) {
            ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(props.getProperty("server-lists"), props.getProperty("namespace"));
            CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
            result.init();
            return result;
        }
        log.warn("Unsupported clusterType '{}'", clusterType);
        return null;
    }
    
    /**
     * Schedule with cron.
     *
     * @param jobName job name
     * @param job job implementation
     * @param cron cron expression
     */
    public void scheduleWithCron(final String jobName, final Consumer<JobParameter> job, final String cron) {
        if (null == registryCenter) {
            log.warn("registryCenter is null, ignore, jobName={}, cron={}", job, cron);
            return;
        }
        JobConfiguration jobConfig = JobConfiguration.newBuilder(jobName, 1).cron(cron).build();
        ScheduleJobBootstrap bootstrap = new ScheduleJobBootstrap(registryCenter, new ConsumerSimpleJob(job), jobConfig);
        bootstrap.schedule();
    }
    
    /**
     * Update job cron.
     *
     * @param jobName job name
     * @param cron cron expression
     */
    public void updateJobCron(final String jobName, final String cron) {
        if (null == jobConfigAPI) {
            log.warn("jobConfigAPI is null, ignore, jobName={}, cron={}", jobName, cron);
            return;
        }
        JobConfigurationPOJO jobConfig = new JobConfigurationPOJO();
        jobConfig.setJobName(jobName);
        jobConfig.setCron(cron);
        jobConfig.setShardingTotalCount(1);
        jobConfigAPI.updateJobConfiguration(jobConfig);
    }
    
    private static final class ConsumerSimpleJob implements SimpleJob {
        
        private final Consumer<JobParameter> job;
        
        ConsumerSimpleJob(final Consumer<JobParameter> job) {
            this.job = job;
        }
        
        @Override
        public void execute(final ShardingContext shardingContext) {
            job.accept(new JobParameter());
        }
    }
}
