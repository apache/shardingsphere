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
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.DialectPipelineSQLBuilder;

import java.util.Optional;

/**
 * MySQL pipeline SQL builder.
 */
public final class MySQLPipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public Optional<String> buildInsertOnDuplicateClause(final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder("ON DUPLICATE KEY UPDATE ");
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(getType());
        for (int i = 0; i < dataRecord.getColumnCount(); i++) {
            Column column = dataRecord.getColumn(i);
            if (!column.isUpdated()) {
                continue;
            }
            // TOOD not skip unique key
            if (column.isUniqueKey()) {
                continue;
            }
            result.append(sqlSegmentBuilder.getEscapedIdentifier(column.getName())).append("=VALUES(").append(sqlSegmentBuilder.getEscapedIdentifier(column.getName())).append("),");
        }
        result.setLength(result.length() - 1);
        return Optional.of(result.toString());
    }
    
    @Override
    public String buildCheckEmptySQL(final String qualifiedTableName) {
        return String.format("SELECT * FROM %s LIMIT 1", qualifiedTableName);
    }
    
    @Override
    public Optional<String> buildEstimatedCountSQL(final String qualifiedTableName) {
        return Optional.of(String.format("SELECT TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = '%s'", qualifiedTableName));
    }
    
    @Override
    public Optional<String> buildCRC32SQL(final String qualifiedTableName, final String columnName) {
        return Optional.of(String.format("SELECT BIT_XOR(CAST(CRC32(%s) AS UNSIGNED)) AS checksum, COUNT(1) AS cnt FROM %s", columnName, qualifiedTableName));
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
