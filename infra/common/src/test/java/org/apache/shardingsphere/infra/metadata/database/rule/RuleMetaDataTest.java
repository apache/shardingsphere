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

package org.apache.shardingsphere.infra.metadata.database.rule;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class RuleMetaDataTest {
    
    private final RuleMetaData ruleMetaData = new RuleMetaData(Arrays.asList(new ShardingSphereRuleFixture(), mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS)));
    
    @Test
    void assertGetConfigurations() {
        assertThat(ruleMetaData.getConfigurations().size(), is(2));
    }
    
    @Test
    void assertFindRules() {
        assertThat(ruleMetaData.findRules(ShardingSphereRuleFixture.class).size(), is(1));
    }
    
    @Test
    void assertFindSingleRule() {
        assertTrue(ruleMetaData.findSingleRule(ShardingSphereRuleFixture.class).isPresent());
    }
    
    @Test
    void assertFindSingleRuleFailed() {
        assertFalse(ruleMetaData.findSingleRule(mock(GlobalRule.class).getClass()).isPresent());
    }
    
    @Test
    void assertGetSingleRule() {
        assertThat(ruleMetaData.getSingleRule(ShardingSphereRuleFixture.class), instanceOf(ShardingSphereRuleFixture.class));
    }
    
    @Test
    void assertGetSingleRuleFailed() {
        assertThrows(IllegalStateException.class, () -> ruleMetaData.getSingleRule(mock(GlobalRule.class).getClass()));
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMapWhenRulesAreEmpty() {
        assertTrue(new RuleMetaData(Collections.emptyList()).getInUsedStorageUnitNameAndRulesMap().isEmpty());
    }
    
    @Test
    void assertGetAttributes() {
        assertTrue(ruleMetaData.getAttributes(RuleAttribute.class).isEmpty());
    }
}
