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

package org.apache.shardingsphere.mode.node.path.metadata;

import org.apache.shardingsphere.mode.node.path.config.database.item.DatabaseRuleItem;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DatabaseRuleMetaDataNodePathTest {
    
    @Test
    void assertGetRootPath() {
        assertThat(DatabaseRuleMetaDataNodePath.getRootPath("foo_db"), is("/metadata/foo_db/rules"));
    }
    
    @Test
    void assertGetRulePath() {
        assertThat(DatabaseRuleMetaDataNodePath.getRulePath("foo_db", "foo_rule"), is("/metadata/foo_db/rules/foo_rule"));
    }
    
    @Test
    void assertGetRulePathWithKey() {
        assertThat(DatabaseRuleMetaDataNodePath.getRulePath("foo_db", "foo_rule", new DatabaseRuleItem("foo_rule_item")), is("/metadata/foo_db/rules/foo_rule/foo_rule_item"));
    }
    
    @Test
    void assertGetVersionNodePathGenerator() {
        DatabaseRuleItem databaseRuleItem = new DatabaseRuleItem("foo_rule_item");
        assertThat(DatabaseRuleMetaDataNodePath.getVersionNodePathGenerator("foo_db", "foo_rule", databaseRuleItem).getActiveVersionPath(),
                is("/metadata/foo_db/rules/foo_rule/foo_rule_item/active_version"));
        assertThat(DatabaseRuleMetaDataNodePath.getVersionNodePathGenerator("foo_db", "foo_rule", databaseRuleItem).getVersionsPath(), is("/metadata/foo_db/rules/foo_rule/foo_rule_item/versions"));
        assertThat(DatabaseRuleMetaDataNodePath.getVersionNodePathGenerator("foo_db", "foo_rule", databaseRuleItem).getVersionPath(0), is("/metadata/foo_db/rules/foo_rule/foo_rule_item/versions/0"));
    }
}
