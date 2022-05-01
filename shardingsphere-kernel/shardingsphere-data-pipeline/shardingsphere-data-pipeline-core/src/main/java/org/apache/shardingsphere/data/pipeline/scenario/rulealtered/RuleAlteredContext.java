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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.detect.RuleAlteredJobAlmostCompletedParameter;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.DataConsistencyCalculateAlgorithmFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MemoryPipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithmFactory;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreatorFactory;
import org.apache.shardingsphere.data.pipeline.spi.lock.RowBasedJobLockAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.lock.RuleBasedJobLockAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.OutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlInputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlOutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.required.RequiredSPIRegistry;

import java.util.Properties;

/**
 * Rule altered context.
 */
@Getter
@Slf4j
// TODO extract Pipeline Context
public final class RuleAlteredContext {
    
    private static final OnRuleAlteredActionConfigurationYamlSwapper ACTION_CONFIG_YAML_SWAPPER = new OnRuleAlteredActionConfigurationYamlSwapper();
    
    static {
        ShardingSphereServiceLoader.register(RowBasedJobLockAlgorithm.class);
        ShardingSphereServiceLoader.register(RuleBasedJobLockAlgorithm.class);
    }
    
    private final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig;
    
    private final JobRateLimitAlgorithm inputRateLimitAlgorithm;
    
    private final JobRateLimitAlgorithm outputRateLimitAlgorithm;
    
    private final PipelineChannelCreator pipelineChannelCreator;
    
    private final JobCompletionDetectAlgorithm<RuleAlteredJobAlmostCompletedParameter> completionDetectAlgorithm;
    
    private final RowBasedJobLockAlgorithm sourceWritingStopAlgorithm;
    
    private final DataConsistencyCalculateAlgorithm dataConsistencyCalculateAlgorithm;
    
    private final RuleBasedJobLockAlgorithm checkoutLockAlgorithm;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine incrementalDumperExecuteEngine;
    
    private final ExecuteEngine importerExecuteEngine;
    
    @SuppressWarnings("unchecked")
    public RuleAlteredContext(final OnRuleAlteredActionConfiguration actionConfig) {
        OnRuleAlteredActionConfiguration onRuleAlteredActionConfig = convertActionConfig(actionConfig);
        this.onRuleAlteredActionConfig = onRuleAlteredActionConfig;
        InputConfiguration inputConfig = onRuleAlteredActionConfig.getInput();
        ShardingSphereAlgorithmConfiguration inputRateLimiter = inputConfig.getRateLimiter();
        inputRateLimitAlgorithm = null != inputRateLimiter ? JobRateLimitAlgorithmFactory.newInstance(inputRateLimiter) : null;
        OutputConfiguration outputConfig = onRuleAlteredActionConfig.getOutput();
        ShardingSphereAlgorithmConfiguration outputRateLimiter = outputConfig.getRateLimiter();
        outputRateLimitAlgorithm = null != outputRateLimiter ? JobRateLimitAlgorithmFactory.newInstance(outputRateLimiter) : null;
        ShardingSphereAlgorithmConfiguration streamChannel = onRuleAlteredActionConfig.getStreamChannel();
        pipelineChannelCreator = PipelineChannelCreatorFactory.newInstance(streamChannel);
        ShardingSphereAlgorithmConfiguration completionDetector = onRuleAlteredActionConfig.getCompletionDetector();
        completionDetectAlgorithm = null != completionDetector ? JobCompletionDetectAlgorithmFactory.newInstance(completionDetector) : null;
        sourceWritingStopAlgorithm = RequiredSPIRegistry.getRegisteredService(RowBasedJobLockAlgorithm.class);
        ShardingSphereAlgorithmConfiguration dataConsistencyChecker = onRuleAlteredActionConfig.getDataConsistencyCalculator();
        dataConsistencyCalculateAlgorithm = null != dataConsistencyChecker
                ? DataConsistencyCalculateAlgorithmFactory.newInstance(dataConsistencyChecker.getType(), dataConsistencyChecker.getProps())
                : null;
        checkoutLockAlgorithm = RequiredSPIRegistry.getRegisteredService(RuleBasedJobLockAlgorithm.class);
        inventoryDumperExecuteEngine = ExecuteEngine.newFixedThreadInstance(inputConfig.getWorkerThread());
        incrementalDumperExecuteEngine = ExecuteEngine.newCachedThreadInstance();
        importerExecuteEngine = ExecuteEngine.newFixedThreadInstance(outputConfig.getWorkerThread());
    }
    
    private OnRuleAlteredActionConfiguration convertActionConfig(final OnRuleAlteredActionConfiguration actionConfig) {
        YamlOnRuleAlteredActionConfiguration yamlActionConfig = ACTION_CONFIG_YAML_SWAPPER.swapToYamlConfiguration(actionConfig);
        if (null == yamlActionConfig.getInput()) {
            yamlActionConfig.setInput(YamlInputConfiguration.buildWithDefaultValue());
        }
        if (null == yamlActionConfig.getOutput()) {
            yamlActionConfig.setOutput(YamlOutputConfiguration.buildWithDefaultValue());
        }
        if (null == yamlActionConfig.getStreamChannel()) {
            yamlActionConfig.setStreamChannel(new YamlShardingSphereAlgorithmConfiguration(MemoryPipelineChannelCreator.TYPE, new Properties()));
        }
        return ACTION_CONFIG_YAML_SWAPPER.swapToObject(yamlActionConfig);
    }
}
