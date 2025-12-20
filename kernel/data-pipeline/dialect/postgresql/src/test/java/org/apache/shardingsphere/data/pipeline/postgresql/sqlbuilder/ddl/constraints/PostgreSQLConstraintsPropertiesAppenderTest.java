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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLConstraintsPropertiesAppenderTest {
    
    @Test
    void assertAppendWithFullCoverage() throws Exception {
        PostgreSQLDDLTemplateExecutor templateExecutor = mock(PostgreSQLDDLTemplateExecutor.class);
        when(templateExecutor.getMajorVersion()).thenReturn(11);
        when(templateExecutor.executeByTemplate(anyMap(), anyString())).thenAnswer(invocation -> mockExecuteByTemplateFullCoverage(invocation.getArgument(0), invocation.getArgument(1)));
        when(templateExecutor.executeByTemplateForSingleRow(anyMap(), anyString())).thenReturn(createParentTable());
        PostgreSQLConstraintsPropertiesAppender appender = createAppender(templateExecutor);
        Map<String, Object> context = createContext(true);
        appender.append(context);
        Collection<Map<String, Object>> primaryKeys = (Collection<Map<String, Object>>) context.get("primary_key");
        assertThat(primaryKeys.size(), is(1));
        Map<String, Object> primaryKey = primaryKeys.iterator().next();
        List<String> primaryColumns = ((Collection<Map<String, Object>>) primaryKey.get("columns")).stream().map(each -> (String) each.get("column")).collect(Collectors.toList());
        assertThat(primaryColumns, is(Arrays.asList("id", "name")));
        assertThat(primaryKey.get("include"), is(Collections.singletonList("include_col")));
        Collection<Map<String, Object>> uniqueConstraints = (Collection<Map<String, Object>>) context.get("unique_constraint");
        assertThat(uniqueConstraints.size(), is(1));
        Collection<Map<String, Object>> checkConstraints = (Collection<Map<String, Object>>) context.get("check_constraint");
        assertThat(checkConstraints.size(), is(1));
        assertFalse((boolean) checkConstraints.iterator().next().get("conislocal"));
        Collection<Map<String, Object>> foreignKeys = (Collection<Map<String, Object>>) context.get("foreign_key");
        assertThat(foreignKeys.size(), is(1));
        Map<String, Object> foreignKey = foreignKeys.iterator().next();
        assertThat(foreignKey.get("coveringindex"), is("idx_fk_col1"));
        assertFalse((boolean) foreignKey.get("autoindex"));
        assertTrue((boolean) foreignKey.get("hasindex"));
        assertThat(foreignKey.get("remote_schema"), is("remote_schema"));
        assertThat(foreignKey.get("remote_table"), is("remote_table"));
        Map<String, Object> foreignKeyColumn = ((Collection<Map<String, Object>>) foreignKey.get("columns")).iterator().next();
        assertThat(foreignKeyColumn.get("local_column"), is("fk_col1"));
        assertThat(foreignKeyColumn.get("referenced"), is("ref_col1"));
        assertThat(foreignKeyColumn.get("references_table_name"), is("public.ref_table"));
        Collection<Map<String, Object>> exclusionConstraints = (Collection<Map<String, Object>>) context.get("exclude_constraint");
        assertThat(exclusionConstraints.size(), is(1));
        Map<String, Object> exclusionConstraint = exclusionConstraints.iterator().next();
        List<Boolean> orders = ((Collection<Map<String, Object>>) exclusionConstraint.get("columns")).stream().map(each -> (Boolean) each.get("order")).collect(Collectors.toList());
        List<Boolean> nullsOrders = ((Collection<Map<String, Object>>) exclusionConstraint.get("columns")).stream().map(each -> (Boolean) each.get("nulls_order")).collect(Collectors.toList());
        assertThat(orders, is(Arrays.asList(true, false)));
        assertThat(nullsOrders, is(Arrays.asList(false, true)));
        assertThat(exclusionConstraint.get("include"), is(Collections.singletonList("excl_inc")));
    }
    
    @Test
    void assertAppendWithoutIncludeWhenVersionLowerThan11() throws Exception {
        PostgreSQLDDLTemplateExecutor templateExecutor = mock(PostgreSQLDDLTemplateExecutor.class);
        when(templateExecutor.getMajorVersion()).thenReturn(10);
        when(templateExecutor.executeByTemplate(anyMap(), anyString())).thenAnswer(invocation -> mockExecuteByTemplateWithoutInclude(invocation.getArgument(1)));
        when(templateExecutor.executeByTemplateForSingleRow(anyMap(), anyString())).thenReturn(Collections.emptyMap());
        PostgreSQLConstraintsPropertiesAppender appender = createAppender(templateExecutor);
        Map<String, Object> context = createContext(false);
        appender.append(context);
        Collection<Map<String, Object>> primaryKeys = (Collection<Map<String, Object>>) context.get("primary_key");
        assertThat(primaryKeys.size(), is(1));
        assertThat(primaryKeys.iterator().next().get("include"), is(Collections.emptyList()));
        Collection<Map<String, Object>> uniqueConstraints = (Collection<Map<String, Object>>) context.get("unique_constraint");
        assertThat(uniqueConstraints.size(), is(1));
        Collection<Map<String, Object>> exclusionConstraints = (Collection<Map<String, Object>>) context.get("exclude_constraint");
        assertThat(exclusionConstraints.size(), is(1));
        assertThat(exclusionConstraints.iterator().next().get("include"), is(Collections.emptyList()));
    }
    
    private PostgreSQLConstraintsPropertiesAppender createAppender(final PostgreSQLDDLTemplateExecutor templateExecutor) throws Exception {
        PostgreSQLConstraintsPropertiesAppender result = new PostgreSQLConstraintsPropertiesAppender(mock(Connection.class), 0, 0);
        Field field = PostgreSQLConstraintsPropertiesAppender.class.getDeclaredField("templateExecutor");
        field.setAccessible(true);
        field.set(result, templateExecutor);
        return result;
    }
    
    private Map<String, Object> createContext(final boolean relispartition) {
        Map<String, Object> result = new HashMap<>();
        result.put("did", 2L);
        result.put("tid", 1L);
        result.put("cid", 3L);
        result.put("relispartition", relispartition);
        return result;
    }
    
    private Collection<Map<String, Object>> createIndexConstraintsProps() {
        Map<String, Object> filtered = new HashMap<>();
        filtered.put("oid", 1L);
        filtered.put("col_count", 2);
        filtered.put("conislocal", true);
        Map<String, Object> remained = new HashMap<>();
        remained.put("oid", 2L);
        remained.put("col_count", 1);
        remained.put("conislocal", false);
        return new LinkedList<>(Arrays.asList(filtered, remained));
    }
    
    private Collection<Map<String, Object>> createIndexConstraintColumns() {
        Collection<Map<String, Object>> result = new LinkedList<>();
        result.add(Collections.singletonMap("column", "\"id\""));
        result.add(Collections.singletonMap("column", "name"));
        return result;
    }
    
    private Collection<Map<String, Object>> createSingleConstraintProps() {
        Map<String, Object> remained = new HashMap<>();
        remained.put("oid", 5L);
        remained.put("col_count", 1);
        remained.put("conislocal", false);
        return Collections.singletonList(remained);
    }
    
    private Map<String, Object> createExclusionConstraintProps() {
        Map<String, Object> result = new HashMap<>();
        result.put("oid", 3L);
        result.put("col_count", 2);
        return result;
    }
    
    private Collection<Map<String, Object>> createExclusionConstraintColumns() {
        Map<String, Object> first = new HashMap<>();
        first.put("options", 0);
        first.put("coldef", "\"colA\"");
        first.put("opcname", "opc1");
        first.put("oprname", "opr1");
        first.put("datatype", "int");
        first.put("is_exp", false);
        Map<String, Object> second = new HashMap<>();
        second.put("options", 3);
        second.put("coldef", "colB\"");
        second.put("opcname", "opc2");
        second.put("oprname", "opr2");
        second.put("datatype", "text");
        second.put("is_exp", true);
        return new LinkedList<>(Arrays.asList(first, second));
    }
    
    private Map<String, Object> createSimpleExclusionColumn() {
        Map<String, Object> result = new HashMap<>();
        result.put("options", 0);
        result.put("coldef", "colC");
        result.put("opcname", "opc");
        result.put("oprname", "opr");
        result.put("datatype", "text");
        result.put("is_exp", false);
        return result;
    }
    
    private Collection<Map<String, Object>> createForeignKeyProps() {
        Map<String, Object> retained = new HashMap<>();
        retained.put("confrelid", 10L);
        retained.put("refnsp", "public");
        retained.put("reftab", "ref_table");
        retained.put("confkey", 1);
        retained.put("conkey", 1);
        retained.put("conislocal", false);
        Map<String, Object> filtered = new HashMap<>();
        filtered.put("confrelid", 20L);
        filtered.put("refnsp", "public");
        filtered.put("reftab", "ignored");
        filtered.put("confkey", 2);
        filtered.put("conkey", 2);
        filtered.put("conislocal", true);
        return new LinkedList<>(Arrays.asList(retained, filtered));
    }
    
    private Collection<Map<String, Object>> createForeignKeyColumns(final Map<String, Object> params) {
        Collection<Map<String, Object>> keys = (Collection<Map<String, Object>>) params.get("keys");
        String confKey = keys.iterator().next().get("confkey").toString();
        Map<String, Object> column = new HashMap<>();
        if ("1".equals(confKey)) {
            column.put("conattname", "fk_col1");
            column.put("confattname", "ref_col1");
        } else {
            column.put("conattname", "fk_col2");
            column.put("confattname", "ref_col2");
        }
        return Collections.singletonList(column);
    }
    
    private Map<String, Object> createCoveringIndex() {
        Map<String, Object> result = new HashMap<>();
        result.put("oid", 6L);
        result.put("col_count", 1);
        result.put("idxname", "idx_fk_col1");
        return result;
    }
    
    private Collection<Map<String, Object>> createCoveringIndexColumns(final Map<String, Object> params) {
        Map<String, Object> column = new HashMap<>();
        if (6L == (long) params.get("cid")) {
            column.put("column", "fk_col1");
        } else {
            column.put("column", "other_col");
        }
        return Collections.singletonList(column);
    }
    
    private Map<String, Object> createParentTable() {
        Map<String, Object> result = new HashMap<>();
        result.put("schema", "remote_schema");
        result.put("table", "remote_table");
        return result;
    }
    
    private Collection<Map<String, Object>> createCheckConstraints() {
        Collection<Map<String, Object>> result = new LinkedList<>();
        Map<String, Object> filtered = new HashMap<>();
        filtered.put("conislocal", true);
        Map<String, Object> remained = new HashMap<>();
        remained.put("conislocal", false);
        result.add(filtered);
        result.add(remained);
        return result;
    }
    
    private Collection<Map<String, Object>> mockExecuteByTemplateFullCoverage(final Map<String, Object> params, final String path) {
        switch (path) {
            case "component/index_constraint/%s/properties.ftl":
                return createIndexConstraintsProps();
            case "component/index_constraint/%s/get_costraint_cols.ftl":
                return createIndexConstraintColumns();
            case "component/index_constraint/%s/get_constraint_include.ftl":
                return Collections.singletonList(Collections.singletonMap("colname", "include_col"));
            case "component/exclusion_constraint/%s/properties.ftl":
                return Collections.singletonList(createExclusionConstraintProps());
            case "component/exclusion_constraint/%s/get_constraint_cols.ftl":
                return createExclusionConstraintColumns();
            case "exclusion_constraint/%s/get_constraint_include.ftl":
                return Collections.singletonList(Collections.singletonMap("colname", "excl_inc"));
            case "component/foreign_key/%s/properties.ftl":
                return createForeignKeyProps();
            case "component/foreign_key/%s/get_constraint_cols.ftl":
                return createForeignKeyColumns(params);
            case "component/foreign_key/%s/get_constraints.ftl":
                return Collections.singletonList(createCoveringIndex());
            case "component/foreign_key/%s/get_cols.ftl":
                return createCoveringIndexColumns(params);
            case "component/check_constraint/%s/properties.ftl":
                return createCheckConstraints();
            default:
                return Collections.emptyList();
        }
    }
    
    private Collection<Map<String, Object>> mockExecuteByTemplateWithoutInclude(final String path) {
        switch (path) {
            case "component/index_constraint/%s/properties.ftl":
                return createSingleConstraintProps();
            case "component/index_constraint/%s/get_costraint_cols.ftl":
                return Collections.singletonList(Collections.singletonMap("column", "\"only_col\""));
            case "component/exclusion_constraint/%s/properties.ftl":
                return Collections.singletonList(createExclusionConstraintProps());
            case "component/exclusion_constraint/%s/get_constraint_cols.ftl":
                return Collections.singletonList(createSimpleExclusionColumn());
            default:
                return Collections.emptyList();
        }
    }
}
