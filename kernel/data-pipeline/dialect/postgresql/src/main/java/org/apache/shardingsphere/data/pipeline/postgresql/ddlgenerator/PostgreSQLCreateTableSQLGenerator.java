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

package org.apache.shardingsphere.data.pipeline.postgresql.ddlgenerator;

import org.apache.shardingsphere.data.pipeline.postgresql.util.PostgreSQLPipelineFreemarkerManager;
import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.CreateTableSQLGenerator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Create table SQL generator for PostgreSQL.
 */
public final class PostgreSQLCreateTableSQLGenerator implements CreateTableSQLGenerator {
    
    private static final String DELIMITER = ";";
    
    // TODO support partitions etc.
    @Override
    public Collection<String> generate(final DataSource dataSource, final String schemaName, final String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            int majorVersion = connection.getMetaData().getDatabaseMajorVersion();
            int minorVersion = connection.getMetaData().getDatabaseMinorVersion();
            Map<String, Object> materials = loadMaterials(tableName, schemaName, connection, majorVersion, minorVersion);
            String tableSQL = generateCreateTableSQL(majorVersion, minorVersion, materials);
            String indexSQL = generateCreateIndexSQL(connection, majorVersion, minorVersion, materials);
            // TODO use ";" to split is not always correct
            return Arrays.asList((tableSQL + System.lineSeparator() + indexSQL).trim().split(DELIMITER));
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
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
