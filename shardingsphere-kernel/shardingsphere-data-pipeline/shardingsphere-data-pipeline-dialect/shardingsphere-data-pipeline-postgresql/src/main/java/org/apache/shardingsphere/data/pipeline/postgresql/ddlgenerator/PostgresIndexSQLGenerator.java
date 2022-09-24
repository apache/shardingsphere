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
import org.postgresql.jdbc.PgArray;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Postgres index SQL generator.
 */
public final class PostgresIndexSQLGenerator extends AbstractPostgresDDLAdapter {
    
    private static final Integer PG_INDEX_INCLUDE_VERSION = 11;
    
    public PostgresIndexSQLGenerator(final Connection connection, final int majorVersion, final int minorVersion) {
        super(connection, majorVersion, minorVersion);
    }
    
    /**
     * Generate create index SQL.
     * 
     * @param context context
     * @return generated SQL
     * @throws SQLException SQL exception
     */
    public String generate(final Map<String, Object> context) throws SQLException {
        StringBuilder result = new StringBuilder();
        Collection<Map<String, Object>> indexNodes = getIndexNodes(context);
        for (Map<String, Object> each : indexNodes) {
            if (each.containsKey("is_inherited") && (Boolean) each.get("is_inherited")) {
                continue;
            }
            result.append(getIndexSql(context, each));
        }
        return result.toString().trim();
    }
    
    private Collection<Map<String, Object>> getIndexNodes(final Map<String, Object> context) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("tid", context.get("tid"));
        return executeByTemplate(param, "component/indexes/%s/nodes.ftl");
    }
    
    private String getIndexSql(final Map<String, Object> context, final Map<String, Object> indexNode) throws SQLException {
        Map<String, Object> indexData = getIndexData(context, indexNode);
        appendColumnDetails(indexData, (Long) indexNode.get("oid"));
        if (getMajorVersion() >= PG_INDEX_INCLUDE_VERSION) {
            appendIncludeDetails(indexData, (Long) indexNode.get("oid"));
        }
        return doGenerateIndexSql(indexData);
    }
    
    private String doGenerateIndexSql(final Map<String, Object> indexData) {
        String result = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(indexData, "component/indexes/%s/create.ftl", getMajorVersion(), getMinorVersion());
        result += System.lineSeparator();
        result += PostgreSQLPipelineFreemarkerManager.getSQLByVersion(indexData, "component/indexes/%s/alter.ftl", getMajorVersion(), getMinorVersion());
        return result;
    }
    
    private Map<String, Object> getIndexData(final Map<String, Object> context, final Map<String, Object> indexNode) {
        Collection<Map<String, Object>> indexProperties = fetchIndexProperties(context, indexNode);
        Map<String, Object> result = indexProperties.iterator().next();
        result.put("schema", context.get("schema"));
        result.put("table", context.get("name"));
        return result;
    }
    
    private Collection<Map<String, Object>> fetchIndexProperties(final Map<String, Object> context, final Map<String, Object> indexNode) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("did", context.get("did"));
        param.put("tid", context.get("tid"));
        param.put("idx", indexNode.get("oid"));
        param.put("datlastsysoid", context.get("datlastsysoid"));
        return executeByTemplate(param, "component/indexes/%s/properties.ftl");
    }
    
    private void appendColumnDetails(final Map<String, Object> indexData, final Long indexId) throws SQLException {
        Collection<Map<String, Object>> columnDetails = fetchColumnDetails(indexId);
        Collection<Map<String, Object>> columns = new LinkedList<>();
        Collection<String> columnDisplays = new LinkedList<>();
        for (Map<String, Object> each : columnDetails) {
            columns.add(getColumnData(indexData, each));
            columnDisplays.add(getColumnPropertyDisplayData(each, indexData));
        }
        indexData.put("columns", columns);
        indexData.put("columns_csv", String.join(", ", columnDisplays));
    }
    
    private Map<String, Object> getColumnData(final Map<String, Object> indexData, final Map<String, Object> columnDetail) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("colname", columnDetail.get("attdef"));
        result.put("collspcname", columnDetail.get("collnspname"));
        result.put("op_class", columnDetail.get("opcname"));
        if ("btree".equals(indexData.get("amname"))) {
            result.put("sort_order", isSortOrder(columnDetail));
            result.put("nulls", isNulls(columnDetail));
        }
        return result;
    }
    
    private boolean isSortOrder(final Map<String, Object> columnDetail) throws SQLException {
        if (null != columnDetail.get("options")) {
            String[] options = (String[]) ((PgArray) columnDetail.get("options")).getArray();
            return options.length > 0 && "DESC".equals(options[0]);
        }
        return false;
    }
    
    private Object isNulls(final Map<String, Object> columnDetail) throws SQLException {
        if (null != columnDetail.get("options")) {
            String[] options = (String[]) ((PgArray) columnDetail.get("options")).getArray();
            return options.length > 1 && options[1].split(" ").length > 1 && "FIRST".equals(options[1].split(" ")[1]);
        }
        return false;
    }
    
    private Collection<Map<String, Object>> fetchColumnDetails(final Long indexId) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("idx", indexId);
        return executeByTemplate(param, "component/indexes/%s/column_details.ftl");
    }
    
    private String getColumnPropertyDisplayData(final Map<String, Object> each, final Map<String, Object> indexData) throws SQLException {
        String result = (String) each.get("attdef");
        if (null != each.get("collnspname")) {
            result += " COLLATE " + each.get("collnspname");
        }
        if (null != each.get("opcname")) {
            result += " " + each.get("opcname");
        }
        if ("btree".equals(indexData.get("amname"))) {
            String[] options = (String[]) ((PgArray) each.get("options")).getArray();
            if (options.length > 0) {
                result += " " + options[0];
            }
            if (options.length > 1) {
                result += " " + options[1];
            }
        }
        return result;
    }
    
    private void appendIncludeDetails(final Map<String, Object> indexData, final Long oid) {
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("idx", oid);
        Collection<Object> includes = new LinkedList<>();
        for (Map<String, Object> each : executeByTemplate(param, "component/indexes/%s/include_details.ftl")) {
            includes.add(each.get("colname"));
        }
        indexData.put("include", includes);
    }
}
