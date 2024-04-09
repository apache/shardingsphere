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

package org.apache.shardingsphere.shadow.metadata.nodepath;

import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowRuleNodePathProviderTest {
    
    @Test
    void assertNew() {
        RuleNodePathProvider ruleNodePathProvider = new ShadowRuleNodePathProvider();
        RuleNodePath actualRuleNodePath = ruleNodePathProvider.getRuleNodePath();
        assertThat(actualRuleNodePath.getNamedItems().size(), is(3));
        assertTrue(actualRuleNodePath.getNamedItems().containsKey(ShadowRuleNodePathProvider.ALGORITHMS));
        assertTrue(actualRuleNodePath.getNamedItems().containsKey(ShadowRuleNodePathProvider.TABLES));
        assertTrue(actualRuleNodePath.getNamedItems().containsKey(ShadowRuleNodePathProvider.DATA_SOURCES));
        assertThat(actualRuleNodePath.getUniqueItems().size(), is(1));
        assertTrue(actualRuleNodePath.getUniqueItems().containsKey(ShadowRuleNodePathProvider.DEFAULT_ALGORITHM));
        assertThat(actualRuleNodePath.getRoot().getRuleType(), is(ShadowRuleNodePathProvider.RULE_TYPE));
    }
}
