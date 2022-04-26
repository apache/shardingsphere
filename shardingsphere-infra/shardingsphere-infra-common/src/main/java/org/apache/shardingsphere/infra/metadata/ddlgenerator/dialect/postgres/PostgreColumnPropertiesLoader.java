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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Postgre column properties loader.
 */
@RequiredArgsConstructor
public class PostgreColumnPropertiesLoader extends PostgreAbstractLoader {
    
    private final Connection connection;
    
    /**
     * Load column properties.
     * 
     * @param context load context
     */
    @SneakyThrows
    public void loadColumnProperties(final Map<String, Object> context) {
        List<Map<String, Object>> allColumns = executeByTemplate(connection, context, "columns/12_plus/properties.ftl");
        if (!allColumns.isEmpty()) {
            Map<String, Collection<String>> editTypes = getEditTypes(allColumns);
            for (Map<String, Object> each : allColumns) {
                columnFormatter(each, editTypes.getOrDefault(each.get("atttypid").toString(), new LinkedList<>()));
            }
        }
        context.put("columns", allColumns);
    }
    
    private Map<String, Collection<String>> getEditTypes(final List<Map<String, Object>> allColumns) throws SQLException {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        Map<String, Object> param = new LinkedHashMap<>();
        param.put("type_ids", allColumns.stream().map(each -> each.get("atttypid").toString()).collect(Collectors.joining(",")));
        for (Map<String, Object> each : executeByTemplate(connection, param, "columns/default/edit_mode_types_multi.ftl")) {
            result.put(each.get("main_oid").toString(), covertPgArrayAndSort(each.get("edit_types")));
        }
        return result;
    }
    
    private Collection<String> covertPgArrayAndSort(final Object editTypes) throws SQLException {
        return Arrays.stream((String[]) ((Array) editTypes).getArray()).sorted(String::compareTo).collect(Collectors.toList());
    }
    
    private void columnFormatter(final Map<String, Object> column, final Collection<String> editTypes) {
        handlePrimaryColumn(column);
        fetchLengthPrecision(column);
        editTypes.add(column.get("cltype").toString());
        column.put("edit_types", editTypes);
        column.put("cltype", parseTypeName(column.get("cltype").toString()));
    }
    
    private void handlePrimaryColumn(final Map<String, Object> column) {
        if (column.containsKey("attnum") && column.containsKey("indkey")) {
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
        Long[] l = {1560L, 1561L, 1562L, 1563L, 1042L, 1043L, 1014L, 1015L};
        Long[] d = {1083L, 1114L, 1115L, 1183L, 1184L, 1185L, 1186L, 1187L, 1266L, 1270L};
        Long[] p = {1231L, 1700L};
        if (0 != elemoid) {
            if (Arrays.asList(l).contains(elemoid)) {
                typeval = "L";
            } else if (Arrays.asList(d).contains(elemoid)) {
                typeval = "D";
            } else if (Arrays.asList(p).contains(elemoid)) {
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
            Pattern pattern = Pattern.compile("(\\d+),(\\d+)");
            Matcher matcher = pattern.matcher(fullType);
            if (matcher.find()) {
                column.put("attlen", matcher.group(1));
                column.put("attprecision", matcher.group(2));
            }
        } else if (length) {
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(fullType);
            if (matcher.find()) {
                column.put("attlen", matcher.group(1));
                column.put("attprecision", null);
            }
        }
    }
    
    private String getFullDataType(final Map<String, Object> column) {
        String nsp = (String) column.get("typnspname");
        String typname = (String) column.get("typname");
        Integer numdims = (Integer) column.get("attndims");
        String schema = null != nsp ? nsp : "";
        String name = "";
        String array = "";
        String length = "";
        name = checkSchemaInName(typname, schema);
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
        } else if ("time with time zone".equals(name)) {
            return "time" + length + " with time zone" + array;
        } else if ("time without time zone".equals(name)) {
            return "time" + length + " without time zone" + array;
        } else if ("timestamp with time zone".equals(name)) {
            return "timestamp" + length + " with time zone" + array;
        } else if ("timestamp without time zone".equals(name)) {
            return "timestamp" + length + " without time zone" + array;
        } else {
            return name + length + array;
        }
    }
    
    private String checkTypmod(final Integer typmod, final String name) {
        String length = "(";
        if ("numeric".equals(name)) {
            int len = (typmod - 4) >> 16;
            int prec = (typmod - 4) & 0xffff;
            length += String.valueOf(len);
            length += "," + prec;
        } else if ("time".equals(name) || "timetz".equals(name) || "time without time zone".equals(name) || "time with time zone".equals(name)
                || "timestamp".equals(name) || "timestamptz".equals(name) || "timestamp without time zone".equals(name) || "timestamp with time zone".equals(name)
                || "bit".equals(name) || "bit varying".equals(name) || "varbit".equals(name)) {
            int prec = 0;
            int len = typmod;
            length += String.valueOf(len);
        } else if ("interval".equals(name)) {
            int prec = 0;
            int len = typmod & 0xffff;
            length += len > 6 ? "" : String.valueOf(len);
        } else if ("date".equals(name)) {
            length = "";
        } else {
            int len = typmod - 4;
            int prec = 0;
            length += String.valueOf(len);
        }
        if (!length.isEmpty()) {
            length += ")";
        }
        return length;
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
                Pattern pattern = Pattern.compile("(\\(\\d+\\))");
                Matcher matcher = pattern.matcher(result);
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
