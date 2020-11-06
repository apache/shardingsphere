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

package org.apache.shardingsphere.scaling.mysql;

import org.apache.shardingsphere.scaling.core.execute.executor.importer.AbstractSQLBuilder;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.PreparedSQL;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MySQL SQL builder.
 */
public final class MySQLSQLBuilder extends AbstractSQLBuilder {
    
    public MySQLSQLBuilder(final Map<String, Set<String>> shardingColumnsMap) {
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
    protected PreparedSQL buildInsertSQLInternal(final DataRecord dataRecord) {
        PreparedSQL preparedSQL = super.buildInsertSQLInternal(dataRecord);
        StringBuilder insertSQL = new StringBuilder(preparedSQL.getSql() + " ON DUPLICATE KEY UPDATE ");
        List<Integer> valuesIndex = new ArrayList<>(preparedSQL.getValuesIndex());
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            Column column = dataRecord.getColumn(i);
            if (!dataRecord.getColumn(i).isPrimaryKey()) {
                insertSQL.append(quote(column.getName())).append("=?,");
                valuesIndex.add(i);
            }
        }
        insertSQL.setLength(insertSQL.length() - 1);
        return new PreparedSQL(insertSQL.toString(), valuesIndex);
    }
    
    /**
     * Build select sum crc32 SQL.
     *
     * @param tableName table Name
     * @param column column
     * @return select sum crc32 SQL
     */
    public String buildSumCrc32SQL(final String tableName, final String column) {
        return String.format("SELECT SUM(CRC32(%s)) from %s", quote(column), quote(tableName));
    }
}
