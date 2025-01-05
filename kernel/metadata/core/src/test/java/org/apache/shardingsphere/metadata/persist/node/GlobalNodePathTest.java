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

package org.apache.shardingsphere.metadata.persist.node;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GlobalNodePathTest {
    
    @Test
    void assertGetRuleRootPath() {
        assertThat(GlobalNodePath.getRuleRootPath(), is("/rules"));
    }
    
    @Test
    void assertGetRulePath() {
        assertThat(GlobalNodePath.getRulePath("foo_rule"), is("/rules/foo_rule"));
    }
    
    @Test
    void assertGetRuleVersionsPath() {
        assertThat(GlobalNodePath.getRuleVersionsPath("foo_rule"), is("/rules/foo_rule/versions"));
    }
    
    @Test
    void assertGetRuleVersionPath() {
        assertThat(GlobalNodePath.getRuleVersionPath("foo_rule", "0"), is("/rules/foo_rule/versions/0"));
    }
    
    @Test
    void assertGetRuleActiveVersionPath() {
        assertThat(GlobalNodePath.getRuleActiveVersionPath("foo_rule"), is("/rules/foo_rule/active_version"));
    }
    
    @Test
    void assertGetPropsRootPath() {
        assertThat(GlobalNodePath.getPropsRootPath(), is("/props"));
    }
    
    @Test
    void assertGetPropsVersionsPath() {
        assertThat(GlobalNodePath.getPropsVersionsPath(), is("/props/versions"));
    }
    
    @Test
    void assertGetPropsVersionPath() {
        assertThat(GlobalNodePath.getPropsVersionPath("0"), is("/props/versions/0"));
    }
    
    @Test
    void assertGetPropsActiveVersionPath() {
        assertThat(GlobalNodePath.getPropsActiveVersionPath(), is("/props/active_version"));
    }
}
