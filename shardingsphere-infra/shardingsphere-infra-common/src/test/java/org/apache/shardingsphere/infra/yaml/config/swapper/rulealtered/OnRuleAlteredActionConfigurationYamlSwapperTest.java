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

package org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered;

import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlInputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlOutputConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class OnRuleAlteredActionConfigurationYamlSwapperTest {
    
    private static final OnRuleAlteredActionConfigurationYamlSwapper CONFIG_YAML_SWAPPER = new OnRuleAlteredActionConfigurationYamlSwapper();
    
    @Test
    public void assertSwap() {
        YamlOnRuleAlteredActionConfiguration yamlConfig = new YamlOnRuleAlteredActionConfiguration();
        Properties rateLimiterProps = new Properties();
        rateLimiterProps.setProperty("batch-size", "1000");
        rateLimiterProps.setProperty("qps", "50");
        YamlInputConfiguration yamlInputConfig = YamlInputConfiguration.buildWithDefaultValue();
        yamlConfig.setInput(yamlInputConfig);
        yamlInputConfig.setRateLimiter(new YamlShardingSphereAlgorithmConfiguration("INPUT", rateLimiterProps));
        YamlOutputConfiguration yamlOutputConfig = YamlOutputConfiguration.buildWithDefaultValue();
        yamlOutputConfig.setRateLimiter(new YamlShardingSphereAlgorithmConfiguration("OUTPUT", rateLimiterProps));
        yamlConfig.setOutput(yamlOutputConfig);
        Properties streamChannelProps = new Properties();
        streamChannelProps.setProperty("block-queue-size", "10000");
        yamlConfig.setStreamChannel(new YamlShardingSphereAlgorithmConfiguration("MEMORY", streamChannelProps));
        Properties completionDetectorProps = new Properties();
        completionDetectorProps.setProperty("incremental-task-idle-seconds-threshold", "1800");
        yamlConfig.setCompletionDetector(new YamlShardingSphereAlgorithmConfiguration("IDLE", completionDetectorProps));
        Properties dataConsistencyCheckerProps = new Properties();
        dataConsistencyCheckerProps.setProperty("chunk-size", "1000");
        yamlConfig.setDataConsistencyChecker(new YamlShardingSphereAlgorithmConfiguration("DATA_MATCH", dataConsistencyCheckerProps));
        OnRuleAlteredActionConfigurationYamlSwapper yamlSwapper = new OnRuleAlteredActionConfigurationYamlSwapper();
        OnRuleAlteredActionConfiguration actualConfig = yamlSwapper.swapToObject(yamlConfig);
        YamlOnRuleAlteredActionConfiguration actualYamlConfig = yamlSwapper.swapToYamlConfiguration(actualConfig);
        assertThat(YamlEngine.marshal(actualYamlConfig), is(YamlEngine.marshal(yamlConfig)));
    }
    
    @Test
    public void assertYamlConfigNull() {
        assertNull(CONFIG_YAML_SWAPPER.swapToYamlConfiguration(null));
    }
    
    @Test
    public void assertConfigNull() {
        assertNull(CONFIG_YAML_SWAPPER.swapToObject(null));
    }
}
