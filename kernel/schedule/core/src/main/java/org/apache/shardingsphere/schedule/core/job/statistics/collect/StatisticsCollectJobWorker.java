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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Statistics collect job worker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatisticsCollectJobWorker {
    
    private static final String JOB_NAME = "statistics-collect";
    
    private static final String CRON_EXPRESSION = "*/30 * * * * ?";
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
    /**
     * Initialize job worker.
     *
     * @param contextManager context manager
     */
    public static void initialize(final ContextManager contextManager) {
        if (WORKER_INITIALIZED.compareAndSet(false, true)) {
            start(contextManager);
        }
    }
    
    private static void start(final ContextManager contextManager) {
        ModeConfiguration modeConfig = contextManager.getInstanceContext().getModeConfiguration();
        if ("ZooKeeper".equals(modeConfig.getRepository().getType())) {
            ScheduleJobBootstrap bootstrap = new ScheduleJobBootstrap(createRegistryCenter(modeConfig), new StatisticsCollectJob(contextManager), createJobConfiguration());
            bootstrap.schedule();
            return;
        }
        throw new IllegalArgumentException("Unsupported cluster type: " + modeConfig.getRepository().getType());
    }
    
    private static CoordinatorRegistryCenter createRegistryCenter(final ModeConfiguration modeConfig) {
        ClusterPersistRepositoryConfiguration repositoryConfig = (ClusterPersistRepositoryConfiguration) modeConfig.getRepository();
        String namespace = String.join("/", repositoryConfig.getNamespace(), ShardingSphereDataNode.getJobPath());
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(new ZookeeperConfiguration(repositoryConfig.getServerLists(), namespace));
        result.init();
        return result;
    }
    
    private static JobConfiguration createJobConfiguration() {
        return JobConfiguration.newBuilder(JOB_NAME, 1).cron(CRON_EXPRESSION).overwrite(true).build();
    }
}
