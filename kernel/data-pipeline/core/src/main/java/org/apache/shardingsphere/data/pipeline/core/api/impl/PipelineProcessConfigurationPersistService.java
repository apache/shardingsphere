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

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineMetaDataPersistService;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

/**
 * Pipeline process configuration persist service.
 */
public final class PipelineProcessConfigurationPersistService implements PipelineMetaDataPersistService<PipelineProcessConfiguration> {
    
    private static final YamlPipelineProcessConfigurationSwapper PROCESS_CONFIG_SWAPPER = new YamlPipelineProcessConfigurationSwapper();
    
    @Override
    public PipelineProcessConfiguration load(final JobType jobType) {
        String yamlText = PipelineAPIFactory.getGovernanceRepositoryAPI().getMetaDataProcessConfiguration(jobType);
        if (StringUtils.isBlank(yamlText)) {
            return null;
        }
        YamlPipelineProcessConfiguration yamlConfig = YamlEngine.unmarshal(yamlText, YamlPipelineProcessConfiguration.class, true);
        if (null == yamlConfig || yamlConfig.isAllFieldsNull()) {
            return null;
        }
        return PROCESS_CONFIG_SWAPPER.swapToObject(yamlConfig);
    }
    
    @Override
    public void persist(final JobType jobType, final PipelineProcessConfiguration processConfig) {
        String yamlText = YamlEngine.marshal(PROCESS_CONFIG_SWAPPER.swapToYamlConfiguration(processConfig));
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistMetaDataProcessConfiguration(jobType, yamlText);
    }
}
