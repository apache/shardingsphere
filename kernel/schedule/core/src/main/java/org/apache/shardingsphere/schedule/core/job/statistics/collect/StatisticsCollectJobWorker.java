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
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.operate.JobOperateAPIImpl;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.internal.settings.JobConfigurationAPIImpl;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsJobNodePath;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.quartz.CronExpression;

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
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
    private static ScheduleJobBootstrap scheduleJobBootstrap;
    
    private static ContextManager contextManager;
    
    private static CoordinatorRegistryCenter registryCenter;
    
    /**
     * Initialize job worker.
     *
     * @param contextManager context manager
     */
    public void initialize(final ContextManager contextManager) {
        if (WORKER_INITIALIZED.compareAndSet(false, true)) {
            ModeConfiguration modeConfig = contextManager.getComputeNodeInstanceContext().getModeConfiguration();
            if (!"ZooKeeper".equals(modeConfig.getRepository().getType())) {
                log.warn("Can not collect statistics because of unsupported cluster type: {}", modeConfig.getRepository().getType());
                return;
            }
            StatisticsCollectJobWorker.contextManager = contextManager;
            registryCenter = createRegistryCenter(modeConfig);
            scheduleJobBootstrap = new ScheduleJobBootstrap(registryCenter, new StatisticsCollectJob(contextManager), createJobConfiguration());
            scheduleJobBootstrap.schedule();
            new JobOperateAPIImpl(registryCenter).trigger(JOB_NAME);
        }
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
        String jobCron = contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON);
        if (!CronExpression.isValidExpression(jobCron)) {
            String defaultJobCron = TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getDefaultValue();
            log.warn("The value `{}` of `{}` is invalid, default value `{}` will be used", jobCron, TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON.getKey(), defaultJobCron);
            jobCron = defaultJobCron;
        }
        return JobConfiguration.newBuilder(JOB_NAME, 1).cron(jobCron).overwrite(true).build();
    }
    
    /**
     * Update job configuration.
     */
    public void updateJobConfiguration() {
        if (null == contextManager) {
            return;
        }
        String cron = contextManager.getMetaDataContexts().getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON);
        log.info("Changing cron of statistics collect job to `{}`", cron);
        try {
            new JobConfigurationAPIImpl(registryCenter).updateJobConfiguration(JobConfigurationPOJO.fromJobConfiguration(createJobConfiguration()));
            log.info("Changed cron of statistics collect job to `{}`", cron);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Change statistics collect job cron value error", ex);
        }
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
