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
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.lock.RuleBasedJobLockAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.lock.RowBasedJobLockAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
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
    
    private final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final JobCompletionDetectAlgorithm<RuleAlteredJobAlmostCompletedParameter> completionDetectAlgorithm;
    
    private final RowBasedJobLockAlgorithm sourceWritingStopAlgorithm;
    
    private final DataConsistencyCheckAlgorithm dataConsistencyCheckAlgorithm;
    
    private final RuleBasedJobLockAlgorithm checkoutLockAlgorithm;
    
    private final ExecuteEngine inventoryDumperExecuteEngine;
    
    private final ExecuteEngine incrementalDumperExecuteEngine;
    
    private final ExecuteEngine importerExecuteEngine;
    
    public RuleAlteredContext(final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig) {
        this.onRuleAlteredActionConfig = onRuleAlteredActionConfig;
        ShardingSphereAlgorithmConfiguration rateLimiter = onRuleAlteredActionConfig.getRateLimiter();
        if (null != rateLimiter) {
            rateLimitAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(rateLimiter, JobRateLimitAlgorithm.class);
        } else {
            rateLimitAlgorithm = null;
        }
        ShardingSphereAlgorithmConfiguration completionDetector = onRuleAlteredActionConfig.getCompletionDetector();
        if (null != completionDetector) {
            completionDetectAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(completionDetector, JobCompletionDetectAlgorithm.class);
        } else {
            completionDetectAlgorithm = null;
        }
        ShardingSphereAlgorithmConfiguration sourceWritingStopper = onRuleAlteredActionConfig.getSourceWritingStopper();
        if (null != sourceWritingStopper) {
            sourceWritingStopAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(sourceWritingStopper, RowBasedJobLockAlgorithm.class);
        } else {
            sourceWritingStopAlgorithm = null;
        }
        ShardingSphereAlgorithmConfiguration dataConsistencyChecker = onRuleAlteredActionConfig.getDataConsistencyChecker();
        if (null != dataConsistencyChecker) {
            dataConsistencyCheckAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(dataConsistencyChecker, DataConsistencyCheckAlgorithm.class);
        } else {
            dataConsistencyCheckAlgorithm = null;
        }
        ShardingSphereAlgorithmConfiguration checkoutLocker = onRuleAlteredActionConfig.getCheckoutLocker();
        if (null != checkoutLocker) {
            checkoutLockAlgorithm = ShardingSphereAlgorithmFactory.createAlgorithm(checkoutLocker, RuleBasedJobLockAlgorithm.class);
        } else {
            checkoutLockAlgorithm = null;
        }
        inventoryDumperExecuteEngine = ExecuteEngine.newFixedThreadInstance(onRuleAlteredActionConfig.getWorkerThread());
        incrementalDumperExecuteEngine = ExecuteEngine.newCachedThreadInstance();
        importerExecuteEngine = ExecuteEngine.newFixedThreadInstance(onRuleAlteredActionConfig.getWorkerThread());
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
}
