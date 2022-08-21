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

package org.apache.shardingsphere.infra.yaml.config.swapper.rule.rulealtered;

import org.apache.shardingsphere.infra.config.rule.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineReadConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.data.pipeline.YamlPipelineWriteConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlOnRuleAlteredActionConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class YamlOnRuleAlteredActionConfigurationSwapperTest {
    
    private static final YamlOnRuleAlteredActionConfigurationSwapper SWAPPER = new YamlOnRuleAlteredActionConfigurationSwapper();
    
    @Test
    public void assertSwap() {
        YamlOnRuleAlteredActionConfiguration yamlConfig = new YamlOnRuleAlteredActionConfiguration();
        Properties rateLimiterProps = new Properties();
        rateLimiterProps.setProperty("batch-size", "1000");
        rateLimiterProps.setProperty("qps", "50");
        YamlPipelineReadConfiguration yamlInputConfig = YamlPipelineReadConfiguration.buildWithDefaultValue();
        yamlConfig.setInput(yamlInputConfig);
        yamlInputConfig.setRateLimiter(new YamlAlgorithmConfiguration("INPUT", rateLimiterProps));
        YamlPipelineWriteConfiguration yamlOutputConfig = YamlPipelineWriteConfiguration.buildWithDefaultValue();
        yamlOutputConfig.setRateLimiter(new YamlAlgorithmConfiguration("OUTPUT", rateLimiterProps));
        yamlConfig.setOutput(yamlOutputConfig);
        Properties streamChannelProps = new Properties();
        streamChannelProps.setProperty("block-queue-size", "10000");
        yamlConfig.setStreamChannel(new YamlAlgorithmConfiguration("MEMORY", streamChannelProps));
        Properties completionDetectorProps = new Properties();
        completionDetectorProps.setProperty("incremental-task-idle-seconds-threshold", "1800");
        yamlConfig.setCompletionDetector(new YamlAlgorithmConfiguration("IDLE", completionDetectorProps));
        Properties dataConsistencyCheckerProps = new Properties();
        dataConsistencyCheckerProps.setProperty("chunk-size", "1000");
        yamlConfig.setDataConsistencyChecker(new YamlAlgorithmConfiguration("DATA_MATCH", dataConsistencyCheckerProps));
        YamlOnRuleAlteredActionConfigurationSwapper onRuleAlteredActionConfigSwapper = new YamlOnRuleAlteredActionConfigurationSwapper();
        OnRuleAlteredActionConfiguration actualConfig = onRuleAlteredActionConfigSwapper.swapToObject(yamlConfig);
        YamlOnRuleAlteredActionConfiguration actualYamlConfig = onRuleAlteredActionConfigSwapper.swapToYamlConfiguration(actualConfig);
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
