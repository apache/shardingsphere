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

package org.apache.shardingsphere.data.pipeline.api.config.rulealtered;

import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.job.JobSubType;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.RuleAlteredJobId;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparer;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;

import java.util.Collections;

/**
 * Scaling job configuration.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Slf4j
// TODO share for totally new scenario
public final class JobConfiguration {
    
    private WorkflowConfiguration workflowConfig;
    
    private PipelineConfiguration pipelineConfig;
    
    private HandleConfiguration handleConfig;
    
    public JobConfiguration(final WorkflowConfiguration workflowConfig, final PipelineConfiguration pipelineConfig) {
        this.workflowConfig = workflowConfig;
        this.pipelineConfig = pipelineConfig;
    }
    
    /**
     * Build handle configuration.
     */
    public void buildHandleConfig() {
        PipelineConfiguration pipelineConfig = getPipelineConfig();
        HandleConfiguration handleConfig = getHandleConfig();
        if (null == handleConfig || null == handleConfig.getJobShardingDataNodes()) {
            RuleAlteredJobConfigurationPreparer preparer = RequiredSPIRegistry.getRegisteredService(RuleAlteredJobConfigurationPreparer.class);
            handleConfig = preparer.createHandleConfiguration(pipelineConfig, getWorkflowConfig());
            this.handleConfig = handleConfig;
        }
        if (null == handleConfig.getJobId()) {
            handleConfig.setJobId(generateJobId());
        }
        if (Strings.isNullOrEmpty(handleConfig.getSourceDatabaseType())) {
            PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                    pipelineConfig.getSource().getType(), pipelineConfig.getSource().getParameter());
            handleConfig.setSourceDatabaseType(sourceDataSourceConfig.getDatabaseType().getName());
        }
        if (Strings.isNullOrEmpty(handleConfig.getTargetDatabaseType())) {
            PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                    pipelineConfig.getTarget().getType(), pipelineConfig.getTarget().getParameter());
            handleConfig.setTargetDatabaseType(targetDataSourceConfig.getDatabaseType().getName());
        }
        if (null == handleConfig.getJobShardingItem()) {
            handleConfig.setJobShardingItem(0);
        }
    }
    
    private String generateJobId() {
        RuleAlteredJobId jobId = new RuleAlteredJobId();
        // TODO type, subTypes
        jobId.setType(JobType.RULE_ALTERED.getValue());
        jobId.setFormatVersion(RuleAlteredJobId.CURRENT_VERSION);
        jobId.setSubTypes(Collections.singletonList(JobSubType.SCALING.getValue()));
        WorkflowConfiguration workflowConfig = getWorkflowConfig();
        jobId.setCurrentMetadataVersion(workflowConfig.getActiveVersion());
        jobId.setNewMetadataVersion(workflowConfig.getNewVersion());
        jobId.setSchemaName(workflowConfig.getSchemaName());
        return jobId.marshal();
    }
}
