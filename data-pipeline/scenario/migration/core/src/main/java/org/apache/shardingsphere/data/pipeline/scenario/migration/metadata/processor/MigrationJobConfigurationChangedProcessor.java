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

package org.apache.shardingsphere.data.pipeline.scenario.migration.metadata.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor.JobConfigurationChangedProcessor;
import org.apache.shardingsphere.data.pipeline.core.preparer.incremental.IncrementalTaskPositionManager;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;

import java.sql.SQLException;
import java.util.Map.Entry;

/**
 * Migration job configuration changed processor.
 */
@Slf4j
public final class MigrationJobConfigurationChangedProcessor implements JobConfigurationChangedProcessor<MigrationJobConfiguration> {
    
    @Override
    public PipelineJob createJob(final MigrationJobConfiguration jobConfig) {
        return new MigrationJob();
    }
    
    @Override
    public void clean(final JobConfiguration jobConfig) {
        MigrationJobConfiguration migrationJobConfig = new YamlMigrationJobConfigurationSwapper().swapToObject(jobConfig.getJobParameter());
        for (Entry<String, PipelineDataSourceConfiguration> entry : migrationJobConfig.getSources().entrySet()) {
            try {
                new IncrementalTaskPositionManager(entry.getValue().getDatabaseType()).destroyPosition(migrationJobConfig.getJobId(), entry.getValue());
            } catch (final SQLException ex) {
                log.warn("Job destroying failed, jobId={}, dataSourceName={}", migrationJobConfig.getJobId(), entry.getKey(), ex);
            }
        }
    }
    
    @Override
    public String getType() {
        return "MIGRATION";
    }
}
