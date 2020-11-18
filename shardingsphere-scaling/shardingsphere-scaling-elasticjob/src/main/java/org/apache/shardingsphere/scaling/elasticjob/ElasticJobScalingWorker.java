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

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.internal.election.LeaderService;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobAPIFactory;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;
import org.apache.shardingsphere.scaling.core.spi.ScalingWorker;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;
import org.apache.shardingsphere.scaling.elasticjob.job.ScalingElasticJob;
import org.apache.shardingsphere.scaling.elasticjob.util.ElasticJobUtils;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Elastic job scaling worker.
 */
@Slf4j
public final class ElasticJobScalingWorker implements ScalingWorker {
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    
    private static final Pattern CONFIG_PATTERN = Pattern.compile(ScalingTaskUtil.getScalingListenerPath("(\\d+)", ScalingConstant.CONFIG));
    
    private static final RegistryRepository REGISTRY_REPOSITORY = RegistryRepositoryHolder.getInstance();
    
    private final Map<String, JobBootstrapWrapper> scalingJobBootstrapMap = Maps.newHashMap();
    
    private GovernanceConfiguration governanceConfiguration;
    
    private CoordinatorRegistryCenter registryCenter;
    
    @Override
    public String getType() {
        return "ElasticJob";
    }
    
    @Override
    public void init(final GovernanceConfiguration governanceConfiguration) {
        log.info("Scaling elastic job start...");
        this.governanceConfiguration = governanceConfiguration;
        registryCenter = ElasticJobUtils.createRegistryCenter(governanceConfiguration);
        watchConfigurationRepository();
    }
    
    private void watchConfigurationRepository() {
        REGISTRY_REPOSITORY.watch(ScalingConstant.SCALING_LISTENER_PATH, event -> {
            Optional<ScalingConfiguration> scalingConfiguration = getScalingConfiguration(event);
            if (!scalingConfiguration.isPresent()) {
                return;
            }
            switch (event.getType()) {
                case ADDED:
                case UPDATED:
                    executeJob(getJobId(event.getKey()), scalingConfiguration.get());
                    break;
                case DELETED:
                    deleteJob(getJobId(event.getKey()), scalingConfiguration.get());
                    break;
                default:
                    break;
            }
        });
    }
    
    private String getJobId(final String key) {
        return key.split("/")[3];
    }
    
    private Optional<ScalingConfiguration> getScalingConfiguration(final DataChangedEvent event) {
        if (!CONFIG_PATTERN.matcher(event.getKey()).matches()) {
            return Optional.empty();
        }
        try {
            log.info("{} scaling config: {} = {}", event.getType(), event.getKey(), event.getValue());
            return Optional.of(GSON.fromJson(event.getValue(), ScalingConfiguration.class));
        } catch (JsonSyntaxException ex) {
            log.error("analyze scaling config failed.", ex);
        }
        return Optional.empty();
    }
    
    private void executeJob(final String jobId, final ScalingConfiguration scalingConfiguration) {
        JobBootstrapWrapper jobBootstrapWrapper = scalingJobBootstrapMap.get(jobId);
        if (null == jobBootstrapWrapper) {
            createJob(jobId, scalingConfiguration);
            return;
        }
        updateJob(jobId, scalingConfiguration);
    }
    
    private void createJob(final String jobId, final ScalingConfiguration scalingConfiguration) {
        if (scalingConfiguration.getJobConfiguration().isRunning()) {
            JobBootstrapWrapper jobBootstrapWrapper = new JobBootstrapWrapper(jobId, scalingConfiguration);
            jobBootstrapWrapper.getJobBootstrap().execute();
            scalingJobBootstrapMap.put(jobId, jobBootstrapWrapper);
        }
    }
    
    private void updateJob(final String jobId, final ScalingConfiguration scalingConfiguration) {
        JobBootstrapWrapper jobBootstrapWrapper = scalingJobBootstrapMap.get(jobId);
        if (jobBootstrapWrapper.isRunning() && scalingConfiguration.getJobConfiguration().isRunning()) {
            log.warn("scaling elastic job has already running, ignore current config.");
            return;
        }
        if (jobBootstrapWrapper.isRunning() == scalingConfiguration.getJobConfiguration().isRunning()) {
            return;
        }
        if (new LeaderService(registryCenter, jobId).isLeader()) {
            log.info("leader worker update config.");
            JobAPIFactory.createJobConfigurationAPI(governanceConfiguration.getRegistryCenterConfiguration().getServerLists(), governanceConfiguration.getName(), null)
                    .updateJobConfiguration(JobConfigurationPOJO.fromJobConfiguration(createJobConfiguration(jobId, scalingConfiguration)));
        }
        jobBootstrapWrapper.setRunning(scalingConfiguration.getJobConfiguration().isRunning());
        jobBootstrapWrapper.getJobBootstrap().execute();
    }
    
    private JobConfiguration createJobConfiguration(final String jobId, final ScalingConfiguration scalingConfiguration) {
        return JobConfiguration.newBuilder(jobId, scalingConfiguration.getJobConfiguration().getShardingTables().length).jobParameter(GSON.toJson(scalingConfiguration)).build();
    }
    
    private void deleteJob(final String jobId, final ScalingConfiguration scalingConfiguration) {
        scalingConfiguration.getJobConfiguration().setRunning(false);
        executeJob(jobId, scalingConfiguration);
    }
    
    @Getter
    @Setter
    private final class JobBootstrapWrapper {
        
        private final OneOffJobBootstrap jobBootstrap;
        
        private boolean running;
    
        private JobBootstrapWrapper(final String jobId, final ScalingConfiguration scalingConfiguration) {
            jobBootstrap = new OneOffJobBootstrap(registryCenter, new ScalingElasticJob(), createJobConfiguration(jobId, scalingConfiguration));
            running = scalingConfiguration.getJobConfiguration().isRunning();
        }
    }
}
