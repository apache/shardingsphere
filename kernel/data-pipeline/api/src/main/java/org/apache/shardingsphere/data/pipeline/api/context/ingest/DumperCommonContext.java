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

package org.apache.shardingsphere.data.pipeline.api.context.ingest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.context.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.ColumnName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dumper common context.
 */
@Getter
@Setter
@ToString(exclude = {"dataSourceConfig", "tableNameSchemaNameMapping"})
public final class DumperCommonContext {
    
    private String dataSourceName;
    
    private PipelineDataSourceConfiguration dataSourceConfig;
    
    private Map<ActualTableName, LogicTableName> tableNameMap;
    
    private TableNameSchemaNameMapping tableNameSchemaNameMapping;
    
    // LinkedHashSet is required
    private Map<LogicTableName, Collection<ColumnName>> targetTableColumnsMap = new HashMap<>();
    
    private IngestPosition position;
    
    /**
     * Get logic table name.
     *
     * @param actualTableName actual table name
     * @return logic table name
     */
    public LogicTableName getLogicTableName(final String actualTableName) {
        return tableNameMap.get(new ActualTableName(actualTableName));
    }
    
    private LogicTableName getLogicTableName(final ActualTableName actualTableName) {
        return tableNameMap.get(actualTableName);
    }
    
    /**
     * Whether contains table.
     *
     * @param actualTableName actual table name
     * @return contains or not
     */
    public boolean containsTable(final String actualTableName) {
        return tableNameMap.containsKey(new ActualTableName(actualTableName));
    }
    
    /**
     * Get schema name.
     *
     * @param logicTableName logic table name
     * @return schema name. nullable
     */
    public String getSchemaName(final LogicTableName logicTableName) {
        return tableNameSchemaNameMapping.getSchemaName(logicTableName);
    }
    
    /**
     * Get schema name.
     *
     * @param actualTableName actual table name
     * @return schema name, can be nullable 
     */
    public String getSchemaName(final ActualTableName actualTableName) {
        return tableNameSchemaNameMapping.getSchemaName(getLogicTableName(actualTableName));
    }
    
    /**
     * Get column names.
     *
     * @param logicTableName logic table name
     * @return column names
     */
    public Collection<String> getColumnNames(final LogicTableName logicTableName) {
        return targetTableColumnsMap.containsKey(logicTableName)
                ? targetTableColumnsMap.get(logicTableName).stream().map(ColumnName::getOriginal).collect(Collectors.toList())
                : Collections.singleton("*");
    }
    
    /**
     * Get column names.
     *
     * @param actualTableName actual table name
     * @return column names
     */
    public Collection<ColumnName> getColumnNames(final String actualTableName) {
        return targetTableColumnsMap.getOrDefault(getLogicTableName(actualTableName), Collections.emptySet());
    }
}
