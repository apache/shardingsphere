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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.index;

import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.ddl.PostgreSQLDDLTemplateExecutor;
import org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.template.PostgreSQLPipelineFreemarkerManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.postgresql.jdbc.PgArray;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class PostgreSQLIndexSQLGeneratorTest {
    
    @Test
    void assertSkipInheritedIndex() throws SQLException, ReflectiveOperationException {
        PostgreSQLDDLTemplateExecutor templateExecutor = mock(PostgreSQLDDLTemplateExecutor.class);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/indexes/%s/nodes.ftl"))).thenReturn(Collections.singletonList(createIndexNode(1L, true)));
        PostgreSQLIndexSQLGenerator generator = new PostgreSQLIndexSQLGenerator(mock(Connection.class), 10, 0);
        Plugins.getMemberAccessor().set(PostgreSQLIndexSQLGenerator.class.getDeclaredField("templateExecutor"), generator, templateExecutor);
        assertThat(generator.generate(createContext("skip_schema", "skip_tbl")), is(""));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateWithoutIncludeForNonBtree() throws SQLException, ReflectiveOperationException {
        PostgreSQLDDLTemplateExecutor templateExecutor = mock(PostgreSQLDDLTemplateExecutor.class);
        when(templateExecutor.getMajorVersion()).thenReturn(10);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/indexes/%s/nodes.ftl"))).thenReturn(Collections.singletonList(createIndexNode(2L, false)));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/indexes/%s/properties.ftl"))).thenReturn(Collections.singletonList(createNonBtreeIndexProps()));
        when(templateExecutor.executeByTemplate(eq(Collections.singletonMap("idx", 2L)), eq("component/indexes/%s/column_details.ftl")))
                .thenReturn(Collections.singletonList(createColumnDetail("foo_col", "foo_collation", "foo_op_class", null)));
        PostgreSQLIndexSQLGenerator generator = new PostgreSQLIndexSQLGenerator(mock(Connection.class), 10, 0);
        Plugins.getMemberAccessor().set(PostgreSQLIndexSQLGenerator.class.getDeclaredField("templateExecutor"), generator, templateExecutor);
        AtomicReference<Map<String, Object>> capturedCreateData = new AtomicReference<>();
        try (MockedStatic<PostgreSQLPipelineFreemarkerManager> mockedStatic = mockStatic(PostgreSQLPipelineFreemarkerManager.class)) {
            mockedStatic.when(() -> PostgreSQLPipelineFreemarkerManager.getSQLByVersion(anyMap(), anyString(), anyInt(), anyInt()))
                    .thenAnswer(invocation -> {
                        Map<String, Object> dataModel = invocation.getArgument(0);
                        String path = invocation.getArgument(1);
                        if (path.contains("create")) {
                            capturedCreateData.set(new LinkedHashMap<>(dataModel));
                            return "create-non-btree";
                        }
                        return "alter-non-btree";
                    });
            String actual = generator.generate(createContext("foo_schema", "foo_tbl"));
            assertThat(actual, is(String.join(System.lineSeparator(), "create-non-btree", "alter-non-btree")));
        }
        Map<String, Object> createData = capturedCreateData.get();
        assertNotNull(createData);
        Collection<Map<String, Object>> columns = (Collection<Map<String, Object>>) createData.get("columns");
        assertThat(columns.size(), is(1));
        Map<String, Object> column = columns.iterator().next();
        assertThat(column.get("colname"), is("foo_col"));
        assertThat(column.get("collspcname"), is("foo_collation"));
        assertThat(column.get("op_class"), is("foo_op_class"));
        String columnsCsv = (String) createData.get("columns_csv");
        assertThat(columnsCsv, is("foo_col COLLATE foo_collation foo_op_class"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateWithIncludeAndBtreeOptions() throws SQLException, ReflectiveOperationException {
        PostgreSQLDDLTemplateExecutor templateExecutor = mock(PostgreSQLDDLTemplateExecutor.class);
        when(templateExecutor.getMajorVersion()).thenReturn(11);
        when(templateExecutor.getMinorVersion()).thenReturn(0);
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/indexes/%s/nodes.ftl"))).thenReturn(Collections.singletonList(Collections.singletonMap("oid", 3L)));
        when(templateExecutor.executeByTemplate(anyMap(), eq("component/indexes/%s/properties.ftl"))).thenReturn(Collections.singletonList(createBtreeIndexProps()));
        PgArray emptyOptions = mock(PgArray.class);
        when(emptyOptions.getArray()).thenReturn(new String[0]);
        PgArray descOptions = mock(PgArray.class);
        when(descOptions.getArray()).thenReturn(new String[]{"DESC"});
        PgArray nullsFirstOptions = mock(PgArray.class);
        when(nullsFirstOptions.getArray()).thenReturn(new String[]{"ASC", "NULLS FIRST"});
        PgArray nullsLastOptions = mock(PgArray.class);
        when(nullsLastOptions.getArray()).thenReturn(new String[]{"ASC", "NULLS LAST"});
        PgArray nullsWithoutSpaceOptions = mock(PgArray.class);
        when(nullsWithoutSpaceOptions.getArray()).thenReturn(new String[]{"ASC", "NULLS"});
        PgArray emptyArrayOptions = mock(PgArray.class);
        when(emptyArrayOptions.getArray()).thenReturn(new String[0]);
        Collection<Map<String, Object>> columnDetails = Arrays.asList(createStatefulColumnDetail(emptyOptions),
                createColumnDetail("desc_col", null, null, descOptions), createColumnDetail("nulls_first_col", null, null, nullsFirstOptions),
                createColumnDetail("nulls_last_col", null, null, nullsLastOptions), createColumnDetail("nulls_no_space_col", null, null, nullsWithoutSpaceOptions),
                createColumnDetail("empty_array_col", null, null, emptyArrayOptions));
        when(templateExecutor.executeByTemplate(eq(Collections.singletonMap("idx", 3L)), eq("component/indexes/%s/column_details.ftl"))).thenReturn(columnDetails);
        when(templateExecutor.executeByTemplate(eq(Collections.singletonMap("idx", 3L)), eq("component/indexes/%s/include_details.ftl")))
                .thenReturn(Arrays.asList(Collections.singletonMap("colname", "include_col_one"), Collections.singletonMap("colname", "include_col_two")));
        PostgreSQLIndexSQLGenerator generator = new PostgreSQLIndexSQLGenerator(mock(Connection.class), 11, 0);
        Plugins.getMemberAccessor().set(PostgreSQLIndexSQLGenerator.class.getDeclaredField("templateExecutor"), generator, templateExecutor);
        AtomicReference<Map<String, Object>> capturedCreateData = new AtomicReference<>();
        try (MockedStatic<PostgreSQLPipelineFreemarkerManager> mockedStatic = mockStatic(PostgreSQLPipelineFreemarkerManager.class)) {
            mockedStatic.when(() -> PostgreSQLPipelineFreemarkerManager.getSQLByVersion(anyMap(), anyString(), anyInt(), anyInt()))
                    .thenAnswer(invocation -> {
                        Map<String, Object> dataModel = invocation.getArgument(0);
                        String path = invocation.getArgument(1);
                        if (path.contains("create")) {
                            capturedCreateData.set(new LinkedHashMap<>(dataModel));
                            return "create-btree";
                        }
                        return "alter-btree";
                    });
            String actual = generator.generate(createContext("bar_schema", "bar_tbl"));
            assertThat(actual, is(String.join(System.lineSeparator(), "create-btree", "alter-btree")));
        }
        Map<String, Object> createData = capturedCreateData.get();
        assertNotNull(createData);
        assertThat(createData.get("include"), is(Arrays.asList("include_col_one", "include_col_two")));
        Collection<Map<String, Object>> columns = (Collection<Map<String, Object>>) createData.get("columns");
        assertThat(columns.size(), is(6));
        Map<String, Object> columnWithNullOptions = columns.iterator().next();
        assertThat(columnWithNullOptions.get("sort_order"), is(false));
        assertThat(columnWithNullOptions.get("nulls"), is(false));
        Map<String, Object> descColumn = columns.stream().filter(each -> "desc_col".equals(each.get("colname"))).findFirst().orElseGet(HashMap::new);
        assertThat(descColumn.get("sort_order"), is(true));
        assertThat(descColumn.get("nulls"), is(false));
        Map<String, Object> nullsFirstColumn = columns.stream().filter(each -> "nulls_first_col".equals(each.get("colname"))).findFirst().orElseGet(HashMap::new);
        assertThat(nullsFirstColumn.get("sort_order"), is(false));
        assertThat(nullsFirstColumn.get("nulls"), is(true));
        Map<String, Object> nullsLastColumn = columns.stream().filter(each -> "nulls_last_col".equals(each.get("colname"))).findFirst().orElseGet(HashMap::new);
        assertThat(nullsLastColumn.get("sort_order"), is(false));
        assertThat(nullsLastColumn.get("nulls"), is(false));
        Map<String, Object> nullsNoSpaceColumn = columns.stream().filter(each -> "nulls_no_space_col".equals(each.get("colname"))).findFirst().orElseGet(HashMap::new);
        assertThat(nullsNoSpaceColumn.get("sort_order"), is(false));
        assertThat(nullsNoSpaceColumn.get("nulls"), is(false));
        Map<String, Object> emptyArrayColumn = columns.stream().filter(each -> "empty_array_col".equals(each.get("colname"))).findFirst().orElseGet(HashMap::new);
        assertThat(emptyArrayColumn.get("sort_order"), is(false));
        assertThat(emptyArrayColumn.get("nulls"), is(false));
        String columnsCsv = (String) createData.get("columns_csv");
        assertThat(columnsCsv, is("col_with_null_options, desc_col DESC, nulls_first_col ASC NULLS FIRST, nulls_last_col ASC NULLS LAST, nulls_no_space_col ASC NULLS, empty_array_col"));
    }
    
    private Map<String, Object> createContext(final String schema, final String table) {
        Map<String, Object> result = new HashMap<>(5, 1F);
        result.put("did", 1);
        result.put("datlastsysoid", 10);
        result.put("tid", 20);
        result.put("schema", schema);
        result.put("name", table);
        return result;
    }
    
    private Map<String, Object> createIndexNode(final long oid, final boolean isInherited) {
        Map<String, Object> result = new HashMap<>(2, 1F);
        result.put("oid", oid);
        result.put("is_inherited", isInherited);
        return result;
    }
    
    private Map<String, Object> createNonBtreeIndexProps() {
        Map<String, Object> result = new HashMap<>(6, 1F);
        result.put("name", "foo_idx");
        result.put("amname", "hash");
        result.put("indisclustered", true);
        result.put("description", "non btree description");
        result.put("spcname", "default_spc");
        result.put("fillfactor", 80);
        return result;
    }
    
    private Map<String, Object> createBtreeIndexProps() {
        Map<String, Object> result = new HashMap<>(4, 1F);
        result.put("name", "bar_idx");
        result.put("amname", "btree");
        result.put("indisclustered", false);
        result.put("description", "");
        return result;
    }
    
    private Map<String, Object> createColumnDetail(final String attdef, final String collation, final String opClass, final PgArray options) {
        Map<String, Object> result = new HashMap<>(4, 1F);
        result.put("attdef", attdef);
        result.put("collnspname", collation);
        result.put("opcname", opClass);
        if (null != options) {
            result.put("options", options);
        }
        return result;
    }
    
    private Map<String, Object> createStatefulColumnDetail(final PgArray fallbackOptions) {
        Map<String, Object> result = spy(new LinkedHashMap<>(4, 1F));
        result.put("attdef", "col_with_null_options");
        result.put("collnspname", null);
        result.put("opcname", null);
        result.put("options", fallbackOptions);
        doReturn(null, null, fallbackOptions).when(result).get("options");
        return result;
    }
}
