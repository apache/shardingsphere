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

package org.apache.shardingsphere.proxy.backend.handler.cdc.fixture;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.cdc.api.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.CreateSubscriptionJobParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.core.api.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class FixtureCDCJobAPI implements InventoryIncrementalJobAPI, CDCJobAPI {
    
    @Override
    public boolean createJob(final CreateSubscriptionJobParameter event) {
        return true;
    }
    
    @Override
    public JobType getJobType() {
        return null;
    }
    
    @Override
    public void startDisabledJob(final String jobId) {
    }
    
    @Override
    public void stop(final String jobId) {
    }
    
    @Override
    public List<? extends PipelineJobInfo> list() {
        return null;
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final PipelineJobConfiguration pipelineJobConfig) {
        return null;
    }
    
    @Override
    public String marshalJobId(final PipelineJobId pipelineJobId) {
        return null;
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
    }
    
    @Override
    public CDCTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        return null;
    }
    
    @Override
    public CDCProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        return null;
    }
    
    @Override
    public Optional<String> start(final PipelineJobConfiguration jobConfig) {
        return Optional.empty();
    }
    
    @Override
    public PipelineJobConfiguration getJobConfiguration(final String jobId) {
        return null;
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
    }
    
    @Override
    public Optional<InventoryIncrementalJobItemProgress> getJobItemProgress(final String jobId, final int shardingItem) {
        return Optional.empty();
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
    }
    
    @Override
    public String getJobItemErrorMessage(final String jobId, final int shardingItem) {
        return null;
    }
    
    @Override
    public void persistJobItemErrorMessage(final String jobId, final int shardingItem, final Object error) {
    }
    
    @Override
    public void cleanJobItemErrorMessage(final String jobId, final int shardingItem) {
    }
    
    @Override
    public DataConsistencyCalculateAlgorithm buildDataConsistencyCalculateAlgorithm(final PipelineJobConfiguration jobConfig, final String algorithmType, final Properties algorithmProps) {
        return null;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final PipelineJobConfiguration pipelineJobConfig, final DataConsistencyCalculateAlgorithm calculateAlgorithm,
                                                                        final ConsistencyCheckJobItemProgressContext progressContext) {
        return null;
    }
    
    @Override
    public boolean aggregateDataConsistencyCheckResults(final String jobId, final Map<String, DataConsistencyCheckResult> checkResults) {
        return false;
    }
    
    @Override
    public void alterProcessConfiguration(final PipelineProcessConfiguration processConfig) {
    }
    
    @Override
    public PipelineProcessConfiguration showProcessConfiguration() {
        return null;
    }
    
    @Override
    public void rollback(final String jobId) throws SQLException {
    }
    
    @Override
    public void commit(final String jobId) {
    }
    
    @Override
    public List<InventoryIncrementalJobItemInfo> getJobItemInfos(final String jobId) {
        return null;
    }
    
    @Override
    public Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms() {
        return null;
    }
}
