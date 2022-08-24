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

package org.apache.shardingsphere.data.pipeline.core.util;

import com.google.common.base.Splitter;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MemoryPipelineChannelCreator;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineProcessConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.data.pipeline.YamlPipelineProcessConfigurationSwapper;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Pipeline process configuration utils.
 */
public final class PipelineProcessConfigurationUtils {
    
    private static final YamlPipelineProcessConfigurationSwapper SWAPPER = new YamlPipelineProcessConfigurationSwapper();
    
    private static final String CONF_PATH_REGEX = "^/|(/[a-zA-Z_]+)+$";
    
    private static final Pattern CONF_PATH_PATTERN = Pattern.compile(CONF_PATH_REGEX);
    
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
    
    /**
     * Verify configuration path valid or not.
     *
     * @param confPath configuration path
     * @throws IllegalArgumentException if path doesn't match pattern
     */
    public static void verifyConfPath(final String confPath) {
        if (!CONF_PATH_PATTERN.matcher(confPath).matches()) {
            throw new IllegalArgumentException("Invalid confPath, it doesn't match pattern: " + CONF_PATH_REGEX);
        }
    }
    
    /**
     * Set fields to null by configuration path.
     *
     * @param targetYamlProcessConfig target YAML process configuration
     * @param confPath configuration path, e.g. <code>/</code>, <code>/READ</code>, <code>/READ/RATE_LIMITER</code>
     */
    public static void setFieldsNullByConfPath(final YamlPipelineProcessConfiguration targetYamlProcessConfig, final String confPath) {
        List<String> confPathNodes = Splitter.on('/').splitToList(confPath);
        if (2 == confPathNodes.size()) {
            String levelOne = confPathNodes.get(1).toUpperCase();
            if (levelOne.isEmpty()) {
                targetYamlProcessConfig.setAllFieldsNull();
            } else {
                targetYamlProcessConfig.setFieldNull(levelOne);
            }
        } else if (3 == confPathNodes.size()) {
            String levelOne = confPathNodes.get(1).toUpperCase();
            String levelTwo = confPathNodes.get(2).toUpperCase();
            if ("READ".equals(levelOne) && null != targetYamlProcessConfig.getRead()) {
                targetYamlProcessConfig.getRead().setFieldNull(levelTwo);
            } else if ("WRITE".equals(levelOne) && null != targetYamlProcessConfig.getWrite()) {
                targetYamlProcessConfig.getWrite().setFieldNull(levelTwo);
            }
        }
    }
}
