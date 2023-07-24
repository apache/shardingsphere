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

package org.apache.shardingsphere.data.pipeline.common.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Pipeline data consistency calculate SQL builder.
 */
public final class PipelineDataConsistencyCalculateSQLBuilder {
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    public PipelineDataConsistencyCalculateSQLBuilder(final DatabaseType databaseType) {
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
    }
    
    /**
     * Build query all ordering SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key, it may be primary key, not null
     * @param firstQuery first query
     * @return built SQL
     */
    public String buildQueryAllOrderingSQL(final String schemaName, final String tableName, final Collection<String> columnNames, final String uniqueKey, final boolean firstQuery) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        String queryColumns = columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
        return firstQuery
                ? String.format("SELECT %s FROM %s ORDER BY %s ASC", queryColumns, qualifiedTableName, escapedUniqueKey)
                : String.format("SELECT %s FROM %s WHERE %s>? ORDER BY %s ASC", queryColumns, qualifiedTableName, escapedUniqueKey, escapedUniqueKey);
    }
    
    /**
     * Build CRC32 SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnName column name
     * @return built SQL
     */
    public Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String columnName) {
        return dialectSQLBuilder.buildCRC32SQL(sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName), sqlSegmentBuilder.getEscapedIdentifier(columnName));
    }
}
