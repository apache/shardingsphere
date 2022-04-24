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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.util.FreemarkerManager;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show create database executor.
 */
@RequiredArgsConstructor
@Getter
public final class PostgreSQLShowCreateTableExecutor {
    
    @SneakyThrows
    public String getCreateSQL(final String tableName, final String schemaName, final Connection connection) {
        Map<String, Object> context = new HashMap<>();
        getDataBaseId(context, connection);
        getSchemaId(context, schemaName, connection);
        getTableId(context, schemaName, tableName, connection);
        fetchTableProperties(context, connection);
        return getReverseEngineeredSql(context, connection);
    }
    
    private String getReverseEngineeredSql(final Map<String, Object> context, final Connection connection) {
        return getReSqlForTable(context, connection);
    }
    
    private String getReSqlForTable(final Map<String, Object> context, final Connection connection) {
        formatter(context, connection);
        formatColumnList(context, connection);
        return getSqlFromTemplate(context, "table/12_plus/create.ftl");
    }
    
    private void formatColumnList(final Map<String, Object> context, final Connection connection) {
    }
    
    private void formatter(final Map<String, Object> context, final Connection connection) {
        if (null != context.get("seclabels")) {
        }
        getFormattedColumns(context, connection);
        addConstraintsToOutput(context, connection);
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
    
    private void addConstraintsToOutput(final Map<String, Object> context, final Connection connection) {
        Map<String, String> indexConstraints = new HashMap<>();
        indexConstraints.put("p", "primary_key");
        indexConstraints.put("u", "unique_constraint");
        for (Map.Entry<String, String> entry : indexConstraints.entrySet()) {
            Map<String, Object> param = new HashMap<>();
            param.put("did", context.get("did"));
            param.put("tid", context.get("tid"));
            param.put("cid", context.get("cid"));
            param.put("constraint_type", entry.getKey());
            List<Map<String, Object>> rows = executeByTemplate(connection, param, "index_constraint/11_plus/properties.ftl");
            for (Map<String, Object> each : rows) {
                Map<String, Object> map = new HashMap<>();
                map.put("cid", each.get("oid"));
                map.put("colcnt", each.get("col_count"));
                List<Map<String, Object>> list = executeByTemplate(connection, map, "index_constraint/default/get_costraint_cols.ftl");
                List<Map<String, Object>> columns = new LinkedList<>();
                for (Map<String, Object> col : list) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("column", col.get("column"));
                    columns.add(r);
                }
                each.put("columns", columns);
                each.put("include", new LinkedList<>());
            }
            context.put(entry.getValue(), rows);
        }
        List<Map<String, Object>> foreignKeys = new LinkedList<>();
        for (Map<String, Object> fk : getForeignKeys((Long) context.get("tid"), connection)) {
            if (!isPartitionAndConstraintInherited(fk, context)) {
                foreignKeys.add(fk);
            }
        }
        context.put("foreign_key", foreignKeys);
        List<Map<String, Object>> checkConstraints = new LinkedList<>();
        for (Map<String, Object> cc : getCheckConstraints((Long) context.get("tid"), connection)) {
            if (!isPartitionAndConstraintInherited(cc, context)) {
                checkConstraints.add(cc);
            }
        }
        context.put("check_constraint", checkConstraints);
        context.put("exclude_constraint", getExclusionConstraints((Long) context.get("tid"), (Long) context.get("did"), connection));
        
    }
    
