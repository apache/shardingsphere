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

public class RuleAlteredJobAPIFixture implements RuleAlteredJobAPI {
    
    @Override
    public void startDisabledJob(String jobId) {
        
    }
    
    @Override
    public void stop(String jobId) {
        
    }
    
    @Override
    public void remove(String jobId) {
        
    }
    
    @Override
    public List<JobInfo> list() {
        return null;
    }
    
    @Override
    public Optional<String> start(RuleAlteredJobConfiguration jobConfig) {
        return Optional.empty();
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(String jobId) {
        return null;
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(RuleAlteredJobConfiguration jobConfig) {
        return null;
    }
    
    @Override
    public void stopClusterWriteDB(String jobId) {
        
    }
    
    @Override
    public void stopClusterWriteDB(String databaseName, String jobId) {
        
    }
    
    @Override
    public void restoreClusterWriteDB(String jobId) {
        
    }
    
    @Override
    public void restoreClusterWriteDB(String databaseName, String jobId) {
        
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        return null;
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(String jobId) {
        return false;
    }
    
    @Override
    public boolean isDataConsistencyCheckNeeded(RuleAlteredJobConfiguration jobConfig) {
        return false;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(String jobId) {
        return null;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(RuleAlteredJobConfiguration jobConfig) {
        return null;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(String jobId, String algorithmType) {
        return null;
    }
    
    @Override
    public boolean aggregateDataConsistencyCheckResults(String jobId, Map<String, DataConsistencyCheckResult> checkResults) {
        return false;
    }
    
    @Override
    public void switchClusterConfiguration(String jobId) {
        
    }
    
    @Override
    public void switchClusterConfiguration(RuleAlteredJobConfiguration jobConfig) {
        
    }
    
    @Override
    public void reset(String jobId) {
        
    }
    
    @Override
    public RuleAlteredJobConfiguration getJobConfig(String jobId) {
        return null;
    }
    
    @Override
    public boolean isDefault() {
        return RuleAlteredJobAPI.super.isDefault();
    }
}
