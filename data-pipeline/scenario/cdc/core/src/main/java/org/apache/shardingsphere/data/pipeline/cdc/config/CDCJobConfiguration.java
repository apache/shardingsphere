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

package org.apache.shardingsphere.data.pipeline.cdc.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.util.List;
import java.util.Properties;

/**
 * CDC job configuration.
 */
@RequiredArgsConstructor
@Getter
public final class CDCJobConfiguration implements PipelineJobConfiguration {
    
    private final String jobId;
    
    private final String databaseName;
    
    private final List<String> schemaTableNames;
    
    private final boolean full;
    
    private final DatabaseType sourceDatabaseType;
    
    private final ShardingSpherePipelineDataSourceConfiguration dataSourceConfig;
    
    private final JobDataNodeLine tablesFirstDataNodes;
    
    private final List<JobDataNodeLine> jobShardingDataNodes;
    
    private final boolean decodeWithTX;
    
    private final SinkConfiguration sinkConfig;
    
    private final int concurrency;
    
    private final int retryTimes;
    
    @Override
    public int getJobShardingCount() {
        return jobShardingDataNodes.size();
    }
    
    @RequiredArgsConstructor
    @Getter
    public static class SinkConfiguration {
        
        private final CDCSinkType sinkType;
        
        private final Properties props;
    }
}
