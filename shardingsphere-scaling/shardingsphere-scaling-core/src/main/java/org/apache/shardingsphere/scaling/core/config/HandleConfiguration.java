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

package org.apache.shardingsphere.scaling.core.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Handle configuration.
 */
@NoArgsConstructor
@Getter
@Setter
@ToString
public final class HandleConfiguration {
    
    private Long jobId;
    
    private int concurrency = 3;
    
    private int retryTimes = 3;
    
    /**
     * Collection of each logic table's first data node.
     * <p>
     * If <pre>actualDataNodes: ds_${0..1}.t_order_${0..1}</pre> and <pre>actualDataNodes: ds_${0..1}.t_order_item_${0..1}</pre>,
     * then value may be: {@code ds_0.t_order_0,ds_0.t_order_item_0}.
     * </p>
     */
    private String tablesFirstDataNodes;
    
    private List<String> jobShardingDataNodes;
    
    private String logicTables;
    
    /**
     * Job sharding item, from {@link org.apache.shardingsphere.elasticjob.api.ShardingContext}.
     */
    private Integer jobShardingItem;
    
    private int shardingSize = 1000 * 10000;
    
    private String databaseType;
    
    private WorkflowConfiguration workflowConfig;
    
    public HandleConfiguration(final WorkflowConfiguration workflowConfig) {
        this.workflowConfig = workflowConfig;
    }
    
    /**
     * Get job sharding count.
     *
     * @return job sharding count
     */
    public int getJobShardingCount() {
        return null == jobShardingDataNodes ? 0 : jobShardingDataNodes.size();
    }
}
