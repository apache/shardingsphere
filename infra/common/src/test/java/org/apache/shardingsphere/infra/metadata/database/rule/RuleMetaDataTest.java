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

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleMetaDataTest {
    
    private RuleMetaData ruleMetaData;
    
    @BeforeEach
    void setUp() {
        ruleMetaData = new RuleMetaData(Arrays.asList(new RuleMetaDataShardingSphereRuleFixture(), mockDataSourceMapperRule(), mockDataNodeRule()));
    }
    
    private ShardingSphereRule mockDataSourceMapperRule() {
        ShardingSphereRule result = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDataSourceMapper().values()).thenReturn(Collections.singletonList(Collections.singletonList("foo_ds")));
        when(result.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class)).thenReturn(Optional.of(ruleAttribute));
        return result;
    }
    
    private ShardingSphereRule mockDataNodeRule() {
        ShardingSphereRule result = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getAllDataNodes().values()).thenReturn(Collections.singleton(Collections.singleton(new DataNode("foo_db.foo_tbl"))));
        when(result.getAttributes().findAttribute(DataNodeRuleAttribute.class)).thenReturn(Optional.of(ruleAttribute));
        return result;
    }
    
    @Test
    void assertGetConfigurations() {
        assertThat(ruleMetaData.getConfigurations().size(), is(3));
    }
    
    @Test
    void assertFindRules() {
        assertThat(ruleMetaData.findRules(RuleMetaDataShardingSphereRuleFixture.class).size(), is(1));
    }
    
    @Test
    void assertFindSingleRule() {
        assertTrue(ruleMetaData.findSingleRule(RuleMetaDataShardingSphereRuleFixture.class).isPresent());
    }
    
    @Test
    void assertFindSingleRuleFailed() {
        assertFalse(ruleMetaData.findSingleRule(mock(GlobalRule.class).getClass()).isPresent());
    }
    
    @Test
    void assertGetSingleRule() {
        assertThat(ruleMetaData.getSingleRule(RuleMetaDataShardingSphereRuleFixture.class), instanceOf(RuleMetaDataShardingSphereRuleFixture.class));
    }
    
    @Test
    void assertGetSingleRuleFailed() {
        assertThrows(IllegalStateException.class, () -> ruleMetaData.getSingleRule(mock(GlobalRule.class).getClass()));
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMap() {
        Map<String, Collection<Class<? extends ShardingSphereRule>>> actual = ruleMetaData.getInUsedStorageUnitNameAndRulesMap();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("foo_ds"));
        assertTrue(actual.containsKey("foo_db"));
    }
    
    @Test
    void assertGetAttributes() {
        assertTrue(ruleMetaData.getAttributes(RuleAttribute.class).isEmpty());
        assertFalse(ruleMetaData.getAttributes(DataSourceMapperRuleAttribute.class).isEmpty());
        assertFalse(ruleMetaData.getAttributes(DataNodeRuleAttribute.class).isEmpty());
    }
}
