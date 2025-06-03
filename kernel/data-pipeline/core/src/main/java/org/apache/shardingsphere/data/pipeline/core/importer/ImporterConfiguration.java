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

package org.apache.shardingsphere.data.pipeline.core.importer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Importer configuration.
 */
@RequiredArgsConstructor
@Getter
@ToString(exclude = {"dataSourceConfig", "tableAndSchemaNameMapper"})
public final class ImporterConfiguration {
    
    private final PipelineDataSourceConfiguration dataSourceConfig;
    
    @Getter(AccessLevel.NONE)
    private final Map<ShardingSphereIdentifier, Set<String>> shardingColumnsMap;
    
    private final TableAndSchemaNameMapper tableAndSchemaNameMapper;
    
    private final int batchSize;
    
    private final JobRateLimitAlgorithm rateLimitAlgorithm;
    
    private final int retryTimes;
    
    private final int concurrency;
    
    /**
     * Get sharding columns.
     *
     * @param logicTableName logic table name
     * @return sharding columns
     */
    public Set<String> getShardingColumns(final String logicTableName) {
        return shardingColumnsMap.getOrDefault(new ShardingSphereIdentifier(logicTableName), Collections.emptySet());
    }
    
    /**
     * Find schema name.
     *
     * @param logicTableName logic table name
     * @return schema name
     */
    public Optional<String> findSchemaName(final String logicTableName) {
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(dataSourceConfig.getDatabaseType()).getDialectDatabaseMetaData();
        return dialectDatabaseMetaData.getSchemaOption().isSchemaAvailable() ? Optional.ofNullable(tableAndSchemaNameMapper.getSchemaName(logicTableName)) : Optional.empty();
    }
    
    /**
     * Get qualified tables.
     *
     * @return qualified tables
     */
    public Collection<QualifiedTable> getQualifiedTables() {
        return shardingColumnsMap.keySet().stream()
                .map(ShardingSphereIdentifier::getValue).map(each -> new QualifiedTable(tableAndSchemaNameMapper.getSchemaName(each), each)).collect(Collectors.toList());
    }
}
