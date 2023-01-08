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

package org.apache.shardingsphere.data.pipeline.cdc.config.job;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;

import java.util.List;

/**
 * CDC job configuration.
 */
@Getter
@RequiredArgsConstructor
public final class CDCJobConfiguration implements PipelineJobConfiguration {
    
    private final String jobId;
    
    private final String database;
    
    private final List<String> tableNames;
    
    private final String subscriptionName;
    
    private final String subscriptionMode;
    
    private final String sourceDatabaseType;
    
    private final ShardingSpherePipelineDataSourceConfiguration dataSourceConfig;
    
    private final JobDataNodeLine tablesFirstDataNodes;
    
    private final List<JobDataNodeLine> jobShardingDataNodes;
    
    private final boolean decodeWithTX;
    
    private final int concurrency;
    
    private final int retryTimes;
    
    @Override
    public int getJobShardingCount() {
        return jobShardingDataNodes.size();
    }
}
