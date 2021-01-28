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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.environmental.ScalingEnvironmentalManager;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract scaling job service.
 */
@Slf4j
public abstract class AbstractScalingJobService implements ScalingJobService {
    
    @Override
    public Optional<JobContext> start(final RuleConfigurationsAlteredEvent event) {
        JobConfiguration jobConfig = new JobConfiguration();
        jobConfig.setRuleConfig(new RuleConfiguration(
                new ShardingSphereJDBCDataSourceConfiguration(event.getSourceDataSource(), event.getSourceRule()),
                new ShardingSphereJDBCDataSourceConfiguration(event.getTargetDataSource(), event.getTargetRule())));
        HandleConfiguration handleConfig = new HandleConfiguration();
        handleConfig.setWorkflowConfig(new WorkflowConfiguration(event.getSchemaName(), event.getRuleCacheId()));
        jobConfig.setHandleConfig(handleConfig);
        return start(jobConfig);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final long jobId) {
        log.info("scaling job {} start data consistency check.", jobId);
        DataConsistencyChecker dataConsistencyChecker = DataConsistencyCheckerFactory.newInstance(getJob(jobId));
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck();
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        log.info("scaling job {} data consistency checker result {}", jobId, result);
        return result;
    }
    
    @Override
    public void reset(final long jobId) throws SQLException {
        new ScalingEnvironmentalManager().resetTargetTable(getJob(jobId));
    }
}
