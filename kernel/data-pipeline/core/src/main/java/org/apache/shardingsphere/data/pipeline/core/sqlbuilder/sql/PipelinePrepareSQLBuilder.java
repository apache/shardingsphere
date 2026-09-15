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

import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Pipeline prepare SQL builder.
 */
public final class PipelinePrepareSQLBuilder {
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    /**
     * Create a builder for actual identifiers only. Creating a schema requires an endpoint context.
     *
     * @param databaseType database type
     */
    public PipelinePrepareSQLBuilder(final DatabaseType databaseType) {
        this(databaseType, null);
    }
    
    /**
     * Create a builder using the SQL endpoint identifier context.
     *
     * @param databaseType database type
     * @param identifierContext endpoint context, or null for actual identifiers only
     */
    public PipelinePrepareSQLBuilder(final DatabaseType databaseType, @Nullable final DatabaseIdentifierContext identifierContext) {
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType, identifierContext);
    }
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return create schema SQL
     */
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return dialectSQLBuilder.buildCreateSchemaSQL(sqlSegmentBuilder.getEscapedIdentifier(IdentifierScope.SCHEMA, schemaName));
    }
    
    /**
     * Build drop SQL for resolved actual identifiers.
     *
     * @param schemaName actual schema name
     * @param tableName actual table name
     * @return drop SQL
     */
    public String buildDropSQL(final String schemaName, final String tableName) {
        return String.format("DROP TABLE IF EXISTS %s", sqlSegmentBuilder.getQualifiedActualTableName(schemaName, tableName));
    }
    
    /**
     * Build count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return count SQL
     */
    public String buildCountSQL(final String schemaName, final String tableName) {
        return String.format("SELECT COUNT(*) FROM %s", sqlSegmentBuilder.getQualifiedActualTableName(schemaName, tableName));
    }
    
    /**
     * Build estimated count SQL.
     *
     * @param catalogName catalog name
     * @param schemaName schema name
     * @param tableName table name
     * @return estimated count SQL
     */
    public Optional<String> buildEstimatedCountSQL(final String catalogName, final String schemaName, final String tableName) {
        return dialectSQLBuilder.buildEstimatedCountSQL(catalogName, sqlSegmentBuilder.getQualifiedActualTableName(schemaName, tableName));
    }
    
    /**
     * Build unique key minimum maximum values SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @return min max unique key SQL
     */
    public String buildUniqueKeyMinMaxValuesSQL(final String schemaName, final String tableName, final String uniqueKey) {
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedActualIdentifier(uniqueKey);
        return String.format("SELECT MIN(%s), MAX(%s) FROM %s", escapedUniqueKey, escapedUniqueKey, sqlSegmentBuilder.getQualifiedActualTableName(schemaName, tableName));
    }
    
    /**
     * Build check empty table SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return check SQL
     */
    public String buildCheckEmptyTableSQL(final String schemaName, final String tableName) {
        return dialectSQLBuilder.buildCheckEmptyTableSQL(sqlSegmentBuilder.getQualifiedActualTableName(schemaName, tableName));
    }
    
    /**
     * Build split by unique key ranged SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @param hasLowerBound has lower bound
     * @return split SQL
     */
    public String buildSplitByUniqueKeyRangedSQL(final String schemaName, final String tableName, final String uniqueKey, final boolean hasLowerBound) {
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedActualIdentifier(uniqueKey);
        String subQueryClause = dialectSQLBuilder.buildSplitByUniqueKeyRangedSubqueryClause(sqlSegmentBuilder.getQualifiedActualTableName(schemaName, tableName), escapedUniqueKey, hasLowerBound);
        return String.format("SELECT MAX(%s), COUNT(1), MIN(%s) FROM (%s) t", escapedUniqueKey, escapedUniqueKey, subQueryClause);
    }
}
