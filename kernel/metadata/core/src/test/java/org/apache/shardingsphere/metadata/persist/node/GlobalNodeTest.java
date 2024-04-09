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

class GlobalNodeTest {
    
    @Test
    void assertGetGlobalRuleRootNode() {
        assertThat(GlobalNode.getGlobalRuleRootNode(), is("/rules"));
    }
    
    @Test
    void assertGetPropsActiveVersionNode() {
        assertThat(GlobalNode.getPropsActiveVersionNode(), is("/props/active_version"));
    }
    
    @Test
    void assertGetPropsVersionNode() {
        assertThat(GlobalNode.getPropsVersionNode("0"), is("/props/versions/0"));
    }
    
    @Test
    void assertGetGlobalRuleActiveVersionNode() {
        assertThat(GlobalNode.getGlobalRuleActiveVersionNode("transaction"), is("/rules/transaction/active_version"));
    }
    
    @Test
    void assertGetGlobalRuleVersionsNode() {
        assertThat(GlobalNode.getGlobalRuleVersionsNode("transaction"), is("/rules/transaction/versions"));
    }
    
    @Test
    void assertGetGlobalRuleVersionNode() {
        assertThat(GlobalNode.getGlobalRuleVersionNode("transaction", "0"), is("/rules/transaction/versions/0"));
    }
}
