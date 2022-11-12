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

import lombok.SneakyThrows;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Postgres column properties appender.
 */
public final class PostgresColumnPropertiesAppender extends AbstractPostgresDDLAdapter {
    
    private static final Pattern LENGTH_PRECISION_PATTERN = Pattern.compile("(\\d+),(\\d+)");
    
    private static final Pattern LENGTH_PATTERN = Pattern.compile("(\\d+)");
    
    private static final Pattern BRACKETS_PATTERN = Pattern.compile("(\\(\\d+\\))");
    
    private static final String ATT_OPTION_SPLIT = "=";
    
    public PostgresColumnPropertiesAppender(final Connection connection, final int majorVersion, final int minorVersion) {
        super(connection, majorVersion, minorVersion);
    }
    
    /**
     * Append column properties.
     * 
     * @param context create table sql context
     */
    @SneakyThrows(SQLException.class)
    public void append(final Map<String, Object> context) {
        Collection<Map<String, Object>> typeAndInheritedColumns = getTypeAndInheritedColumns(context);
        Collection<Map<String, Object>> allColumns = executeByTemplate(context, "component/columns/%s/properties.ftl");
        for (Map<String, Object> each : allColumns) {
            for (Map<String, Object> column : typeAndInheritedColumns) {
                if (each.get("name").equals(column.get("name"))) {
                    each.put(getInheritedFromTableOrType(context), column.get("inheritedfrom"));
                }
            }
        }
        if (!allColumns.isEmpty()) {
            Map<String, Collection<String>> editTypes = getEditTypes(allColumns);
            for (Map<String, Object> each : allColumns) {
                columnFormatter(each, editTypes.getOrDefault(each.get("atttypid").toString(), new LinkedList<>()));
            }
        }
        context.put("columns", allColumns);
    }
    
    private Collection<Map<String, Object>> getTypeAndInheritedColumns(final Map<String, Object> context) throws SQLException {
        if (null != context.get("typoid")) {
            return getColumnFromType(context);
        }
        if (null != context.get("coll_inherits")) {
            Collection<String> collInherits = convertPgArrayToList(context.get("coll_inherits"));
            context.put("coll_inherits", collInherits);
            if (!collInherits.isEmpty()) {
                return getColumnFromInherits(collInherits);
            }
        }
        return Collections.emptyList();
    }
    
