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

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.PostgreSQLDDLTemplateExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
        context.put("coll_inherits", mockSQLArray(new String[]{"missing"}));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.singleton(createInheritEntry(1L)));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        assertThat(context.get("coll_inherits"), is(Collections.singletonList("missing")));
        assertThat(context.get("columns"), is(Collections.emptyList()));
    }
    
    @Test
    void assertAppendUsesDefaultInheritedFromWithEmptyInheritsAndInheritedColumns() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", null);
        context.put("coll_inherits", mockSQLArray(new String[0]));
        Map<String, Object> baseColumn = createTextColumnWithName("test_col");
        baseColumn.put("inheritedfrom", "parent_table");
        Map<String, Object> inheritedColumn = new LinkedHashMap<>();
        inheritedColumn.put("name", "test_col");
        inheritedColumn.put("inheritedfrom", "parent_table");
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singletonList(baseColumn));
        when(templateExecutor.executeByTemplate(context, "table/%s/get_columns_for_table.ftl")).thenReturn(Collections.singletonList(inheritedColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("inheritedfrom"), is("parent_table"));
        assertFalse(singleColumn.containsKey("inheritedfromtype"));
        assertFalse(singleColumn.containsKey("inheritedfromtable"));
    }
    
    @Test
    void assertAppendLeavesPrimaryMarkersMissingWhenIndkeyMissing() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("pk_col");
        column.put("attnum", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        assertFalse(getSingleColumn(context).containsKey("is_pk"));
    }
    
    @Test
    void assertAppendMarksPrimaryColumnFalseWhenIndkeyDoesNotContainAttnum() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("pk_col");
        column.put("attnum", 1);
        column.put("indkey", "2");
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("is_pk"), is(false));
        assertThat(singleColumn.get("is_primary_key"), is(false));
    }
    
    @Test
    void assertAppendKeepsLengthAbsentWhenTypmodMissing() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("numeric_col");
        column.put("elemoid", 1231L);
        column.put("typname", "numeric");
        column.put("atttypmod", -1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        assertFalse(getSingleColumn(context).containsKey("attlen"));
    }
    
    @Test
    void assertAppendSkipsLengthForVarCharWithoutDigits() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("var_char_col");
        column.put("elemoid", 1043L);
        column.put("typname", "text");
        column.put("atttypmod", -1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertFalse(singleColumn.containsKey("attlen"));
        assertFalse(singleColumn.containsKey("attprecision"));
    }
    
    @Test
    void assertAppendHandlesDateLengthBranch() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> typeColumn = createMapWithNameAndInherited();
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.singleton(typeColumn));
        Map<String, Object> column = createTextColumnWithName("date_col");
        column.put("elemoid", 1114L);
        column.put("typname", "timestamp without time zone");
        column.put("cltype", "timestamp(3) without time zone");
        column.put("atttypmod", 4 + (3 << 16));
        column.put("indkey", "1");
        column.put("attnum", 1);
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(column));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        assertThat(getSingleColumn(context).get("cltype"), is("timestamp without time zone"));
    }
    
    @Test
    void assertAppendSkipsLengthWhenElemoidUnknown() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 2L);
        Map<String, Object> typeColumn = createMapWithNameAndInherited();
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.singletonList(typeColumn));
        Map<String, Object> column = createTextColumnWithName("unknown_col");
        column.put("elemoid", 9999L);
        column.put("typname", "unknown");
        column.put("cltype", "unknown");
        column.put("indkey", "1");
        column.put("attnum", 1);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertFalse(singleColumn.containsKey("attlen"));
        assertFalse(singleColumn.containsKey("attprecision"));
    }
    
    @Test
    void assertAppendFormatsColumnVariables() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("opt_col");
        column.put("attoptions", mockSQLArray(new String[]{"foo=bar"}));
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Collection<?> options = (Collection<?>) getSingleColumn(context).get("attoptions");
        assertThat(options.size(), is(1));
        Map<?, ?> option = (Map<?, ?>) options.iterator().next();
        assertThat(option.get("name"), is("foo"));
        assertThat(option.get("value"), is("bar"));
    }
    
    @Test
    void assertAppendCopiesInheritedFromTable() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("coll_inherits", mockSQLArray(new String[]{"parent"}));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_inherits.ftl"))).thenReturn(Collections.singletonList(createInheritEntry(5L)));
        Map<String, Object> inheritedColumn = new LinkedHashMap<>();
        inheritedColumn.put("name", "col");
        inheritedColumn.put("inheritedfrom", "parent_table");
        when(templateExecutor.executeByTemplate(anyMap(), eq("table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.singletonList(inheritedColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(createTextColumnWithName("col")));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        assertThat(getSingleColumn(context).get("inheritedfromtable"), is("parent_table"));
    }
    
    @Test
    void assertNormalizeSequenceValuesRemovesGroupingSeparator() {
        Map<String, Object> column = createTextColumnWithName("id");
        column.put("attidentity", "a");
        column.put("colconstype", "i");
        column.put("seqincrement", "1");
        column.put("seqstart", "1");
        column.put("seqmin", "1");
        column.put("seqmax", "2,147,483,647");
        column.put("seqcache", "1");
        Map<String, Object> context = new LinkedHashMap<>();
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("seqincrement"), is(1L));
        assertThat(singleColumn.get("seqstart"), is(1L));
        assertThat(singleColumn.get("seqmin"), is(1L));
        assertThat(singleColumn.get("seqmax"), is(2147483647L));
        assertThat(singleColumn.get("seqcache"), is(1L));
    }
    
    @Test
    void assertNormalizeSequenceValuesKeepsNumberValues() {
        Map<String, Object> column = createTextColumnWithName("id");
        column.put("attidentity", "a");
        column.put("colconstype", "i");
        column.put("seqincrement", 1L);
        Map<String, Object> singleColumn = appendWithSingleColumn(column);
        assertThat(singleColumn.get("seqincrement"), is(1L));
    }
    
    @Test
    void assertNormalizeSequenceValuesIgnoresNullValue() {
        Map<String, Object> column = createTextColumnWithName("id");
        column.put("attidentity", "a");
        column.put("colconstype", "i");
        column.put("seqmax", null);
        Map<String, Object> singleColumn = appendWithSingleColumn(column);
        assertThat(singleColumn.get("seqmax"), nullValue());
    }
    
    @Test
    void assertNormalizeSequenceValuesRemovesInvalidFormat() {
        Map<String, Object> column = createTextColumnWithName("id");
        column.put("attidentity", "a");
        column.put("colconstype", "i");
        column.put("seqmax", ", ,");
        Map<String, Object> singleColumn = appendWithSingleColumn(column);
        assertFalse(singleColumn.containsKey("seqmax"));
    }
    
    @Test
    void assertNormalizeSequenceValuesRemovesMultipleSigns() {
        Map<String, Object> column = createTextColumnWithName("id");
        column.put("attidentity", "a");
        column.put("colconstype", "i");
        column.put("seqmax", "+-123");
        Map<String, Object> singleColumn = appendWithSingleColumn(column);
        assertFalse(singleColumn.containsKey("seqmax"));
    }
    
    @Test
    void assertNormalizeSequenceValuesRemovesOverflowValue() {
        Map<String, Object> column = createTextColumnWithName("id");
        column.put("attidentity", "a");
        column.put("colconstype", "i");
        column.put("seqmax", "999999999999999999999999");
        Map<String, Object> singleColumn = appendWithSingleColumn(column);
        assertFalse(singleColumn.containsKey("seqmax"));
    }
    
    @Test
    void assertCheckTypmodInterval() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> intervalColumn = createTextColumnWithName("interval_col");
        intervalColumn.put("elemoid", 1186L);
        intervalColumn.put("typname", "interval");
        intervalColumn.put("typnspname", "pg_catalog");
        intervalColumn.put("atttypmod", 3);
        intervalColumn.put("cltype", "interval");
        intervalColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(intervalColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("atttypmod"), is(3));
        assertThat(singleColumn.get("typname"), is("interval"));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("interval"));
    }
    
    @Test
    void assertCheckTypmodDate() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> dateColumn = createTextColumnWithName("date_col");
        dateColumn.put("elemoid", 1083L);
        dateColumn.put("typname", "date");
        dateColumn.put("typnspname", "pg_catalog");
        dateColumn.put("atttypmod", 1);
        dateColumn.put("cltype", "date");
        dateColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(dateColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("atttypmod"), is(1));
        assertThat(singleColumn.get("typname"), is("date"));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("date"));
    }
    
    @Test
    void assertCheckTypmodBitType() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> bitColumn = createTextColumnWithName("bit_col");
        bitColumn.put("elemoid", 1560L);
        bitColumn.put("typname", "bit");
        bitColumn.put("typnspname", "pg_catalog");
        bitColumn.put("atttypmod", 5);
        bitColumn.put("cltype", "bit");
        bitColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singleton(bitColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("atttypmod"), is(5));
        assertThat(singleColumn.get("typname"), is("bit"));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("bit"));
    }
    
    @Test
    void assertCheckTypmodDefaultCaseSubtractsFour() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> textColumn = createTextColumnWithName("text_col");
        textColumn.put("elemoid", 9999L);
        textColumn.put("typname", "text");
        textColumn.put("typnspname", "pg_catalog");
        textColumn.put("atttypmod", 10);
        textColumn.put("cltype", "text");
        textColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singleton(textColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("atttypmod"), is(10));
        assertThat(singleColumn.get("typname"), is("text"));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("text"));
    }
    
    @Test
    void assertCheckTypmodIntervalLenGreaterThanSixProducesEmptyPrecision() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> intervalColumn = createTextColumnWithName("interval_col");
        intervalColumn.put("elemoid", 1186L);
        intervalColumn.put("typname", "interval");
        intervalColumn.put("typnspname", "pg_catalog");
        intervalColumn.put("atttypmod", 7);
        intervalColumn.put("cltype", "interval");
        intervalColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singleton(intervalColumn));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("atttypmod"), is(7));
        assertThat(singleColumn.get("typname"), is("interval"));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("interval"));
    }
    
    @Test
    void assertGetFullTypeValueCharCatalog() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> charColumn = createTextColumnWithName("char_col");
        charColumn.put("elemoid", 18L);
        charColumn.put("typname", "\"char\"");
        charColumn.put("typnspname", "pg_catalog");
        charColumn.put("atttypmod", 2);
        charColumn.put("cltype", "\"char\"");
        charColumn.put("attndims", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(charColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("\"char\""));
        assertThat(singleColumn.get("typnspname"), is("pg_catalog"));
        assertThat(singleColumn.get("atttypmod"), is(2));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("\"char\""));
    }
    
    @Test
    void assertGetFullTypeValueTimeWithTimeZone() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> timeColumn = createTextColumnWithName("time_col");
        timeColumn.put("elemoid", 1186L);
        timeColumn.put("typname", "time with time zone");
        timeColumn.put("typnspname", "public");
        timeColumn.put("atttypmod", -1);
        timeColumn.put("cltype", "time with time zone");
        timeColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(timeColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("time with time zone"));
        assertThat(singleColumn.get("typnspname"), is("public"));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("time with time zone"));
    }
    
    @Test
    void assertGetFullTypeValueTimeWithoutTimeZone() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> timeColumn = createTextColumnWithName("time_col");
        timeColumn.put("elemoid", 1183L);
        timeColumn.put("typname", "time without time zone");
        timeColumn.put("typnspname", "public");
        timeColumn.put("atttypmod", 2);
        timeColumn.put("cltype", "time without time zone");
        timeColumn.put("attndims", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(timeColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("time without time zone"));
        assertThat(singleColumn.get("typnspname"), is("public"));
        assertThat(singleColumn.get("atttypmod"), is(2));
        assertThat(singleColumn.get("attndims"), is(1));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("time without time zone"));
    }
    
    @Test
    void assertGetFullTypeValueTimestampWithTimeZone() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> timestampColumn = createTextColumnWithName("timestamp_col");
        timestampColumn.put("elemoid", 1184L);
        timestampColumn.put("typname", "timestamp with time zone");
        timestampColumn.put("typnspname", "public");
        timestampColumn.put("atttypmod", -1);
        timestampColumn.put("cltype", "timestamp with time zone");
        timestampColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(timestampColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("timestamp with time zone"));
        assertThat(singleColumn.get("typnspname"), is("public"));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("timestamp with time zone"));
    }
    
    @Test
    void assertGetFullDataTypeHandlesSchemaWithQuotes() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("char_col");
        column.put("typname", "public.\"char\"");
        column.put("typnspname", "public");
        column.put("atttypmod", -1);
        column.put("cltype", "\"char\"");
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("cltype"), is("\"char\""));
    }
    
    @Test
    void assertGetFullDataTypeHandlesArrayAndPrefix() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = createTextColumnWithName("int4_col");
        arrayColumn.put("elemoid", 23L);
        arrayColumn.put("typname", "_int4");
        arrayColumn.put("typnspname", null);
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "_int4[]");
        arrayColumn.put("attndims", 0);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("_int4"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        assertThat(singleColumn.get("attndims"), is(0));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("_int4[]"));
    }
    
    @Test
    void assertGetFullDataTypeSkipsNumdimsAdjustmentWhenAttndimsNonZeroForArray() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = createTextColumnWithName("int8_col");
        arrayColumn.put("elemoid", 20L);
        arrayColumn.put("typname", "_int8");
        arrayColumn.put("typnspname", null);
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "_int8[]");
        arrayColumn.put("attndims", 2);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("_int8"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        assertThat(singleColumn.get("attndims"), is(2));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("_int8[]"));
    }
    
    @Test
    void assertGetFullDataTypeHandlesArrayWithoutPrefixWhenAttndimsMissing() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = new LinkedHashMap<>();
        arrayColumn.put("name", "int4_col");
        arrayColumn.put("elemoid", 23L);
        arrayColumn.put("typname", "int4[]");
        arrayColumn.put("typnspname", null);
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "int4[]");
        arrayColumn.put("atttypid", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("int4[]"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("int4[]"));
    }
    
    @Test
    void assertGetFullDataTypeHandlesArrayWithoutPrefixWhenAttndimsZero() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = new LinkedHashMap<>();
        arrayColumn.put("name", "int4_col");
        arrayColumn.put("elemoid", 23L);
        arrayColumn.put("typname", "int4[]");
        arrayColumn.put("typnspname", null);
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "int4[]");
        arrayColumn.put("attndims", 0);
        arrayColumn.put("atttypid", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("int4[]"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        assertThat(singleColumn.get("attndims"), is(0));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("int4[]"));
    }
    
    @Test
    void assertGetFullDataTypeArrayWhenAttndimsMissingLeavesNumdimsNull() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = new LinkedHashMap<>();
        arrayColumn.put("name", "int8_col");
        arrayColumn.put("elemoid", 20L);
        arrayColumn.put("typname", "_int8");
        arrayColumn.put("typnspname", null);
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "_int8[]");
        arrayColumn.put("atttypid", 1);
        arrayColumn.put("attndims", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("_int8"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("_int8[]"));
    }
    
    @Test
    void assertGetFullDataTypeLeavesNameWithOpeningQuoteOnly() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> quotedColumn = new LinkedHashMap<>();
        quotedColumn.put("name", "foo_col");
        quotedColumn.put("elemoid", 9999L);
        quotedColumn.put("typname", "\"foo");
        quotedColumn.put("typnspname", null);
        quotedColumn.put("atttypmod", -1);
        quotedColumn.put("cltype", "\"foo");
        quotedColumn.put("attndims", 0);
        quotedColumn.put("atttypid", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(quotedColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("\"foo"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        assertThat(singleColumn.get("attndims"), is(0));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("\"foo"));
    }
    
    @Test
    void assertGetFullDataTypeHandlesUnderscorePrefixWithNullNumdims() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = new LinkedHashMap<>();
        arrayColumn.put("name", "int4_col");
        arrayColumn.put("elemoid", 23L);
        arrayColumn.put("typname", "_int4");
        arrayColumn.put("typnspname", null);
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "_int4");
        arrayColumn.put("atttypid", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("_int4"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("_int4"));
    }
    
    @Test
    void assertGetFullDataTypeHandlesUnderscorePrefixWithZeroNumdims() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = new LinkedHashMap<>();
        arrayColumn.put("name", "int4_col");
        arrayColumn.put("elemoid", 23L);
        arrayColumn.put("typname", "_int4");
        arrayColumn.put("typnspname", null);
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "_int4");
        arrayColumn.put("attndims", 0);
        arrayColumn.put("atttypid", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("_int4"));
        assertThat(singleColumn.get("typnspname"), nullValue());
        assertThat(singleColumn.get("atttypmod"), is(-1));
        assertThat(singleColumn.get("attndims"), is(0));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("_int4"));
    }
    
    @Test
    void assertGetFullDataTypeHandlesArraySuffixWithNonZeroNumdims() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> arrayColumn = new LinkedHashMap<>();
        arrayColumn.put("name", "text_col");
        arrayColumn.put("elemoid", 25L);
        arrayColumn.put("typname", "text[]");
        arrayColumn.put("typnspname", "public");
        arrayColumn.put("atttypmod", -1);
        arrayColumn.put("cltype", "text[]");
        arrayColumn.put("attndims", 2);
        arrayColumn.put("atttypid", 1);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singleton(arrayColumn));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("typname"), is("text[]"));
        assertThat(singleColumn.get("typnspname"), is("public"));
        assertThat(singleColumn.get("atttypmod"), is(-1));
        assertThat(singleColumn.get("attndims"), is(2));
        @SuppressWarnings("unchecked")
        Collection<String> editTypes = (Collection<String>) singleColumn.get("edit_types");
        assertThat(editTypes, contains("text[]"));
    }
    
    @Test
    void assertCheckSchemaInNameHandlesQuotedSchemaDot() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("test_col");
        column.put("typname", "public\".\"foo\"");
        column.put("typnspname", "public");
        column.put("atttypmod", -1);
        column.put("cltype", "public\".\"foo\"");
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("cltype"), is("public\".\"foo\""));
    }
    
    @Test
    void assertParseTypeNameHandlesArraySuffix() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("text_col");
        column.put("typname", "text[]");
        column.put("typnspname", "public");
        column.put("atttypmod", -1);
        column.put("cltype", "text[]");
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("cltype"), is("text[]"));
    }
    
    @Test
    void assertParseTypeNameHandlesInterval() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("interval_col");
        column.put("typname", "interval");
        column.put("typnspname", "public");
        column.put("atttypmod", -1);
        column.put("cltype", "interval");
        column.put("atttypid", 1186);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("cltype"), is("interval"));
    }
    
    @Test
    void assertParseTypeNameIgnoresTimeBranchWhenPrefixMismatch() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("typoid", 1L);
        Map<String, Object> column = createTextColumnWithName("foo_time_col");
        column.put("typname", "foo(time");
        column.put("typnspname", "public");
        column.put("atttypmod", -1);
        column.put("cltype", "foo(time");
        column.put("atttypid", 25);
        when(templateExecutor.executeByTemplate(context, "component/table/%s/get_columns_for_table.ftl")).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/properties.ftl")).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(context, "component/columns/%s/edit_mode_types_multi.ftl")).thenReturn(Collections.emptyList());
        appender.append(context);
        Map<String, Object> singleColumn = getSingleColumn(context);
        assertThat(singleColumn.get("cltype"), is("foo(time"));
    }
    
    @Test
    void assertAppendPopulatesInheritedAndEditTypes() {
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
        Map<String, Object> editModeTypesEntry = createEditModeTypesEntry();
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.singleton(editModeTypesEntry));
        appender.append(context);
        Collection<?> columns = (Collection<?>) context.get("columns");
        assertThat(columns.size(), is(2));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualColumn = columns.stream().map(each -> (Map<String, Object>) each)
                .filter(each -> "col".equals(each.get("name"))).findFirst().orElseThrow(() -> new AssertionError("missing column 'col'"));
        assertThat(actualColumn.get("inheritedfromtype"), is("parent"));
        assertThat(actualColumn.get("attlen"), is("5"));
        assertThat(actualColumn.get("attprecision"), is("2"));
        assertThat(actualColumn.get("is_pk"), is(true));
        assertThat(actualColumn.get("cltype"), is("numeric"));
        assertThat((Collection<?>) actualColumn.get("edit_types"), contains("alpha", "numeric(5,2)"));
    }
    
    private Map<String, Object> createInheritEntry(final long oid) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("inherits", "parent");
        result.put("oid", oid);
        return result;
    }
    
    private Map<String, Object> createTextColumnWithName(final String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", name);
        result.put("cltype", "text");
        result.put("typname", "text");
        result.put("typnspname", "public");
        result.put("attndims", 0);
        result.put("atttypmod", -1);
        result.put("atttypid", 1);
        return result;
    }
    
    private Map<String, Object> createMapWithNameAndInherited() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("name", "col");
        result.put("inheritedfrom", "parent_type");
        return result;
    }
    
    private Map<String, Object> createUnmatchedColumn() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "other");
        result.put("atttypid", 2);
        result.put("cltype", "text");
        result.put("typname", "text");
        result.put("typnspname", "public");
        result.put("attndims", 0);
        result.put("atttypmod", -1);
        return result;
    }
    
    private Map<String, Object> createEditModeTypesEntry() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("main_oid", "1");
        entry.put("edit_types", mockSQLArray(new String[]{"alpha"}));
        return entry;
    }
    
    @SneakyThrows(SQLException.class)
    private Array mockSQLArray(final Object data) {
        Array result = mock(Array.class);
        doReturn(data).when(result).getArray();
        return result;
    }
    
    private Map<String, Object> appendWithSingleColumn(final Map<String, Object> column) {
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/table/%s/get_columns_for_table.ftl"))).thenReturn(Collections.emptyList());
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/properties.ftl"))).thenReturn(Collections.singletonList(column));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/columns/%s/edit_mode_types_multi.ftl"))).thenReturn(Collections.emptyList());
        Map<String, Object> context = new LinkedHashMap<>();
        
        appender.append(context);
        return getSingleColumn(context);
    }
    
    private Map<String, Object> getSingleColumn(final Map<String, Object> context) {
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> columns = (Collection<Map<String, Object>>) context.get("columns");
        assertThat(columns.size(), is(1));
        return columns.iterator().next();
    }
}
