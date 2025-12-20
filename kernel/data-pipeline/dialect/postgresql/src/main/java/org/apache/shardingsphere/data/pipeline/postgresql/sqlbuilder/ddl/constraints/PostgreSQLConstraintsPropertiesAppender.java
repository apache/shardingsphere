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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.constraints;

import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.PostgreSQLDDLTemplateExecutor;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Constraints properties appender for PostgreSQL.
 */
public final class PostgreSQLConstraintsPropertiesAppender {
    
    private static final Integer PG_CONSTRAINTS_INCLUDE_VERSION = 11;
    
    private final PostgreSQLDDLTemplateExecutor templateExecutor;
    
    public PostgreSQLConstraintsPropertiesAppender(final Connection connection, final int majorVersion, final int minorVersion) {
        templateExecutor = new PostgreSQLDDLTemplateExecutor(connection, majorVersion, minorVersion);
    }
    
    /**
     * Append constraints properties.
     *
     * @param context create table SQL context
     */
    public void append(final Map<String, Object> context) {
        loadPrimaryOrUniqueConstraint(context, "primary_key", "p");
        loadPrimaryOrUniqueConstraint(context, "unique_constraint", "u");
        context.put("foreign_key", fetchForeignKeys(context));
        context.put("check_constraint", fetchCheckConstraints(context));
        context.put("exclude_constraint", getExclusionConstraints(context));
    }
    
    private void loadPrimaryOrUniqueConstraint(final Map<String, Object> context, final String name, final String type) {
        Collection<Map<String, Object>> constraintsProps = fetchConstraintsProperties(context, type);
        fetchConstraintsColumns(constraintsProps);
        context.put(name, constraintsProps.stream().filter(each -> !isPartitionAndConstraintInherited(each, context)).collect(Collectors.toList()));
    }
    
    private Collection<Map<String, Object>> fetchConstraintsProperties(final Map<String, Object> context, final String constraintType) {
        Map<String, Object> params = new HashMap<>(4, 1F);
        params.put("did", context.get("did"));
        params.put("tid", context.get("tid"));
        params.put("cid", context.get("cid"));
        params.put("constraint_type", constraintType);
        return templateExecutor.executeByTemplate(params, "component/index_constraint/%s/properties.ftl");
    }
    
    private void fetchConstraintsColumns(final Collection<Map<String, Object>> constraintsProps) {
        for (Map<String, Object> each : constraintsProps) {
            each.put("columns",
                    fetchConstraintsCols(each).stream().<Map<String, Object>>map(col -> Collections.singletonMap("column", stripQuote((String) col.get("column")))).collect(Collectors.toList()));
            appendConstraintsInclude(each);
        }
    }
    
    private void appendConstraintsInclude(final Map<String, Object> constraintsProp) {
        Collection<Object> includes = templateExecutor.getMajorVersion() >= PG_CONSTRAINTS_INCLUDE_VERSION
                ? templateExecutor.executeByTemplate(Collections.singletonMap("cid", constraintsProp.get("oid")),
                        "component/index_constraint/%s/get_constraint_include.ftl").stream().map(each -> each.get("colname")).collect(Collectors.toList())
                : Collections.emptyList();
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
        Map<String, Object> params = new HashMap<>(2, 1F);
        params.put("cid", constraintColProps.get("oid"));
        params.put("colcnt", constraintColProps.get("col_count"));
        return templateExecutor.executeByTemplate(params, "component/index_constraint/%s/get_costraint_cols.ftl");
    }
    
    private Collection<Map<String, Object>> fetchForeignKeys(final Map<String, Object> context) {
        return getForeignKeys((Long) context.get("tid")).stream().filter(each -> !isPartitionAndConstraintInherited(each, context)).collect(Collectors.toList());
    }
    
    private Collection<Map<String, Object>> fetchCheckConstraints(final Map<String, Object> context) {
        return getCheckConstraints((Long) context.get("tid")).stream().filter(each -> !isPartitionAndConstraintInherited(each, context)).collect(Collectors.toList());
    }
    
    private Collection<Map<String, Object>> getExclusionConstraints(final Map<String, Object> context) {
        Map<String, Object> params = new HashMap<>(2, 1F);
        params.put("tid", context.get("tid"));
        params.put("did", context.get("did"));
        Collection<Map<String, Object>> result = templateExecutor.executeByTemplate(params, "component/exclusion_constraint/%s/properties.ftl");
        for (Map<String, Object> each : result) {
            getExclusionConstraintsColumns(each);
        }
        return result;
    }
    
