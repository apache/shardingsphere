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

package org.apache.shardingsphere.infra.datanode;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataNodeTest {
    
    private static final DatabaseType MYSQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private static final DatabaseType POSTGRESQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private static final DatabaseType ORACLE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dataNodeWithoutSchemaArguments")
    void assertNewDataNodeWithoutSchema(final String name, final String dataNodeText, final String expectedDataSourceName, final String expectedTableName) {
        DataNode actual = new DataNode(dataNodeText);
        assertThat(actual.getDataSourceName(), is(expectedDataSourceName));
        assertNull(actual.getSchemaName());
        assertThat(actual.getTableName(), is(expectedTableName));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("dataNodeWithSchemaArguments")
    void assertNewDataNodeWithSchema(final String name, final String dataNodeText, final String expectedDataSourceName, final String expectedSchemaName, final String expectedTableName) {
        DataNode actual = new DataNode(dataNodeText);
        assertThat(actual.getDataSourceName(), is(expectedDataSourceName));
        assertThat(actual.getSchemaName(), is(expectedSchemaName));
        assertThat(actual.getTableName(), is(expectedTableName));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDataNodeArguments")
    void assertNewDataNodeWithInvalidFormat(final String name, final String dataNodeText) {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode(dataNodeText));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("databaseTypeDataNodeArguments")
    void assertNewDataNodeWithDatabaseType(final String name, final String databaseName, final DatabaseType databaseType, final String dataNodeText,
                                           final String expectedDataSourceName, final String expectedSchemaName, final String expectedTableName) {
        DataNode actual = new DataNode(databaseName, databaseType, dataNodeText);
        assertThat(actual.getDataSourceName(), is(expectedDataSourceName));
        assertThat(actual.getSchemaName(), is(expectedSchemaName));
        assertThat(actual.getTableName(), is(expectedTableName));
    }
    
    @Test
    void assertNewDataNodeWithDatabaseTypeAndInvalidFormat() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("test_db", POSTGRESQL_DATABASE_TYPE, "invalid_format_without_delimiter"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("formatArguments")
    void assertFormat(final String name, final DataNode dataNode, final String expectedText) {
        assertThat(dataNode.format(), is(expectedText));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("formatWithDatabaseTypeArguments")
    void assertFormatWithDatabaseType(final String name, final DataNode dataNode, final DatabaseType databaseType, final String expectedText) {
        assertThat(dataNode.format(databaseType), is(expectedText));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("equalsArguments")
    void assertEquals(final String name, final DataNode dataNode, final Object other, final boolean expectedMatched) {
        assertThat(dataNode.equals(other), is(expectedMatched));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("hashCodeArguments")
    void assertHashCode(final String name, final DataNode dataNode, final DataNode other) {
        assertThat(dataNode.hashCode(), is(other.hashCode()));
    }
    
    @Test
    void assertCaseSensitiveDataNodesRemainDistinctInSet() {
        Collection<DataNode> actual = new LinkedHashSet<>();
        actual.add(new DataNode("foo_ds", "public", "Test3"));
        actual.add(new DataNode("foo_ds", "public", "test3"));
        assertThat(actual.size(), is(2));
    }
    
    private static Stream<Arguments> dataNodeWithoutSchemaArguments() {
        return Stream.of(
                Arguments.of("simple_names", "ds_0.tbl_0", "ds_0", "tbl_0"),
                Arguments.of("special_characters", "ds-0.tbl_0", "ds-0", "tbl_0"),
                Arguments.of("underscores", "data_source_0.table_name_0", "data_source_0", "table_name_0"),
                Arguments.of("numbers", "ds123.tbl456", "ds123", "tbl456"),
                Arguments.of("single_characters", "a.b", "a", "b"));
    }
    
    private static Stream<Arguments> dataNodeWithSchemaArguments() {
        return Stream.of(
                Arguments.of("simple_schema", "ds_0.schema_0.tbl_0", "ds_0", "schema_0", "tbl_0"),
                Arguments.of("mixed_format", "prod-db-01.schema_01.users", "prod-db-01", "schema_01", "users"),
                Arguments.of("instance_format", "instance1.database1.table1", "instance1", "database1", "table1"),
                Arguments.of("complex_instance_format", "prod-cluster-01.mysql-master.users", "prod-cluster-01", "mysql-master", "users"));
    }
    
    private static Stream<Arguments> invalidDataNodeArguments() {
        return Stream.of(
                Arguments.of("without_delimiter", "ds_0tbl_0"),
                Arguments.of("too_many_segments", "ds_0.db_0.tbl_0.tbl_1"),
                Arguments.of("invalid_delimiter", "ds_0,tbl_0"),
                Arguments.of("empty_data_source", ".tbl_0"),
                Arguments.of("empty_table", "ds_0."),
                Arguments.of("consecutive_delimiters", "ds_0..tbl_0"),
                Arguments.of("whitespace_before_delimiter", "ds_0 .tbl_0"),
                Arguments.of("trailing_delimiter", "ds_0.tbl_0."),
                Arguments.of("whitespace_after_delimiter", "ds_0. tbl_0"),
                Arguments.of("blank_segment_with_tab", "ds.\t.tbl"));
    }
    
    private static Stream<Arguments> databaseTypeDataNodeArguments() {
        return Stream.of(
                Arguments.of("postgresql_with_schema", "test_db", POSTGRESQL_DATABASE_TYPE, "ds.schema.tbl", "ds", "schema", "tbl"),
                Arguments.of("postgresql_without_schema_segment", "test_db", POSTGRESQL_DATABASE_TYPE, "ds.tbl", "ds", "*", "tbl"),
                Arguments.of("mysql_without_schema_support", "test_db", MYSQL_DATABASE_TYPE, "ds.tbl", "ds", "test_db", "tbl"),
                Arguments.of("mysql_three_segments_kept_as_table_suffix", "test_db", MYSQL_DATABASE_TYPE, "ds.schema.tbl", "ds", "test_db", "schema.tbl"),
                Arguments.of("postgresql_preserves_table_case", "test_db", POSTGRESQL_DATABASE_TYPE, "ds.schema.TABLE", "ds", "schema", "TABLE"),
                Arguments.of("oracle_normalizes_database_schema", "logic_db", ORACLE_DATABASE_TYPE, "ds.tbl", "ds", "LOGIC_DB", "tbl"));
    }
    
    private static Stream<Arguments> formatArguments() {
        return Stream.of(
                Arguments.of("with_schema", new DataNode("foo_ds", "foo_schema", "foo_tbl"), "foo_ds.foo_schema.foo_tbl"),
                Arguments.of("without_schema", new DataNode("foo_ds", (String) null, "foo_tbl"), "foo_ds.foo_tbl"));
    }
    
    private static Stream<Arguments> formatWithDatabaseTypeArguments() {
        return Stream.of(
                Arguments.of("postgresql_with_schema", new DataNode("ds", "schema", "tbl"), POSTGRESQL_DATABASE_TYPE, "ds.schema.tbl"),
                Arguments.of("mysql_without_schema", new DataNode("ds", (String) null, "tbl"), MYSQL_DATABASE_TYPE, "ds.tbl"),
                Arguments.of("mysql_ignores_explicit_schema", new DataNode("ds", "schema", "tbl"), MYSQL_DATABASE_TYPE, "ds.tbl"));
    }
    
    private static Stream<Arguments> equalsArguments() {
        final DataNode self = new DataNode("ds_0.tbl_0");
        return Stream.of(
                Arguments.of("self", self, self, true),
                Arguments.of("null_object", new DataNode("ds_0.tbl_0"), null, false),
                Arguments.of("different_type", new DataNode("ds_0.tbl_0"), "ds.tbl", false),
                Arguments.of("different_case", new DataNode("ds_0.tbl_0"), new DataNode("DS_0.TBL_0"), false),
                Arguments.of("different_data_source", new DataNode("ds_0.tbl_0"), new DataNode("ds_1.tbl_0"), false),
                Arguments.of("different_table", new DataNode("ds_0.tbl_0"), new DataNode("ds_0.tbl_1"), false),
                Arguments.of("different_schema", new DataNode("ds", "schema1", "tbl"), new DataNode("ds", "schema2", "tbl"), false));
    }
    
    private static Stream<Arguments> hashCodeArguments() {
        return Stream.of(
                Arguments.of("without_schema", new DataNode("ds_0.tbl_0"), new DataNode("ds_0.tbl_0")),
                Arguments.of("with_schema", new DataNode("ds_0.db_0.tbl_0"), new DataNode("ds_0.db_0.tbl_0")),
                Arguments.of("manual_constructor", new DataNode("ds", "schema", "tbl"), new DataNode("ds", "schema", "tbl")));
    }
}
