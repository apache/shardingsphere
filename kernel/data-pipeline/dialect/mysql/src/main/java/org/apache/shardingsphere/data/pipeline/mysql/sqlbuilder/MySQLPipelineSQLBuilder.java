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

import java.util.Optional;

/**
 * MySQL pipeline SQL builder.
 */
public final class MySQLPipelineSQLBuilder extends AbstractPipelineSQLBuilder {
    
    @Override
    public String buildInsertSQL(final String schemaName, final DataRecord dataRecord) {
        return super.buildInsertSQL(schemaName, dataRecord) + buildDuplicateUpdateSQL(dataRecord);
    }
    
    private String buildDuplicateUpdateSQL(final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            Column column = dataRecord.getColumn(i);
            // TOOD not skip unique key
            if (column.isUniqueKey()) {
                continue;
            }
            result.append(quote(column.getName())).append("=VALUES(").append(quote(column.getName())).append("),");
        }
        result.setLength(result.length() - 1);
        return result.toString();
    }
    
    @Override
    public Optional<String> buildCRC32SQL(final String schemaName, final String tableName, final String column) {
        return Optional.of(String.format("SELECT BIT_XOR(CAST(CRC32(%s) AS UNSIGNED)) AS checksum, COUNT(1) AS cnt FROM %s", quote(column), quote(tableName)));
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
