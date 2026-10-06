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
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsJobNodePath;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.apache.shardingsphere.schedule.spi.CoordinatorRegistryCenterProvider;
import org.quartz.CronExpression;

import java.util.Optional;
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
            try {
                ModeConfiguration modeConfig = contextManager.getComputeNodeInstanceContext().getModeConfiguration();
                Optional<CoordinatorRegistryCenterProvider> provider = TypedSPILoader.findService(CoordinatorRegistryCenterProvider.class, modeConfig.getRepository().getType());
                if (!provider.isPresent()) {
                    log.warn("Can not collect statistics because of unsupported cluster type: {}", modeConfig.getRepository().getType());
                    WORKER_INITIALIZED.set(false);
                    return;
                }
                StatisticsCollectJobWorker.contextManager = contextManager;
                ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) modeConfig.getRepository();
                registryCenter = provider.get().create(repositoryConfig, NodePathGenerator.toPath(new StatisticsJobNodePath()));
                scheduleJobBootstrap = new ScheduleJobBootstrap(registryCenter, new StatisticsCollectJob(contextManager), createJobConfiguration());
                scheduleJobBootstrap.schedule();
                new JobOperateAPIImpl(registryCenter).trigger(JOB_NAME);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                try {
                    destroy();
                    // CHECKSTYLE:OFF
                } catch (final RuntimeException cleanupEx) {
                    // CHECKSTYLE:ON
                    ex.addSuppressed(cleanupEx);
                }
                throw ex;
            }
        }
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
            try {
                Optional.ofNullable(scheduleJobBootstrap).ifPresent(ScheduleJobBootstrap::shutdown);
            } finally {
                scheduleJobBootstrap = null;
                try {
                    Optional.ofNullable(registryCenter).ifPresent(CoordinatorRegistryCenter::close);
                } finally {
                    registryCenter = null;
                    contextManager = null;
                }
            }
        }
    }
}
