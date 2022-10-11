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

package org.apache.shardingsphere.data.pipeline.api.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Importer configuration.
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString(exclude = "dataSourceConfig")
public final class ImporterConfiguration {
    
    private final PipelineDataSourceConfiguration dataSourceConfig;
    
    // TODO columnName case-insensitive?
    private final Map<LogicTableName, Set<String>> shardingColumnsMap;
    
    private final TableNameSchemaNameMapping tableNameSchemaNameMapping;
    
    private final int batchSize;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final int retryTimes;
    
    private final int concurrency;
    
    /**
     * Get logic table names.
     *
     * @return logic table names
     */
    public Collection<String> getLogicTableNames() {
        List<String> result = shardingColumnsMap.keySet().stream().map(LogicTableName::getLowercase).collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }
    
    /**
     * Get sharding columns.
     *
     * @param logicTableName logic table name
     * @return sharding columns
     */
    public Set<String> getShardingColumns(final String logicTableName) {
        return ObjectUtils.defaultIfNull(shardingColumnsMap.get(new LogicTableName(logicTableName)), Collections.emptySet());
    }
    
    /**
     * Get schema name.
     *
     * @param logicTableName logic table name
     * @return schema name. nullable
     */
    public String getSchemaName(final LogicTableName logicTableName) {
        String databaseType = dataSourceConfig.getDatabaseType().getType();
        return DatabaseTypeFactory.getInstance(databaseType).isSchemaAvailable() ? tableNameSchemaNameMapping.getSchemaName(logicTableName) : null;
    }
}
