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

package org.apache.shardingsphere.mode.path.item;

import org.apache.shardingsphere.mode.path.root.RuleRootNodePath;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NamedRuleItemNodePathTest {
    
    private final NamedRuleItemNodePath converter = new NamedRuleItemNodePath(new RuleRootNodePath("foo"), "tables");
    
    @Test
    void assertGetPath() {
        assertThat(converter.getPath("foo_table"), is("tables/foo_table"));
    }
    
    @Test
    void assertGetName() {
        Optional<String> actual = converter.getName("/metadata/foo_db/rules/foo/tables/foo_table/versions/0");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
    
    @Test
    void assertGetNameWithInvalidPath() {
        assertFalse(converter.getName("/metadata/foo_db/rules/bar/tables/foo_table/versions/0").isPresent());
    }
    
    @Test
    void assertGetNameByActiveVersionPath() {
        Optional<String> actual = converter.getNameByActiveVersion("/metadata/foo_db/rules/foo/tables/foo_table/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_table"));
    }
}
