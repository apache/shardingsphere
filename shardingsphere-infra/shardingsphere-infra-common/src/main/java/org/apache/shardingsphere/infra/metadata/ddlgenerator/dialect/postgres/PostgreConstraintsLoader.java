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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PostgreConstraintsLoader extends PostgreAbstractLoader {
    
    private final Connection connection;
    
    
    public void loadConstraints(final Map<String, Object> context) {
        addConstraintsToOutput(context, connection);
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
    
    private String strip(String column) {
        if (column.startsWith("\"")) {
            column = column.substring(1);
        }
        if (column.endsWith("\"")) {
            column = column.substring(0, column.length() - 1);
        }
        return column;
    }
    
    private List<Map<String, Object>> getCheckConstraints(final Long tid, final Connection connection) {
        Map<String, Object> param = new HashMap<>();
        param.put("tid", tid);
        return executeByTemplate(connection, param, "check_constraint/9.2_plus/get_cols.ftl");
    }
}
