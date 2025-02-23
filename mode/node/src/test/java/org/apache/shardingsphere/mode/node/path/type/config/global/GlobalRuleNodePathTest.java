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

package org.apache.shardingsphere.mode.node.path.type.config.global;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePathParser;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalRuleNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new GlobalRuleNodePath(null), false), is("/rules"));
        assertThat(NodePathGenerator.toPath(new GlobalRuleNodePath("foo_rule"), false), is("/rules/foo_rule"));
    }
    
    @Test
    void assertGetVersion() {
        VersionNodePathParser versionNodePathParser = NodePathSearcher.getVersion(new GlobalRuleNodePath("foo_rule"));
        assertTrue(versionNodePathParser.isActiveVersionPath("/rules/foo_rule/active_version"));
        assertTrue(versionNodePathParser.isVersionPath("/rules/foo_rule/versions/0"));
    }
}
