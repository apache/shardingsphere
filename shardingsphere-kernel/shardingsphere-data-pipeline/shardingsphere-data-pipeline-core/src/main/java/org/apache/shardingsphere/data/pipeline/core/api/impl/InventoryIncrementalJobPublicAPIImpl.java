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

package org.apache.shardingsphere.data.pipeline.core.api.impl;

import org.apache.shardingsphere.data.pipeline.api.InventoryIncrementalJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.yaml.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.config.process.PipelineProcessConfigurationUtil;
import org.apache.shardingsphere.data.pipeline.core.exception.metadata.AlterNotExistProcessConfigurationException;
import org.apache.shardingsphere.data.pipeline.core.exception.metadata.CreateExistsProcessConfigurationException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

/**
 * Inventory incremental job API implementation.
 */
public abstract class InventoryIncrementalJobPublicAPIImpl extends AbstractPipelineJobAPIImpl implements InventoryIncrementalJobPublicAPI {
    
    private static final YamlPipelineProcessConfigurationSwapper PROCESS_CONFIG_SWAPPER = new YamlPipelineProcessConfigurationSwapper();
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    @Override
    public void createProcessConfiguration(final PipelineProcessConfiguration processConfig) {
        PipelineProcessConfiguration existingProcessConfig = processConfigPersistService.load(getJobType());
        ShardingSpherePreconditions.checkState(null == existingProcessConfig, CreateExistsProcessConfigurationException::new);
        processConfigPersistService.persist(getJobType(), processConfig);
    }
    
    @Override
    public void alterProcessConfiguration(final PipelineProcessConfiguration processConfig) {
        // TODO check rateLimiter type match or not
        YamlPipelineProcessConfiguration targetYamlProcessConfig = getTargetYamlProcessConfiguration();
        targetYamlProcessConfig.copyNonNullFields(PROCESS_CONFIG_SWAPPER.swapToYamlConfiguration(processConfig));
        processConfigPersistService.persist(getJobType(), PROCESS_CONFIG_SWAPPER.swapToObject(targetYamlProcessConfig));
    }
    
    private YamlPipelineProcessConfiguration getTargetYamlProcessConfiguration() {
        PipelineProcessConfiguration existingProcessConfig = processConfigPersistService.load(getJobType());
        ShardingSpherePreconditions.checkNotNull(existingProcessConfig, AlterNotExistProcessConfigurationException::new);
        return PROCESS_CONFIG_SWAPPER.swapToYamlConfiguration(existingProcessConfig);
    }
    
    @Override
    public void dropProcessConfiguration(final String confPath) {
        String finalConfPath = confPath.trim();
        PipelineProcessConfigurationUtil.verifyConfPath(confPath);
        YamlPipelineProcessConfiguration targetYamlProcessConfig = getTargetYamlProcessConfiguration();
        PipelineProcessConfigurationUtil.setFieldsNullByConfPath(targetYamlProcessConfig, finalConfPath);
        processConfigPersistService.persist(getJobType(), PROCESS_CONFIG_SWAPPER.swapToObject(targetYamlProcessConfig));
    }
    
    @Override
    public PipelineProcessConfiguration showProcessConfiguration() {
        PipelineProcessConfiguration result = processConfigPersistService.load(getJobType());
        result = PipelineProcessConfigurationUtil.convertWithDefaultValue(result);
        return result;
    }
}
