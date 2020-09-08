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

package org.apache.shardingsphere.scaling.elasticjob;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobAPIFactory;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.zookeeper.CuratorZookeeperRepository;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.spi.ElasticJobEntry;
import org.apache.shardingsphere.scaling.elasticjob.job.ScalingElasticJob;

import java.util.Optional;

/**
 * Scaling elastic job entry.
 */
@Slf4j
public final class ScalingElasticJobEntry implements ElasticJobEntry {
    
    private static final String SCALING_JOB_NAME = "ScalingJob";
    
    private static final String SCALING_JOB_CONFIG = "/__scalingjob_config";
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    
    private final CuratorZookeeperRepository curatorZookeeperRepository = new CuratorZookeeperRepository();
    
    private OneOffJobBootstrap scalingJobBootstrap;
    
    private boolean running;
    
    private String namespace;
    
    private OrchestrationCenterConfiguration registryCenter;
    
    @Override
    public void init(final String namespace, final OrchestrationCenterConfiguration registryCenter) {
        log.info("Scaling elastic job start...");
        this.namespace = namespace;
        this.registryCenter = registryCenter;
        initConfigurationRepository();
        watchConfigurationRepository();
    }
    
    private void initConfigurationRepository() {
        scalingJobBootstrap = new OneOffJobBootstrap(createRegistryCenter(), new ScalingElasticJob(), createJobConfiguration());
        curatorZookeeperRepository.init(namespace, registryCenter);
    }
    
    private CoordinatorRegistryCenter createRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(registryCenter.getServerLists(), namespace);
        zkConfig.setMaxSleepTimeMilliseconds(getProperty("max.sleep.time.milliseconds", zkConfig.getMaxSleepTimeMilliseconds()));
        zkConfig.setBaseSleepTimeMilliseconds(getProperty("base.sleep.time.milliseconds", zkConfig.getBaseSleepTimeMilliseconds()));
        zkConfig.setConnectionTimeoutMilliseconds(getProperty("connection.timeout.milliseconds", zkConfig.getConnectionTimeoutMilliseconds()));
        zkConfig.setSessionTimeoutMilliseconds(getProperty("session.timeout.milliseconds", zkConfig.getSessionTimeoutMilliseconds()));
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zkConfig);
        regCenter.init();
        return regCenter;
    }
    
    private int getProperty(final String key, final int defaultValue) {
        if (Strings.isNullOrEmpty(registryCenter.getProps().getProperty(key))) {
            return defaultValue;
        }
        return Integer.parseInt(registryCenter.getProps().getProperty(key));
    }
    
    private JobConfiguration createJobConfiguration() {
        return createJobConfiguration(1, null);
    }
    
    private JobConfiguration createJobConfiguration(final int shardingTotalCount, final String jobParameter) {
        return JobConfiguration.newBuilder(SCALING_JOB_NAME, shardingTotalCount).jobParameter(jobParameter).build();
    }
    
    private void watchConfigurationRepository() {
        curatorZookeeperRepository.watch(SCALING_JOB_CONFIG, event -> {
            Optional<ScalingConfiguration> scalingConfiguration = getScalingConfiguration(event);
            if (!scalingConfiguration.isPresent()) {
                return;
            }
            switch (event.getChangedType()) {
                case ADDED:
                case UPDATED:
                    executeJob(scalingConfiguration.get());
                    break;
                case DELETED:
                    deleteJob(scalingConfiguration.get());
                    break;
                default:
                    break;
            }
        });
    }
    
    private Optional<ScalingConfiguration> getScalingConfiguration(final DataChangedEvent event) {
        try {
            log.info("{} scaling config: {}", event.getChangedType(), event.getValue());
            return Optional.of(GSON.fromJson(event.getValue(), ScalingConfiguration.class));
        } catch (JsonSyntaxException ex) {
            log.error("analyze scaling config failed.", ex);
        }
        return Optional.empty();
    }
    
    private void executeJob(final ScalingConfiguration scalingConfiguration) {
        if (running && scalingConfiguration.getJobConfiguration().isRunning()) {
            log.warn("scaling elastic job has already running, ignore current config.");
            return;
        }
        if (running == scalingConfiguration.getJobConfiguration().isRunning()) {
            return;
        }
        if (new LeaderService(createRegistryCenter(), SCALING_JOB_NAME).isLeader()) {
            log.info("leader worker update config.");
            updateJobConfiguration(scalingConfiguration);
        }
        scalingJobBootstrap.execute();
        running = scalingConfiguration.getJobConfiguration().isRunning();
    }
    
    private void deleteJob(final ScalingConfiguration scalingConfiguration) {
        scalingConfiguration.getJobConfiguration().setRunning(false);
        executeJob(scalingConfiguration);
    }
    
    private void updateJobConfiguration(final ScalingConfiguration scalingConfiguration) {
        JobConfigurationAPI jobConfigurationAPI = JobAPIFactory.createJobConfigurationAPI(registryCenter.getServerLists(), namespace, null);
        jobConfigurationAPI.updateJobConfiguration(
                JobConfigurationPOJO.fromJobConfiguration(createJobConfiguration(scalingConfiguration.getJobConfiguration().getShardingTables().length, GSON.toJson(scalingConfiguration))));
    }
}
