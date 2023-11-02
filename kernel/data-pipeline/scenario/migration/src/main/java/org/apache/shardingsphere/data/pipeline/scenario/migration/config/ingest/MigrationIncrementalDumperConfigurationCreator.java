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

package org.apache.shardingsphere.data.pipeline.scenario.migration.config.ingest;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.IncrementalDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.common.config.ingest.IncrementalDumperConfigurationCreator;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;

import java.util.Map;

/**
 * Migration incremental dumper configuration creator.
 */
@RequiredArgsConstructor
public final class MigrationIncrementalDumperConfigurationCreator implements IncrementalDumperConfigurationCreator {
    
    private final MigrationJobConfiguration jobConfig;
    
    @Override
    public IncrementalDumperConfiguration createDumperConfiguration(final JobDataNodeLine jobDataNodeLine) {
        Map<ActualTableName, LogicTableName> tableNameMap = JobDataNodeLineConvertUtils.buildTableNameMap(jobDataNodeLine);
        TableNameSchemaNameMapping tableNameSchemaNameMapping = new TableNameSchemaNameMapping(jobConfig.getTargetTableSchemaMap());
        String dataSourceName = jobDataNodeLine.getEntries().get(0).getDataNodes().get(0).getDataSourceName();
        return buildDumperConfiguration(jobConfig.getJobId(), dataSourceName, jobConfig.getSources().get(dataSourceName), tableNameMap, tableNameSchemaNameMapping);
    }
    
    private IncrementalDumperConfiguration buildDumperConfiguration(final String jobId, final String dataSourceName, final PipelineDataSourceConfiguration sourceDataSource,
                                                                    final Map<ActualTableName, LogicTableName> tableNameMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping) {
        IncrementalDumperConfiguration result = new IncrementalDumperConfiguration();
        result.setJobId(jobId);
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(sourceDataSource);
        result.setTableNameMap(tableNameMap);
        result.setTableNameSchemaNameMapping(tableNameSchemaNameMapping);
        return result;
    }
}
