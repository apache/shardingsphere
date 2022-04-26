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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PostgreColumnPropertiesLoader extends PostgreAbstractLoader {
    
    private final Connection connection;
    
    public void loadColumnProperties(final Map<String, Object> context) {
        getFormattedColumns(context, connection);
    }
    
    @SneakyThrows
    private void getFormattedColumns(final Map<String, Object> context, final Connection connection) {
        List<Map<String, Object>> allColumns = executeByTemplate(connection, context, "columns/12_plus/properties.ftl");
        Map<String, Object> editTypes = new HashMap<>();
        for (Map<String, Object> each : allColumns) {
            editTypes.put(each.get("atttypid").toString(), new LinkedList<>());
        }
        context.put("columns", allColumns);
        Map<String, Object> param = new HashMap<>();
        param.put("type_ids", String.join(",", editTypes.keySet()));
        if (!allColumns.isEmpty()) {
            for (Map<String, Object> each : executeByTemplate(connection, param, "columns/default/edit_mode_types_multi.ftl")) {
                editTypes.put(each.get("main_oid").toString(), each.get("edit_types"));
            }
            for (Map<String, Object> each : allColumns) {
                columnFormatter(each, editTypes.get(each.get("atttypid").toString()));
            }
        }
    }
    
    private void columnFormatter(final Map<String, Object> column, final Object editTypeList) throws SQLException {
        checkPrimaryColumn(column);
        fetchLengthPrecision(column);
        List<String> editTypes = Arrays.stream((String[]) ((Array) editTypeList).getArray()).collect(Collectors.toList());
        editTypes.add(column.get("cltype").toString());
        editTypes.sort(String::compareTo);
        column.put("edit_types", editTypes);
        column.put("cltype", parseTypeName(column.get("cltype").toString()));
    }
    
    private void checkPrimaryColumn(final Map<String, Object> column) {
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
            getLengthPrecision((Long) column.get("elemoid"), column, fullType);
        }
    }
    
    private void getLengthPrecision(final Long elemoid, final Map<String, Object> column, final String fullType) {
        boolean precision = false;
        boolean length = false;
        String typeval = "";
        Long[] l = {1560L,1561L,1562L,1563L,1042L,1043L,1014L,1015L};
        Long[] d = {1083L,1114L,1115L,1183L,1184L,1185L,1186L,1187L,1266L,1270L};
        Long[] p = {1231L,1700L};
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
//        data['typnspname'], data['typname'],
//                data['isdup'], data['attndims'], data['atttypmod']
        String nsp = (String) column.get("typnspname");
        String typname = (String) column.get("typname");
        Boolean is_dup = (Boolean) column.get("isdup");
        Integer numdims = (Integer) column.get("attndims");
        Integer typmod = (Integer) column.get("atttypmod");
        
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
    
    private String parseTypeName(String typeName) {
        boolean isArray = false;
        if (typeName.endsWith("[]")) {
            isArray = true;
            typeName = typeName.substring(0, typeName.lastIndexOf("[]"));
        }
        int idx = typeName.indexOf("(");
        if (idx > 0 && typeName.endsWith(")")) {
            typeName = typeName.substring(0, idx);
        } else if (idx > 0 && typeName.startsWith("time")) {
            int endIdx = typeName.indexOf(")");
            if (1 != endIdx) {
                Pattern pattern = Pattern.compile("(\\(\\d+\\))");
                Matcher matcher = pattern.matcher(typeName);
                StringBuffer buffer = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(buffer, "");
                }
                matcher.appendTail(buffer);
                typeName = buffer.toString();
            }
        } else if (typeName.startsWith("interval")) {
            typeName = "interval";
        }
        if (isArray) {
            typeName += "[]";
        }
        return typeName;
    }
}
