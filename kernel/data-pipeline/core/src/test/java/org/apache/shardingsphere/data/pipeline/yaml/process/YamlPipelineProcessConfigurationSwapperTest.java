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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

public final class YamlPipelineProcessConfigurationSwapperTest {
    
    @Test
    public void assertSwap() {
        YamlPipelineProcessConfiguration yamlConfig = createYamlPipelineProcessConfiguration();
        YamlPipelineProcessConfigurationSwapper swapper = new YamlPipelineProcessConfigurationSwapper();
        PipelineProcessConfiguration actualConfig = swapper.swapToObject(yamlConfig);
        YamlPipelineProcessConfiguration actualYamlConfig = swapper.swapToYamlConfiguration(actualConfig);
        assertThat(YamlEngine.marshal(actualYamlConfig), is(YamlEngine.marshal(yamlConfig)));
    }
    
    private YamlPipelineProcessConfiguration createYamlPipelineProcessConfiguration() {
        YamlPipelineProcessConfiguration result = new YamlPipelineProcessConfiguration();
        Properties rateLimiterProps = new Properties();
        rateLimiterProps.setProperty("batch-size", "1000");
        rateLimiterProps.setProperty("qps", "50");
        YamlPipelineReadConfiguration yamlInputConfig = YamlPipelineReadConfiguration.buildWithDefaultValue();
        yamlInputConfig.setRateLimiter(new YamlAlgorithmConfiguration("INPUT", rateLimiterProps));
        result.setRead(yamlInputConfig);
        YamlPipelineWriteConfiguration yamlOutputConfig = YamlPipelineWriteConfiguration.buildWithDefaultValue();
        yamlOutputConfig.setRateLimiter(new YamlAlgorithmConfiguration("OUTPUT", rateLimiterProps));
        result.setWrite(yamlOutputConfig);
        Properties streamChannelProps = new Properties();
        streamChannelProps.setProperty("block-queue-size", "10000");
        result.setStreamChannel(new YamlAlgorithmConfiguration("MEMORY", streamChannelProps));
        return result;
    }
    
    @Test
    public void assertSwapToYamlConfigurationWithNull() {
        assertNull(new YamlPipelineProcessConfigurationSwapper().swapToYamlConfiguration(null));
    }
    
    @Test
    public void assertSwapToObjectWithNull() {
        assertNull(new YamlPipelineProcessConfigurationSwapper().swapToObject(null));
    }
}
