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

import org.apache.shardingsphere.data.pipeline.spi.ddlgenerator.DialectDDLGenerator;
import org.apache.shardingsphere.data.pipeline.postgresql.util.FreemarkerManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * DDL generator for PostgreSQL.
 */
public final class PostgreDDLGenerator implements DialectDDLGenerator {
    
    // TODO support version, partitions, index etc.
    @Override
    public String generateDDLSQL(final String tableName, final String schemaName, final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Object> context = new PostgresTablePropertiesLoader(connection, tableName, schemaName).loadTableProperties();
            new PostgresColumnPropertiesLoader(connection).loadColumnProperties(context);
            new PostgresConstraintsLoader(connection).loadConstraints(context);
            return doGenerateDDLSQL(context);
        }
    }
    
    private String doGenerateDDLSQL(final Map<String, Object> context) {
        formatColumnList(context);
        return FreemarkerManager.getSqlFromTemplate(context, "table/12_plus/create.ftl");
    }
    
    @SuppressWarnings("unchecked")
    private void formatColumnList(final Map<String, Object> context) {
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
    public String getType() {
        return "PostgreSQL";
    }
}
