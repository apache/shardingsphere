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

package org.apache.shardingsphere.data.pipeline.core.job.progress.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.config.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.yaml.swapper.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;

/**
 * Pipeline process configuration utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineProcessConfigurationUtils {
    
    private static final YamlPipelineProcessConfigurationSwapper SWAPPER = new YamlPipelineProcessConfigurationSwapper();
    
    /**
     * Fill default value for pipeline process configuration.
     *
     * @param originalConfig original process configuration
     * @return process configuration
     */
    public static PipelineProcessConfiguration fillInDefaultValue(final PipelineProcessConfiguration originalConfig) {
        YamlPipelineProcessConfiguration yamlConfig = null == originalConfig ? new YamlPipelineProcessConfiguration() : SWAPPER.swapToYamlConfiguration(originalConfig);
        fillInDefaultValue(yamlConfig);
        return SWAPPER.swapToObject(yamlConfig);
    }
    
    private static void fillInDefaultValue(final YamlPipelineProcessConfiguration yamlConfig) {
        if (null == yamlConfig.getRead()) {
            yamlConfig.setRead(new YamlPipelineReadConfiguration());
        }
        if (null == yamlConfig.getWrite()) {
            yamlConfig.setWrite(new YamlPipelineWriteConfiguration());
        }
        if (null == yamlConfig.getStreamChannel()) {
            YamlAlgorithmConfiguration yamlAlgorithmConfig = new YamlAlgorithmConfiguration();
            yamlAlgorithmConfig.setType("MEMORY");
            yamlAlgorithmConfig.setProps(PropertiesBuilder.build(new Property("block-queue-size", "2000")));
            yamlConfig.setStreamChannel(yamlAlgorithmConfig);
        }
    }
}
