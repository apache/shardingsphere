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

package org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.context.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;

import java.util.Map;

/**
 * Base dumper context.
 */
@Getter
@Setter
@ToString(exclude = {"dataSourceConfig", "tableAndSchemaNameMapper"})
public abstract class DumperCommonContext {
    
    private String dataSourceName;
    
    private PipelineDataSourceConfiguration dataSourceConfig;
    
    private Map<ActualTableName, LogicTableName> tableNameMap;
    
    private TableAndSchemaNameMapper tableAndSchemaNameMapper;
    
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
        return tableAndSchemaNameMapper.getSchemaName(logicTableName);
    }
    
    /**
     * Get schema name.
     *
     * @param actualTableName actual table name
     * @return schema name, can be nullable 
     */
    public String getSchemaName(final ActualTableName actualTableName) {
        return tableAndSchemaNameMapper.getSchemaName(getLogicTableName(actualTableName));
    }
}
