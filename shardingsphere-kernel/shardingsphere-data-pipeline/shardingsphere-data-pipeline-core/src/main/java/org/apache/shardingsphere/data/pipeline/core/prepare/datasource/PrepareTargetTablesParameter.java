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

package org.apache.shardingsphere.data.pipeline.core.prepare.datasource;

import lombok.Getter;
import lombok.NonNull;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Prepare target tables parameter.
 */
@Getter
public final class PrepareTargetTablesParameter {
    
    private final String databaseName;
    
    private final JobDataNodeLine tablesFirstDataNodes;
    
    private final PipelineDataSourceConfiguration targetDataSourceConfig;
    
    private final Map<String, DataSource> sourceDataSourceMap;
    
    private final PipelineDataSourceManager dataSourceManager;
    
    private final Map<String, String> tableNameMap;
    
    private final TableNameSchemaNameMapping tableNameSchemaNameMapping;
    
    private final ShardingSphereSQLParserEngine sqlParserEngine;
    
    public PrepareTargetTablesParameter(@NonNull final String databaseName, @NonNull final PipelineDataSourceConfiguration targetDataSourceConfig,
                                        @NonNull final Map<String, DataSource> sourceDataSourceMap, @NonNull final PipelineDataSourceManager dataSourceManager,
                                        @NonNull final JobDataNodeLine tablesFirstDataNodes, final Map<String, String> tableNameMap, final TableNameSchemaNameMapping tableNameSchemaNameMapping,
                                        @NonNull final ShardingSphereSQLParserEngine sqlParserEngine) {
        this.databaseName = databaseName;
        this.targetDataSourceConfig = targetDataSourceConfig;
        this.sourceDataSourceMap = sourceDataSourceMap;
        this.tablesFirstDataNodes = tablesFirstDataNodes;
        this.dataSourceManager = dataSourceManager;
        this.tableNameMap = tableNameMap;
        this.tableNameSchemaNameMapping = tableNameSchemaNameMapping;
        this.sqlParserEngine = sqlParserEngine;
    }
}
