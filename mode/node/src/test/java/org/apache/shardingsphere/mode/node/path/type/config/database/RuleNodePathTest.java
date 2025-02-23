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

package org.apache.shardingsphere.mode.node.path.type.config.database;

import org.apache.shardingsphere.mode.node.path.type.config.database.item.NamedDatabaseRuleItemNodePath;
import org.apache.shardingsphere.mode.node.path.type.config.database.item.UniqueDatabaseRuleItemNodePath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RuleNodePathTest {
    
    private DatabaseRuleNodePath databaseRuleNodePath;
    
    @BeforeEach
    void setup() {
        List<String> namedRuleItemNodePathTypes = Collections.singletonList("tables");
        List<String> uniqueRuleItemNodePathTypes = Arrays.asList("tables", "tables/type");
        databaseRuleNodePath = new DatabaseRuleNodePath("foo", namedRuleItemNodePathTypes, uniqueRuleItemNodePathTypes);
    }
    
    @Test
    void assertFindNameByVersion() {
        NamedDatabaseRuleItemNodePath namedRulePath = databaseRuleNodePath.getNamedItem("tables");
        assertThat(namedRulePath.getPath("foo_tbl"), is("tables/foo_tbl"));
    }
    
    @Test
    void assertGetUniqueItem() {
        UniqueDatabaseRuleItemNodePath uniqueRulePath = databaseRuleNodePath.getUniqueItem("tables");
        assertThat(uniqueRulePath.getPath(), is("tables"));
        UniqueDatabaseRuleItemNodePath uniqueRulePathWithType = databaseRuleNodePath.getUniqueItem("tables/type");
        assertThat(uniqueRulePathWithType.getPath(), is("tables/type"));
    }
}