    private Collection<Map<String, Object>> getColumnFromInherits(final Collection<String> collInherits) {
        Collection<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : executeByTemplate(new LinkedHashMap<>(), "component/table/%s/get_inherits.ftl")) {
            if (collInherits.contains((String) each.get("inherits"))) {
                Map<String, Object> params = new LinkedHashMap<>();
                params.put("tid", each.get("oid"));
                result.addAll(executeByTemplate(params, "table/%s/get_columns_for_table.ftl"));
            }
        }
        return result;
    }
    
    private Collection<Map<String, Object>> getColumnFromType(final Map<String, Object> context) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("tid", context.get("typoid"));
        return executeByTemplate(params, "component/table/%s/get_columns_for_table.ftl");
    }
    
    @SuppressWarnings("unchecked")
    private String getInheritedFromTableOrType(final Map<String, Object> context) {
        String result = "inheritedfrom";
        if (null != context.get("typoid")) {
            result += "type";
        } else if (null != context.get("coll_inherits") && !((Collection<String>) context.get("coll_inherits")).isEmpty()) {
            result += "table";
        }
        return result;
    }
    
    private Collection<String> convertPgArrayToList(final Object array) throws SQLException {
        return Arrays.stream((String[]) ((Array) array).getArray()).collect(Collectors.toList());
    }
    
    private Map<String, Collection<String>> getEditTypes(final Collection<Map<String, Object>> allColumns) throws SQLException {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("type_ids", allColumns.stream().map(each -> each.get("atttypid").toString()).collect(Collectors.joining(",")));
        for (Map<String, Object> each : executeByTemplate(params, "component/columns/%s/edit_mode_types_multi.ftl")) {
            result.put(each.get("main_oid").toString(), covertPgArrayAndSort(each.get("edit_types")));
        }
        return result;
    }
    
    private Collection<String> covertPgArrayAndSort(final Object editTypes) throws SQLException {
        return Arrays.stream((String[]) ((Array) editTypes).getArray()).sorted(String::compareTo).collect(Collectors.toList());
    }
    
    private void columnFormatter(final Map<String, Object> column, final Collection<String> editTypes) throws SQLException {
        handlePrimaryColumn(column);
        fetchLengthPrecision(column);
        formatColumnVariables(column);
        formatSecurityLabels(column);
        editTypes.add(column.get("cltype").toString());
        column.put("edit_types", editTypes.stream().sorted().collect(Collectors.toList()));
        column.put("cltype", parseTypeName(column.get("cltype").toString()));
    }
    
    private void handlePrimaryColumn(final Map<String, Object> column) {
        if (null != column.get("attnum") && null != column.get("indkey")) {
            if (Arrays.stream(column.get("indkey").toString().split(" ")).collect(Collectors.toList()).contains(column.get("attnum").toString())) {
                column.put("is_pk", true);
                column.put("is_primary_key", true);
            } else {
                column.put("is_pk", false);
                column.put("is_primary_key", false);
            }
        }
    }
    
    private void fetchLengthPrecision(final Map<String, Object> column) {
        String fullType = getFullDataType(column);
        if (column.containsKey("elemoid")) {
            handleLengthPrecision((Long) column.get("elemoid"), column, fullType);
        }
    }
    
    private void handleLengthPrecision(final Long elemoid, final Map<String, Object> column, final String fullType) {
        boolean precision = false;
        boolean length = false;
        String typeval = "";
        Long[] lTypes = {1560L, 1561L, 1562L, 1563L, 1042L, 1043L, 1014L, 1015L};
        Long[] dTypes = {1083L, 1114L, 1115L, 1183L, 1184L, 1185L, 1186L, 1187L, 1266L, 1270L};
        Long[] pTypes = {1231L, 1700L};
        if (0 != elemoid) {
            if (Arrays.asList(lTypes).contains(elemoid)) {
                typeval = "L";
            } else if (Arrays.asList(dTypes).contains(elemoid)) {
                typeval = "D";
            } else if (Arrays.asList(pTypes).contains(elemoid)) {
                typeval = "P";
            } else {
                typeval = " ";
            }
        }
        if ("P".equals(typeval)) {
            precision = true;
        }
        if (precision || "L".equals(typeval) || "D".equals(typeval)) {
            length = true;
        }
        
        if (length && precision) {
            Matcher matcher = LENGTH_PRECISION_PATTERN.matcher(fullType);
            if (matcher.find()) {
                column.put("attlen", matcher.group(1));
                column.put("attprecision", matcher.group(2));
            }
        } else if (length) {
            Matcher matcher = LENGTH_PATTERN.matcher(fullType);
            if (matcher.find()) {
                column.put("attlen", matcher.group(1));
                column.put("attprecision", null);
            }
        }
    }
    
    private void formatColumnVariables(final Map<String, Object> column) throws SQLException {
        if (null == column.get("attoptions")) {
            return;
        }
        Collection<Map<String, String>> attOptions = new LinkedList<>();
        Collection<String> columnVariables = Arrays.stream((String[]) ((Array) column.get("attoptions")).getArray()).collect(Collectors.toList());
        for (String each : columnVariables) {
            Map<String, String> columnVariable = new LinkedHashMap<>();
            columnVariable.put("name", each.substring(0, each.indexOf(ATT_OPTION_SPLIT)));
            columnVariable.put("value", each.substring(each.indexOf(ATT_OPTION_SPLIT) + 1));
            attOptions.add(columnVariable);
        }
        column.put("attoptions", attOptions);
    }
    
    private String getFullDataType(final Map<String, Object> column) {
        String namespace = (String) column.get("typnspname");
        String typeName = (String) column.get("typname");
        Integer numdims = (Integer) column.get("attndims");
        String schema = null != namespace ? namespace : "";
        String array = "";
        String length = "";
        String name = checkSchemaInName(typeName, schema);
        if (name.startsWith("_")) {
            if (null == numdims || 0 == numdims) {
                numdims = 1;
            }
            name = name.substring(1);
        }
        if (name.endsWith("[]")) {
            if (null == numdims || 0 == numdims) {
                numdims = 1;
            }
            name = name.substring(0, name.length() - 2);
        }
        if (name.startsWith("\"") && name.endsWith("\"")) {
            name = name.substring(1, name.length() - 1);
        }
        if (numdims == 1) {
            array = "[]";
        }
        Integer typmod = (Integer) column.get("atttypmod");
        if (-1 != typmod) {
            length = checkTypmod(typmod, name);
        }
        return getFullTypeValue(name, schema, length, array);
    }
    
    private String checkSchemaInName(final String typname, final String schema) {
        if (typname.indexOf(schema + "\".") > 0) {
            return typname.substring(schema.length() + 3);
        }
        if (typname.indexOf(schema + ".") > 0) {
            return typname.substring(schema.length() + 1);
        }
        return typname;
    }
    
    private String getFullTypeValue(final String name, final String schema, final String length, final String array) {
        if ("char".equals(name) && "pg_catalog".equals(schema)) {
            return "\"char\"" + array;
        }
        if ("time with time zone".equals(name)) {
            return "time" + length + " with time zone" + array;
        }
        if ("time without time zone".equals(name)) {
            return "time" + length + " without time zone" + array;
        }
        if ("timestamp with time zone".equals(name)) {
            return "timestamp" + length + " with time zone" + array;
        }
        if ("timestamp without time zone".equals(name)) {
            return "timestamp" + length + " without time zone" + array;
        }
        return name + length + array;
    }
    
    private String checkTypmod(final Integer typmod, final String name) {
        String result = "(";
        if ("numeric".equals(name)) {
            int len = (typmod - 4) >> 16;
            int prec = (typmod - 4) & 0xffff;
            result += String.valueOf(len);
            result += "," + prec;
        } else if ("time".equals(name) || "timetz".equals(name) || "time without time zone".equals(name) || "time with time zone".equals(name)
                || "timestamp".equals(name) || "timestamptz".equals(name) || "timestamp without time zone".equals(name) || "timestamp with time zone".equals(name)
                || "bit".equals(name) || "bit varying".equals(name) || "varbit".equals(name)) {
            int len = typmod;
            result += String.valueOf(len);
        } else if ("interval".equals(name)) {
            int len = typmod & 0xffff;
            result += len > 6 ? "" : String.valueOf(len);
        } else if ("date".equals(name)) {
            result = "";
        } else {
            int len = typmod - 4;
            result += String.valueOf(len);
        }
        if (!result.isEmpty()) {
            result += ")";
        }
        return result;
    }
    
    private String parseTypeName(final String name) {
        String result = name;
        boolean isArray = false;
        if (result.endsWith("[]")) {
            isArray = true;
            result = result.substring(0, result.lastIndexOf("[]"));
        }
        int idx = result.indexOf("(");
        if (idx > 0 && result.endsWith(")")) {
            result = result.substring(0, idx);
        } else if (idx > 0 && result.startsWith("time")) {
            int endIdx = result.indexOf(")");
            if (1 != endIdx) {
                Matcher matcher = BRACKETS_PATTERN.matcher(result);
                StringBuffer buffer = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(buffer, "");
                }
                matcher.appendTail(buffer);
                result = buffer.toString();
            }
        } else if (result.startsWith("interval")) {
            result = "interval";
        }
        if (isArray) {
            result += "[]";
        }
        return result;
    }
}
