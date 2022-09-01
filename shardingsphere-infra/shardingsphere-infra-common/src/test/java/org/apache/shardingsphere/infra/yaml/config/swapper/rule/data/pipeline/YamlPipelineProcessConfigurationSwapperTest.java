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

package org.apache.shardingsphere.infra.yaml.config.swapper.rule.data.pipeline;

import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineProcessConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class YamlPipelineProcessConfigurationSwapperTest {
    
    private static final YamlPipelineProcessConfigurationSwapper SWAPPER = new YamlPipelineProcessConfigurationSwapper();
    
    @Test
    public void assertSwap() {
        YamlPipelineProcessConfiguration yamlConfig = new YamlPipelineProcessConfiguration();
        Properties rateLimiterProps = new Properties();
        rateLimiterProps.setProperty("batch-size", "1000");
        rateLimiterProps.setProperty("qps", "50");
        YamlPipelineReadConfiguration yamlInputConfig = YamlPipelineReadConfiguration.buildWithDefaultValue();
        yamlConfig.setRead(yamlInputConfig);
        yamlInputConfig.setRateLimiter(new YamlAlgorithmConfiguration("INPUT", rateLimiterProps));
        YamlPipelineWriteConfiguration yamlOutputConfig = YamlPipelineWriteConfiguration.buildWithDefaultValue();
        yamlOutputConfig.setRateLimiter(new YamlAlgorithmConfiguration("OUTPUT", rateLimiterProps));
        yamlConfig.setWrite(yamlOutputConfig);
        Properties streamChannelProps = new Properties();
        streamChannelProps.setProperty("block-queue-size", "10000");
        yamlConfig.setStreamChannel(new YamlAlgorithmConfiguration("MEMORY", streamChannelProps));
        PipelineProcessConfiguration actualConfig = SWAPPER.swapToObject(yamlConfig);
        YamlPipelineProcessConfiguration actualYamlConfig = SWAPPER.swapToYamlConfiguration(actualConfig);
        assertThat(YamlEngine.marshal(actualYamlConfig), is(YamlEngine.marshal(yamlConfig)));
    }
    
    @Test
    public void assertYamlConfigNull() {
        assertNull(SWAPPER.swapToYamlConfiguration(null));
    }
    
    @Test
    public void assertConfigNull() {
        assertNull(SWAPPER.swapToObject(null));
    }
}
