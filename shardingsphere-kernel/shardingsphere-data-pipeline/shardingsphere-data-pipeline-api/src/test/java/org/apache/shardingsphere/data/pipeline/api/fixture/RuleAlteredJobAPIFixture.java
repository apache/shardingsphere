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

package org.apache.shardingsphere.data.pipeline.api.fixture;

import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class RuleAlteredJobAPIFixture implements RuleAlteredJobAPI {
    
    @Override
    public void startDisabledJob(final String jobId) {
    }
    
    @Override
    public void stop(final String jobId) {
    }
    
    @Override
    public void remove(final String jobId) {
    }
    
    @Override
    public List<JobInfo> list() {
        return null;
    }
    
    @Override
    public Optional<String> start(final RuleAlteredJobConfiguration jobConfig) {
        return Optional.empty();
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final String jobId) {
        return null;
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final RuleAlteredJobConfiguration jobConfig) {
        return null;
    }
    
    @Override
    public void stopClusterWriteDB(final String jobId) {
    }
    
    @Override
    public void stopClusterWriteDB(final RuleAlteredJobConfiguration jobConfig) {
    }
    
    @Override
    public void restoreClusterWriteDB(final String jobId) {
    }
    
    @Override
    public void restoreClusterWriteDB(final RuleAlteredJobConfiguration jobConfig) {
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        return null;
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(final String jobId) {
        return false;
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(final RuleAlteredJobConfiguration jobConfig) {
        return false;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId) {
        return null;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final RuleAlteredJobConfiguration jobConfig) {
        return null;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId, final String algorithmType, final Properties algorithmProps) {
        return null;
    }
    
    @Override
    public boolean aggregateDataConsistencyCheckResults(final String jobId, final Map<String, DataConsistencyCheckResult> checkResults) {
        return false;
    }
    
    @Override
    public void switchClusterConfiguration(final String jobId) {
    }
    
    @Override
    public void switchClusterConfiguration(final RuleAlteredJobConfiguration jobConfig) {
    }
    
    @Override
    public void reset(final String jobId) {
    }
    
    @Override
    public RuleAlteredJobConfiguration getJobConfig(final String jobId) {
        return null;
    }
    
    @Override
    public boolean isDefault() {
        return RuleAlteredJobAPI.super.isDefault();
    }
}
