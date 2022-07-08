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

import java.util.Properties;

/**
 * Rule altered context.
 */
@Getter
@Slf4j
// TODO extract Pipeline Context
public final class RuleAlteredContext {
    
    private static final String INVENTORY_THREAD_PREFIX = "Inventory-";
    
    private static final String INCREMENTAL_THREAD_PREFIX = "Incremental-";
    
    private static final String IMPORTER_THREAD_PREFIX = "Importer-";
    
    private static final OnRuleAlteredActionConfigurationYamlSwapper SWAPPER = new OnRuleAlteredActionConfigurationYamlSwapper();
    
    private final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig;
    
    private final JobRateLimitAlgorithm inputRateLimitAlgorithm;
    
    private final JobRateLimitAlgorithm outputRateLimitAlgorithm;
    
    private final PipelineChannelCreator pipelineChannelCreator;
    
    private final JobCompletionDetectAlgorithm<RuleAlteredJobAlmostCompletedParameter> completionDetectAlgorithm;
    
    private final DataConsistencyCalculateAlgorithm dataConsistencyCalculateAlgorithm;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine incrementalDumperExecuteEngine;
    
    private final ExecuteEngine importerExecuteEngine;
    
    @SuppressWarnings("unchecked")
    public RuleAlteredContext(final String jobId, final OnRuleAlteredActionConfiguration actionConfig) {
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
        ShardingSphereAlgorithmConfiguration dataConsistencyCheckerConfig = onRuleAlteredActionConfig.getDataConsistencyCalculator();
        dataConsistencyCalculateAlgorithm = null != dataConsistencyCheckerConfig
                ? DataConsistencyCalculateAlgorithmFactory.newInstance(dataConsistencyCheckerConfig.getType(), dataConsistencyCheckerConfig.getProps())
                : null;
        inventoryDumperExecuteEngine = ExecuteEngine.newFixedThreadInstance(inputConfig.getWorkerThread(), INVENTORY_THREAD_PREFIX + jobId);
        incrementalDumperExecuteEngine = ExecuteEngine.newCachedThreadInstance(INCREMENTAL_THREAD_PREFIX + jobId);
        importerExecuteEngine = ExecuteEngine.newFixedThreadInstance(outputConfig.getWorkerThread(), IMPORTER_THREAD_PREFIX + jobId);
    }
    
    private OnRuleAlteredActionConfiguration convertActionConfig(final OnRuleAlteredActionConfiguration actionConfig) {
        YamlOnRuleAlteredActionConfiguration yamlActionConfig = SWAPPER.swapToYamlConfiguration(actionConfig);
        if (null == yamlActionConfig.getInput()) {
            yamlActionConfig.setInput(YamlInputConfiguration.buildWithDefaultValue());
        } else {
            yamlActionConfig.getInput().fillInNullFieldsWithDefaultValue();
        }
        if (null == yamlActionConfig.getOutput()) {
            yamlActionConfig.setOutput(YamlOutputConfiguration.buildWithDefaultValue());
        } else {
            yamlActionConfig.getOutput().fillInNullFieldsWithDefaultValue();
        }
        if (null == yamlActionConfig.getStreamChannel()) {
            yamlActionConfig.setStreamChannel(new YamlShardingSphereAlgorithmConfiguration(MemoryPipelineChannelCreator.TYPE, new Properties()));
        }
        return SWAPPER.swapToObject(yamlActionConfig);
    }
}
