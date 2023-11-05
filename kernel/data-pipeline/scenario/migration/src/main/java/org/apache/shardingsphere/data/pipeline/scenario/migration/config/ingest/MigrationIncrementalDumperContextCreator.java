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
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.common.config.ingest.IncrementalDumperContextCreator;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLineConvertUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;

/**
 * Migration incremental dumper context creator.
 */
@RequiredArgsConstructor
public final class MigrationIncrementalDumperContextCreator implements IncrementalDumperContextCreator {
    
    private final MigrationJobConfiguration jobConfig;
    
    @Override
    public IncrementalDumperContext createDumperContext(final JobDataNodeLine jobDataNodeLine) {
        String dataSourceName = jobDataNodeLine.getEntries().get(0).getDataNodes().get(0).getDataSourceName();
        ActualAndLogicTableNameMapper tableNameMapper = new ActualAndLogicTableNameMapper(JobDataNodeLineConvertUtils.buildTableNameMap(jobDataNodeLine));
        TableAndSchemaNameMapper tableAndSchemaNameMapper = new TableAndSchemaNameMapper(jobConfig.getTargetTableSchemaMap());
        return new IncrementalDumperContext(
                new DumperCommonContext(dataSourceName, jobConfig.getSources().get(dataSourceName), tableNameMapper, tableAndSchemaNameMapper), jobConfig.getJobId(), false);
    }
}
