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
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereTableIdentifierTest {
    
    @Test
    void assertContainsColumn() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        table.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertTrue(table.containsColumn("FOO_COL"));
    }
    
    @Test
    void assertContainsColumnWithOracleRule() {
        ShardingSphereColumn column = new ShardingSphereColumn("FOO_COL", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        table.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newUpperCaseRuleSet()));
        assertTrue(table.containsColumn("foo_col"));
    }
    
    @Test
    void assertGetColumn() {
        ShardingSphereColumn column = new ShardingSphereColumn("foo_col", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        table.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertThat(table.getColumn("FOO_COL"), is(column));
    }
    
    @Test
    void assertFindColumnNamesIfNotExistedFrom() {
        ShardingSphereColumn column1 = new ShardingSphereColumn("foo_col", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("bar_col", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Arrays.asList(column1, column2), Collections.emptyList(), Collections.emptyList());
        table.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertThat(table.findColumnNamesIfNotExistedFrom(Collections.singleton("FOO_COL")), is(Collections.singleton("bar_col")));
    }
    
    @Test
    void assertGetIndex() {
        ShardingSphereIndex index = new ShardingSphereIndex("foo_idx", Collections.emptyList(), false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.singleton(index), Collections.emptyList());
        table.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertThat(table.getIndex("FOO_IDX"), is(index));
    }
    
    @Test
    void assertContainsConstraint() {
        ShardingSphereConstraint constraint = new ShardingSphereConstraint("FOO_FK", "ref_tbl");
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.singleton(constraint));
        table.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newUpperCaseRuleSet()));
        assertTrue(table.containsConstraint("foo_fk"));
    }
    
    @Test
    void assertRefreshIdentifierContext() {
        ShardingSphereColumn column = new ShardingSphereColumn("Foo_Col", Types.INTEGER, false, true, false, true, false, false);
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        assertTrue(table.containsColumn("FOO_COL"));
        table.refreshIdentifierContext(new DatabaseIdentifierContext(IdentifierCaseRuleSets.newLowerCaseRuleSet()));
        assertFalse(table.containsColumn("FOO_COL"));
    }
}
