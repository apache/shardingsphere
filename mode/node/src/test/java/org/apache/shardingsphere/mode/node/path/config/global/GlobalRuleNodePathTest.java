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

package org.apache.shardingsphere.mode.node.path.config.global;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalRuleNodePathTest {
    
    @Test
    void assertGetRootPath() {
        assertThat(GlobalRuleNodePath.getRootPath(), is("/rules"));
    }
    
    @Test
    void assertGetRulePath() {
        assertThat(GlobalRuleNodePath.getRulePath("foo_rule"), is("/rules/foo_rule"));
    }
    
    @Test
    void assertGetVersionNodePathGenerator() {
        assertThat(GlobalRuleNodePath.getVersionNodePathGenerator("foo_rule").getActiveVersionPath(), is("/rules/foo_rule/active_version"));
        assertThat(GlobalRuleNodePath.getVersionNodePathGenerator("foo_rule").getVersionsPath(), is("/rules/foo_rule/versions"));
        assertThat(GlobalRuleNodePath.getVersionNodePathGenerator("foo_rule").getVersionPath(0), is("/rules/foo_rule/versions/0"));
    }
    
    @Test
    void assertGetVersionNodePathParser() {
        assertTrue(GlobalRuleNodePath.getVersionNodePathParser().findIdentifierByActiveVersionPath("/rules/foo_rule/active_version", 1).isPresent());
        assertThat(GlobalRuleNodePath.getVersionNodePathParser().findIdentifierByActiveVersionPath("/rules/foo_rule/active_version", 1).get(), is("foo_rule"));
        assertFalse(GlobalRuleNodePath.getVersionNodePathParser().findIdentifierByActiveVersionPath("/rules/foo_rule/versions", 1).isPresent());
    }
    
    @Test
    void assertGetRuleVersionNodePathParser() {
        assertTrue(GlobalRuleNodePath.getRuleVersionNodePathParser("foo_rule").isVersionPath("/rules/foo_rule/versions/0"));
        assertFalse(GlobalRuleNodePath.getRuleVersionNodePathParser("foo_rule").isVersionPath("/rules/foo_rule/active_version"));
    }
}
