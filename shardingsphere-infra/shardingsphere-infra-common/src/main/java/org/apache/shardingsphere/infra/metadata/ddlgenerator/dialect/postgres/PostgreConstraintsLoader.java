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

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Postgre constraints loader.
 */
@RequiredArgsConstructor
public final class PostgreConstraintsLoader extends PostgreAbstractLoader {
    
    private final Connection connection;
    
    /**
     * Load constraints.
     * 
     * @param context load context
     */
    public void loadConstraints(final Map<String, Object> context) {
        loadPrimaryOrUniqueConstraint(context, "primary_key", "p");
        loadPrimaryOrUniqueConstraint(context, "unique_constraint", "u");
        context.put("foreign_key", fetchForeignKeys(context));
        context.put("check_constraint", fetchCheckConstraints(context));
        context.put("exclude_constraint", getExclusionConstraints(context));
    }
    
    private List<Map<String, Object>> fetchCheckConstraints(final Map<String, Object> context) {
        List<Map<String, Object>> checkConstraints = new LinkedList<>();
        for (Map<String, Object> each : getCheckConstraints((Long) context.get("tid"))) {
            if (!isPartitionAndConstraintInherited(each, context)) {
                checkConstraints.add(each);
            }
        }
        return checkConstraints;
    }
    
    private List<Map<String, Object>> fetchForeignKeys(final Map<String, Object> context) {
        List<Map<String, Object>> foreignKeys = new LinkedList<>();
        for (Map<String, Object> each : getForeignKeys((Long) context.get("tid"))) {
            if (!isPartitionAndConstraintInherited(each, context)) {
                foreignKeys.add(each);
            }
        }
        return foreignKeys;
    }
    
    private void loadPrimaryOrUniqueConstraint(final Map<String, Object> context, final String name, final String type) {
        List<Map<String, Object>> constraintsProperties = fetchConstraintsProperties(context, type);
        fetchConstraintsColumns(constraintsProperties);
        context.put(name, constraintsProperties);
    }
    
    private void fetchConstraintsColumns(final List<Map<String, Object>> constraintsProperties) {
        for (Map<String, Object> each : constraintsProperties) {
            List<Map<String, Object>> columns = new LinkedList<>();
            for (Map<String, Object> col : fetchConstraintsCols(each)) {
                Map<String, Object> column = new HashMap<>();
                column.put("column", col.get("column"));
                columns.add(column);
            }
            each.put("columns", columns);
            each.put("include", new LinkedList<>());
        }
    }
    
    private List<Map<String, Object>> fetchConstraintsCols(final Map<String, Object> constraintColProperties) {
        Map<String, Object> map = new HashMap<>();
        map.put("cid", constraintColProperties.get("oid"));
        map.put("colcnt", constraintColProperties.get("col_count"));
        return executeByTemplate(connection, map, "index_constraint/default/get_costraint_cols.ftl");
    }
    
    private List<Map<String, Object>> fetchConstraintsProperties(final Map<String, Object> context, final String constraintType) {
        Map<String, Object> param = new HashMap<>();
        param.put("did", context.get("did"));
        param.put("tid", context.get("tid"));
        param.put("cid", context.get("cid"));
        param.put("constraint_type", constraintType);
        return executeByTemplate(connection, param, "index_constraint/11_plus/properties.ftl");
    }
    
