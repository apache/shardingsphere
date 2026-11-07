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

package org.apache.shardingsphere.infra.metadata.database.schema.model;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereSchemaTest {
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseType oracleDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    @Test
    void assertGetAllTables() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertThat(new HashSet<>(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList())
                .getAllTables()), is(Collections.singleton(table)));
    }
    
    @Test
    void assertContainsTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertTrue(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList()).containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList()).getTable("foo_tbl"), is(table));
    }
    
    @Test
    void assertPutTable() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", postgreSQLDatabaseType);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        schema.putTable(table);
        assertThat(schema.getTable("foo_tbl"), is(table));
    }
    
    @Test
    void assertRemoveTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.removeTable("foo_tbl");
        assertNull(schema.getTable("foo_tbl"));
    }
    
    @Test
    void assertGetAllViews() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        assertThat(new HashSet<>(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view))
                .getAllViews()), is(Collections.singleton(view)));
    }
    
    @Test
    void assertContainsView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        assertTrue(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view)).containsView("foo_view"));
    }
    
    @Test
    void assertGetView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        assertThat(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view)).getView("foo_view"), is(view));
    }
    
    @Test
    void assertPutView() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.emptyList());
        schema.putView(new ShardingSphereView("foo_view", "SELECT * FROM test_table"));
        assertTrue(schema.containsView("foo_view"));
    }
    
    @Test
    void assertRemoveView() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(),
                Collections.singleton(new ShardingSphereView("foo_view", "SELECT * FROM test_table")));
        schema.removeView("foo_view");
        assertFalse(schema.containsView("foo_view"));
    }
    
    @Test
    void assertContainsIndex() {
        ShardingSphereTable table = new ShardingSphereTable(
                "foo_tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("col_idx", Collections.emptyList(), false)), Collections.emptyList());
        assertTrue(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList()).containsIndex("foo_tbl", "col_idx"));
    }
    
    @Test
    void assertContainsIndexWithIndexNotExists() {
        ShardingSphereTable table = new ShardingSphereTable(
                "foo_tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("col_idx", Collections.emptyList(), false)), Collections.emptyList());
        assertFalse(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList()).containsIndex("foo_tbl", "foo_idx"));
    }
    
    @Test
    void assertContainsIndexWithTableNotExists() {
        assertFalse(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType).containsIndex("nonexistent_tbl", "nonexistent_idx"));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenTableNotExists() {
        assertTrue(new ShardingSphereSchema("foo_tbl", postgreSQLDatabaseType, Collections.emptyList(), Collections.emptyList()).getVisibleColumnNames("nonexistent_tbl").isEmpty());
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(
                new ShardingSphereColumn("foo_col", 0, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        assertThat(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList()).getVisibleColumnNames("foo_tbl"),
                is(Collections.singletonList("foo_col")));
    }
    
    @Test
    void assertGetVisibleColumnNamesWhenNotContainsKey() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(
                new ShardingSphereColumn("foo_col", 0, false, false, false, false, true, false)), Collections.emptyList(), Collections.emptyList());
        assertTrue(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList()).getVisibleColumnNames("foo_tbl").isEmpty());
    }
    
    @Test
    void assertGetVisibleColumnAndIndexMapWhenContainsTable() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", 0, false, false, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singletonList(column), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        Map<String, Integer> actual = schema.getVisibleColumnAndIndexMap("foo_tbl");
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("foo_col"));
    }
    
    @Test
    void assertGetVisibleColumnAndIndexMapWhenNotContainsTable() {
        assertTrue(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType).getVisibleColumnAndIndexMap("nonexistent_tbl").isEmpty());
    }
    
    @Test
    void assertIsEmptyWithEmptyTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertFalse(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList()).isEmpty());
    }
    
    @Test
    void assertIsEmptyWithEmptyView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "");
        assertFalse(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view)).isEmpty());
    }
    
    @Test
    void assertIsEmpty() {
        assertTrue(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType).isEmpty());
    }
    
    @Test
    void assertContainsUpperCaseTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertTrue(schema.containsTable("FOO_TBL"));
    }
    
    @Test
    void assertContainsTableWithOracleRule() {
        ShardingSphereTable table = new ShardingSphereTable("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", oracleDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newUpperCasePolicySet()));
        assertTrue(schema.containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetUpperCaseTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertThat(schema.getTable("FOO_TBL"), is(table));
    }
    
    @Test
    void assertContainsUpperCaseView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertTrue(schema.containsView("FOO_VIEW"));
    }
    
    @Test
    void assertContainsViewWithOracleRule() {
        ShardingSphereView view = new ShardingSphereView("FOO_VIEW", "SELECT 1");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", oracleDatabaseType, Collections.emptyList(), Collections.singleton(view));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newUpperCasePolicySet()));
        assertTrue(schema.containsView("foo_view"));
    }
    
    @Test
    void assertGetUpperCaseView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertThat(schema.getView("FOO_VIEW"), is(view));
    }
    
    @Test
    void assertAttachIdentifierContext() {
        ShardingSphereTable table = new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        assertTrue(schema.containsTable("FOO_TBL"));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertFalse(schema.containsTable("FOO_TBL"));
    }
    
    @Test
    void assertAttachIdentifierContextToTable() {
        ShardingSphereColumn column = new ShardingSphereColumn("Foo_Col", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertFalse(schema.getTable("foo_tbl").containsColumn("FOO_COL"));
    }
    
    @Test
    void assertContainsTableByLogicalTableIndexWhenHeterogeneousLookupEnabled() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        DatabaseIdentifierContext context = createHeterogeneousIdentifierContext();
        schema.refreshIdentifierContext(context);
        assertTrue(schema.containsTable("FOO_TBL"));
        assertThat(schema.getTable("FOO_TBL"), is(table));
    }
    
    @Test
    void assertGetTablePrioritizesPhysicalTableIndexWhenBothIndexesCanMatch() {
        ShardingSphereTable lowerCaseTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable upperCaseTable = new ShardingSphereTable("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(lowerCaseTable, upperCaseTable), Collections.emptyList());
        DatabaseIdentifierContext context = createHeterogeneousIdentifierContext();
        schema.refreshIdentifierContext(context);
        assertThat(schema.getTable("FOO_TBL"), is(upperCaseTable));
    }
    
    @Test
    void assertRemoveTableByLogicalTableIndexWhenHeterogeneousLookupEnabled() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        DatabaseIdentifierContext context = createHeterogeneousIdentifierContext();
        schema.refreshIdentifierContext(context);
        schema.removeTable("FOO_TBL");
        assertNull(schema.getTable("foo_tbl"));
    }
    
    @Test
    void assertRemoveTableByActualName() {
        ShardingSphereTable table = new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        schema.removeTable("Foo_Tbl");
        assertTrue(schema.getAllTables().isEmpty());
    }
    
    @Test
    void assertRemoveTablePrioritizesPhysicalTableIndexWhenBothIndexesCanMatch() {
        ShardingSphereTable lowerCaseTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable upperCaseTable = new ShardingSphereTable("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(lowerCaseTable, upperCaseTable), Collections.emptyList());
        DatabaseIdentifierContext context = createHeterogeneousIdentifierContext();
        schema.refreshIdentifierContext(context);
        schema.removeTable("FOO_TBL");
        assertThat(schema.getTable("FOO_TBL"), is(lowerCaseTable));
    }
    
    @Test
    void assertRemoveViewByActualName() {
        ShardingSphereView view = new ShardingSphereView("Foo_View", "SELECT 1");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        schema.removeView("Foo_View");
        assertTrue(schema.getAllViews().isEmpty());
    }
    
    private DatabaseIdentifierContext createHeterogeneousIdentifierContext() {
        Map<IdentifierScope, IdentifierCasePolicy> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.TABLE));
        IdentifierCasePolicySet policySet = new IdentifierCasePolicySet(IdentifierCasePolicyFactory.newUpperCasePolicySet().getPolicy(IdentifierScope.TABLE), scopedRules);
        return new DatabaseIdentifierContext(policySet, policySet, policySet, true);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("changedTableStructures")
    void assertPutTableWithChangedStructure(final String name, final ShardingSphereTable table) {
        ShardingSphereTable previousTable = createBindingTable();
        ShardingSphereTable unrelatedTable = new ShardingSphereTable("bar_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(previousTable, unrelatedTable), Collections.emptyList());
        schema.putTable(table);
        assertTrue(previousTable.isBindingInvalidated());
        assertFalse(table.isBindingInvalidated());
        assertFalse(unrelatedTable.isBindingInvalidated());
    }
    
    private static ShardingSphereTable createBindingTable() {
        return new ShardingSphereTable("foo_tbl", Arrays.asList(
                new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, true),
                new ShardingSphereColumn("bar_col", Types.VARCHAR, false, false, false, true, false, true)),
                Collections.singleton(new ShardingSphereIndex("foo_idx", Arrays.asList("foo_col", "bar_col"), false)),
                Collections.singleton(new ShardingSphereConstraint("foo_fk", "foo_parent")));
    }
    
    private static Stream<Arguments> changedTableStructures() {
        ShardingSphereColumn fooColumn = new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, true);
        ShardingSphereColumn barColumn = new ShardingSphereColumn("bar_col", Types.VARCHAR, false, false, false, true, false, true);
        Collection<ShardingSphereColumn> columns = Arrays.asList(fooColumn, barColumn);
        Collection<ShardingSphereIndex> indexes = Collections.singleton(new ShardingSphereIndex("foo_idx", Arrays.asList("foo_col", "bar_col"), false));
        Collection<ShardingSphereConstraint> constraints = Collections.singleton(new ShardingSphereConstraint("foo_fk", "foo_parent"));
        return Stream.of(
                Arguments.of("column order", new ShardingSphereTable("foo_tbl", Arrays.asList(barColumn, fooColumn), indexes, constraints)),
                Arguments.of("column added", new ShardingSphereTable("foo_tbl", Arrays.asList(fooColumn, barColumn,
                        new ShardingSphereColumn("baz_col", Types.BIGINT, false, false, false, true, false, true)), indexes, constraints)),
                Arguments.of("column removed", new ShardingSphereTable("foo_tbl", Collections.singleton(fooColumn), indexes, constraints)),
                Arguments.of("column renamed", new ShardingSphereTable("foo_tbl", Arrays.asList(
                        new ShardingSphereColumn("baz_col", Types.INTEGER, false, false, false, true, false, true), barColumn), indexes, constraints)),
                Arguments.of("column type", new ShardingSphereTable("foo_tbl", Arrays.asList(
                        new ShardingSphereColumn("foo_col", Types.BIGINT, false, false, false, true, false, true), barColumn), indexes, constraints)),
                Arguments.of("primary key", new ShardingSphereTable("foo_tbl", Arrays.asList(
                        new ShardingSphereColumn("foo_col", Types.INTEGER, true, false, false, true, false, true), barColumn), indexes, constraints)),
                Arguments.of("generated column", new ShardingSphereTable("foo_tbl", Arrays.asList(
                        new ShardingSphereColumn("foo_col", Types.INTEGER, false, true, false, true, false, true), barColumn), indexes, constraints)),
                Arguments.of("case sensitivity", new ShardingSphereTable("foo_tbl", Arrays.asList(fooColumn,
                        new ShardingSphereColumn("bar_col", Types.VARCHAR, false, false, true, true, false, true)), indexes, constraints)),
                Arguments.of("column visibility", new ShardingSphereTable("foo_tbl", Arrays.asList(
                        new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, false, false, true), barColumn), indexes, constraints)),
                Arguments.of("unsigned column", new ShardingSphereTable("foo_tbl", Arrays.asList(
                        new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, true, true), barColumn), indexes, constraints)),
                Arguments.of("column nullability", new ShardingSphereTable("foo_tbl", Arrays.asList(
                        new ShardingSphereColumn("foo_col", Types.INTEGER, false, false, false, true, false, false), barColumn), indexes, constraints)),
                Arguments.of("index removed", new ShardingSphereTable("foo_tbl", columns, Collections.emptyList(), constraints)),
                Arguments.of("index renamed", new ShardingSphereTable("foo_tbl", columns,
                        Collections.singleton(new ShardingSphereIndex("bar_idx", Arrays.asList("foo_col", "bar_col"), false)), constraints)),
                Arguments.of("index order", new ShardingSphereTable("foo_tbl", columns,
                        Collections.singleton(new ShardingSphereIndex("foo_idx", Arrays.asList("bar_col", "foo_col"), false)), constraints)),
                Arguments.of("unique index", new ShardingSphereTable("foo_tbl", columns,
                        Collections.singleton(new ShardingSphereIndex("foo_idx", Arrays.asList("foo_col", "bar_col"), true)), constraints)),
                Arguments.of("constraint removed", new ShardingSphereTable("foo_tbl", columns, indexes, Collections.emptyList())),
                Arguments.of("constraint renamed", new ShardingSphereTable("foo_tbl", columns, indexes, Collections.singleton(new ShardingSphereConstraint("bar_fk", "foo_parent")))),
                Arguments.of("referenced table", new ShardingSphereTable("foo_tbl", columns, indexes, Collections.singleton(new ShardingSphereConstraint("foo_fk", "bar_parent")))),
                Arguments.of("table type", new ShardingSphereTable("foo_tbl", columns, indexes, constraints, TableType.VIEW)));
    }
    
    @Test
    void assertPutTableWithEquivalentRefreshChain() {
        ShardingSphereTable previousTable = createBindingTable();
        ShardingSphereTable equivalentTable = createBindingTable();
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(previousTable), Collections.emptyList());
        schema.putTable(equivalentTable);
        assertFalse(previousTable.isBindingInvalidated());
        assertFalse(equivalentTable.isBindingInvalidated());
        schema.putTable(new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        assertTrue(previousTable.isBindingInvalidated());
        assertTrue(equivalentTable.isBindingInvalidated());
    }
    
    @Test
    void assertPutTableWithOriginalStructureRestored() {
        ShardingSphereTable previousTable = createBindingTable();
        ShardingSphereTable changedTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable restoredTable = createBindingTable();
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(previousTable), Collections.emptyList());
        schema.putTable(changedTable);
        schema.putTable(restoredTable);
        assertTrue(previousTable.isBindingInvalidated());
        assertTrue(changedTable.isBindingInvalidated());
        assertFalse(restoredTable.isBindingInvalidated());
    }
    
    @Test
    void assertPutTableWithSameInstance() {
        ShardingSphereTable table = createBindingTable();
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.putTable(table);
        assertFalse(table.isBindingInvalidated());
    }
    
    @Test
    void assertRemoveTableWithRecreation() {
        ShardingSphereTable previousTable = createBindingTable();
        ShardingSphereTable recreatedTable = createBindingTable();
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(previousTable), Collections.emptyList());
        schema.removeTable("FOO_TBL");
        schema.putTable(recreatedTable);
        assertTrue(previousTable.isBindingInvalidated());
        assertFalse(recreatedTable.isBindingInvalidated());
    }
    
    @Test
    void assertInheritBindingsWithChangedAndRemovedTables() {
        ShardingSphereTable changedTable = createBindingTable();
        ShardingSphereTable removedTable = new ShardingSphereTable("bar_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable unchangedTable = new ShardingSphereTable("baz_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema previousSchema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(changedTable, removedTable, unchangedTable), Collections.emptyList());
        ShardingSphereTable replacementTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable equivalentTable = new ShardingSphereTable("baz_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(replacementTable, equivalentTable), Collections.emptyList());
        schema.inheritBindings(previousSchema);
        assertTrue(changedTable.isBindingInvalidated());
        assertTrue(removedTable.isBindingInvalidated());
        assertFalse(unchangedTable.isBindingInvalidated());
        assertFalse(replacementTable.isBindingInvalidated());
        schema.removeTable("baz_tbl");
        assertTrue(unchangedTable.isBindingInvalidated());
    }
    
    @Test
    void assertInheritBindingsWithSameSchema() {
        ShardingSphereTable table = createBindingTable();
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.inheritBindings(schema);
        assertFalse(table.isBindingInvalidated());
    }
}
