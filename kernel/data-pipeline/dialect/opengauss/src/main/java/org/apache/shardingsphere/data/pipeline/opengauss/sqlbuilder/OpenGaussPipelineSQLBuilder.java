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

package org.apache.shardingsphere.data.pipeline.opengauss.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.core.exception.job.CreateTableSQLGenerateException;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Pipeline SQL builder of openGauss.
 */
public final class OpenGaussPipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return Optional.of(String.format("CREATE SCHEMA %s", schemaName));
    }
    
    @Override
    public Optional<String> buildInsertOnDuplicateClause(final DataRecord dataRecord) {
        StringBuilder result = new StringBuilder("ON DUPLICATE KEY UPDATE ");
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(getType());
        result.append(dataRecord.getColumns().stream()
                .filter(each -> !each.isUniqueKey()).map(each -> sqlSegmentBuilder.getEscapedIdentifier(each.getName()) + "=EXCLUDED." + sqlSegmentBuilder.getEscapedIdentifier(each.getName()))
                .collect(Collectors.joining(",")));
        return Optional.of(result.toString());
    }
    
    @Override
    public String buildCheckEmptyTableSQL(final String qualifiedTableName) {
        return String.format("SELECT * FROM %s LIMIT 1", qualifiedTableName);
    }
    
    @Override
    public Optional<String> buildEstimatedCountSQL(final String catalogName, final String qualifiedTableName) {
        return Optional.of(String.format("SELECT reltuples::integer FROM pg_class WHERE oid='%s'::regclass::oid;", qualifiedTableName));
    }
    
    @Override
    public String buildSplitByUniqueKeyRangedSubqueryClause(final String qualifiedTableName, final String uniqueKey, final boolean hasLowerBound) {
        return hasLowerBound
                ? String.format("SELECT %s FROM %s WHERE %s>? ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey, uniqueKey)
                : String.format("SELECT %s FROM %s ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey);
    }
    
    @Override
    public Collection<String> buildCreateTableSQLs(final DataSource dataSource, final String schemaName, final String tableName) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("SELECT * FROM pg_get_tabledef('%s.%s')", schemaName, tableName))) {
            if (resultSet.next()) {
                // TODO use ";" to split is not always correct if return value's comments contains ";"
                String tableDefinition = resultSet.getString("pg_get_tabledef");
                return Arrays.asList(tableDefinition.split(";"));
            }
        }
        throw new CreateTableSQLGenerateException(tableName);
    }
    
    @Override
    public Optional<String> buildQueryCurrentPositionSQL() {
        return Optional.of("SELECT * FROM pg_current_xlog_location()");
    }
    
    @Override
    public String wrapWithPageQuery(final String sql) {
        return sql + " LIMIT ?";
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
