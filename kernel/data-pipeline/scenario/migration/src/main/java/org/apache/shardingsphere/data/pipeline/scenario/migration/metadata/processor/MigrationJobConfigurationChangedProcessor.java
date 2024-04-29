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

import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor.JobConfigurationChangedProcessor;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.scenario.migration.preparer.MigrationJobPreparer;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;

/**
 * Migration job configuration changed processor.
 */
public final class MigrationJobConfigurationChangedProcessor implements JobConfigurationChangedProcessor<MigrationJobConfiguration> {
    
    @Override
    public PipelineJob createJob(final MigrationJobConfiguration jobConfig) {
        return new MigrationJob(jobConfig.getJobId());
    }
    
    @Override
    public void clean(final JobConfiguration jobConfig) {
        new MigrationJobPreparer().cleanup(new YamlMigrationJobConfigurationSwapper().swapToObject(jobConfig.getJobParameter()));
    }
    
    @Override
    public String getType() {
        return "MIGRATION";
    }
}
