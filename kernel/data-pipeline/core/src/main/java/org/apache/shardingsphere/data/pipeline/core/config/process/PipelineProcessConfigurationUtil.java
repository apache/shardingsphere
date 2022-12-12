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

package org.apache.shardingsphere.data.pipeline.core.config.process;

import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MemoryPipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineProcessConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.process.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;

import java.util.Properties;

/**
 * Pipeline process configuration util.
 */
public final class PipelineProcessConfigurationUtil {
    
    private static final YamlPipelineProcessConfigurationSwapper SWAPPER = new YamlPipelineProcessConfigurationSwapper();
    
    /**
     * Convert with default value.
     *
     * @param originalConfig original process configuration, nullable
     * @return process configuration
     */
    public static PipelineProcessConfiguration convertWithDefaultValue(final PipelineProcessConfiguration originalConfig) {
        YamlPipelineProcessConfiguration yamlConfig = null != originalConfig ? SWAPPER.swapToYamlConfiguration(originalConfig) : new YamlPipelineProcessConfiguration();
        fillInDefaultValue(yamlConfig);
        return SWAPPER.swapToObject(yamlConfig);
    }
    
    /**
     * Fill in default value.
     *
     * @param yamlConfig YAML configuration, non-null
     */
    public static void fillInDefaultValue(final YamlPipelineProcessConfiguration yamlConfig) {
        if (null == yamlConfig.getRead()) {
            yamlConfig.setRead(YamlPipelineReadConfiguration.buildWithDefaultValue());
        } else {
            yamlConfig.getRead().fillInNullFieldsWithDefaultValue();
        }
        if (null == yamlConfig.getWrite()) {
            yamlConfig.setWrite(YamlPipelineWriteConfiguration.buildWithDefaultValue());
        } else {
            yamlConfig.getWrite().fillInNullFieldsWithDefaultValue();
        }
        if (null == yamlConfig.getStreamChannel()) {
            Properties props = new Properties();
            props.put(MemoryPipelineChannelCreator.BLOCK_QUEUE_SIZE_KEY, MemoryPipelineChannelCreator.BLOCK_QUEUE_SIZE_DEFAULT_VALUE);
            yamlConfig.setStreamChannel(new YamlAlgorithmConfiguration(MemoryPipelineChannelCreator.TYPE, props));
        }
    }
}
