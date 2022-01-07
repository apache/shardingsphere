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
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.lock.RowBasedJobLockAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.lock.RuleBasedJobLockAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.InputConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration.OutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlInputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rulealtered.YamlOnRuleAlteredActionConfiguration.YamlOutputConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper.InputConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rulealtered.OnRuleAlteredActionConfigurationYamlSwapper.OutputConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

/**
 * Rule altered context.
 */
@Getter
@Slf4j
// TODO extract Pipeline Context
public final class RuleAlteredContext {
    
    static {
        ShardingSphereServiceLoader.register(JobRateLimitAlgorithm.class);
        ShardingSphereServiceLoader.register(JobCompletionDetectAlgorithm.class);
        ShardingSphereServiceLoader.register(RowBasedJobLockAlgorithm.class);
        ShardingSphereServiceLoader.register(DataConsistencyCheckAlgorithm.class);
        ShardingSphereServiceLoader.register(RuleBasedJobLockAlgorithm.class);
    }
    
    private static volatile ModeConfiguration modeConfig;
    
    private static volatile ContextManager contextManager;
    
    private final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig;
    
    private final JobRateLimitAlgorithm inputRateLimitAlgorithm;
    
    private final JobRateLimitAlgorithm outputRateLimitAlgorithm;
    
    private final JobCompletionDetectAlgorithm<RuleAlteredJobAlmostCompletedParameter> completionDetectAlgorithm;
    
    private final RowBasedJobLockAlgorithm sourceWritingStopAlgorithm;
    
    private final DataConsistencyCheckAlgorithm dataConsistencyCheckAlgorithm;
    
    private final RuleBasedJobLockAlgorithm checkoutLockAlgorithm;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine incrementalDumperExecuteEngine;
    
    private final ExecuteEngine importerExecuteEngine;
    
    public RuleAlteredContext(final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig) {
        this.onRuleAlteredActionConfig = onRuleAlteredActionConfig;
        InputConfiguration inputConfig = onRuleAlteredActionConfig.getInputConfig();
        if (null == inputConfig) {
            inputConfig = new InputConfigurationSwapper().swapToObject(new YamlInputConfiguration());
        }
        ShardingSphereAlgorithmConfiguration inputRateLimiter = inputConfig.getRateLimiter();
        inputRateLimitAlgorithm = null != inputRateLimiter ? ShardingSphereAlgorithmFactory.createAlgorithm(inputRateLimiter, JobRateLimitAlgorithm.class) : null;
        OutputConfiguration outputConfig = onRuleAlteredActionConfig.getOutputConfig();
        if (null == outputConfig) {
            outputConfig = new OutputConfigurationSwapper().swapToObject(new YamlOutputConfiguration());
        }
        ShardingSphereAlgorithmConfiguration outputRateLimiter = outputConfig.getRateLimiter();
        outputRateLimitAlgorithm = null != outputRateLimiter ? ShardingSphereAlgorithmFactory.createAlgorithm(outputRateLimiter, JobRateLimitAlgorithm.class) : null;
        ShardingSphereAlgorithmConfiguration completionDetector = onRuleAlteredActionConfig.getCompletionDetector();
        completionDetectAlgorithm = null != completionDetector ? ShardingSphereAlgorithmFactory.createAlgorithm(completionDetector, JobCompletionDetectAlgorithm.class) : null;
        sourceWritingStopAlgorithm = null;
        ShardingSphereAlgorithmConfiguration dataConsistencyChecker = onRuleAlteredActionConfig.getDataConsistencyChecker();
        dataConsistencyCheckAlgorithm = null != dataConsistencyChecker ? ShardingSphereAlgorithmFactory.createAlgorithm(dataConsistencyChecker, DataConsistencyCheckAlgorithm.class) : null;
        checkoutLockAlgorithm = null;
        inventoryDumperExecuteEngine = ExecuteEngine.newFixedThreadInstance(inputConfig.getWorkerThread());
        incrementalDumperExecuteEngine = ExecuteEngine.newCachedThreadInstance();
        importerExecuteEngine = ExecuteEngine.newFixedThreadInstance(outputConfig.getWorkerThread());
    }
    
    /**
     * Get mode configuration.
     *
     * @return mode configuration
     */
    public static ModeConfiguration getModeConfig() {
        return modeConfig;
    }
    
    /**
     * Initialize mode configuration.
     *
     * @param modeConfig configuration
     */
    public static void initModeConfig(final ModeConfiguration modeConfig) {
        RuleAlteredContext.modeConfig = modeConfig;
    }
    
    /**
     * Get context manager.
     *
     * @return context manager
     */
    public static ContextManager getContextManager() {
        return contextManager;
    }
    
    /**
     * Initialize context manager.
     *
     * @param contextManager context manager
     */
    public static void initContextManager(final ContextManager contextManager) {
        RuleAlteredContext.contextManager = contextManager;
    }
}
