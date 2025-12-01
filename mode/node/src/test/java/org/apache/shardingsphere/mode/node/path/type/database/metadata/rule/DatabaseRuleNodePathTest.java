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

package org.apache.shardingsphere.mode.node.path.type.database.metadata.rule;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseRuleNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new DatabaseRuleNodePath("foo_db", null, null)), is("/metadata/foo_db/rules"));
        assertThat(NodePathGenerator.toPath(new DatabaseRuleNodePath("foo_db", "foo_rule", null)), is("/metadata/foo_db/rules/foo_rule"));
        assertThat(NodePathGenerator.toPath(new DatabaseRuleNodePath("foo_db", "foo_rule", new DatabaseRuleItem("unique_rule_item"))),
                is("/metadata/foo_db/rules/foo_rule/unique_rule_item"));
        assertThat(NodePathGenerator.toPath(new DatabaseRuleNodePath("foo_db", "foo_rule", new DatabaseRuleItem("named_rule_item/item"))),
                is("/metadata/foo_db/rules/foo_rule/named_rule_item/item"));
    }
    
    @Test
    void assertToVersionPath() {
        VersionNodePath versionNodePath = new VersionNodePath(new DatabaseRuleNodePath("foo_db", "foo_rule", new DatabaseRuleItem("named_rule_item/item")));
        assertThat(versionNodePath.getActiveVersionPath(), is("/metadata/foo_db/rules/foo_rule/named_rule_item/item/active_version"));
        assertThat(versionNodePath.getVersionsPath(), is("/metadata/foo_db/rules/foo_rule/named_rule_item/item/versions"));
        assertThat(versionNodePath.getVersionPath(0), is("/metadata/foo_db/rules/foo_rule/named_rule_item/item/versions/0"));
    }
    
    @Test
    void assertCreateRuleItemNameSearchCriteria() {
        assertThat(NodePathSearcher.get("/metadata/foo_db/rules/foo_rule/foo_rule_item/item_value",
                DatabaseRuleNodePath.createRuleItemNameSearchCriteria("foo_db", "foo_rule", "foo_rule_item")), is("item_value"));
        assertThat(NodePathSearcher.get("/metadata/foo_db/rules/foo_rule/foo_rule_item/item_value/versions/0",
                DatabaseRuleNodePath.createRuleItemNameSearchCriteria("foo_db", "foo_rule", "foo_rule_item")), is("item_value"));
        assertFalse(NodePathSearcher.find("/metadata/foo_db/rules/foo_rule/foo_rule_item",
                DatabaseRuleNodePath.createRuleItemNameSearchCriteria("foo_db", "foo_rule", "foo_rule_item")).isPresent());
        assertFalse(NodePathSearcher.find("/metadata/foo_db/rules/bar_rule/foo_rule_item/item_value",
                DatabaseRuleNodePath.createRuleItemNameSearchCriteria("foo_db", "foo_rule", "foo_rule_item")).isPresent());
    }
    
    @Test
    void assertCreateRuleTypeSearchCriteria() {
        assertTrue(NodePathSearcher.isMatchedPath("/metadata/foo_db/rules/foo_rule", DatabaseRuleNodePath.createRuleTypeSearchCriteria("foo_db")));
        assertTrue(DatabaseRuleNodePath.isRuleTypePath("foo_db", "/metadata/foo_db/rules/foo_rule"));
        assertFalse(DatabaseRuleNodePath.isRuleTypePath("foo_db", "/metadata/foo_db/rules/foo_rule/foo_rule_item"));
    }
}
