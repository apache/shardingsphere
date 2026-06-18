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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSets;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRuleSet;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCaseRule;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereSchemaIdentifierTest {
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseType oracleDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    @Test
    void assertContainsTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertTrue(schema.containsTable("FOO_TBL"));
    }
    
    @Test
    void assertContainsTableWithOracleRule() {
        ShardingSphereTable table = new ShardingSphereTable("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", oracleDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newUpperCaseRuleSet()));
        assertTrue(schema.containsTable("foo_tbl"));
    }
    
    @Test
    void assertGetTable() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertThat(schema.getTable("FOO_TBL"), is(table));
    }
    
    @Test
    void assertContainsView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertTrue(schema.containsView("FOO_VIEW"));
    }
    
    @Test
    void assertContainsViewWithOracleRule() {
        ShardingSphereView view = new ShardingSphereView("FOO_VIEW", "SELECT 1");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", oracleDatabaseType, Collections.emptyList(), Collections.singleton(view));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newUpperCaseRuleSet()));
        assertTrue(schema.containsView("foo_view"));
    }
    
    @Test
    void assertGetView() {
        ShardingSphereView view = new ShardingSphereView("foo_view", "SELECT 1");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.emptyList(), Collections.singleton(view));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertThat(schema.getView("FOO_VIEW"), is(view));
    }
    
    @Test
    void assertAttachIdentifierContext() {
        ShardingSphereTable table = new ShardingSphereTable("Foo_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        assertTrue(schema.containsTable("FOO_TBL"));
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertFalse(schema.containsTable("FOO_TBL"));
    }
    
    @Test
    void assertAttachIdentifierContextToTable() {
        ShardingSphereColumn column = new ShardingSphereColumn("Foo_Col", java.sql.Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        schema.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertFalse(schema.getTable("foo_tbl").containsColumn("FOO_COL"));
    }
    
    @Test
    void assertContainsTableByLogicalTableIndexWhenHeterogeneousLookupEnabled() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        Map<IdentifierScope, IdentifierCaseRule> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCaseRuleSets.newLowerCaseRuleSet().getRule(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCaseRuleSet(IdentifierCaseRuleSets.newUpperCaseRuleSet().getRule(IdentifierScope.TABLE), scopedRules), true);
        schema.refreshIdentifierContext(context);
        assertTrue(schema.containsTable("FOO_TBL"));
        assertThat(schema.getTable("FOO_TBL"), is(table));
    }
    
    @Test
    void assertGetTablePrioritizesPhysicalTableIndexWhenBothIndexesCanMatch() {
        ShardingSphereTable lowerCaseTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable upperCaseTable = new ShardingSphereTable("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(lowerCaseTable, upperCaseTable), Collections.emptyList());
        Map<IdentifierScope, IdentifierCaseRule> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCaseRuleSets.newLowerCaseRuleSet().getRule(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCaseRuleSet(IdentifierCaseRuleSets.newUpperCaseRuleSet().getRule(IdentifierScope.TABLE), scopedRules), true);
        schema.refreshIdentifierContext(context);
        assertThat(schema.getTable("FOO_TBL"), is(upperCaseTable));
    }
    
    @Test
    void assertRemoveTableByLogicalTableIndexWhenHeterogeneousLookupEnabled() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Collections.singleton(table), Collections.emptyList());
        Map<IdentifierScope, IdentifierCaseRule> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCaseRuleSets.newLowerCaseRuleSet().getRule(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCaseRuleSet(IdentifierCaseRuleSets.newUpperCaseRuleSet().getRule(IdentifierScope.TABLE), scopedRules), true);
        schema.refreshIdentifierContext(context);
        schema.removeTable("FOO_TBL");
        assertNull(schema.getTable("foo_tbl"));
    }
    
    @Test
    void assertRemoveTablePrioritizesPhysicalTableIndexWhenBothIndexesCanMatch() {
        ShardingSphereTable lowerCaseTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable upperCaseTable = new ShardingSphereTable("FOO_TBL", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", postgreSQLDatabaseType, Arrays.asList(lowerCaseTable, upperCaseTable), Collections.emptyList());
        Map<IdentifierScope, IdentifierCaseRule> scopedRules = new EnumMap<>(IdentifierScope.class);
        scopedRules.put(IdentifierScope.LOGICAL_TABLE, IdentifierCaseRuleSets.newLowerCaseRuleSet().getRule(IdentifierScope.TABLE));
        DatabaseIdentifierContext context = new DatabaseIdentifierContext(new IdentifierCaseRuleSet(IdentifierCaseRuleSets.newUpperCaseRuleSet().getRule(IdentifierScope.TABLE), scopedRules), true);
        schema.refreshIdentifierContext(context);
        schema.removeTable("FOO_TBL");
        assertThat(schema.getTable("FOO_TBL"), is(lowerCaseTable));
    }
}
