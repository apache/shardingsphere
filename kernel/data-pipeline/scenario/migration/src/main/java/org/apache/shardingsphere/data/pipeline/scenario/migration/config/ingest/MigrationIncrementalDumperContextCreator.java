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
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.common.config.ingest.IncrementalDumperContextCreator;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;

import java.util.Map;

/**
 * Migration incremental dumper configuration creator.
 */
@RequiredArgsConstructor
public final class MigrationIncrementalDumperContextCreator implements IncrementalDumperContextCreator {
    
    private final MigrationJobConfiguration jobConfig;
    
    @Override
    public IncrementalDumperContext createDumperContext(final JobDataNodeLine jobDataNodeLine) {
        Map<ActualTableName, LogicTableName> tableNameMap = JobDataNodeLineConvertUtils.buildTableNameMap(jobDataNodeLine);
        TableAndSchemaNameMapper tableAndSchemaNameMapper = new TableAndSchemaNameMapper(jobConfig.getTargetTableSchemaMap());
        String dataSourceName = jobDataNodeLine.getEntries().get(0).getDataNodes().get(0).getDataSourceName();
        return buildDumperContext(jobConfig.getJobId(), dataSourceName, jobConfig.getSources().get(dataSourceName), tableNameMap, tableAndSchemaNameMapper);
    }
    
    private IncrementalDumperContext buildDumperContext(final String jobId, final String dataSourceName, final PipelineDataSourceConfiguration sourceDataSource,
                                                        final Map<ActualTableName, LogicTableName> tableNameMap, final TableAndSchemaNameMapper tableAndSchemaNameMapper) {
        IncrementalDumperContext result = new IncrementalDumperContext();
        result.setJobId(jobId);
        result.setDataSourceName(dataSourceName);
        result.setDataSourceConfig(sourceDataSource);
        result.setTableNameMapper(new ActualAndLogicTableNameMapper(tableNameMap));
        result.setTableAndSchemaNameMapper(tableAndSchemaNameMapper);
        return result;
    }
}
