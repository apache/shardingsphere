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

package org.apache.shardingsphere.data.pipeline.core.metadata;

import com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.swapper.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

/**
 * Pipeline process configuration persist service.
 */
public final class PipelineProcessConfigurationPersistService implements PipelineMetaDataPersistService<PipelineProcessConfiguration> {
    
    private final YamlPipelineProcessConfigurationSwapper swapper = new YamlPipelineProcessConfigurationSwapper();
    
    @Override
    public PipelineProcessConfiguration load(final PipelineContextKey contextKey, final String jobType) {
        String yamlText = PipelineAPIFactory.getPipelineGovernanceFacade(contextKey).getMetaDataFacade().getProcessConfiguration().load(jobType);
        if (Strings.isNullOrEmpty(yamlText)) {
            return null;
        }
        YamlPipelineProcessConfiguration yamlConfig = YamlEngine.unmarshal(yamlText, YamlPipelineProcessConfiguration.class, true);
        return swapper.swapToObject(yamlConfig);
    }
    
    @Override
    public void persist(final PipelineContextKey contextKey, final String jobType, final PipelineProcessConfiguration processConfig) {
        String yamlText = YamlEngine.marshal(swapper.swapToYamlConfiguration(processConfig));
        PipelineAPIFactory.getPipelineGovernanceFacade(contextKey).getMetaDataFacade().getProcessConfiguration().persist(jobType, yamlText);
    }
}
