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
import org.apache.shardingsphere.data.pipeline.api.metadata.SchemaTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
    private final PipelineDataSourceWrapper dataSource;
    
    private final SchemaTableName table;
    
    private final List<String> columnNames;
    
    /**
     * It could be primary key.
     * It could be used in order by clause.
     */
    private final List<PipelineColumnMetaData> uniqueKeys;
    
    private final Object tableCheckPosition;
    
    private final AtomicReference<AutoCloseable> calculationContext = new AtomicReference<>();
    
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
        return table.getSchemaName().getOriginal();
    }
    
    /**
     * Get logic table name.
     *
     * @return logic table name
     */
    public String getLogicTableName() {
        return table.getTableName().getOriginal();
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
}
