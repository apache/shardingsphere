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

package org.apache.shardingsphere.infra.metadata.ddlgenerator.dialect.postgres;

import org.apache.shardingsphere.infra.metadata.ddlgenerator.spi.DialectDDLSQLGenerator;
import org.apache.shardingsphere.infra.metadata.ddlgenerator.util.FreemarkerManager;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * DDL SQL generator for PostgreSQL.
 */
public final class PostgreDDLSQLGenerator implements DialectDDLSQLGenerator {
    
    @Override
    public String generateDDLSQL(final String tableName, final String schemaName, final Connection connection) {
        Map<String, Object> context = new PostgreTablePropertiesLoader(connection, tableName, schemaName).loadTableProperties();
        return getReverseEngineeredSql(context, connection);
    }
    
    private String getReverseEngineeredSql(final Map<String, Object> context, final Connection connection) {
        return getReSqlForTable(context, connection);
    }
    
    private String getReSqlForTable(final Map<String, Object> context, final Connection connection) {
        formatter(context, connection);
        formatColumnList(context);
        StringBuilder result = new StringBuilder();
        result.append(String.format("-- Table: %s.%s\n\n-- ", context.get("schema"), context.get("name")));
        result.append(FreemarkerManager.getSqlFromTemplate(context, "table/default/delete.ftl"));
        result.append("\n");
        String tableSQL = FreemarkerManager.getSqlFromTemplate(context, "table/12_plus/create.ftl");
        result.append(tableSQL);
        return result.toString();
    }
    
    private void formatColumnList(final Map<String, Object> context) {
        List<Map<String, Object>> columns = (List<Map<String, Object>>) context.get("columns");
        for (Map<String, Object> c : columns) {
            if (c.containsKey("cltype")) {
                typeFormatter(c, (String) c.get("cltype"));
            }
        }
    }
    
    private void typeFormatter(final Map<String, Object> c, final String cltype) {
        if (cltype.contains("[]")) {
            c.put("cltype", cltype.substring(0, cltype.length() - 2));
            c.put("hasSqrBracket", true);
        } else {
            c.put("hasSqrBracket", false);
        }
    }
    
    private void formatter(final Map<String, Object> context, final Connection connection) {
        new PostgreColumnPropertiesLoader(connection).loadColumnProperties(context);
        new PostgreConstraintsLoader(connection).loadConstraints(context);
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
