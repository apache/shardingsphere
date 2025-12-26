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

import org.apache.shardingsphere.data.pipeline.core.exception.job.CreateTableSQLGenerateException;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
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
            result.append(sqlSegmentBuilder.getEscapedIdentifier(column.getName())).append("=VALUES(").append(sqlSegmentBuilder.getEscapedIdentifier(column.getName())).append("),");
        }
        result.setLength(result.length() - 1);
        return Optional.of(result.toString());
    }
    
    @Override
    public String buildCheckEmptyTableSQL(final String qualifiedTableName) {
        return String.format("SELECT * FROM %s LIMIT 1", qualifiedTableName);
    }
    
    @Override
    public Optional<String> buildEstimatedCountSQL(final String catalogName, final String qualifiedTableName) {
        return Optional.of(String.format("SELECT TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'", catalogName, qualifiedTableName));
    }
    
    @Override
    public Optional<String> buildCRC32SQL(final String qualifiedTableName, final String columnName) {
        return Optional.of(String.format("SELECT BIT_XOR(CAST(CRC32(%s) AS UNSIGNED)) AS checksum, COUNT(1) AS cnt FROM %s", columnName, qualifiedTableName));
    }
    
    @Override
    public String buildSplitByUniqueKeyRangedSubqueryClause(final String qualifiedTableName, final String uniqueKey, final boolean hasLowerValue) {
        return hasLowerValue
                ? String.format("SELECT %s FROM %s WHERE %s>? ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey, uniqueKey)
                : String.format("SELECT %s FROM %s ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey);
    }
    
    @Override
    public Collection<String> buildCreateTableSQLs(final DataSource dataSource, final String schemaName, final String tableName) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("SHOW CREATE TABLE %s", tableName))) {
            if (resultSet.next()) {
                return Collections.singleton(resultSet.getString("create table"));
            }
        }
        throw new CreateTableSQLGenerateException(tableName);
    }
    
    @Override
    public String wrapWithPageQuery(final String sql) {
        return sql + " LIMIT ?";
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
