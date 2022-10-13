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
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Pipeline SQL builder.
 */
public interface PipelineSQLBuilder extends TypedSPI, RequiredSPI {
    
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
     * Build divisible inventory dump first SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @param uniqueKeyDataType unique key data type
     * @param firstQuery whether it's the first time query
     * @return divisible inventory dump SQL
     */
    String buildDivisibleInventoryDumpSQL(String schemaName, String tableName, String uniqueKey, int uniqueKeyDataType, boolean firstQuery);
    
    /**
     * Build indivisible inventory dump first SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @param uniqueKeyDataType unique key data type
     * @param firstQuery whether it's the first time query
     * @return indivisible inventory dump SQL
     */
    String buildIndivisibleInventoryDumpSQL(String schemaName, String tableName, String uniqueKey, int uniqueKeyDataType, boolean firstQuery);
    
    /**
     * Build insert SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param shardingColumnsMap sharding columns map
     * @return insert SQL
     */
    String buildInsertSQL(String schemaName, DataRecord dataRecord, Map<LogicTableName, Set<String>> shardingColumnsMap);
    
    /**
     * Build update SQL.
     *
     * @param schemaName schema name
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @param shardingColumnsMap sharding columns map
     * @return update SQL
     */
    String buildUpdateSQL(String schemaName, DataRecord dataRecord, Collection<Column> conditionColumns, Map<LogicTableName, Set<String>> shardingColumnsMap);
    
    /**
     * Extract updated columns.
     *
     * @param record data record
     * @param shardingColumnsMap sharding columns map
     * @return filtered columns
     */
    List<Column> extractUpdatedColumns(DataRecord record, Map<LogicTableName, Set<String>> shardingColumnsMap);
    
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
    // TODO keep it for now, it might be used later
    String buildCountSQL(String schemaName, String tableName);
    
    /**
     * Build query SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key, it may be primary key, not null
     * @param firstQuery first query
     * @return query SQL
     */
    String buildChunkedQuerySQL(String schemaName, String tableName, String uniqueKey, boolean firstQuery);
    
    /**
     * Build query unique key SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key, it may be primary key, not null
     * @param firstQuery first query
     * @return query unique key SQL
     */
    String buildGetMaxUniqueKeyValueSQL(String schemaName, String tableName, String uniqueKey, boolean firstQuery);
    
    /**
     * Build check empty SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return check SQL
     */
    String buildCheckEmptySQL(String schemaName, String tableName);
    
    /**
     * Build split by primary key range SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param primaryKey primary key
     * @return split SQL
     */
    String buildSplitByPrimaryKeyRangeSQL(String schemaName, String tableName, String primaryKey);
    
    /**
     * Build CRC32 SQL.
     *
     * @param schemaName schema name
     * @param tableName table Name
     * @param column column
     * @param uniqueKey unique key
     * @return CRC32 SQL
     */
    default Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String column, final String uniqueKey) {
        return Optional.empty();
    }
}
