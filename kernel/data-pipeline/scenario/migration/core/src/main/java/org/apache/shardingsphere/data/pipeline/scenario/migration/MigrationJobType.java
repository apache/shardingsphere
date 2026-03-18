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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;
import org.apache.shardingsphere.data.pipeline.scenario.migration.check.consistency.MigrationDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Migration job type.
 */
public final class MigrationJobType implements PipelineJobType<MigrationJobConfiguration> {
    
    @Override
    public PipelineJobOption getOption() {
        return new PipelineJobOption("01", MigrationJob.class, true, new YamlMigrationJobConfigurationSwapper(), false, "CONSISTENCY_CHECK", "CONSISTENCY_CHECK", false);
    }
    
    @Override
    public PipelineJobTarget getJobTarget(final MigrationJobConfiguration jobConfig) {
        Collection<String> sourceTables = new LinkedList<>();
        jobConfig.getJobShardingDataNodes().forEach(each -> each.getEntries().forEach(entry -> entry.getDataNodes().forEach(dataNode -> sourceTables.add(dataNode.format()))));
        return new PipelineJobTarget(null, String.join(",", sourceTables));
    }
    
    @Override
    public PipelineDataConsistencyChecker buildDataConsistencyChecker(final MigrationJobConfiguration jobConfig,
                                                                      final TransmissionProcessContext processContext, final ConsistencyCheckJobItemProgressContext progressContext) {
        return new MigrationDataConsistencyChecker(jobConfig, processContext, progressContext);
    }
    
    @Override
    public String getType() {
        return "MIGRATION";
    }
}
