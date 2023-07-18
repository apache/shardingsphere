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

package org.apache.shardingsphere.data.pipeline.api.config.ingest;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.ColumnName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dumper configuration.
 */
@Getter
@Setter
@ToString(exclude = {"dataSourceConfig", "tableNameSchemaNameMapping"})
// TODO it should be final and not extends by sub-class
// TODO fields final
public class DumperConfiguration {
    
    private String jobId;
    
    private String dataSourceName;
    
    private PipelineDataSourceConfiguration dataSourceConfig;
    
    private IngestPosition position;
    
    private Map<ActualTableName, LogicTableName> tableNameMap;
    
    private TableNameSchemaNameMapping tableNameSchemaNameMapping;
    
    // LinkedHashSet is required
    @Getter(AccessLevel.PROTECTED)
    private Map<LogicTableName, Set<ColumnName>> targetTableColumnsMap;
    
    private boolean decodeWithTX;
    
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
     * @return schema name. nullable
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
    public Optional<List<String>> getColumnNames(final LogicTableName logicTableName) {
        Set<ColumnName> columnNames = null != targetTableColumnsMap ? targetTableColumnsMap.get(logicTableName) : null;
        if (null == columnNames) {
            return Optional.empty();
        }
        return Optional.of(columnNames.stream().map(ColumnName::getOriginal).collect(Collectors.toList()));
    }
    
    /**
     * Get column name set of table.
     *
     * @param actualTableName actual table name
     * @return column names of table
     */
    public Optional<Set<ColumnName>> getColumnNameSet(final String actualTableName) {
        Set<ColumnName> result = null != targetTableColumnsMap ? targetTableColumnsMap.get(getLogicTableName(actualTableName)) : null;
        return Optional.ofNullable(result);
    }
}
