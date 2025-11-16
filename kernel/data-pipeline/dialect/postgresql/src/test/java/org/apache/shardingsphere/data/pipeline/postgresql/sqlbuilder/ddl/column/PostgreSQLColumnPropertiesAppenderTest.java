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
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
    void assertGetTypeAndInheritedColumnsFromInheritsWithNoMatchReturnsEmpty() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", new SimpleArray(new String[]{"missing"}));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.singleton(createInheritEntry("parent", 1L)));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        assertThat(context.get("coll_inherits"), is(Collections.singletonList("missing")));
        assertThat(context.get("columns"), is(Collections.emptyList()));
    }
    
    @Test
    void assertAppendUsesDefaultInheritedFromWithEmptyInheritsAndInheritedColumns() throws SQLException {
        // Given: Context with BOTH typoid (null) and empty coll_inherits to trigger the missing branch
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", null);  // Explicitly set to null
        context.put("coll_inherits", new SimpleArray(new String[0]));  // Empty array, not null

        // Mock base column properties
        Map<String, Object> baseColumn = createColumnWithName("test_col");
        baseColumn.put("inheritedfrom", "parent_table");

        // Mock inherited columns to trigger the inheritance processing logic through getColumnFromType
        Map<String, Object> inheritedColumn = new LinkedHashMap<>();
        inheritedColumn.put("name", "test_col");
        inheritedColumn.put("inheritedfrom", "parent_table");

        // Even with null typoid, getTypeAndInheritedColumns should return columns through getColumnFromType if properly mocked
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl"))
                .thenReturn(Collections.singletonList(baseColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("table/%s/get_columns_for_table.ftl")))
                .thenReturn(Collections.singletonList(inheritedColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl")))
                .thenReturn(Collections.emptyList());
        doNothing().when(templateExecutor).formatSecurityLabels(anyMap());

        // When
        appender.append(context);

        // Then
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> columns = (Collection<Map<String, Object>>) context.get("columns");
        assertThat(columns, hasSize(1));
        Map<String, Object> resultColumn = columns.iterator().next();

        // This should use default "inheritedfrom" since typoid is null and coll_inherits is empty
        assertThat(resultColumn.get("inheritedfrom"), is("parent_table"));
        assertThat(resultColumn.containsKey("inheritedfromtype"), is(false));
        assertThat(resultColumn.containsKey("inheritedfromtable"), is(false));
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
        column.put("elemoid", 1043L);
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
    void assertAppendHandlesDateLengthBranch() throws SQLException {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> typeColumn = createMapWithNameAndInherited("col", "parent_type");
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(typeColumn));
        Map<String, Object> column = createColumnWithName("date_col");
        column.put("elemoid", 1114L);
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
    void assertGetFullDataTypeArrayWhenAttndimsMissingLeavesNumdimsNull() throws ReflectiveOperationException {
        Map<String, Object> column = new LinkedHashMap<>();
        column.put("typnspname", null);
        column.put("typname", "_int8[]");
        column.put("atttypmod", -1);
        String result = invoke(appender, "getFullDataType", Map.class, column);
        assertThat(result, is("int8[]"));
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
    void assertParseTypeNameHandlesInterval() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "interval");
        assertThat(result, is("interval"));
    }

    @Test
    void assertParseTypeNameIgnoresTimeBranchWhenPrefixMismatch() throws ReflectiveOperationException {
        String result = invoke(appender, "parseTypeName", String.class, "foo(time");
        assertThat(result, is("foo(time"));
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
