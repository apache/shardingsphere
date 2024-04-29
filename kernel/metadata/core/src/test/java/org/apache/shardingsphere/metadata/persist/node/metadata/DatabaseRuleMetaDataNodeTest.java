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

package org.apache.shardingsphere.metadata.persist.node.metadata;

import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DatabaseRuleMetaDataNodeTest {
    
    @Test
    void assertGetDatabaseRuleNode() {
        assertThat(DatabaseRuleMetaDataNode.getDatabaseRuleNode("foo_db", "foo_rule", "sharding"), is("/metadata/foo_db/rules/foo_rule/sharding"));
    }
    
    @Test
    void assertGetDatabaseRuleActiveVersionNode() {
        assertThat(DatabaseRuleMetaDataNode.getDatabaseRuleActiveVersionNode("foo_db", "foo_rule", "foo_tables"), is("/metadata/foo_db/rules/foo_rule/foo_tables/active_version"));
    }
    
    @Test
    void assertGetDatabaseRuleVersionsNode() {
        assertThat(DatabaseRuleMetaDataNode.getDatabaseRuleVersionsNode("foo_db", "sharding", "foo_key"), is("/metadata/foo_db/rules/sharding/foo_key/versions"));
    }
    
    @Test
    void assertGetDatabaseRuleVersionNode() {
        assertThat(DatabaseRuleMetaDataNode.getDatabaseRuleVersionNode("foo_db", "foo_rule", "foo_tables", "1"), is("/metadata/foo_db/rules/foo_rule/foo_tables/versions/1"));
    }
}
