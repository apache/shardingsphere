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

package org.apache.shardingsphere.mode.node.path.type.global.config;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalRuleNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new GlobalRuleNodePath(null)), is("/rules"));
        assertThat(NodePathGenerator.toPath(new GlobalRuleNodePath("foo_rule")), is("/rules/foo_rule"));
    }
    
    @Test
    void assertGetVersion() {
        assertTrue(new VersionNodePath(new GlobalRuleNodePath("foo_rule")).isActiveVersionPath("/rules/foo_rule/active_version"));
    }
    
    @Test
    void assertCreateRuleTypeSearchCriteria() {
        assertThat(NodePathSearcher.get("/rules/foo_rule", GlobalRuleNodePath.createRuleTypeSearchCriteria()), is("foo_rule"));
        assertThat(NodePathSearcher.get("/rules/foo_rule/active_version", GlobalRuleNodePath.createRuleTypeSearchCriteria()), is("foo_rule"));
        assertFalse(NodePathSearcher.find("/xxx/foo_rule/active_version", GlobalRuleNodePath.createRuleTypeSearchCriteria()).isPresent());
    }
}
