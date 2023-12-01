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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.common.datanode.DataNodeUtils;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.option.TransmissionJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency.MigrationDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.YamlMigrationJobConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Migration job option.
 */
@Slf4j
public final class MigrationJobOption implements TransmissionJobOption {
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlMigrationJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlMigrationJobConfigurationSwapper();
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
        new PipelineJobConfigurationManager(this).<MigrationJobConfiguration>getJobConfiguration(jobId).getJobShardingDataNodes()
                .forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes().forEach(dataNode -> sourceTables.add(DataNodeUtils.formatWithSchema(dataNode)))));
        return new PipelineJobInfo(jobMetaData, null, String.join(",", sourceTables));
    }
    
    @Override
    public TransmissionProcessContext buildProcessContext(final PipelineJobConfiguration jobConfig) {
        PipelineProcessConfiguration processConfig = new TransmissionJobManager(this).showProcessConfiguration(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()));
        return new TransmissionProcessContext(jobConfig.getJobId(), processConfig);
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
