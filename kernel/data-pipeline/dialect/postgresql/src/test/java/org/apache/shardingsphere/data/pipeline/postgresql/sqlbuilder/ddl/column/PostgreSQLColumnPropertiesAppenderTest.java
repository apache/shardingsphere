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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.column;

import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.PostgreSQLDDLTemplateExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("CollectionWithoutInitialCapacity")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostgreSQLColumnPropertiesAppenderTest {
    
    private final PostgreSQLColumnPropertiesAppender appender = new PostgreSQLColumnPropertiesAppender(mock(Connection.class), 0, 0);
    
    @Mock
    private PostgreSQLDDLTemplateExecutor templateExecutor;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        reset(templateExecutor);
        Plugins.getMemberAccessor().set(PostgreSQLColumnPropertiesAppender.class.getDeclaredField("templateExecutor"), appender, templateExecutor);
    }
    
    @Test
    void assertGetTypeAndInheritedColumnsWithoutTypeOrInheritsReturnsEmpty() {
        Map<String, Object> context = new LinkedHashMap<>();
        appender.append(context);
        assertThat(context.size(), is(1));
        assertThat(context.get("columns"), is(Collections.emptyList()));
    }
    
    @Test
    void assertGetTypeAndInheritedColumnsFromTypeUsesTemplate() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 10L);
        Map<String, Object> typeColumn = new LinkedHashMap<>();
        typeColumn.put("name", "col");
        typeColumn.put("atttypid", 1);
        typeColumn.put("attnum", 1);
        typeColumn.put("typnspname", "public");
        typeColumn.put("typname", "text");
        typeColumn.put("cltype", "text");
        typeColumn.put("attndims", 0);
        typeColumn.put("atttypmod", -1);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(typeColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(typeColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> result = (Collection<Map<String, Object>>) context.get("columns");
        assertThat(result, hasSize(1));
        assertThat(result.iterator().next().get("name"), is("col"));
    }
    
    @Test
    void assertGetTypeAndInheritedColumnsFromInheritsFiltersByName() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", new SimpleArray(new String[]{"parent"}));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.singletonList(createInheritEntry("parent", 1L)));
        Map<String, Object> inheritedColumn = createColumnWithName("col");
        inheritedColumn.put("inheritedfrom", "parent_table");
        when(templateExecutor.executeByTemplate(anyMap(), eq("table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(inheritedColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(inheritedColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        assertThat(context.get("coll_inherits"), is(Arrays.asList("parent")));
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> result = (Collection<Map<String, Object>>) context.get("columns");
        assertThat(result, hasSize(1));
        assertThat(result.iterator().next().get("name"), is("col"));
    }

    @Test
    void assertGetTypeAndInheritedColumnsFromInheritsWithNoMatchReturnsEmpty() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", new SimpleArray(new String[]{"missing"}));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.singletonList(createInheritEntry("parent", 1L)));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        assertThat(context.get("coll_inherits"), is(Collections.singletonList("missing")));
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> result = (Collection<Map<String, Object>>) context.get("columns");
        assertThat(result, hasSize(0));
    }
    
    @Test
    void assertGetTypeAndInheritedColumnsFromEmptyInheritsReturnsEmpty() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", new SimpleArray(new String[0]));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> result = (Collection<Map<String, Object>>) context.get("columns");
        assertThat(result, hasSize(0));
        assertThat(context.get("coll_inherits"), is(Collections.emptyList()));
    }

    @Test
    void assertGetInheritedFromTableOrTypeReturnsDefaultWhenNoTypeAndEmptyInherits() throws ReflectiveOperationException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", Collections.emptyList());
        String result = invoke(appender, "getInheritedFromTableOrType", Map.class, context);
        assertThat(result, is("inheritedfrom"));
    }

    @Test
    void assertGetInheritedFromTableOrTypeAppendsTableWhenInheritsPresent() throws ReflectiveOperationException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", Collections.singletonList("parent"));
        String result = invoke(appender, "getInheritedFromTableOrType", Map.class, context);
        assertThat(result, is("inheritedfromtable"));
    }

    @Test
    void assertGetInheritedFromTableOrTypeDefaultsWhenInheritsMissing() throws ReflectiveOperationException {
        Map<String, Object> context = new LinkedHashMap<>();
        String result = invoke(appender, "getInheritedFromTableOrType", Map.class, context);
        assertThat(result, is("inheritedfrom"));
    }

    @Test
    void assertAppendAddsInheritedFromTypeFieldForTypeColumns() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> typeColumn = createMapWithNameAndInherited("col", "parent_type");
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(typeColumn));
        Map<String, Object> propColumn = createColumnWithName("col");
        stubColumnProperties(propColumn);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("inheritedfromtype"), is("parent_type"));
    }

    @Test
    void assertAppendAddsInheritedFromTableFieldForInheritedColumns() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", new SimpleArray(new String[]{"parent"}));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.singletonList(createInheritEntry("parent", 5L)));
        Map<String, Object> inheritedColumn = createMapWithNameAndInherited("col", "parent_table");
        when(templateExecutor.executeByTemplate(anyMap(), eq("table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(inheritedColumn));
        Map<String, Object> propColumn = createColumnWithName("col");
        stubColumnProperties(propColumn);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("inheritedfromtable"), is("parent_table"));
    }

    @Test
    void assertAppendMarksPrimaryKeyWhenColumnInIndex() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("pk_col");
        column.put("attnum", 1);
        column.put("indkey", "1 2");
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("is_pk"), is(true));
        assertThat(result.get("is_primary_key"), is(true));
    }

    @Test
    void assertAppendLeavesPrimaryMarkersMissingWhenIndkeyMissing() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("pk_col");
        column.put("attnum", 1);
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.containsKey("is_pk"), is(false));
    }

    @Test
    void assertAppendMarksPrimaryColumnFalseWhenIndkeyDoesNotContainAttnum() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("pk_col");
        column.put("attnum", 1);
        column.put("indkey", "2");
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("is_pk"), is(false));
        assertThat(result.get("is_primary_key"), is(false));
    }

    @Test
    void assertAppendPopulatesNumericLengthPrecision() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("numeric_col");
        column.put("elemoid", 1231L);
        column.put("typname", "numeric");
        column.put("atttypmod", 4 + (5 << 16) + 2);
        column.put("cltype", "numeric(5,2)");
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("attlen"), is("5"));
        assertThat(result.get("attprecision"), is("2"));
    }

    @Test
    void assertAppendKeepsLengthAbsentWhenTypmodMissing() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("numeric_col");
        column.put("elemoid", 1231L);
        column.put("typname", "numeric");
        column.put("atttypmod", -1);
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.containsKey("attlen"), is(false));
    }

    @Test
    void assertAppendSkipsLengthForVarCharWithoutDigits() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("var_char_col");
        column.put("elemoid", 1043L); // VARCHAR type
        column.put("typname", "text");
        column.put("atttypmod", -1);
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.containsKey("attlen"), is(false));
        assertThat(result.containsKey("attprecision"), is(false));
    }

    @Test
    void assertAppendPopulatesVarCharLengthWhenTypmodProvidesDigits() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("var_char_col");
        column.put("elemoid", 1043L); // VARCHAR type
        column.put("typname", "varchar");
        column.put("atttypmod", 24); // typmod - 4 = 20 should be captured
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("attlen"), is("20"));
        assertThat(result.get("attprecision"), is(nullValue()));
    }

    @Test
    void assertAppendHandlesDateLengthBranch() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> typeColumn = createMapWithNameAndInherited("col", "parent_type");
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(typeColumn));
        Map<String, Object> column = createColumnWithName("date_col");
        column.put("elemoid", 1114L); // TIMESTAMP WITHOUT TIME ZONE
        column.put("typname", "timestamp without time zone");
        column.put("cltype", "timestamp(3) without time zone");
        column.put("atttypmod", 4 + (3 << 16));
        column.put("indkey", "1");
        column.put("attnum", 1);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("cltype"), is("timestamp without time zone"));
    }

    @Test
    void assertAppendSkipsLengthWhenElemoidUnknown() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 2L);
        Map<String, Object> typeColumn = createMapWithNameAndInherited("col", "parent_type");
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(typeColumn));
        Map<String, Object> column = createColumnWithName("unknown_col");
        column.put("elemoid", 9999L);
        column.put("typname", "unknown");
        column.put("cltype", "unknown");
        column.put("indkey", "1");
        column.put("attnum", 1);
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.containsKey("attlen"), is(false));
        assertThat(result.containsKey("attprecision"), is(false));
    }

    @Test
    void assertAppendFormatsColumnVariables() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("opt_col");
        column.put("attoptions", new SimpleArray(new String[]{"foo=bar"}));
        stubEmptyTypeColumns();
        stubColumnProperties(column);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        Collection<?> options = (Collection<?>) result.get("attoptions");
        assertThat(options, hasSize(1));
        Map<?, ?> option = (Map<?, ?>) options.iterator().next();
        assertThat(option.get("name"), is("foo"));
        assertThat(option.get("value"), is("bar"));
    }

    @Test
    void assertAppendCopiesInheritedFromTable() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", new SimpleArray(new String[]{"parent"}));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.singletonList(createInheritEntry("parent", 5L)));
        Map<String, Object> inheritedColumn = new LinkedHashMap<>();
        inheritedColumn.put("name", "col");
        inheritedColumn.put("inheritedfrom", "parent_table");
        when(templateExecutor.executeByTemplate(anyMap(), eq("table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(inheritedColumn));
        Map<String, Object> propColumn = createColumnWithName("col");
        stubColumnProperties(propColumn);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("inheritedfromtable"), is("parent_table"));
    }

    @Test
    void assertAppendCopiesInheritedFromType() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 99L);
        lenient().when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(createTypeColumnEntry("col", "type_def")));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.emptyList());
        Map<String, Object> propColumn = createColumnWithName("col");
        stubColumnProperties(propColumn);
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("inheritedfromtype"), is("type_def"));
    }

    @Test
    void assertAppendFormatsSecurityLabelsAndEditTypes() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createColumnWithName("col");
        column.put("cltype", "text(4)");
        column.put("typname", "text");
        column.put("typnspname", "public");
        column.put("seclabels", new SimpleArray(new String[]{"prov=lbl"}));
        Map<String, Object> property = createColumnWithName("col");
        property.putAll(column);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(property));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.singletonList(createEditModeTypesEntry("1", "alpha")));
        lenient().when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.emptyList());
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Map<String, Object> result = onlyColumn(context);
        assertThat(result.get("cltype"), is("text"));
        Collection<?> editTypes = (Collection<?>) result.get("edit_types");
        assertThat(editTypes, contains("alpha", "text(4)"));
        verify(templateExecutor).formatSecurityLabels(result);
    }

    @Test
    void assertGetEditTypesQueriesAndSorts() throws ReflectiveOperationException {
        List<Map<String, Object>> allColumns = Collections.singletonList(Collections.singletonMap("atttypid", 1));
        Array editArray = new SimpleArray(new String[]{"delta", "alpha"});
        Map<String, Object> mocked = new LinkedHashMap<>();
        mocked.put("main_oid", "1");
        mocked.put("edit_types", editArray);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.singletonList(mocked));
        Map<String, Collection<String>> result = invoke(appender, "getEditTypes", Collection.class, allColumns);
        assertThat(result.get("1"), contains("alpha", "delta"));
    }

    @Test
    void assertCheckTypmodNumeric() throws ReflectiveOperationException {
        String result = invoke(appender, "checkTypmod", Integer.class, String.class, 4 + (5 << 16) + 2, "numeric");
        assertThat(result, is("(5,2)"));
    }

    @Test
    void assertCheckTypmodTimeType() throws ReflectiveOperationException {
        String result = invoke(appender, "checkTypmod", Integer.class, String.class, 8, "time");
        assertThat(result, is("(8)"));
    }

    @Test
    void assertCheckTypmodInterval() throws ReflectiveOperationException {
        String result = invoke(appender, "checkTypmod", Integer.class, String.class, 3, "interval");
        assertThat(result, is("(3)"));
    }

    @Test
    void assertCheckTypmodDate() throws ReflectiveOperationException {
        String result = invoke(appender, "checkTypmod", Integer.class, String.class, 1, "date");
        assertThat(result, is(""));
    }

    @Test
    void assertCheckTypmodBitType() throws ReflectiveOperationException {
        String result = invoke(appender, "checkTypmod", Integer.class, String.class, 5, "bit");
        assertThat(result, is("(5)"));
    }

    @Test
    void assertCheckTypmodDefaultCaseSubtractsFour() throws ReflectiveOperationException {
        String result = invoke(appender, "checkTypmod", Integer.class, String.class, 10, "text");
        assertThat(result, is("(6)"));
    }

    @Test
    void assertCheckTypmodIntervalLenGreaterThanSixProducesEmptyPrecision() throws ReflectiveOperationException {
        int typmod = 7;
        String result = invoke(appender, "checkTypmod", Integer.class, String.class, typmod, "interval");
        assertThat(result, is("()"));
    }

    @Test
    void assertGetFullTypeValueCharCatalog() throws ReflectiveOperationException {
        String result = invoke(appender, "getFullTypeValue", String.class, String.class, String.class, String.class, "char", "pg_catalog", "(2)", "[]");
        assertThat(result, is("\"char\"[]"));
    }

    @Test
    void assertGetFullTypeValueTimeWithTimeZone() throws ReflectiveOperationException {
        String result = invoke(appender, "getFullTypeValue", String.class, String.class, String.class, String.class, "time with time zone", "public", "", "");
        assertThat(result, is("time with time zone"));
    }

    @Test
    void assertGetFullTypeValueTimeWithoutTimeZone() throws ReflectiveOperationException {
        String result = invoke(appender, "getFullTypeValue", String.class, String.class, String.class, String.class, "time without time zone", "public", "(2)", "[]");
        assertThat(result, is("time(2) without time zone[]"));
    }

    @Test
    void assertGetFullTypeValueTimestampWithTimeZone() throws ReflectiveOperationException {
        String result = invoke(appender, "getFullTypeValue", String.class, String.class, String.class, String.class, "timestamp with time zone", "public", "", "");
        assertThat(result, is("timestamp with time zone"));
    }

    @Test
    void assertGetFullTypeValueTimestampWithoutTimeZone() throws ReflectiveOperationException {
        String result = invoke(appender, "getFullTypeValue", String.class, String.class, String.class, String.class, "timestamp without time zone", "public", "", "[]");
        assertThat(result, is("timestamp without time zone[]"));
    }

    @Test
    void assertGetFullDataTypeResolvesSchemaWithoutQuotes() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", "public");
        column.put("typname", "public.int4");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int4"));
    }

    @Test
    void assertGetFullDataTypeHandlesSchemaWithQuotes() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", "public");
        column.put("typname", "public.\"char\"");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("char"));
    }

    @Test
    void assertGetFullDataTypeHandlesArrayAndPrefix() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "_int4[]");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int4[]"));
    }

    @Test
    void assertGetFullDataTypeSkipsNumdimsAdjustmentWhenAttndimsNonZeroForUnderscore() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "_int8");
        column.put("attndims", 2);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int8"));
    }

    @Test
    void assertGetFullDataTypeSkipsNumdimsAdjustmentWhenAttndimsNonZeroForArray() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "_int8[]");
        column.put("attndims", 2);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int8"));
    }

    @Test
    void assertGetFullDataTypeHandlesArrayWithoutPrefixWhenAttndimsMissing() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "int4[]");
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int4[]"));
    }

    @Test
    void assertGetFullDataTypeHandlesArrayWithoutPrefixWhenAttndimsZero() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "int4[]");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int4[]"));
    }

    @Test
    void assertGetFullDataTypeHandlesQuotedNameWithoutSchema() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "\"char\"");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("char"));
    }

    @Test
    void assertGetFullDataTypeHandlesQuotedSchemaDot() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", "public");
        column.put("typname", "public\".\"foo\"");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("foo\""));
    }

    @Test
    void assertGetFullDataTypeIgnoresTrailingQuoteWithoutLeading() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "foo\"");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("foo\""));
    }

    @Test
    void assertGetFullDataTypeHandlesUnderscoreWithoutAttndims() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "_int4");
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int4[]"));
    }

    @Test
    void assertGetFullDataTypeArrayWhenAttndimsMissingLeavesNumdimsNull() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "_int8[]");
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int8[]"));
    }

    @Test
    void assertGetFullDataTypeSkipsArrayLengthWhenNumdimsGreaterThanOne() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "int8[]");
        column.put("attndims", 2);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int8"));
    }

    @Test
    void assertGetFullDataTypeLeavesNameWithOpeningQuoteOnly() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "\"foo");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("\"foo"));
    }

    @Test
    void assertCheckSchemaInNameHandlesQuotedSchemaDot() throws ReflectiveOperationException {
        String result = invoke(appender, "checkSchemaInName", String.class, String.class, "public\".\"foo\"", "public");
        assertThat(result, is("foo\""));
    }

    @Test
    void assertParseTypeNameHandlesArraySuffix() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "text[]");
        assertThat(result, is("text[]"));
    }

    @Test
    void assertParseTypeNameHandlesTimeParentheses() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "time(3) with time zone");
        assertThat(result, is("time with time zone"));
    }

    @Test
    void assertParseTypeNameHandlesInterval() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "interval");
        assertThat(result, is("interval"));
    }

    @Test
    void assertParseTypeNameStripsSimpleParentheses() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "text(4)");
        assertThat(result, is("text"));
    }

    @Test
    void assertParseTypeNameHandlesTimeWithoutParenthesis() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "time");
        assertThat(result, is("time"));
    }

    @Test
    void assertParseTypeNameHandlesTimeWithSuffix() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "time(3) with zone");
        assertThat(result, is("time with zone"));
    }

    @Test
    void assertParseTypeNameIgnoresTimeBranchWhenPrefixMismatch() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "foo(time");
        assertThat(result, is("foo(time"));
    }

    @Test
    void assertAppendWithNoColumnsDoesNotLookupEditTypes() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 10L);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        assertThat(context.get("columns"), is(Collections.emptyList()));
        verify(templateExecutor, never()).executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"));
    }

    @Test
    void assertAppendPopulatesInheritedAndEditTypes() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 20L);
        Map<String, Object> typeColumn = new LinkedHashMap<>();
        typeColumn.put("name", "col");
        typeColumn.put("inheritedfrom", "parent");
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(typeColumn));
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("name", "col");
        column.put("atttypid", 1);
        column.put("attnum", 1);
        column.put("indkey", "1");
        column.put("elemoid", 1231L);
        column.put("typname", "numeric");
        column.put("typnspname", "public");
        column.put("attndims", 0);
        column.put("atttypmod", 4 + (5 << 16) + 2);
        column.put("cltype", "numeric(5,2)");
        Map<String, Object> unmatchedColumn = createUnmatchedColumn();
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Arrays.asList(column, unmatchedColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.singletonList(createEditModeTypesEntry("1", "alpha")));
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());
        appender.append(context);
        Collection<?> resultColumns = (Collection<?>) context.get("columns");
        assertThat(resultColumns, hasSize(2));
        @SuppressWarnings("unchecked")
        Map<String, Object> resultColumn = resultColumns.stream()
                .map(each -> (Map<String, Object>) each)
                .filter(each -> "col".equals(each.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing column 'col'"));
        assertThat(resultColumn.get("inheritedfromtype"), is("parent"));
        assertThat(resultColumn.get("attlen"), is("5"));
        assertThat(resultColumn.get("attprecision"), is("2"));
        assertThat(resultColumn.get("is_pk"), is(true));
        assertThat(resultColumn.get("cltype"), is("numeric"));
        assertThat((Collection<?>) resultColumn.get("edit_types"), contains("alpha", "numeric(5,2)"));
    }

    private static Map<String, Object> createInheritEntry(final String inherits, final long oid) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("inherits", inherits);
        entry.put("oid", oid);
        return entry;
    }

    private Map<String, Object> createColumnWithName(final String name) {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("name", name);
        column.put("cltype", "text");
        column.put("typname", "text");
        column.put("typnspname", "public");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        column.put("atttypid", 1);
        return column;
    }

    private Map<String, Object> createMapWithNameAndInherited(final String name, final String inheritedFrom) {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("name", name);
        column.put("inheritedfrom", inheritedFrom);
        return column;
    }

    private Map<String, Object> createTypeColumnEntry(final String name, final String inheritedFrom) {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("name", name);
        column.put("inheritedfrom", inheritedFrom);
        return column;
    }

    private void stubEmptyTypeColumns() {
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.emptyList());
    }

    private void stubColumnProperties(final Map<String, Object> column) {
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
    }

    private Map<String, Object> onlyColumn(final Map<String, Object> context) {
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> columns = (Collection<Map<String, Object>>) context.get("columns");
        return columns.iterator().next();
    }

    private static Map<String, Object> createUnmatchedColumn() {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("name", "other");
        column.put("atttypid", 2);
        column.put("cltype", "text");
        column.put("typname", "text");
        column.put("typnspname", "public");
        column.put("attndims", 0);
        column.put("atttypmod", -1);
        return column;
    }

    private static Map<String, Object> createEditModeTypesEntry(final String mainOid, final String... editTypes) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("main_oid", mainOid);
        entry.put("edit_types", new SimpleArray(editTypes));
        return entry;
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(final PostgreSQLColumnPropertiesAppender target, final String methodName, final Class<?> parameterType, final Object parameter) throws ReflectiveOperationException {
        Method method = PostgreSQLColumnPropertiesAppender.class.getDeclaredMethod(methodName, parameterType);
        method.setAccessible(true);
        return (T) method.invoke(target, parameter);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(final PostgreSQLColumnPropertiesAppender target, final String methodName, final Class<?> firstType, final Class<?> secondType, final Object first, final Object second) throws ReflectiveOperationException {
        Method method = PostgreSQLColumnPropertiesAppender.class.getDeclaredMethod(methodName, firstType, secondType);
        method.setAccessible(true);
        return (T) method.invoke(target, first, second);
    }

    @SuppressWarnings("unchecked")
    private static <T> T invoke(final PostgreSQLColumnPropertiesAppender target, final String methodName, final Class<?> firstType, final Class<?> secondType, final Class<?> thirdType, final Class<?> fourthType, final Object first, final Object second, final Object third, final Object fourth) throws ReflectiveOperationException {
        Method method = PostgreSQLColumnPropertiesAppender.class.getDeclaredMethod(methodName, firstType, secondType, thirdType, fourthType);
        method.setAccessible(true);
        return (T) method.invoke(target, first, second, third, fourth);
    }

    private static final class SimpleArray implements Array {

        private final Object data;

        private SimpleArray(final Object data) {
            this.data = data;
        }

        @Override
        public String getBaseTypeName() {
            return null;
        }

        @Override
        public int getBaseType() {
            return 0;
        }

        @Override
        public Object getArray() {
            return data;
        }

        @Override
        public Object getArray(final Map<String, Class<?>> map) {
            return data;
        }

        @Override
        public Object getArray(final long index, final int count) {
            return data;
        }

        @Override
        public Object getArray(final long index, final int count, final Map<String, Class<?>> map) {
            return data;
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public ResultSet getResultSet(final Map<String, Class<?>> map) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public ResultSet getResultSet(final long index, final int count) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public ResultSet getResultSet(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public void free() {
        }
    }
}
