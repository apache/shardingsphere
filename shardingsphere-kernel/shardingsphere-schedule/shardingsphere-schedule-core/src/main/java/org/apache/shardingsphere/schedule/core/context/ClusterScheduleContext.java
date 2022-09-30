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

package org.apache.shardingsphere.schedule.core.context;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.schedule.CronJob;
import org.apache.shardingsphere.infra.schedule.ScheduleContext;
import org.apache.shardingsphere.schedule.core.model.JobParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Cluster schedule context.
 */
@RequiredArgsConstructor
@Slf4j
public final class ClusterScheduleContext implements ScheduleContext {
    
    private static final Map<String, ScheduleJobBootstrap> SCHEDULE_JOB_BOOTSTRAP_MAP = new HashMap<>();
    
    private final String serverList;
    
    private final String namespace;
    
    private final LazyInitializer<CoordinatorRegistryCenter> registryCenterLazyInitializer = new LazyInitializer<CoordinatorRegistryCenter>() {
        
        @Override
        protected CoordinatorRegistryCenter initialize() {
            return initRegisterCenter();
        }
    };
    
    /**
     * Start schedule.
     *
     * @param job cron job
     */
    @SuppressWarnings("unchecked")
    public void startSchedule(final CronJob job) {
        CoordinatorRegistryCenter registryCenter = getRegistryCenter();
        Preconditions.checkNotNull(registryCenter, "Coordinator registry center failed to initialize");
        if (null != SCHEDULE_JOB_BOOTSTRAP_MAP.get(job.getJobName())) {
            SCHEDULE_JOB_BOOTSTRAP_MAP.get(job.getJobName()).shutdown();
        }
        JobConfiguration jobConfig = JobConfiguration.newBuilder(job.getJobName(), 1).cron(job.getCron()).overwrite(true).build();
        ScheduleJobBootstrap bootstrap = new ScheduleJobBootstrap(registryCenter, new ConsumerSimpleJob(job.getJob()), jobConfig);
        SCHEDULE_JOB_BOOTSTRAP_MAP.put(job.getJobName(), bootstrap);
        SCHEDULE_JOB_BOOTSTRAP_MAP.get(job.getJobName()).schedule();
    }
    
    @Override
    public void closeSchedule(final String jobName) {
        Optional.ofNullable(SCHEDULE_JOB_BOOTSTRAP_MAP.remove(jobName)).ifPresent(ScheduleJobBootstrap::shutdown);
    }
    
    @SneakyThrows(ConcurrentException.class)
    private CoordinatorRegistryCenter getRegistryCenter() {
        return registryCenterLazyInitializer.get();
    }
    
    private CoordinatorRegistryCenter initRegisterCenter() {
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(new ZookeeperConfiguration(serverList, namespace));
        result.init();
        return result;
    }
    
    @RequiredArgsConstructor
    private static final class ConsumerSimpleJob implements SimpleJob {
        
        private final Consumer<JobParameter> job;
        
        @Override
        public void execute(final ShardingContext shardingContext) {
            job.accept(new JobParameter());
        }
    }
}
