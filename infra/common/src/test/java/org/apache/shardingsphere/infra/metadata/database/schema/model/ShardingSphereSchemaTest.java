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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    void assertGetAllSequences() {
        ShardingSphereSequence sequence = new ShardingSphereSequence("foo_seq");
        assertThat(new HashSet<>(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.emptyList(), Collections.singleton(sequence))
                .getAllSequences()), is(Collections.singleton(sequence)));
    }
    
    @Test
    void assertContainsSequence() {
        ShardingSphereSequence sequence = new ShardingSphereSequence("foo_seq");
        assertTrue(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.emptyList(), Collections.singleton(sequence)).containsSequence("foo_seq"));
    }
    
    @Test
    void assertGetSequence() {
        ShardingSphereSequence sequence = new ShardingSphereSequence("foo_seq");
        assertThat(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.emptyList(), Collections.singleton(sequence)).getSequence("foo_seq"), is(sequence));
    }
    
    @Test
    void assertPutSequence() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", postgreSQLDatabaseType);
        schema.putSequence(new ShardingSphereSequence("foo_seq"));
        assertTrue(schema.containsSequence("foo_seq"));
    }
    
    @Test
    void assertRemoveSequence() {
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.emptyList(),
                Collections.singleton(new ShardingSphereSequence("foo_seq")));
        schema.removeSequence("foo_seq");
        assertFalse(schema.containsSequence("foo_seq"));
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
    void assertIsEmptyWithEmptySequence() {
        ShardingSphereSequence sequence = new ShardingSphereSequence("foo_seq");
        assertFalse(new ShardingSphereSchema("foo_db", postgreSQLDatabaseType, Collections.emptyList(), Collections.emptyList(), Collections.singleton(sequence)).isEmpty());
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
        ShardingSphereColumn column = new ShardingSphereColumn("Foo_Col", java.sql.Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
        assertFalse(schema.getTable("foo_tbl").containsColumn("FOO_COL"));
    }
    
    @Test
    void assertContainsTableByLogicalTableIndexWhenHeterogeneousLookupEnabled() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        Map<IdentifierScope, IdentifierCasePolicy> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCasePolicySet(IdentifierCasePolicyFactory.newUpperCasePolicySet()
                .getPolicy(IdentifierScope.TABLE), scopedRules), true);
        schema.refreshIdentifierContext(context);
        assertTrue(schema.containsTable("FOO_TBL"));
        assertThat(schema.getTable("FOO_TBL"), is(table));
    }
    
    @Test
    void assertGetTablePrioritizesPhysicalTableIndexWhenBothIndexesCanMatch() {
        ShardingSphereTable lowerCaseTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable upperCaseTable = new ShardingSphereTable("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(lowerCaseTable, upperCaseTable), Collections.emptyList());
        Map<IdentifierScope, IdentifierCasePolicy> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCasePolicySet(IdentifierCasePolicyFactory.newUpperCasePolicySet()
                .getPolicy(IdentifierScope.TABLE), scopedRules), true);
        schema.refreshIdentifierContext(context);
        assertThat(schema.getTable("FOO_TBL"), is(upperCaseTable));
    }
    
    @Test
    void assertRemoveTableByLogicalTableIndexWhenHeterogeneousLookupEnabled() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        Map<IdentifierScope, IdentifierCasePolicy> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCasePolicySet(IdentifierCasePolicyFactory.newUpperCasePolicySet()
                .getPolicy(IdentifierScope.TABLE), scopedRules), true);
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
        Map<IdentifierScope, IdentifierCasePolicy> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCasePolicySet(IdentifierCasePolicyFactory.newUpperCasePolicySet()
                .getPolicy(IdentifierScope.TABLE), scopedRules), true);
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
}
