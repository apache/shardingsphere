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

package org.apache.shardingsphere.data.pipeline.spi.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.infra.spi.DatabaseTypedSPI;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Pipeline SQL builder.
 */
public interface PipelineSQLBuilder extends DatabaseTypedSPI {
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return create schema SQL
     */
    default Optional<String> buildCreateSchemaSQL(String schemaName) {
        return Optional.empty();
    }
    
    /**
     * Build divisible inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return divisible inventory dump SQL
     */
    String buildDivisibleInventoryDumpSQL(String schemaName, String tableName, List<String> columnNames, String uniqueKey);
    
    /**
     * Build divisible inventory dump SQL without end value.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return divisible inventory dump SQL without end value
     */
    String buildDivisibleInventoryDumpSQLNoEnd(String schemaName, String tableName, List<String> columnNames, String uniqueKey);
    
    /**
     * Build indivisible inventory dump first SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key
     * @return indivisible inventory dump SQL
     */
    String buildIndivisibleInventoryDumpSQL(String schemaName, String tableName, List<String> columnNames, String uniqueKey);
    
    /**
     * Build no unique key inventory dump SQL.
     *
     * @param schemaName schema name
     * @param tableName tableName
     * @return inventory dump all SQL
     */
    String buildNoUniqueKeyInventoryDumpSQL(String schemaName, String tableName);
    
    /**
     * Build insert SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @return insert SQL
     */
    String buildInsertSQL(String schemaName, DataRecord dataRecord);
    
    /**
     * Build update SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return update SQL
     */
    String buildUpdateSQL(String schemaName, DataRecord dataRecord, Collection<Column> conditionColumns);
    
    /**
     * Extract updated columns.
     *
     * @param dataRecord data record
     * @return filtered columns
     */
    // TODO Consider remove extractUpdatedColumns. openGauss has special impl currently
    List<Column> extractUpdatedColumns(DataRecord dataRecord);
    
    /**
     * Build delete SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return delete SQL
     */
    String buildDeleteSQL(String schemaName, DataRecord dataRecord, Collection<Column> conditionColumns);
    
    /**
     * Build drop SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return drop SQL
     */
    String buildDropSQL(String schemaName, String tableName);
    
    /**
     * Build count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return count SQL
     */
    String buildCountSQL(String schemaName, String tableName);
    
    /**
     * Build estimated count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return estimated count SQL
     */
    Optional<String> buildEstimatedCountSQL(String schemaName, String tableName);
    
    /**
     * Build unique key minimum maximum values SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @return min max unique key SQL
     */
    String buildUniqueKeyMinMaxValuesSQL(String schemaName, String tableName, String uniqueKey);
    
    /**
     * Build query all ordering SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param columnNames column names
     * @param uniqueKey unique key, it may be primary key, not null
     * @param firstQuery first query
     * @return query SQL
     */
    String buildQueryAllOrderingSQL(String schemaName, String tableName, List<String> columnNames, String uniqueKey, boolean firstQuery);
    
    /**
     * Build check empty SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return check SQL
     */
    String buildCheckEmptySQL(String schemaName, String tableName);
    
    /**
     * Build CRC32 SQL.
     *
     * @param schemaName schema name
     * @param tableName table Name
     * @param column column
     * @return CRC32 SQL
     */
    default Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String column) {
        return Optional.empty();
    }
}
