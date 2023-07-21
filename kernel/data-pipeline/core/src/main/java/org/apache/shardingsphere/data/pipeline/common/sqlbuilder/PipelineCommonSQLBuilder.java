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

import java.util.Optional;

/**
 * Pipeline common SQL builder.
 */
public final class PipelineCommonSQLBuilder {
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    public PipelineCommonSQLBuilder(final DatabaseType databaseType) {
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
    }
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return create schema SQL
     */
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return dialectSQLBuilder.buildCreateSchemaSQL(sqlSegmentBuilder.getEscapedIdentifier(schemaName));
    }
    
    /**
     * Build drop SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return drop SQL
     */
    public String buildDropSQL(final String schemaName, final String tableName) {
        return String.format("DROP TABLE IF EXISTS %s", sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
    }
    
    /**
     * Build count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return count SQL
     */
    public String buildCountSQL(final String schemaName, final String tableName) {
        return String.format("SELECT COUNT(*) FROM %s", sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
    }
    
    /**
     * Build estimated count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return estimated count SQL
     */
    public Optional<String> buildEstimatedCountSQL(final String schemaName, final String tableName) {
        return dialectSQLBuilder.buildEstimatedCountSQL(sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
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
        String escapedUniqueKey = sqlSegmentBuilder.getEscapedIdentifier(uniqueKey);
        return String.format("SELECT MIN(%s), MAX(%s) FROM %s", escapedUniqueKey, escapedUniqueKey, sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
    }
    
    /**
     * Build check empty SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return check SQL
     */
    public String buildCheckEmptySQL(final String schemaName, final String tableName) {
        return dialectSQLBuilder.buildCheckEmptySQL(sqlSegmentBuilder.getQualifiedTableName(schemaName, tableName));
    }
}
