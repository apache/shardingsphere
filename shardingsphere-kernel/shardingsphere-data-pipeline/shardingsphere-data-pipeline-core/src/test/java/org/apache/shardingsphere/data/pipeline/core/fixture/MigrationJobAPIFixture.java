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

package org.apache.shardingsphere.data.pipeline.core.fixture;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateMigrationJobParameter;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationProcessContext;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class MigrationJobAPIFixture implements MigrationJobAPI {
    
    @Override
    public String marshalJobId(final PipelineJobId pipelineJobId) {
        return null;
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
    }
    
    @Override
    public void createProcessConfiguration(final PipelineProcessConfiguration processConfig) {
    }
    
    @Override
    public void alterProcessConfiguration(final PipelineProcessConfiguration processConfig) {
    }
    
    @Override
    public PipelineProcessConfiguration showProcessConfiguration() {
        return null;
    }
    
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
    public List<PipelineJobInfo> list() {
        return null;
    }
    
    @Override
    public Optional<String> start(final PipelineJobConfiguration jobConfig) {
        return Optional.empty();
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final String jobId) {
        return null;
    }
    
    @Override
    public Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(final MigrationJobConfiguration jobConfig) {
        return null;
    }
    
    @Override
    public void stopClusterWriteDB(final String jobId) {
    }
    
    @Override
    public void stopClusterWriteDB(final MigrationJobConfiguration jobConfig) {
    }
    
    @Override
    public void restoreClusterWriteDB(final String jobId) {
    }
    
    @Override
    public void restoreClusterWriteDB(final MigrationJobConfiguration jobConfig) {
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
    public boolean isDataConsistencyCheckNeeded(final MigrationJobConfiguration jobConfig) {
        return false;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final String jobId) {
        return null;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final MigrationJobConfiguration jobConfig) {
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
    public void switchClusterConfiguration(final MigrationJobConfiguration jobConfig) {
    }
    
    @Override
    public void reset(final String jobId) {
    }
    
    @Override
    public void addMigrationSourceResources(final Map<String, DataSourceProperties> dataSourcePropsMap) {
    }
    
    @Override
    public void dropMigrationSourceResources(final Collection<String> resourceNames) {
    }
    
    @Override
    public Collection<Collection<Object>> listMigrationSourceResources() {
        return null;
    }
    
    @Override
    public void createJobAndStart(final CreateMigrationJobParameter parameter) {
    }
    
    @Override
    public MigrationJobConfiguration getJobConfiguration(final String jobId) {
        return null;
    }
    
    @Override
    public TaskConfiguration buildTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        return null;
    }
    
    @Override
    public MigrationProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        return null;
    }
    
    @Override
    public boolean isDefault() {
        return MigrationJobAPI.super.isDefault();
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
    }
    
    @Override
    public InventoryIncrementalJobItemProgress getJobItemProgress(final String jobId, final int shardingItem) {
        return null;
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
    }
}
