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
import org.apache.shardingsphere.spi.type.typed.StatefulTypedSPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Pipeline SQL builder.
 */
public interface PipelineSQLBuilder extends StatefulTypedSPI {
    
    /**
     * Build insert SQL.
     *
     * @param dataRecord data record
     * @param shardingColumnsMap sharding columns map
     * @return insert SQL
     */
    String buildInsertSQL(DataRecord dataRecord, Map<LogicTableName, Set<String>> shardingColumnsMap);
    
    /**
     * Build update SQL.
     *
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @param shardingColumnsMap sharding columns map
     * @return update SQL
     */
    String buildUpdateSQL(DataRecord dataRecord, Collection<Column> conditionColumns, Map<LogicTableName, Set<String>> shardingColumnsMap);
    
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
     * @param dataRecord data record
     * @param conditionColumns condition columns
     * @return delete SQL
     */
    String buildDeleteSQL(DataRecord dataRecord, Collection<Column> conditionColumns);
    
    /**
     * Build truncate SQL.
     *
     * @param tableName table name
     * @return truncate SQL
     */
    String buildTruncateSQL(String tableName);
    
    /**
     * Build count SQL.
     *
     * @param tableName table name
     * @return count SQL
     */
    String buildCountSQL(String tableName);
    
    /**
     * Build query SQL.
     *
     * @param tableName table name
     * @param uniqueKey unique key, it may be primary key, not null
     * @param startUniqueValue start unique value, not null
     * @return query SQL
     */
    String buildChunkedQuerySQL(String tableName, String uniqueKey, Number startUniqueValue);
    
    /**
     * Build check empty SQL.
     *
     * @param tableName table name
     * @return check SQL
     */
    String buildCheckEmptySQL(String tableName);
    
    /**
     * Build split by primary key range SQL.
     *
     * @param tableName table name
     * @param primaryKey primary key
     * @return split SQL
     */
    String buildSplitByPrimaryKeyRangeSQL(String tableName, String primaryKey);
    
    /**
     * Build CRC32 SQL.
     *
     * @param tableName table Name
     * @param column column
     * @return CRC32 SQL
     */
    default Optional<String> buildCRC32SQL(final String tableName, final String column) {
        return Optional.empty();
    }
}
