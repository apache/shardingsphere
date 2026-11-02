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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PostgreSQLPipelineFreemarkerManagerTest {
    
    private static final Pattern PATTERN = Pattern.compile("\\s+");
    
    @ParameterizedTest(name = "PostgreSQL {0}")
    @ValueSource(ints = {10, 11, 12, 13, 14, 15, 16, 17})
    void assertGetSQLByDefaultVersion(final int majorVersion) {
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(Collections.singletonMap("databaseName", "foo_db"), "component/table/%s/get_database_id.ftl", majorVersion, 0);
        String expected = "\nSELECT oid AS did FROM pg_catalog.pg_database WHERE datname = 'foo_db';\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGetSQLByVersion() {
        Map<String, Object> dataModel = new HashMap<>(2, 1F);
        dataModel.put("tid", 1);
        dataModel.put("tname", "foo_tb l");
        assertNotNull(PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/table/%s/get_columns_for_table.ftl", 10, 0));
    }
    
    @Test
    void assertGetTableIdSQLWithActualIdentifiers() {
        Map<String, Object> dataModel = new HashMap<>(2, 1F);
        dataModel.put("schemaName", "UPPER_SCHEMA");
        dataModel.put("tableName", "UPPER_TABLE");
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/table/%s/get_table_id.ftl", 12, 0);
        String expected = "\nSELECT (pg_catalog.quote_ident('UPPER_SCHEMA') || '.' || pg_catalog.quote_ident('UPPER_TABLE'))::REGCLASS::OID AS tid;\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertCreateTableTemplateRendersNormalizedSequenceNumbers() {
        Map<String, Object> dataModel = new LinkedHashMap<>(8, 1F);
        dataModel.put("schema", "public");
        dataModel.put("name", "t_order");
        dataModel.put("columns", Collections.singletonList(getColumn()));
        dataModel.put("primary_key", Collections.emptyList());
        dataModel.put("unique_constraint", Collections.emptyList());
        dataModel.put("foreign_key", Collections.emptyList());
        dataModel.put("check_constraint", Collections.emptyList());
        dataModel.put("exclude_constraint", Collections.emptyList());
        dataModel.put("coll_inherits", Collections.emptyList());
        dataModel.put("autovacuum_enabled", "x");
        dataModel.put("toast_autovacuum_enabled", "x");
        dataModel.put("autovacuum_custom", false);
        dataModel.put("toast_autovacuum", false);
        dataModel.put("add_vacuum_settings_in_sql", false);
        String sql = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/table/%s/create.ftl", 12, 0);
        String compactSql = PATTERN.matcher(sql).replaceAll("");
        String expectedSql = "CREATETABLEIFNOTEXISTS\"public\".\"t_order\"(\"id\"integerNOTNULLGENERATEDALWAYSASIDENTITY(INCREMENT1START1MINVALUE1MAXVALUE2147483647CACHE1))";
        assertThat(compactSql, is(expectedSql));
    }
    
    @ParameterizedTest(name = "PostgreSQL {0}")
    @ValueSource(ints = {9, 10, 11, 12, 13, 14, 15, 16, 17})
    void assertCreateTableWithQuotedIdentifiersAndComment(final int majorVersion) {
        Map<String, Object> dataModel = new HashMap<>(16, 1F);
        dataModel.put("schema", "Sales");
        dataModel.put("name", "Order");
        Map<String, Object> column = new HashMap<>(5, 1F);
        column.put("name", "I\"D");
        column.put("cltype", "text");
        column.put("displaytypname", "text");
        column.put("colconstype", "n");
        column.put("defval", "'a;b'");
        dataModel.put("columns", Collections.singletonList(column));
        dataModel.put("primary_key", Collections.emptyList());
        dataModel.put("unique_constraint", Collections.emptyList());
        dataModel.put("foreign_key", Collections.emptyList());
        dataModel.put("check_constraint", Collections.emptyList());
        dataModel.put("exclude_constraint", Collections.emptyList());
        dataModel.put("coll_inherits", Collections.emptyList());
        dataModel.put("autovacuum_enabled", "x");
        dataModel.put("toast_autovacuum_enabled", "x");
        dataModel.put("spcname", "pg_default");
        dataModel.put("description", "owner's;comment");
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/table/%s/create.ftl", majorVersion, 0);
        String expected = "CREATETABLEIFNOTEXISTS\"Sales\".\"Order\"(\"I\"\"D\"textDEFAULT'a;b')" + (majorVersion < 12 ? "WITH(OIDS=FALSE)" : "") + "TABLESPACE\"pg_default\";"
                + "COMMENTONTABLE\"Sales\".\"Order\"ISE'owner''s;comment';";
        assertThat(PATTERN.matcher(actual).replaceAll(""), is(expected));
    }
    
    @Test
    void assertIndexCommentWithQuotedIdentifiersAndBackslash() {
        Map<String, Object> dataModel = new HashMap<>(3, 1F);
        dataModel.put("schema", "Sales");
        dataModel.put("name", "I\"D");
        dataModel.put("description", "owner's;C:\\index");
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/indexes/%s/alter.ftl", 17, 0);
        assertThat(actual.trim(), is("COMMENT ON INDEX \"Sales\".\"I\"\"D\"\nIS E'owner''s;C:\\\\index';"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("catalogNames")
    void assertQuotedCatalogLookup(final String template, final Map<String, Object> params, final String expected) {
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(params, "component/table/%s/" + template + ".ftl", 17, 0);
        assertThat(actual.trim(), is(expected));
    }
    
    private static Stream<Arguments> catalogNames() {
        Map<String, Object> table = new HashMap<>(2, 1F);
        table.put("schemaName", "owner's");
        table.put("tableName", "table's");
        return Stream.of(
                Arguments.of("get_database_id", Collections.singletonMap("databaseName", "db's"), "SELECT oid AS did FROM pg_catalog.pg_database WHERE datname = 'db''s';"),
                Arguments.of("get_schema_id", Collections.singletonMap("schemaName", "owner's"), "SELECT oid AS scid FROM pg_catalog.pg_namespace WHERE nspname = 'owner''s';"),
                Arguments.of("get_table_id", table, "SELECT (pg_catalog.quote_ident('owner''s') || '.' || pg_catalog.quote_ident('table''s'))::REGCLASS::OID AS tid;"));
    }
    
    @ParameterizedTest(name = "PostgreSQL {0} composite foreign key")
    @ValueSource(ints = {9, 10, 11, 12, 17})
    void assertCompositeForeignKeyWithQuotedIdentifiers(final int majorVersion) {
        Map<String, Object> foreignKey = new HashMap<>(10, 1F);
        foreignKey.put("name", "foo_\"fk");
        foreignKey.put("columns", Arrays.asList(Map.of("local_column", "foo_b", "referenced", "bar_y"), Map.of("local_column", "foo_a", "referenced", "bar_x")));
        foreignKey.put("remote_schema", "bar_schema");
        foreignKey.put("remote_table", "bar_table");
        foreignKey.put("confupdtype", "c");
        foreignKey.put("confdeltype", "r");
        foreignKey.put("condeferrable", false);
        foreignKey.put("convalidated", true);
        Map<String, Object> dataModel = new HashMap<>(14, 1F);
        dataModel.put("schema", "foo_schema");
        dataModel.put("name", "foo_table");
        dataModel.put("columns", Collections.emptyList());
        dataModel.put("primary_key", Collections.emptyList());
        dataModel.put("unique_constraint", Collections.emptyList());
        dataModel.put("foreign_key", Collections.singletonList(foreignKey));
        dataModel.put("check_constraint", Collections.emptyList());
        dataModel.put("exclude_constraint", Collections.emptyList());
        dataModel.put("coll_inherits", Collections.emptyList());
        dataModel.put("autovacuum_enabled", "x");
        dataModel.put("toast_autovacuum_enabled", "x");
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/table/%s/create.ftl", majorVersion, 0);
        String expected = "CREATETABLEIFNOTEXISTS\"foo_schema\".\"foo_table\"(CONSTRAINT\"foo_\"\"fk\"FOREIGNKEY(\"foo_b\",\"foo_a\")"
                + "REFERENCES\"bar_schema\".\"bar_table\"(\"bar_y\",\"bar_x\")MATCHSIMPLEONUPDATECASCADEONDELETERESTRICT)" + (majorVersion < 12 ? "WITH(OIDS=FALSE);" : "");
        assertThat(PATTERN.matcher(actual).replaceAll(""), is(expected));
    }
    
    @Test
    void assertForeignKeyColumnMappingQuery() {
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(Collections.singletonMap("cid", 12345L), "component/foreign_key/%s/get_constraint_cols.ftl", 17, 0);
        String expected = "SELECT a1.attname as conattname, a2.attname as confattname FROM ( SELECT conrelid, confrelid, conkey, confkey, generate_subscripts(conkey, 1) AS ordinal"
                + " FROM pg_catalog.pg_constraint WHERE oid = 12345::oid ) mapping"
                + " JOIN pg_catalog.pg_attribute a1 ON a1.attrelid = mapping.conrelid AND a1.attnum = mapping.conkey[mapping.ordinal]"
                + " JOIN pg_catalog.pg_attribute a2 ON a2.attrelid = mapping.confrelid AND a2.attnum = mapping.confkey[mapping.ordinal] ORDER BY mapping.ordinal";
        assertThat(PATTERN.matcher(actual).replaceAll(" ").trim(), is(expected));
    }
    
    @ParameterizedTest(name = "{0} index columns")
    @ValueSource(ints = {1, 2, 3})
    void assertForeignKeyCoveringIndexColumns(final int columnCount) {
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(Map.of("cid", 12345L, "colcnt", columnCount), "component/foreign_key/%s/get_cols.ftl", 17, 0);
        StringBuilder expected = new StringBuilder("SELECT pg_catalog.pg_get_indexdef(12345, 1, true) AS column");
        for (int i = 2; i <= columnCount; i++) {
            expected.append(" UNION SELECT pg_catalog.pg_get_indexdef(12345, ").append(i).append(", true) AS column");
        }
        assertThat(PATTERN.matcher(actual).replaceAll(""), is(PATTERN.matcher(expected).replaceAll("")));
    }
    
    @ParameterizedTest(name = "PostgreSQL {0} index identifiers")
    @ValueSource(ints = {9, 10, 11, 12, 17})
    void assertCreateIndexWithQuotedIdentifiers(final int majorVersion) {
        Map<String, Object> dataModel = new HashMap<>(7, 1F);
        dataModel.put("schema", "foo_schema");
        dataModel.put("table", "foo_table");
        dataModel.put("name", "foo_\"index");
        dataModel.put("columns", Collections.singletonList(Collections.singletonMap("colname", "lower(foo_column)")));
        dataModel.put("include", Collections.singletonList("foo_\"included"));
        dataModel.put("spcname", "foo_\"space");
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/indexes/%s/create.ftl", majorVersion, 0);
        String expected = "CREATEINDEXIFNOTEXISTS\"foo_\"\"index\"ON\"foo_schema\".\"foo_table\"(lower(foo_column))" + (majorVersion >= 11 ? "INCLUDE(\"foo_\"\"included\")" : "")
                + "TABLESPACE\"foo_\"\"space\";";
        assertThat(PATTERN.matcher(actual).replaceAll(""), is(expected));
    }
    
    private static Map<String, Object> getColumn() {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("name", "id");
        result.put("cltype", "int4");
        result.put("displaytypname", "integer");
        result.put("attnotnull", true);
        result.put("attidentity", "a");
        result.put("colconstype", "i");
        result.put("seqincrement", 1L);
        result.put("seqstart", 1L);
        result.put("seqmin", 1L);
        result.put("seqmax", 2147483647L);
        result.put("seqcache", 1L);
        result.put("seqcycle", false);
        return result;
    }
}
