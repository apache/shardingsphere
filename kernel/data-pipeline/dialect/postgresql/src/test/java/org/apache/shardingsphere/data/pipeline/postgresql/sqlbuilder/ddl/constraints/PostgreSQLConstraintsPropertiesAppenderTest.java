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

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.PostgreSQLDDLTemplateExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class PostgreSQLConstraintsPropertiesAppenderTest {
    
    private final AtomicInteger constraintsCallCounter = new AtomicInteger();
    
    @Test
    void assertAppendWithFullCoverage() {
        constraintsCallCounter.set(0);
        PostgreSQLDDLTemplateExecutor templateExecutor = mock(PostgreSQLDDLTemplateExecutor.class);
        when(templateExecutor.getMajorVersion()).thenReturn(11);
        when(templateExecutor.executeByTemplate(anyMap(), anyString())).thenAnswer(invocation -> mockExecuteByTemplateFullCoverage(invocation.getArgument(0), invocation.getArgument(1)));
        when(templateExecutor.executeByTemplateForSingleRow(anyMap(), anyString())).thenReturn(createParentTable());
        Map<String, Object> context = createContext(true);
        createAppender(templateExecutor).append(context);
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
        List<Map<String, Object>> foreignKeys = new LinkedList<>((Collection<Map<String, Object>>) context.get("foreign_key"));
        assertThat(foreignKeys.size(), is(2));
        Map<String, Object> foreignKeyWithIndex = foreignKeys.get(0);
        assertThat(foreignKeyWithIndex.get("coveringindex"), is("idx_fk_col1"));
        assertFalse((boolean) foreignKeyWithIndex.get("autoindex"));
        assertTrue((boolean) foreignKeyWithIndex.get("hasindex"));
        assertThat(foreignKeyWithIndex.get("remote_schema"), is("remote_schema"));
        assertThat(foreignKeyWithIndex.get("remote_table"), is("remote_table"));
        Map<String, Object> foreignKeyColumn = ((Collection<Map<String, Object>>) foreignKeyWithIndex.get("columns")).iterator().next();
        assertThat(foreignKeyColumn.get("local_column"), is("fk_col1"));
        assertThat(foreignKeyColumn.get("referenced"), is("ref_col1"));
        assertThat(foreignKeyColumn.get("references_table_name"), is("public.ref_table"));
        Map<String, Object> foreignKeyWithoutIndex = foreignKeys.get(1);
        assertThat(foreignKeyWithoutIndex.get("coveringindex"), is((Object) null));
        assertTrue((boolean) foreignKeyWithoutIndex.get("autoindex"));
        assertFalse((boolean) foreignKeyWithoutIndex.get("hasindex"));
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
    void assertAppendWithoutIncludeWhenVersionLowerThan11() {
        PostgreSQLDDLTemplateExecutor templateExecutor = mock(PostgreSQLDDLTemplateExecutor.class);
        when(templateExecutor.getMajorVersion()).thenReturn(10);
        when(templateExecutor.executeByTemplate(anyMap(), anyString())).thenAnswer(invocation -> mockExecuteByTemplateWithoutInclude(invocation.getArgument(1)));
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
    
    @SneakyThrows(ReflectiveOperationException.class)
    private PostgreSQLConstraintsPropertiesAppender createAppender(final PostgreSQLDDLTemplateExecutor templateExecutor) {
        PostgreSQLConstraintsPropertiesAppender result = new PostgreSQLConstraintsPropertiesAppender(mock(Connection.class), 0, 0);
        Plugins.getMemberAccessor().set(PostgreSQLConstraintsPropertiesAppender.class.getDeclaredField("templateExecutor"), result, templateExecutor);
        return result;
    }
    
    private Map<String, Object> createContext(final boolean relispartition) {
        Map<String, Object> result = new HashMap<>(4, 1F);
        result.put("did", 2L);
        result.put("tid", 1L);
        result.put("cid", 3L);
        result.put("relispartition", relispartition);
        return result;
    }
    
    private Collection<Map<String, Object>> createIndexConstraintsProps() {
        Map<String, Object> filtered = new HashMap<>(3, 1F);
        filtered.put("oid", 1L);
        filtered.put("col_count", 2);
        filtered.put("conislocal", true);
        Map<String, Object> remained = new HashMap<>(3, 1F);
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
        Map<String, Object> remained = new HashMap<>(3, 1F);
        remained.put("oid", 5L);
        remained.put("col_count", 1);
        remained.put("conislocal", false);
        return Collections.singletonList(remained);
    }
    
    private Map<String, Object> createExclusionConstraintProps() {
        Map<String, Object> result = new HashMap<>(2, 1F);
        result.put("oid", 3L);
        result.put("col_count", 2);
        return result;
    }
    
    private Collection<Map<String, Object>> createExclusionConstraintColumns() {
        Map<String, Object> first = new HashMap<>(6, 1F);
        first.put("options", 0);
        first.put("coldef", "\"colA\"");
        first.put("opcname", "opc1");
        first.put("oprname", "opr1");
        first.put("datatype", "int");
        first.put("is_exp", false);
        Map<String, Object> second = new HashMap<>(6, 1F);
        second.put("options", 3);
        second.put("coldef", "colB\"");
        second.put("opcname", "opc2");
        second.put("oprname", "opr2");
        second.put("datatype", "text");
        second.put("is_exp", true);
        return new LinkedList<>(Arrays.asList(first, second));
    }
    
    private Map<String, Object> createSimpleExclusionColumn() {
        Map<String, Object> result = new HashMap<>(6, 1F);
        result.put("options", 0);
        result.put("coldef", "colC");
        result.put("opcname", "opc");
        result.put("oprname", "opr");
        result.put("datatype", "text");
        result.put("is_exp", false);
        return result;
    }
    
    private Collection<Map<String, Object>> createForeignKeyProps() {
        Map<String, Object> retained = new HashMap<>(6, 1F);
        retained.put("confrelid", 10L);
        retained.put("refnsp", "public");
        retained.put("reftab", "ref_table");
        retained.put("confkey", 1);
        retained.put("conkey", 1);
        retained.put("conislocal", false);
        Map<String, Object> second = new HashMap<>(6, 1F);
        second.put("confrelid", 30L);
        second.put("refnsp", "public");
        second.put("reftab", "ref_table2");
        second.put("confkey", 3);
        second.put("conkey", 3);
        second.put("conislocal", false);
        Map<String, Object> filtered = new HashMap<>(6, 1F);
        filtered.put("confrelid", 20L);
        filtered.put("refnsp", "public");
        filtered.put("reftab", "ignored");
        filtered.put("confkey", 2);
        filtered.put("conkey", 2);
        filtered.put("conislocal", true);
        return new LinkedList<>(Arrays.asList(retained, second, filtered));
    }
    
    private Collection<Map<String, Object>> createForeignKeyColumns(final Map<String, Object> params) {
        Collection<Map<String, Object>> keys = (Collection<Map<String, Object>>) params.get("keys");
        String confKey = keys.iterator().next().get("confkey").toString();
        Map<String, Object> column = new HashMap<>(2, 1F);
        if ("1".equals(confKey)) {
            column.put("conattname", "fk_col1");
            column.put("confattname", "ref_col1");
        } else if ("3".equals(confKey)) {
            column.put("conattname", "fk_col3");
            column.put("confattname", "ref_col3");
        } else {
            column.put("conattname", "fk_col2");
            column.put("confattname", "ref_col2");
        }
        return Collections.singletonList(column);
    }
    
    private Map<String, Object> createCoveringIndex(final long oid, final String idxName) {
        Map<String, Object> result = new HashMap<>(3, 1F);
        result.put("oid", oid);
        result.put("col_count", 1);
        result.put("idxname", idxName);
        return result;
    }
    
    private Collection<Map<String, Object>> createCoveringIndexColumns(final Map<String, Object> params) {
        Map<String, Object> column = new HashMap<>(1, 1F);
        if (6L == (long) params.get("cid")) {
            column.put("column", "fk_col1");
        } else if (7L == (long) params.get("cid")) {
            column.put("column", "other_col");
        } else {
            column.put("column", "another_col");
        }
        return Collections.singletonList(column);
    }
    
    private Map<String, Object> createParentTable() {
        Map<String, Object> result = new HashMap<>(2, 1F);
        result.put("schema", "remote_schema");
        result.put("table", "remote_table");
        return result;
    }
    
    private Collection<Map<String, Object>> createCheckConstraints() {
        Collection<Map<String, Object>> result = new LinkedList<>();
        Map<String, Object> filtered = new HashMap<>(1, 1F);
        filtered.put("conislocal", true);
        Map<String, Object> remained = new HashMap<>(1, 1F);
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
                int callCount = constraintsCallCounter.incrementAndGet();
                if (1 == callCount) {
                    return Arrays.asList(createCoveringIndex(7L, "idx_other"), createCoveringIndex(6L, "idx_fk_col1"));
                }
                if (2 == callCount) {
                    return Collections.singletonList(createCoveringIndex(8L, "idx_not_match"));
                }
                return Collections.emptyList();
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
