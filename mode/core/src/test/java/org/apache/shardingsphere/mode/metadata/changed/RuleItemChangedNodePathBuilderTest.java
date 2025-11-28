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

package org.apache.shardingsphere.mode.metadata.changed;

import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleItemChangedNodePathBuilderTest {
    
    private static final String DATABASE_NAME = "foo_db";
    
    private static final String RULE_TYPE = "fixture";
    
    private final RuleItemChangedNodePathBuilder builder = new RuleItemChangedNodePathBuilder();
    
    @Test
    void assertBuildWhenRuleTypeNotFound() {
        assertFalse(builder.build(DATABASE_NAME, "invalid/path", Type.ADDED).isPresent());
    }
    
    @Test
    void assertBuildForRuleTypeDeletion() {
        Optional<DatabaseRuleNodePath> actual = builder.build(DATABASE_NAME, NodePathGenerator.toPath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, null)), Type.DELETED);
        assertTrue(actual.isPresent());
        DatabaseRuleNodePath actualPath = actual.get();
        assertThat(actualPath.getRuleType(), is(RULE_TYPE));
        assertNull(actualPath.getDatabaseRuleItem());
    }
    
    @Test
    void assertBuildForNamedItemDeletionMatched() {
        String itemPath = NodePathGenerator.toPath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, new DatabaseRuleItem("named", "foo_rule")));
        Optional<DatabaseRuleNodePath> actual = builder.build(DATABASE_NAME, itemPath, Type.DELETED);
        assertTrue(actual.isPresent());
        DatabaseRuleNodePath actualPath = actual.get();
        assertThat(actualPath.getDatabaseRuleItem().getType(), is("named"));
        assertThat(actualPath.getDatabaseRuleItem().getName(), is("foo_rule"));
    }
    
    @Test
    void assertBuildForNamedItemDeletionWithExtraPath() {
        String itemPath = new VersionNodePath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, new DatabaseRuleItem("named", "bar_rule"))).getActiveVersionPath();
        assertFalse(builder.build(DATABASE_NAME, itemPath, Type.DELETED).isPresent());
    }
    
    @Test
    void assertBuildForNamedItemActiveVersion() {
        String activeVersionPath = new VersionNodePath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, new DatabaseRuleItem("named", "baz_rule"))).getActiveVersionPath();
        Optional<DatabaseRuleNodePath> actual = builder.build(DATABASE_NAME, activeVersionPath, Type.UPDATED);
        assertTrue(actual.isPresent());
        DatabaseRuleNodePath actualPath = actual.get();
        assertThat(actualPath.getDatabaseRuleItem().getType(), is("named"));
        assertThat(actualPath.getDatabaseRuleItem().getName(), is("baz_rule"));
    }
    
    @Test
    void assertBuildForNamedItemVersionsPathReturnsEmpty() {
        String versionsPath = new VersionNodePath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, new DatabaseRuleItem("named", "qux_rule"))).getVersionsPath() + "/0";
        assertFalse(builder.build(DATABASE_NAME, versionsPath, Type.UPDATED).isPresent());
    }
    
    @Test
    void assertBuildForUniqueItemDeletionMatched() {
        String uniquePath = NodePathGenerator.toPath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, new DatabaseRuleItem("unique")));
        Optional<DatabaseRuleNodePath> actual = builder.build(DATABASE_NAME, uniquePath, Type.DELETED);
        assertTrue(actual.isPresent());
        DatabaseRuleNodePath actualPath = actual.get();
        assertThat(actualPath.getDatabaseRuleItem().getType(), is("unique"));
        assertNull(actualPath.getDatabaseRuleItem().getName());
    }
    
    @Test
    void assertBuildForUniqueItemDeletionNotMatched() {
        String uniquePath = NodePathGenerator.toPath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, new DatabaseRuleItem("unique"))) + "/extra";
        Optional<DatabaseRuleNodePath> actual = builder.build(DATABASE_NAME, uniquePath, Type.DELETED);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertBuildForUniqueItemActiveVersion() {
        String activeVersionPath = new VersionNodePath(new DatabaseRuleNodePath(DATABASE_NAME, RULE_TYPE, new DatabaseRuleItem("unique"))).getActiveVersionPath();
        Optional<DatabaseRuleNodePath> actual = builder.build(DATABASE_NAME, activeVersionPath, Type.UPDATED);
        assertTrue(actual.isPresent());
        DatabaseRuleNodePath actualPath = actual.get();
        assertThat(actualPath.getDatabaseRuleItem().getType(), is("unique"));
        assertNull(actualPath.getDatabaseRuleItem().getName());
    }
}
