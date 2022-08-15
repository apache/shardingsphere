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

package org.apache.shardingsphere.data.pipeline.api.config.job;

import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Migration job configuration.
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public final class MigrationJobConfiguration implements PipelineJobConfiguration {
    
    private final String jobId;
    
    private final String databaseName;
    
    private final Integer activeVersion;
    
    private final Integer newVersion;
    
    private final String sourceDatabaseType;
    
    private final String targetDatabaseType;
    
    private final PipelineDataSourceConfiguration source;
    
    private final PipelineDataSourceConfiguration target;
    
    /**
     * Map{altered rule yaml class name, re-shard needed table names}.
     */
    private final Map<String, List<String>> alteredRuleYamlClassNameTablesMap;
    
    /**
     * Map{schema name, logic table names}.
     */
    private final Map<String, List<String>> schemaTablesMap;
    
    private final String logicTables;
    
    /**
     * Collection of each logic table's first data node.
     * <p>
     * If <pre>actualDataNodes: ds_${0..1}.t_order_${0..1}</pre> and <pre>actualDataNodes: ds_${0..1}.t_order_item_${0..1}</pre>,
     * then value may be: {@code t_order:ds_0.t_order_0|t_order_item:ds_0.t_order_item_0}.
     * </p>
     */
    private final String tablesFirstDataNodes;
    
    private final List<String> jobShardingDataNodes;
    
    private final int concurrency;
    
    private final int retryTimes;
    
    /**
     * Get job sharding count.
     *
     * @return job sharding count
     */
    public int getJobShardingCount() {
        return null == jobShardingDataNodes ? 0 : jobShardingDataNodes.size();
    }
    
    /**
     * Split {@linkplain #logicTables} to logic table names.
     *
     * @return logic table names
     */
    public List<String> splitLogicTableNames() {
        return Splitter.on(',').splitToList(logicTables);
    }
    
    @Override
    public String toString() {
        return "MigrationJobConfiguration{"
                + "jobId='" + jobId + '\'' + ", databaseName='" + databaseName + '\''
                + ", activeVersion=" + activeVersion + ", newVersion=" + newVersion
                + ", sourceDatabaseType='" + sourceDatabaseType + '\'' + ", targetDatabaseType='" + targetDatabaseType + '\''
                + '}';
    }
}
