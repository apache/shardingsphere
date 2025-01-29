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
        assertThat(DatabaseRuleMetaDataNodePath.getRulePath("foo_db", "foo_rule", "sharding"), is("/metadata/foo_db/rules/foo_rule/sharding"));
    }
    
    @Test
    void assertGetVersionsPath() {
        assertThat(DatabaseRuleMetaDataNodePath.getVersionsPath("foo_db", "sharding", "foo_key"), is("/metadata/foo_db/rules/sharding/foo_key/versions"));
    }
    
    @Test
    void assertGetVersionPath() {
        assertThat(DatabaseRuleMetaDataNodePath.getVersionPath("foo_db", "foo_rule", "foo_tbl", "1"), is("/metadata/foo_db/rules/foo_rule/foo_tbl/versions/1"));
    }
    
    @Test
    void assertGetActiveVersionPath() {
        assertThat(DatabaseRuleMetaDataNodePath.getActiveVersionPath("foo_db", "foo_rule", "foo_tbl"), is("/metadata/foo_db/rules/foo_rule/foo_tbl/active_version"));
    }
}
