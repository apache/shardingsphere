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

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Postgres constraints properties appender.
 */
public final class PostgresConstraintsPropertiesAppender extends AbstractPostgresDDLAdapter {
    
    private static final Integer PG_CONSTRAINTS_INCLUDE_VERSION = 11;
    
    public PostgresConstraintsPropertiesAppender(final Connection connection, final int majorVersion, final int minorVersion) {
        super(connection, majorVersion, minorVersion);
    }
    
    /**
     * Append constraints properties.
     * 
     * @param context create table sql context
     */
    public void append(final Map<String, Object> context) {
        loadPrimaryOrUniqueConstraint(context, "primary_key", "p");
        loadPrimaryOrUniqueConstraint(context, "unique_constraint", "u");
        context.put("foreign_key", fetchForeignKeys(context));
        context.put("check_constraint", fetchCheckConstraints(context));
        context.put("exclude_constraint", getExclusionConstraints(context));
    }
    
    private Collection<Map<String, Object>> fetchCheckConstraints(final Map<String, Object> context) {
        Collection<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : getCheckConstraints((Long) context.get("tid"))) {
            if (!isPartitionAndConstraintInherited(each, context)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<Map<String, Object>> fetchForeignKeys(final Map<String, Object> context) {
        Collection<Map<String, Object>> result = new LinkedList<>();
        for (Map<String, Object> each : getForeignKeys((Long) context.get("tid"))) {
            if (!isPartitionAndConstraintInherited(each, context)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private void loadPrimaryOrUniqueConstraint(final Map<String, Object> context, final String name, final String type) {
        Collection<Map<String, Object>> constraintsProps = fetchConstraintsProperties(context, type);
        fetchConstraintsColumns(constraintsProps);
        context.put(name, constraintsProps.stream().filter(each -> !isPartitionAndConstraintInherited(each, context)).collect(Collectors.toList()));
    }
    
    private void fetchConstraintsColumns(final Collection<Map<String, Object>> constraintsProps) {
        for (Map<String, Object> each : constraintsProps) {
            Collection<Map<String, Object>> columns = new LinkedList<>();
            for (Map<String, Object> col : fetchConstraintsCols(each)) {
                Map<String, Object> column = new HashMap<>();
                column.put("column", stripQuote((String) col.get("column")));
                columns.add(column);
            }
            each.put("columns", columns);
            appendConstraintsInclude(each);
        }
    }
    
    private void appendConstraintsInclude(final Map<String, Object> constraintsProp) {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("cid", constraintsProp.get("oid"));
        Collection<Object> includes = new LinkedList<>();
        if (getMajorVersion() >= PG_CONSTRAINTS_INCLUDE_VERSION) {
            for (Map<String, Object> each : executeByTemplate(parameters, "component/index_constraint/%s/get_constraint_include.ftl")) {
                includes.add(each.get("colname"));
            }
        }
        constraintsProp.put("include", includes);
    }
    
    private String stripQuote(final String column) {
        String result = column;
        if (column.startsWith("\"")) {
            result = result.substring(1);
        }
        if (column.endsWith("\"")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
    
    private Collection<Map<String, Object>> fetchConstraintsCols(final Map<String, Object> constraintColProps) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", constraintColProps.get("oid"));
        parameters.put("colcnt", constraintColProps.get("col_count"));
        return executeByTemplate(parameters, "component/index_constraint/%s/get_costraint_cols.ftl");
    }
    
    private Collection<Map<String, Object>> fetchConstraintsProperties(final Map<String, Object> context, final String constraintType) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("did", context.get("did"));
        parameters.put("tid", context.get("tid"));
        parameters.put("cid", context.get("cid"));
        parameters.put("constraint_type", constraintType);
        return executeByTemplate(parameters, "component/index_constraint/%s/properties.ftl");
    }
    
    private Collection<Map<String, Object>> getExclusionConstraints(final Map<String, Object> context) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tid", context.get("tid"));
        parameters.put("did", context.get("did"));
        Collection<Map<String, Object>> result = executeByTemplate(parameters, "component/exclusion_constraint/%s/properties.ftl");
        for (Map<String, Object> each : result) {
            getExclusionConstraintsColumns(each);
        }
        return result;
    }
    
    private void getExclusionConstraintsColumns(final Map<String, Object> exclusionConstraintsProps) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cid", exclusionConstraintsProps.get("oid"));
        parameters.put("col_count", exclusionConstraintsProps.get("col_count"));
        Collection<Map<String, Object>> columns = new LinkedList<>();
        for (Map<String, Object> each : executeByTemplate(parameters, "component/exclusion_constraint/%s/get_constraint_cols.ftl")) {
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
        exclusionConstraintsProps.put("columns", columns);
        Map<String, Object> map = new HashMap<>();
        map.put("cid", exclusionConstraintsProps.get("oid"));
        Collection<String> include = new LinkedList<>();
        if (getMajorVersion() >= PG_CONSTRAINTS_INCLUDE_VERSION) {
            for (Map<String, Object> each : executeByTemplate(map, "exclusion_constraint/%s/get_constraint_include.ftl")) {
                include.add(each.get("colname").toString());
            }
        }
        exclusionConstraintsProps.put("include", include);
    }
    
    private Collection<Map<String, Object>> getForeignKeys(final Long tid) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tid", tid);
        Collection<Map<String, Object>> result = executeByTemplate(parameters, "component/foreign_key/%s/properties.ftl");
        for (Map<String, Object> each : result) {
            Collection<Map<String, Object>> columns = new LinkedList<>();
            Set<String> cols = new HashSet<>();
            for (Map<String, Object> col : getForeignKeysCols(tid, each)) {
                Map<String, Object> foreignKeysRef = new HashMap<>();
                foreignKeysRef.put("local_column", col.get("conattname"));
                foreignKeysRef.put("references", each.get("confrelid"));
                foreignKeysRef.put("referenced", col.get("confattname"));
                foreignKeysRef.put("references_table_name", each.get("refnsp") + "." + each.get("reftab"));
                columns.add(foreignKeysRef);
                cols.add((String) col.get("conattname"));
            }
            setRemoteName(each, columns);
            Optional<String> coveringindex = searchCoveringIndex(tid, cols);
            each.put("coveringindex", coveringindex.orElse(null));
            each.put("autoindex", !coveringindex.isPresent());
            each.put("hasindex", coveringindex.isPresent());
            each.put("columns", columns);
        }
        return result;
    }
    
    private void setRemoteName(final Map<String, Object> foreignKey, final Collection<Map<String, Object>> columns) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tid", columns.iterator().next().get("references"));
        Collection<Map<String, Object>> parents = executeByTemplate(parameters, "component/foreign_key/%s/get_parent.ftl");
        for (Map<String, Object> each : parents) {
            foreignKey.put("remote_schema", each.get("schema"));
            foreignKey.put("remote_table", each.get("table"));
            break;
        }
    }
    
    private Collection<Map<String, Object>> getForeignKeysCols(final Long tid, final Map<String, Object> foreignKeyProps) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tid", tid);
        Collection<Map<String, Object>> keys = new LinkedList<>();
        Map<String, Object> key = new HashMap<>();
        key.put("confkey", foreignKeyProps.get("confkey"));
        key.put("conkey", foreignKeyProps.get("conkey"));
        keys.add(key);
        parameters.put("keys", keys);
        return executeByTemplate(parameters, "component/foreign_key/%s/get_constraint_cols.ftl");
    }
    
    private boolean isPartitionAndConstraintInherited(final Map<String, Object> constraint, final Map<String, Object> context) {
        return context.containsKey("relispartition") && (boolean) context.get("relispartition") && constraint.containsKey("conislocal") && (boolean) constraint.get("conislocal");
    }
    
    private Optional<String> searchCoveringIndex(final Long tid, final Set<String> cols) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tid", tid);
        for (Map<String, Object> each : executeByTemplate(parameters, "component/foreign_key/%s/get_constraints.ftl")) {
            Map<String, Object> map = new HashMap<>();
            map.put("cid", each.get("oid"));
            map.put("colcnt", each.get("col_count"));
            Collection<Map<String, Object>> rows = executeByTemplate(map, "component/foreign_key/%s/get_cols.ftl");
            Set<String> indexCols = new HashSet<>();
            for (Map<String, Object> row : rows) {
                indexCols.add(strip(row.get("column").toString()));
            }
            if (isSame(indexCols, cols)) {
                return Optional.of((String) each.get("idxname"));
            }
        }
        return Optional.empty();
    }
    
    private boolean isSame(final Set<String> indexCols, final Set<String> cols) {
        Set<String> copyIndexCols = new HashSet<>(indexCols);
        Set<String> copyCols = new HashSet<>(cols);
        copyIndexCols.removeAll(copyCols);
        if (0 == copyIndexCols.size()) {
            cols.removeAll(indexCols);
            return 0 == cols.size();
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
    
    private Collection<Map<String, Object>> getCheckConstraints(final Long tid) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tid", tid);
        return executeByTemplate(parameters, "component/check_constraint/%s/properties.ftl");
    }
}
