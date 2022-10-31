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

package org.apache.shardingsphere.data.pipeline.yaml.process;

import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;

/**
 * YAML pipeline process configuration swapper.
 */
public final class YamlPipelineProcessConfigurationSwapper implements YamlConfigurationSwapper<YamlPipelineProcessConfiguration, PipelineProcessConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final YamlPipelineReadConfigurationSwapper readConfigSwapper = new YamlPipelineReadConfigurationSwapper();
    
    private final YamlPipelineWriteConfigurationSwapper writeConfigSwapper = new YamlPipelineWriteConfigurationSwapper();
    
    @Override
    public YamlPipelineProcessConfiguration swapToYamlConfiguration(final PipelineProcessConfiguration data) {
        if (null == data) {
            return null;
        }
        YamlPipelineProcessConfiguration result = new YamlPipelineProcessConfiguration();
        result.setRead(readConfigSwapper.swapToYamlConfiguration(data.getRead()));
        result.setWrite(writeConfigSwapper.swapToYamlConfiguration(data.getWrite()));
        result.setStreamChannel(algorithmSwapper.swapToYamlConfiguration(data.getStreamChannel()));
        return result;
    }
    
    @Override
    public PipelineProcessConfiguration swapToObject(final YamlPipelineProcessConfiguration yamlConfig) {
        return null == yamlConfig
                ? null
                : new PipelineProcessConfiguration(
                        readConfigSwapper.swapToObject(yamlConfig.getRead()), writeConfigSwapper.swapToObject(yamlConfig.getWrite()), algorithmSwapper.swapToObject(yamlConfig.getStreamChannel()));
    }
}