    private List<Map<String, Object>> getExclusionConstraints(final Map<String, Object> context) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", context.get("tid"));
        param.put("did", context.get("did"));
        List<Map<String, Object>> result = executeByTemplate(connection, param, "exclusion_constraint/11_plus/properties.ftl");
        for (Map<String, Object> each : result) {
            getExclusionConstraintsColumns(each);
        }
        return result;
    }
    
    private void getExclusionConstraintsColumns(final Map<String, Object> exclusionConstraintsProperties) {
        Map<String, Object> p = new HashMap<>();
        p.put("cid", exclusionConstraintsProperties.get("oid"));
        p.put("col_count", exclusionConstraintsProperties.get("col_count"));
        List<Map<String, Object>> columns = new LinkedList<>();
        for (Map<String, Object> each : executeByTemplate(connection, p, "exclusion_constraint/9.2_plus/get_constraint_cols.ftl")) {
            boolean order = (((int) each.get("options")) & 1) == 0;
            boolean nullsOrder = (((int) each.get("options")) & 2) != 0;
            Map<String, Object> col = new HashMap<>();
            col.put("column", strip((String) each.get("coldef")));
            col.put("oper_class", each.get("opcname"));
            col.put("order", order);
            col.put("nulls_order", nullsOrder);
            col.put("operator", each.get("oprname"));
            col.put("col_type", each.get("datatype"));
            col.put("is_exp", each.get("is_exp"));
            columns.add(col);
        }
        exclusionConstraintsProperties.put("columns", columns);
        Map<String, Object> map = new HashMap<>();
        map.put("cid", exclusionConstraintsProperties.get("oid"));
        List<String> include = new LinkedList<>();
        for (Map<String, Object> each : executeByTemplate(connection, map, "exclusion_constraint/11_plus/get_constraint_include.ftl")) {
            include.add(each.get("colname").toString());
        }
        exclusionConstraintsProperties.put("include", include);
    }
    
    private List<Map<String, Object>> getForeignKeys(final Long tid) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        List<Map<String, Object>> result = executeByTemplate(connection, param, "foreign_key/9.1_plus/properties.ftl");
        for (Map<String, Object> each : result) {
            List<Map<String, Object>> columns = new LinkedList<>();
            Set<String> cols = new HashSet<>();
            for (Map<String, Object> col : getForeignKeysCols(tid, each)) {
                Map<String, Object> f = new HashMap<>();
                f.put("local_column", col.get("conattname"));
                f.put("references", each.get("confrelid"));
                f.put("referenced", col.get("confattname"));
                f.put("references_table_name", each.get("refnsp") + "." + each.get("reftab"));
                columns.add(f);
                cols.add((String) col.get("conattname"));
            }
            setRemoteName(each, columns);
            Optional<String> coveringindex = searchCoveringindex(tid, cols, connection);
            each.put("coveringindex", coveringindex.orElse(null));
            each.put("autoindex", !coveringindex.isPresent());
            each.put("hasindex", coveringindex.isPresent());
            each.put("columns", columns);
        }
        return result;
    }
    
    private void setRemoteName(final Map<String, Object> each, final List<Map<String, Object>> columns) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", columns.get(0).get("references"));
        List<Map<String, Object>> r = executeByTemplate(connection, param, "foreign_key/default/get_parent.ftl");
        for (Map<String, Object> sf : r) {
            each.put("remote_schema", sf.get("schema"));
            each.put("remote_table", sf.get("table"));
            break;
        }
    }
    
    private List<Map<String, Object>> getForeignKeysCols(final Long tid, final Map<String, Object> foreginKeyProperties) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        List<Map<String, Object>> keys = new LinkedList<>();
        Map<String, Object> s = new HashMap<>();
        s.put("confkey", foreginKeyProperties.get("confkey"));
        s.put("conkey", foreginKeyProperties.get("conkey"));
        keys.add(s);
        param.put("keys", keys);
        return executeByTemplate(connection, param, "foreign_key/default/get_constraint_cols.ftl");
    }
    
    private boolean isPartitionAndConstraintInherited(final Map<String, Object> constraint, final Map<String, Object> context) {
        return context.containsKey("relispartition") && (boolean) context.get("relispartition") && constraint.containsKey("conislocal") && (boolean) constraint.get("conislocal");
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
    
    private String strip(final String column) {
        if (column.startsWith("\"")) {
            return column.substring(1);
        }
        if (column.endsWith("\"")) {
            return column.substring(0, column.length() - 1);
        }
        return column;
    }
    
    private List<Map<String, Object>> getCheckConstraints(final Long tid) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        return executeByTemplate(connection, param, "check_constraint/9.2_plus/get_cols.ftl");
    }
}
