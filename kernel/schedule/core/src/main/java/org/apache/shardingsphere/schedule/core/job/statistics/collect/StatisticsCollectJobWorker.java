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

package org.apache.shardingsphere.schedule.core.job.statistics.collect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsJobNodePath;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Statistics collect job worker.
 */
@RequiredArgsConstructor
@Slf4j
public final class StatisticsCollectJobWorker {
    
    private static final String JOB_NAME = "statistics-collect";
    
    private static final String CRON_EXPRESSION = "*/30 * * * * ?";
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
    private static ScheduleJobBootstrap scheduleJobBootstrap;
    
    private final ContextManager contextManager;
    
    /**
     * Initialize job worker.
     */
    public void initialize() {
        if (!WORKER_INITIALIZED.compareAndSet(false, true)) {
            return;
        }
        ModeConfiguration modeConfig = contextManager.getComputeNodeInstanceContext().getModeConfiguration();
        if (!"ZooKeeper".equals(modeConfig.getRepository().getType())) {
            log.warn("Can not collect statistics because of unsupported cluster type: {}", modeConfig.getRepository().getType());
            return;
        }
        scheduleJobBootstrap = new ScheduleJobBootstrap(createRegistryCenter(modeConfig), new StatisticsCollectJob(contextManager), createJobConfiguration());
        scheduleJobBootstrap.schedule();
    }
    
    private CoordinatorRegistryCenter createRegistryCenter(final ModeConfiguration modeConfig) {
        ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) modeConfig.getRepository();
        String namespace = repositoryConfig.getNamespace() + NodePathGenerator.toPath(new StatisticsJobNodePath());
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(getZookeeperConfiguration(repositoryConfig, namespace));
        result.init();
        return result;
    }
    
    private ZookeeperConfiguration getZookeeperConfiguration(final ClusterPersistRepositoryConfiguration repositoryConfig, final String namespace) {
        // TODO Merge registry center code in ElasticJob and ShardingSphere mode; Use SPI to load impl
        ZookeeperConfiguration result = new ZookeeperConfiguration(repositoryConfig.getServerLists(), namespace);
        Properties props = repositoryConfig.getProps();
        int retryIntervalMilliseconds = props.containsKey("retryIntervalMilliseconds") ? (int) props.get("retryIntervalMilliseconds") : 500;
        int maxRetries = props.containsKey("maxRetries") ? (int) props.get("maxRetries") : 3;
        result.setBaseSleepTimeMilliseconds(retryIntervalMilliseconds);
        result.setMaxRetries(maxRetries);
        result.setMaxSleepTimeMilliseconds(retryIntervalMilliseconds * maxRetries);
        int timeToLiveSeconds = props.containsKey("timeToLiveSeconds") ? (int) props.get("timeToLiveSeconds") : 60;
        if (0 != timeToLiveSeconds) {
            result.setSessionTimeoutMilliseconds(timeToLiveSeconds * 1000);
        }
        int operationTimeoutMilliseconds = props.containsKey("operationTimeoutMilliseconds") ? (int) props.get("operationTimeoutMilliseconds") : 500;
        if (0 != operationTimeoutMilliseconds) {
            result.setConnectionTimeoutMilliseconds(operationTimeoutMilliseconds);
        }
        result.setDigest(props.getProperty("digest"));
        return result;
    }
    
    private JobConfiguration createJobConfiguration() {
        return JobConfiguration.newBuilder(JOB_NAME, 1).cron(CRON_EXPRESSION).overwrite(true).build();
    }
    
    /**
     * Destroy job worker.
     */
    public void destroy() {
        if (WORKER_INITIALIZED.compareAndSet(true, false)) {
            Optional.ofNullable(scheduleJobBootstrap).ifPresent(ScheduleJobBootstrap::shutdown);
            scheduleJobBootstrap = null;
        }
    }
}
