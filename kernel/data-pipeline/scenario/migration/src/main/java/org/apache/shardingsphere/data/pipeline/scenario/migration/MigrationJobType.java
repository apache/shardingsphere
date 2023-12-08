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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.DataNodeUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlTransmissionJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency.MigrationDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Migration job type.
 */
public final class MigrationJobType implements PipelineJobType {
    
    @Override
    public String getCode() {
        return "01";
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlMigrationJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlMigrationJobConfigurationSwapper();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlTransmissionJobItemProgressSwapper getYamlJobItemProgressSwapper() {
        return new YamlTransmissionJobItemProgressSwapper();
    }
    
    @Override
    public Class<MigrationJob> getJobClass() {
        return MigrationJob.class;
    }
    
    @Override
    public Optional<String> getToBeStartDisabledNextJobType() {
        return Optional.of("CONSISTENCY_CHECK");
    }
    
    @Override
    public Optional<String> getToBeStoppedPreviousJobType() {
        return Optional.of("CONSISTENCY_CHECK");
    }
    
    @Override
    public PipelineJobInfo getJobInfo(final String jobId) {
        PipelineJobMetaData jobMetaData = new PipelineJobMetaData(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId));
        Collection<String> sourceTables = new LinkedList<>();
        new PipelineJobConfigurationManager(new MigrationJobType()).<MigrationJobConfiguration>getJobConfiguration(jobId).getJobShardingDataNodes()
                .forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes().forEach(dataNode -> sourceTables.add(DataNodeUtils.formatWithSchema(dataNode)))));
        return new PipelineJobInfo(jobMetaData, null, String.join(",", sourceTables));
    }
    
    @Override
    public PipelineDataConsistencyChecker buildDataConsistencyChecker(final PipelineJobConfiguration jobConfig, final TransmissionProcessContext processContext,
                                                                      final ConsistencyCheckJobItemProgressContext progressContext) {
        return new MigrationDataConsistencyChecker((MigrationJobConfiguration) jobConfig, processContext, progressContext);
    }
    
    @Override
    public String getType() {
        return "MIGRATION";
    }
}
