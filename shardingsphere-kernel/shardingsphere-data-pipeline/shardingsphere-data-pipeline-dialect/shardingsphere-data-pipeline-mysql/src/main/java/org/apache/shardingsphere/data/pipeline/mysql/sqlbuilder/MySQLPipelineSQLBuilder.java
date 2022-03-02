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

package org.apache.shardingsphere.data.pipeline.mysql.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.AbstractPipelineSQLBuilder;

import java.util.Map;
import java.util.Set;

/**
 * MySQL pipeline SQL builder.
 */
public final class MySQLPipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    public MySQLPipelineSQLBuilder() {
    }
    
    public MySQLPipelineSQLBuilder(final Map<String, Set<String>> shardingColumnsMap) {
        super(shardingColumnsMap);
    }
    
    @Override
    public String getLeftIdentifierQuoteString() {
        return "`";
    }
    
    @Override
    public String getRightIdentifierQuoteString() {
        return "`";
    }
    
    @Override
    public String buildInsertSQL(final DataRecord dataRecord) {
        return super.buildInsertSQL(dataRecord) + buildDuplicateUpdateSQL(dataRecord);
    }
    
    private String buildDuplicateUpdateSQL(final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            Column column = dataRecord.getColumn(i);
            if (column.isPrimaryKey() || isShardingColumn(getShardingColumnsMap(), dataRecord.getTableName(), column.getName())) {
                continue;
            }
            result.append(quote(column.getName())).append("=VALUES(").append(quote(column.getName())).append("),");
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }
    
    private boolean isShardingColumn(final Map<String, Set<String>> shardingColumnsMap,
                                     final String tableName, final String columnName) {
        return shardingColumnsMap.containsKey(tableName)
                && shardingColumnsMap.get(tableName).contains(columnName);
    }
    
    /**
     * Build CRC32 SQL.
     *
     * @param tableName table Name
     * @param column column
     * @return select CRC32 SQL
     */
    public String buildCRC32SQL(final String tableName, final String column) {
        return String.format("SELECT BIT_XOR(CAST(CRC32(%s) AS UNSIGNED)) AS checksum FROM %s", quote(column), quote(tableName));
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
