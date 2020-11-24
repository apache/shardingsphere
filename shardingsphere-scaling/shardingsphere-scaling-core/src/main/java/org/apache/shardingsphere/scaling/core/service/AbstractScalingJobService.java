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

package org.apache.shardingsphere.scaling.core.service;

import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.utils.ProxyConfigurationUtil;
import org.apache.shardingsphere.scaling.core.utils.SyncConfigurationUtil;

import java.util.Map;
import java.util.Optional;

/**
 * Abstract scaling job service.
 */
public abstract class AbstractScalingJobService implements ScalingJobService {
    
    @Override
    public boolean shouldScaling(final String oldYamlProxyConfig, final String newYamlProxyConfig) {
        ScalingConfiguration scalingConfig = ProxyConfigurationUtil.toScalingConfig(oldYamlProxyConfig, newYamlProxyConfig);
        SyncConfigurationUtil.fillInShardingTables(scalingConfig);
        return shouldScaling(scalingConfig);
    }
    
    private boolean shouldScaling(final ScalingConfiguration scalingConfig) {
        return scalingConfig.getJobConfiguration().getShardingTables().length > 0;
    }
    
    @Override
    public Optional<ShardingScalingJob> start(final String oldYamlProxyConfig, final String newYamlProxyConfig) {
        ScalingConfiguration scalingConfig = ProxyConfigurationUtil.toScalingConfig(oldYamlProxyConfig, newYamlProxyConfig);
        SyncConfigurationUtil.fillInShardingTables(scalingConfig);
        if (!shouldScaling(scalingConfig)) {
            return Optional.empty();
        }
        return start(scalingConfig);
    }
    
    @Override
    public void reset(final long jobId) {
        // TODO reset target tables.
    }
    
    /**
     * Do data consistency check.
     *
     * @param shardingScalingJob sharding scaling job
     * @return data consistency check result
     */
    protected Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final ShardingScalingJob shardingScalingJob) {
        DataConsistencyChecker dataConsistencyChecker = shardingScalingJob.getDataConsistencyChecker();
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck();
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        return result;
    }
}
