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
import com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Pipeline import SQL builder engine.
 */
@HighFrequencyInvocation
public final class PipelineImportSQLBuilder {
    
    private static final String INSERT_SQL_CACHE_KEY_PREFIX = "INSERT_";
    
    private static final String UPDATE_SQL_CACHE_KEY_PREFIX = "UPDATE_";
    
    private static final String DELETE_SQL_CACHE_KEY_PREFIX = "DELETE_";
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    private final Cache<String, String> sqlCache;
    
    private final boolean actualTableNames;
    
    private final boolean schemaAvailable;
    
    /**
     * Create an import builder with endpoint identifier policies.
     *
     * @param databaseType database type
     * @param identifierContext endpoint identifier context
     * @param actualTableNames whether record table names are resolved actual names; schemas and columns remain references
     */
    public PipelineImportSQLBuilder(final DatabaseType databaseType, final DatabaseIdentifierContext identifierContext, final boolean actualTableNames) {
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType, identifierContext);
        this.actualTableNames = actualTableNames;
        schemaAvailable = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable();
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
        String sqlCacheKey = INSERT_SQL_CACHE_KEY_PREFIX.concat(dataRecord.getTableName());
        if (null == sqlCache.getIfPresent(sqlCacheKey)) {
            sqlCache.put(sqlCacheKey, buildInsertSQL0(schemaName, dataRecord));
        }
        return sqlCache.getIfPresent(sqlCacheKey);
    }
    
    private String buildInsertSQL0(final String schemaName, final DataRecord dataRecord) {
        String insertMainClause = buildInsertMainClause(schemaName, dataRecord);
        return dialectSQLBuilder.buildInsertOnDuplicateClause(dataRecord, sqlSegmentBuilder).map(optional -> String.join(" ", insertMainClause, optional)).orElse(insertMainClause);
    }
    
    private String buildInsertMainClause(final String schemaName, final DataRecord dataRecord) {
        StringJoiner columnsLiteral = new StringJoiner(",");
        StringJoiner valuesLiteral = new StringJoiner(",");
        for (Column each : dataRecord.getColumns()) {
            columnsLiteral.add(sqlSegmentBuilder.getEscapedIdentifier(IdentifierScope.COLUMN, each.getName()));
            valuesLiteral.add("?");
        }
        return String.format("INSERT INTO %s(%s) VALUES(%s)", getQualifiedTableName(schemaName, dataRecord.getTableName()), columnsLiteral, valuesLiteral);
    }
    
    private String getQualifiedTableName(final String schemaName, final String tableName) {
        if (!actualTableNames) {
            return sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName);
        }
        String actualSchemaName = schemaAvailable && !Strings.isNullOrEmpty(schemaName) ? sqlSegmentBuilder.normalizeStorageIdentifier(IdentifierScope.SCHEMA, schemaName) : schemaName;
        return sqlSegmentBuilder.getQualifiedActualTableName(actualSchemaName, tableName);
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
        String sqlCacheKey = UPDATE_SQL_CACHE_KEY_PREFIX.concat(dataRecord.getTableName());
        if (null == sqlCache.getIfPresent(sqlCacheKey)) {
            sqlCache.put(sqlCacheKey, buildUpdateSQL0(schemaName, dataRecord, conditionColumns));
        }
        StringJoiner updateSetClause = new StringJoiner(",");
        for (Column each : dataRecord.getColumns()) {
            if (each.isUpdated()) {
                updateSetClause.add(sqlSegmentBuilder.getEscapedIdentifier(IdentifierScope.COLUMN, each.getName()).concat(" = ?"));
            }
        }
        return String.format(Objects.requireNonNull(sqlCache.getIfPresent(sqlCacheKey)), updateSetClause);
    }
    
    private String buildUpdateSQL0(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String updateMainClause = String.format("UPDATE %s SET %%s", getQualifiedTableName(schemaName, dataRecord.getTableName()));
        return buildWhereClause(conditionColumns).map(optional -> updateMainClause.concat(optional)).orElse(updateMainClause);
    }
    
    private Optional<String> buildWhereClause(final Collection<Column> conditionColumns) {
        if (conditionColumns.isEmpty()) {
            return Optional.empty();
        }
        StringJoiner result = new StringJoiner(" AND ", " WHERE ", "");
        for (Column each : conditionColumns) {
            result.add(sqlSegmentBuilder.getEscapedIdentifier(IdentifierScope.COLUMN, each.getName()).concat(" = ?"));
        }
        return Optional.of(result.toString());
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
        String sqlCacheKey = DELETE_SQL_CACHE_KEY_PREFIX.concat(dataRecord.getTableName());
        if (null == sqlCache.getIfPresent(sqlCacheKey)) {
            sqlCache.put(sqlCacheKey, buildDeleteSQL0(schemaName, dataRecord, conditionColumns));
        }
        return sqlCache.getIfPresent(sqlCacheKey);
    }
    
    private String buildDeleteSQL0(final String schemaName, final DataRecord dataRecord, final Collection<Column> conditionColumns) {
        String deleteMainClause = buildDeleteMainClause(schemaName, dataRecord);
        return buildWhereClause(conditionColumns).map(optional -> deleteMainClause.concat(optional)).orElse(deleteMainClause);
    }
    
    private String buildDeleteMainClause(final String schemaName, final DataRecord dataRecord) {
        return String.format("DELETE FROM %s", getQualifiedTableName(schemaName, dataRecord.getTableName()));
    }
    
}
