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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Pipeline inventory dump SQL builder.
 */
public final class PipelineInventoryDumpSQLBuilder {
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    public PipelineInventoryDumpSQLBuilder(final DatabaseType databaseType) {
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
    }
    
    /**
     * Build fetch all inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName tableName
     * @param columnNames column names
     * @return built SQL
     */
    public String buildFetchAllSQL(final String schemaName, final String tableName, final Collection<String> columnNames) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String queryColumns = columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
        return String.format("SELECT %s FROM %s", queryColumns, qualifiedTableName);
    }
}
