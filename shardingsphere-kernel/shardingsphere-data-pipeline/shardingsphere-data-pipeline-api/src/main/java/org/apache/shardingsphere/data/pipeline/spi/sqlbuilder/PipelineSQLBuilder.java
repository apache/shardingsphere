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
import org.apache.shardingsphere.spi.type.typed.TypedSPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Pipeline SQL builder.
 */
public interface PipelineSQLBuilder extends TypedSPI {
    
    /**
     * Build create schema SQL.
     *
     * @param schemaName schema name
     * @return create schema SQL
     */
    default String buildCreateSchemaSQL(String schemaName) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Build inventory dump first SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @param uniqueKey unique key
     * @param uniqueKeyDataType unique key data type
     * @param firstQuery whether it's the first time query
     * @return inventory dump SQL
     */
    String buildInventoryDumpSQL(String schemaName, String tableName, String uniqueKey, int uniqueKeyDataType, boolean firstQuery);
    
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
     * Build truncate SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return truncate SQL
     */
    String buildTruncateSQL(String schemaName, String tableName);
    
    /**
     * Build count SQL.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return count SQL
     */
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
     * @return CRC32 SQL
     */
    default Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String column) {
        return Optional.empty();
    }
}