    private List<Map<String, Object>> getExclusionConstraints(final Long tid, final Long did, final Connection connection) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        param.put("did", did);
        List<Map<String, Object>> result = executeByTemplate(connection, param,"exclusion_constraint/11_plus/properties.ftl");
        for (Map<String, Object> ex : result) {
            Map<String, Object> p = new HashMap<>();
            p.put("cid", ex.get("oid"));
            p.put("col_count", ex.get("col_count"));
            List<Map<String, Object>> r = executeByTemplate(connection, p, "exclusion_constraint/9.2_plus/get_constraint_cols.ftl");
            List<Map<String, Object>> columns = new LinkedList<>();
            for (Map<String, Object> row : r) {
                boolean order;
                boolean nullsOrder;
                if ((((int) row.get("options")) & 1) != 0) {
                    order = false;
                    nullsOrder = (((int) row.get("options")) & 2) != 0;
                } else {
                    order = true;
                    nullsOrder = (((int) row.get("options")) & 2) != 0;
                }
                Map<String, Object> s = new HashMap<>();
                s.put("column", strip((String) row.get("coldef")));
                s.put("oper_class", row.get("opcname"));
                s.put("order", order);
                s.put("nulls_order", nullsOrder);
                s.put("operator", row.get("oprname"));
                s.put("col_type", row.get("datatype"));
                s.put("is_exp", row.get("is_exp"));
                columns.add(s);
            }
            ex.put("columns", columns);
            Map<String, Object> map = new HashMap<>();
            map.put("cid", ex.get("oid"));
            List<String> include = new LinkedList<>();
            for (Map<String, Object> e : executeByTemplate(connection, map, "exclusion_constraint/11_plus/get_constraint_include.ftl")) {
                include.add(e.get("colname").toString());
            }
            ex.put("include", include);
        }
        return result;
    }
    
    private List<Map<String, Object>> getCheckConstraints(final Long tid, final Connection connection) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        return executeByTemplate(connection, param, "template/check_constraint/9.2_plus/get_cols.ftl");
    }
    
    private boolean isPartitionAndConstraintInherited(final Map<String, Object> constraint, final Map<String, Object> context) {
        return context.containsKey("relispartition") && (boolean) context.get("relispartition") && constraint.containsKey("conislocal") && (boolean) constraint.get("conislocal");
    }
    
    private List<Map<String, Object>> getForeignKeys(final Long tid, final Connection connection) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        List<Map<String, Object>> foreignKeys = executeByTemplate(connection, param,"foreign_key/9.1_plus/properties.ftl");
        for (Map<String, Object> fk : foreignKeys) {
            Map<String, Object> param1 = new HashMap<>();
            param1.put("tid", tid);
            List<Map<String, Object>> keys = new LinkedList<>();
            Map<String, Object> s = new HashMap<>();
            s.put("confkey", fk.get("confkey"));
            s.put("conkey", fk.get("conkey"));
            keys.add(s);
            param1.put("keys", keys);
            List<Map<String, Object>> re = executeByTemplate(connection, param1, "foreign_key/default/get_constraint_cols.ftl");
            List<Map<String, Object>> columns = new LinkedList<>();
            Set<String> cols = new HashSet<>();
            for (Map<String, Object> row : re) {
                Map<String, Object> f = new HashMap<>();
                f.put("local_column", row.get("conattname"));
                f.put("references", fk.get("confrelid"));
                f.put("referenced", row.get("confattname"));
                f.put("references_table_name", fk.get("refnsp") + "." + fk.get("reftab"));
                columns.add(f);
                cols.add((String) row.get("conattname"));
            }
            fk.put("columns", columns);
            fk.get("columns");
            Map<String, Object> p = new HashMap<>();
            p.put("tid", columns.get(0).get("references"));
            List<Map<String, Object>> r = executeByTemplate(connection, p, "foreign_key/default/get_parent.ftl");
            for (Map<String, Object> sf : r) {
                fk.put("remote_schema", sf.get("schema"));
                fk.put("remote_table", sf.get("table"));
                break;
            }
            Optional<String> coveringindex = searchCoveringindex(tid, cols, connection);
            fk.put("coveringindex", coveringindex.orElse(null));
            fk.put("autoindex", !coveringindex.isPresent());
            fk.put("hasindex", coveringindex.isPresent());
        }
        return foreignKeys;
    }
    
    private Optional<String> searchCoveringindex(final Long tid, final Set<String> cols, final Connection connection) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        for (Map<String, Object> row : executeByTemplate(connection, param, "foreign_key/default/get_constraints.ftl")) {
            Map<String, Object> s = new HashMap<>();
            s.put("cid", row.get("oid"));
            s.put("colcnt", row.get("col_count"));
            List<Map<String, Object>> rows = executeByTemplate(connection, s, "foreign_key/default/get_cols.ftl");
            Set<String> indexCols = new HashSet<>();
            for (Map<String, Object> map : rows) {
                indexCols.add(strip(map.get("column").toString()));
            }
            if (isSame(indexCols, cols)) {
                return Optional.of((String) row.get("idxname"));
            }
        }
        return Optional.empty();
    }
    
    private boolean isSame(final Set<String> indexCols, final Set<String> cols) {
        Set<String> a = new HashSet<>(indexCols);
        Set<String> b = new HashSet<>(cols);
        a.removeAll(b);
        if (a.size() == 0) {
            cols.removeAll(indexCols);
            return cols.size() == 0;
        }
        return false;
    }
    
    private String strip(String column) {
        if (column.startsWith("\"")) {
            column = column.substring(1);
        }
        if (column.endsWith("\"")) {
            column = column.substring(0, column.length() - 1);
        }
        return column;
    }
    
    private void fetchLengthPrecision(final Map<String, Object> column) {
        String fullType = getFullDataType(column);
        if (column.containsKey("elemoid")) {
            getLengthPrecision((Integer) column.get("elemoid"), column, fullType);
        }
    }
    
    private void getLengthPrecision(final Integer elemoid, final Map<String, Object> column, final String fullType) {
        boolean precision = false;
        boolean length = false;
        String typeval = "";
        Integer[] l = {1560,1561,1562,1563,1042,1043,1014,1015};
        Integer[] d = {1083,1114,1115,1183,1184,1185,1186,1187,1266,1270};
        Integer[] p = {1231,1700};
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
    
    
    private String checkSchemaInName(final String typname, final String schema) {
        if (typname.indexOf(schema + "\".") > 0) {
            return typname.substring(schema.length() + 3);
        } 
        if (typname.indexOf(schema + ".") > 0) {
            return typname.substring(schema.length() + 1);
        } 
        return typname;
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
    
    private void fetchTableProperties(final Map<String, Object> context, final Connection connection) throws SQLException, IOException, TemplateException {
        appendFirstRow(executeByTemplate(connection, context, "table/12_plus/properties.ftl"), context);
        updateAutovacuumProperties(context);
        checkRlspolicySupport(context);
        setRowsCount(context);
        fetchPrivileges(context);
    }
    
    private void fetchPrivileges(final Map<String, Object> context) {
        context.put("acl", new LinkedList<>());
    }
    
    private void setRowsCount(final Map<String, Object> context) {
        context.put("rows_cnt", "0");
    }
    
    private void checkRlspolicySupport(final Map<String, Object> context) {
        if (context.containsKey("rlspolicy")) {
            if (context.get("rlspolicy") instanceof String && "true".equals(context.get("rlspolicy"))) {
                context.put("rlspolicy", true);
            }
            if (context.get("forcerlspolicy") instanceof String && "true".equals(context.get("forcerlspolicy"))) {
                context.put("forcerlspolicy", true);
            }
        }
    }
    
    @SneakyThrows
    private String getSqlFromTemplate(final Map<String, Object> context, final String path) {
        Template template = FreemarkerManager.getInstance().getTemplateConfig().getTemplate(path);
        try (StringWriter result = new StringWriter()) {
            template.process(context, result);
            return result.toString();
        }
        
    }
    
    private void updateAutovacuumProperties(final Map<String, Object> context) {
        if (null == context.get("autovacuum_enabled")) {
            context.put("autovacuum_enabled", "x");
        } else if ("true".equalsIgnoreCase(context.get("autovacuum_enabled").toString())) {
            context.put("autovacuum_enabled", "t");
        } else {
            context.put("autovacuum_enabled", "f");
        }
        if (null == context.get("toast_autovacuum_enabled")) {
            context.put("toast_autovacuum_enabled", "x");
        } else if ("true".equalsIgnoreCase(context.get("toast_autovacuum_enabled").toString())) {
            context.put("toast_autovacuum_enabled", "t");
        } else {
            context.put("toast_autovacuum_enabled", "f");
        }
        context.put("autovacuum_custom", anyIsTrue(Arrays.asList(
                context.get("autovacuum_vacuum_threshold"),
                context.get("autovacuum_vacuum_scale_factor"),
                context.get("autovacuum_analyze_threshold"),
                context.get("autovacuum_analyze_scale_factor"),
                context.get("autovacuum_vacuum_cost_delay"),
                context.get("autovacuum_vacuum_cost_limit"),
                context.get("autovacuum_freeze_min_age"),
                context.get("autovacuum_freeze_max_age"),
                context.get("autovacuum_freeze_table_age"))) || "t".equals(context.get("autovacuum_enabled")) || "f".equals(context.get("autovacuum_enabled")));
        context.put("toast_autovacuum", anyIsTrue(Arrays.asList(
                context.get("toast_autovacuum_vacuum_threshold"),
                context.get("toast_autovacuum_vacuum_scale_factor"),
                context.get("toast_autovacuum_analyze_threshold"),
                context.get("toast_autovacuum_analyze_scale_factor"),
                context.get("toast_autovacuum_vacuum_cost_delay"),
                context.get("toast_autovacuum_vacuum_cost_limit"),
                context.get("toast_autovacuum_freeze_min_age"),
                context.get("toast_autovacuum_freeze_max_age"),
                context.get("toast_autovacuum_freeze_table_age"))) || "t".equals(context.get("toast_autovacuum_enabled")) || "f".equals(context.get("toast_autovacuum_enabled")));
    }
    
    private boolean anyIsTrue(final List<Object> collection) {
        for (Object each : collection) {
            if (each instanceof Boolean && (Boolean) each) {
                return true;
            }
        }
        return false;
    }
    
    private void getTableId(final Map<String, Object> context, final String schemaName, final String tableName, final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement
                        .executeQuery(String.format("SELECT tablename::REGCLASS::OID AS tid FROM pg_catalog.pg_tables WHERE schemaname = '%s' and tablename = '%s';", schemaName, tableName))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void getSchemaId(final Map<String, Object> context, final String schemaName, final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("select oid as scid from pg_catalog.pg_namespace where nspname = '%s';", schemaName))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void getDataBaseId(final Map<String, Object> context, final Connection connection) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("select oid as did, datlastsysoid from pg_catalog.pg_database where datname = '%s';", connection.getCatalog()))) {
            appendToMap(resultSet, context);
        }
    }
    
    private void appendToMap(final ResultSet resultSet, final Map<String, Object> map) throws SQLException {
        List<Map<String, Object>> rows = getRows(resultSet);
        appendFirstRow(rows, map);
    }
    
    private void appendFirstRow(final List<Map<String, Object>> rows, final Map<String, Object> context) {
        for (Map<String, Object> each : rows) {
            context.putAll(each);
            break;
        }
    }
    
    private List<Map<String, Object>> getRows(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<Map<String, Object>> result = new LinkedList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnName(i), resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }
    
    @SneakyThrows
    private List<Map<String, Object>> executeByTemplate(final Connection connection, final Map<String, Object> param, final String path) {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getSqlFromTemplate(param, path))) {
            return getRows(resultSet);
        }
    }
}
