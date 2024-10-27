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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.range.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveQualifiedTable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Single table inventory calculate parameter.
 */
@RequiredArgsConstructor
@Getter
public final class SingleTableInventoryCalculateParameter {
    
    /**
     * Data source of source side or target side.
     * Do not close it, it will be reused later.
     */
    private final PipelineDataSource dataSource;
    
    private final CaseInsensitiveQualifiedTable table;
    
    private final List<String> columnNames;
    
    /**
     * It could be primary key.
     * It could be used in order by clause.
     */
    private final List<PipelineColumnMetaData> uniqueKeys;
    
    private final AtomicReference<AutoCloseable> calculationContext = new AtomicReference<>();
    
    private final AtomicReference<Collection<Object>> uniqueKeysValues = new AtomicReference<>();
    
    private final AtomicReference<QueryRange> uniqueKeysValuesRange = new AtomicReference<>();
    
    private final AtomicReference<List<String>> shardingColumnsNames = new AtomicReference<>();
    
    private final AtomicReference<List<Object>> shardingColumnsValues = new AtomicReference<>();
    
    private final QueryType queryType;
    
    public SingleTableInventoryCalculateParameter(final PipelineDataSource dataSource, final CaseInsensitiveQualifiedTable table, final List<String> columnNames,
                                                  final List<PipelineColumnMetaData> uniqueKeys, final Object tableCheckPosition) {
        this.dataSource = dataSource;
        this.table = table;
        this.columnNames = columnNames;
        this.uniqueKeys = uniqueKeys;
        queryType = QueryType.RANGE_QUERY;
        setQueryRange(new QueryRange(tableCheckPosition, false, null));
    }
    
    /**
     * Get database type.
     *
     * @return database type
     */
    public DatabaseType getDatabaseType() {
        return dataSource.getDatabaseType();
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public String getSchemaName() {
        return table.getSchemaName().toString();
    }
    
    /**
     * Get logic table name.
     *
     * @return logic table name
     */
    public String getLogicTableName() {
        return table.getTableName().toString();
    }
    
    /**
     * Get first unique key.
     *
     * @return first unique key
     */
    public PipelineColumnMetaData getFirstUniqueKey() {
        return uniqueKeys.get(0);
    }
    
    /**
     * Get calculation context.
     *
     * @return calculation context
     */
    public AutoCloseable getCalculationContext() {
        return calculationContext.get();
    }
    
    /**
     * Set calculation context.
     *
     * @param calculationContext calculation context
     */
    public void setCalculationContext(final AutoCloseable calculationContext) {
        this.calculationContext.set(calculationContext);
    }
    
    /**
     * Get unique keys names.
     *
     * @return unique keys names
     */
    public List<String> getUniqueKeysNames() {
        return uniqueKeys.stream().map(PipelineColumnMetaData::getName).collect(Collectors.toList());
    }
    
    /**
     * Get unique keys values.
     *
     * @return unique keys values
     */
    public Collection<Object> getUniqueKeysValues() {
        return uniqueKeysValues.get();
    }
    
    /**
     * Set unique keys values.
     *
     * @param uniqueKeysValues unique keys values
     */
    public void setUniqueKeysValues(final Collection<Object> uniqueKeysValues) {
        this.uniqueKeysValues.set(uniqueKeysValues);
    }
    
    /**
     * Get query range.
     *
     * @return query range
     */
    public QueryRange getQueryRange() {
        return uniqueKeysValuesRange.get();
    }
    
    /**
     * Set query range.
     *
     * @param queryRange query range
     */
    public void setQueryRange(final QueryRange queryRange) {
        this.uniqueKeysValuesRange.set(queryRange);
    }
    
    /**
     * Get sharding columns names.
     *
     * @return sharding columns names
     */
    public @Nullable List<String> getShardingColumnsNames() {
        return shardingColumnsNames.get();
    }
    
    /**
     * Set sharding columns names.
     *
     * @param shardingColumnsNames sharding columns names
     */
    public void setShardingColumnsNames(final List<String> shardingColumnsNames) {
        this.shardingColumnsNames.set(shardingColumnsNames);
    }
    
    /**
     * Get sharding columns values.
     *
     * @return sharding columns values
     */
    public @Nullable List<Object> getShardingColumnsValues() {
        return shardingColumnsValues.get();
    }
    
    /**
     * Set sharding columns values.
     *
     * @param shardingColumnsValues sharding columns values
     */
    public void setShardingColumnsValues(final List<Object> shardingColumnsValues) {
        this.shardingColumnsValues.set(shardingColumnsValues);
    }
}
