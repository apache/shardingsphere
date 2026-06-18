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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Pipeline import SQL builder engine.
 */
public final class PipelineImportSQLBuilder {
    
    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    private final Cache<String, String> sqlCache;
    
    public PipelineImportSQLBuilder(final DatabaseType databaseType) {
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
        sqlCache = Caffeine.newBuilder().initialCapacity(16).maximumSize(1024L).build();
    }
    
    /**
     * Build insert SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @return insert SQL
     */
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord) {
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (null == sqlCache.getIfPresent(sqlCacheKey)) {
            sqlCache.put(sqlCacheKey, buildInsertSQL0(schemaName, dataRecord));
        }
        return sqlCache.getIfPresent(sqlCacheKey);
    }
    
    private String buildInsertSQL0(final String schemaName, final DataRecord dataRecord) {
        String insertMainClause = buildInsertMainClause(schemaName, dataRecord);
        return dialectSQLBuilder.buildInsertOnDuplicateClause(dataRecord).map(optional -> insertMainClause + " " + optional).orElse(insertMainClause);
    }
    
    private String buildInsertMainClause(final String schemaName, final DataRecord dataRecord) {
        String columnsLiteral = dataRecord.getColumns().stream().map(each -> sqlSegmentBuilder.getEscapedIdentifier(each.getName())).collect(Collectors.joining(","));
        String valuesLiteral = dataRecord.getColumns().stream().map(each -> "?").collect(Collectors.joining(","));
        return String.format("INSERT INTO %s(%s) VALUES(%s)", sqlSegmentBuilder.getQualifiedTableName(schemaName, dataRecord.getTableName()), columnsLiteral, valuesLiteral);
    }
    
    /**
     * Build update SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return update SQL
     */
    public String buildUpdateSQL(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (null == sqlCache.getIfPresent(sqlCacheKey)) {
            sqlCache.put(sqlCacheKey, buildUpdateSQL0(schemaName, dataRecord, conditionColumns));
        }
        Collection<Column> setColumns = dataRecord.getColumns().stream().filter(Column::isUpdated).collect(Collectors.toList());
        String updateSetClause = setColumns.stream().map(each -> sqlSegmentBuilder.getEscapedIdentifier(each.getName()) + " = ?").collect(Collectors.joining(","));
        return String.format(Objects.requireNonNull(sqlCache.getIfPresent(sqlCacheKey)), updateSetClause);
    }
    
    private String buildUpdateSQL0(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String updateMainClause = String.format("UPDATE %s SET %%s", sqlSegmentBuilder.getQualifiedTableName(schemaName, dataRecord.getTableName()));
        return buildWhereClause(conditionColumns).map(optional -> updateMainClause + optional).orElse(updateMainClause);
    }
    
    /**
     * Build delete SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return delete SQL
     */
    public String buildDeleteSQL(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX + dataRecord.getTableName();
        if (null == sqlCache.getIfPresent(sqlCacheKey)) {
            sqlCache.put(sqlCacheKey, buildDeleteSQL0(schemaName, dataRecord, conditionColumns));
        }
        return sqlCache.getIfPresent(sqlCacheKey);
    }
    
    private String buildDeleteSQL0(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String deleteMainClause = buildDeleteMainClause(schemaName, dataRecord);
        return buildWhereClause(conditionColumns).map(optional -> deleteMainClause + optional).orElse(deleteMainClause);
    }
    
    private String buildDeleteMainClause(final String schemaName, final DataRecord dataRecord) {
        return String.format("DELETE FROM %s", sqlSegmentBuilder.getQualifiedTableName(schemaName, dataRecord.getTableName()));
    }
    
    private Optional<String> buildWhereClause(final Collection<Column> conditionColumns) {
        return conditionColumns.isEmpty()
                ? Optional.empty()
                : Optional.of(" WHERE " + conditionColumns.stream().map(each -> sqlSegmentBuilder.getEscapedIdentifier(each.getName()) + " = ?").collect(Collectors.joining(" AND ")));
    }
}
