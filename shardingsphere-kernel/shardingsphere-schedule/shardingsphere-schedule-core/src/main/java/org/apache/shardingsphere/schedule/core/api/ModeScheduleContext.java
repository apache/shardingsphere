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
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Mode schedule context, used for proxy and jdbc.
 */
@Slf4j
public final class ModeScheduleContext {
    
    private static final Map<String, ScheduleJobBootstrap> SCHEDULE_JOB_BOOTSTRAP_MAP = new HashMap<>(16, 1);
    
    private final ModeConfiguration modeConfig;
    
    private final LazyInitializer<CoordinatorRegistryCenter> registryCenterLazyInitializer = new LazyInitializer<CoordinatorRegistryCenter>() {
        
        @Override
        protected CoordinatorRegistryCenter initialize() {
            return initRegistryCenter(modeConfig);
        }
    };
    
    public ModeScheduleContext(final ModeConfiguration modeConfig) {
        this.modeConfig = modeConfig;
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
            // TODO add timeout settings; CoordinatorRegistryCenterInitializer could not be used for now since dependency;
            CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
            result.init();
            return result;
        }
        log.warn("Unsupported clusterType '{}'", clusterType);
        return null;
    }
    
    @SneakyThrows(ConcurrentException.class)
    private CoordinatorRegistryCenter getRegistryCenter() {
        return registryCenterLazyInitializer.get();
    }
    
    /**
     * Start cron job.
     *
     * @param job cron job
     */
    @SuppressWarnings("unchecked")
    public void startCronJob(final CronJob job) {
        CoordinatorRegistryCenter registryCenter = getRegistryCenter();
        if (null == registryCenter) {
            log.warn("registryCenter is null, ignore, jobName={}, cron={}", job.getJobName(), job.getCron());
            return;
        }
        if (null != SCHEDULE_JOB_BOOTSTRAP_MAP.get(job.getJobName())) {
            SCHEDULE_JOB_BOOTSTRAP_MAP.get(job.getJobName()).shutdown();
        }
        JobConfiguration jobConfig = JobConfiguration.newBuilder(job.getJobName(), 1).cron(job.getCron()).overwrite(true).build();
        ScheduleJobBootstrap bootstrap = new ScheduleJobBootstrap(registryCenter, new ConsumerSimpleJob(job.getJob()), jobConfig);
        SCHEDULE_JOB_BOOTSTRAP_MAP.put(job.getJobName(), bootstrap);
        SCHEDULE_JOB_BOOTSTRAP_MAP.get(job.getJobName()).schedule();
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
