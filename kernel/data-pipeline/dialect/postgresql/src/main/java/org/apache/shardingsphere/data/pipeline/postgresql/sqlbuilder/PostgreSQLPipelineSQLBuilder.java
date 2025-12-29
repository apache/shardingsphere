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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.column.PostgreSQLColumnPropertiesAppender;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.constraints.PostgreSQLConstraintsPropertiesAppender;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.index.PostgreSQLIndexSQLGenerator;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.table.PostgreSQLTablePropertiesLoader;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.template.PostgreSQLPipelineFreemarkerManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PostgreSQL pipeline SQL builder.
 */
public final class PostgreSQLPipelineSQLBuilder implements DialectPipelineSQLBuilder {
    
    @Override
    public Optional<String> buildCreateSchemaSQL(final String schemaName) {
        return Optional.of(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
    }
    
    @Override
    public Optional<String> buildInsertOnDuplicateClause(final DataRecord dataRecord) {
        // TODO without unique key, job has been interrupted, which may lead to data duplication
        if (dataRecord.getUniqueKeyValue().isEmpty()) {
            return Optional.empty();
        }
        StringBuilder result = new StringBuilder("ON CONFLICT (");
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(getType());
        result.append(dataRecord.getColumns().stream().filter(Column::isUniqueKey).map(each -> sqlSegmentBuilder.getEscapedIdentifier(each.getName())).collect(Collectors.joining(",")));
        result.append(") DO UPDATE SET ");
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
    
    // TODO support partitions etc. If user use partition table, after sharding, the partition definition will not be needed. So we need to remove it after supported.
    @Override
    public Optional<String> buildCRC32SQL(final String qualifiedTableName, final String columnName) {
        return Optional.of(String.format("SELECT pg_catalog.pg_checksum_table('%s', true)", qualifiedTableName));
    }
    
    @Override
    public String buildSplitByUniqueKeyRangedSubqueryClause(final String qualifiedTableName, final String uniqueKey, final boolean hasLowerBound) {
        return hasLowerBound
                ? String.format("SELECT %s FROM %s WHERE %s>? ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey, uniqueKey)
                : String.format("SELECT %s FROM %s ORDER BY %s LIMIT ?", uniqueKey, qualifiedTableName, uniqueKey);
    }
    
    @Override
    public Collection<String> buildCreateTableSQLs(final DataSource dataSource, final String schemaName, final String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
            int minorVersion = connection.getMetaData().getDatabaseMinorVersion();
            Map<String, Object> materials = loadMaterials(tableName, schemaName, connection, majorVersion, minorVersion);
            String tableSQL = generateCreateTableSQL(majorVersion, minorVersion, materials);
            String indexSQL = generateCreateIndexSQL(connection, majorVersion, minorVersion, materials);
            // TODO use ";" to split is not always correct if return value's comments contains ";"
            return Arrays.asList((tableSQL + System.lineSeparator() + indexSQL).trim().split(";"));
        }
    }
    
    private Map<String, Object> loadMaterials(final String tableName, final String schemaName, final Connection connection, final int majorVersion, final int minorVersion) throws SQLException {
        Map<String, Object> result = new PostgreSQLTablePropertiesLoader(connection, tableName, schemaName, majorVersion, minorVersion).load();
        new PostgreSQLColumnPropertiesAppender(connection, majorVersion, minorVersion).append(result);
        new PostgreSQLConstraintsPropertiesAppender(connection, majorVersion, minorVersion).append(result);
        formatColumns(result);
        return result;
    }
    
    private String generateCreateTableSQL(final int majorVersion, final int minorVersion, final Map<String, Object> materials) {
        return PostgreSQLPipelineFreemarkerManager.getSQLByVersion(materials, "component/table/%s/create.ftl", majorVersion, minorVersion).trim();
    }
    
    private String generateCreateIndexSQL(final Connection connection, final int majorVersion, final int minorVersion, final Map<String, Object> materials) throws SQLException {
        return new PostgreSQLIndexSQLGenerator(connection, majorVersion, minorVersion).generate(materials);
    }
    
    @SuppressWarnings("unchecked")
    private void formatColumns(final Map<String, Object> context) {
        Collection<Map<String, Object>> columns = (Collection<Map<String, Object>>) context.get("columns");
        for (Map<String, Object> each : columns) {
            if (each.containsKey("cltype")) {
                typeFormatter(each, (String) each.get("cltype"));
            }
        }
    }
    
    private void typeFormatter(final Map<String, Object> column, final String columnType) {
        if (columnType.contains("[]")) {
            column.put("cltype", columnType.substring(0, columnType.length() - 2));
            column.put("hasSqrBracket", true);
        } else {
            column.put("hasSqrBracket", false);
        }
    }
    
    @Override
    public Optional<String> buildQueryCurrentPositionSQL() {
        return Optional.of("SELECT * FROM pg_current_wal_lsn()");
    }
    
    @Override
    public String wrapWithPageQuery(final String sql) {
        return sql + " LIMIT ?";
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