    private void getExclusionConstraintsColumns(final Map<String, Object> exclusionConstraintsProps) {
        Map<String, Object> params = new HashMap<>(2, 1F);
        params.put("cid", exclusionConstraintsProps.get("oid"));
        params.put("col_count", exclusionConstraintsProps.get("col_count"));
        Collection<Map<String, Object>> columns = new LinkedList<>();
        for (Map<String, Object> each : templateExecutor.executeByTemplate(params, "component/exclusion_constraint/%s/get_constraint_cols.ftl")) {
            boolean order = 0 == (((int) each.get("options")) & 1);
            boolean nullsOrder = 0 != (((int) each.get("options")) & 2);
            Map<String, Object> col = new HashMap<>(7, 1F);
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
        Collection<String> include = new LinkedList<>();
        if (templateExecutor.getMajorVersion() >= PG_CONSTRAINTS_INCLUDE_VERSION) {
            Map<String, Object> map = Collections.singletonMap("cid", exclusionConstraintsProps.get("oid"));
            for (Map<String, Object> each : templateExecutor.executeByTemplate(map, "exclusion_constraint/%s/get_constraint_include.ftl")) {
                include.add(each.get("colname").toString());
            }
        }
        exclusionConstraintsProps.put("include", include);
    }
    
    private Collection<Map<String, Object>> getForeignKeys(final Long tid) {
        Collection<Map<String, Object>> result = templateExecutor.executeByTemplate(Collections.singletonMap("tid", tid), "component/foreign_key/%s/properties.ftl");
        for (Map<String, Object> each : result) {
            Collection<Map<String, Object>> columns = new LinkedList<>();
            Set<String> cols = new HashSet<>();
            for (Map<String, Object> col : getForeignKeysCols(tid, each)) {
                Map<String, Object> foreignKeysRef = new HashMap<>(4, 1F);
                foreignKeysRef.put("local_column", col.get("conattname"));
                foreignKeysRef.put("references", each.get("confrelid"));
                foreignKeysRef.put("referenced", col.get("confattname"));
                foreignKeysRef.put("references_table_name", each.get("refnsp") + "." + each.get("reftab"));
                columns.add(foreignKeysRef);
                cols.add((String) col.get("conattname"));
            }
            setRemoteName(each, columns);
            Optional<String> coveringIndex = searchCoveringIndex(tid, cols);
            each.put("coveringindex", coveringIndex.orElse(null));
            each.put("autoindex", !coveringIndex.isPresent());
            each.put("hasindex", coveringIndex.isPresent());
            each.put("columns", columns);
        }
        return result;
    }
    
    private void setRemoteName(final Map<String, Object> foreignKey, final Collection<Map<String, Object>> columns) {
        Map<String, Object> parents = templateExecutor.executeByTemplateForSingleRow(
                Collections.singletonMap("tid", columns.iterator().next().get("references")), "component/foreign_key/%s/get_parent.ftl");
        foreignKey.put("remote_schema", parents.get("schema"));
        foreignKey.put("remote_table", parents.get("table"));
    }
    
    private Collection<Map<String, Object>> getForeignKeysCols(final Long tid, final Map<String, Object> foreignKeyProps) {
        Map<String, Object> params = new HashMap<>(2, 1F);
        params.put("tid", tid);
        Map<String, Object> key = new HashMap<>(2, 1F);
        key.put("confkey", foreignKeyProps.get("confkey"));
        key.put("conkey", foreignKeyProps.get("conkey"));
        params.put("keys", Collections.singleton(key));
        return templateExecutor.executeByTemplate(params, "component/foreign_key/%s/get_constraint_cols.ftl");
    }
    
    private boolean isPartitionAndConstraintInherited(final Map<String, Object> constraint, final Map<String, Object> context) {
        return context.containsKey("relispartition") && (boolean) context.get("relispartition") && constraint.containsKey("conislocal") && (boolean) constraint.get("conislocal");
    }
    
    private Optional<String> searchCoveringIndex(final Long tid, final Set<String> cols) {
        for (Map<String, Object> each : templateExecutor.executeByTemplate(Collections.singletonMap("tid", tid), "component/foreign_key/%s/get_constraints.ftl")) {
            Map<String, Object> map = new HashMap<>(2, 1F);
            map.put("cid", each.get("oid"));
            map.put("colcnt", each.get("col_count"));
            Collection<Map<String, Object>> rows = templateExecutor.executeByTemplate(map, "component/foreign_key/%s/get_cols.ftl");
            Set<String> indexCols = new HashSet<>(rows.size(), 1F);
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
        if (copyIndexCols.isEmpty()) {
            cols.removeAll(indexCols);
            return cols.isEmpty();
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
        return templateExecutor.executeByTemplate(Collections.singletonMap("tid", tid), "component/check_constraint/%s/properties.ftl");
    }
}
