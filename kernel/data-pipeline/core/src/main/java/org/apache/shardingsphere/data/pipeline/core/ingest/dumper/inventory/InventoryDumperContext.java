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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;

import java.util.List;

/**
 * Inventory dumper context.
 */
@Getter
@Setter
@ToString
public final class InventoryDumperContext {
    
    private final DumperCommonContext commonContext;
    
    private String actualTableName;
    
    private String logicTableName;
    
    private List<PipelineColumnMetaData> uniqueKeyColumns;
    
    private List<String> insertColumnNames;
    
    private String querySQL;
    
    private Integer transactionIsolation;
    
    private int shardingItem;
    
    private int batchSize = 1000;
    
    private JobRateLimitAlgorithm rateLimitAlgorithm;
    
    public InventoryDumperContext(final DumperCommonContext commonContext) {
        this.commonContext = new DumperCommonContext(
                commonContext.getDataSourceName(), commonContext.getDataSourceConfig(), commonContext.getTableNameMapper(), commonContext.getTableAndSchemaNameMapper());
    }
    
    /**
     * Has unique key or not.
     *
     * @return true when there's unique key, else false
     */
    public boolean hasUniqueKey() {
        return null != uniqueKeyColumns && !uniqueKeyColumns.isEmpty();
    }
}
