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

package org.apache.shardingsphere.data.pipeline.core.spi.process;

import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobPreparer;
import org.apache.shardingsphere.data.pipeline.spi.process.JobConfigEventProcess;

/**
 * Migration job config event process.
 */
public final class MigrationJobConfigEventProcess implements JobConfigEventProcess {
    
    @Override
    public void cleanup(final String jobParameter) {
        MigrationJobConfiguration jobConfig = YamlMigrationJobConfigurationSwapper.swapToObject(jobParameter);
        new MigrationJobPreparer().cleanup(jobConfig);
    }
    
    @Override
    public boolean isJobSuccessful(final String jobParameter) {
        MigrationJobConfiguration jobConfig = YamlMigrationJobConfigurationSwapper.swapToObject(jobParameter);
        return PipelineJobProgressDetector.isJobSuccessful(jobConfig.getJobShardingCount(), MigrationJobAPIFactory.getInstance().getJobProgress(jobConfig).values());
    }
    
    @Override
    public String getType() {
        return JobType.MIGRATION.getTypeName();
    }
}
