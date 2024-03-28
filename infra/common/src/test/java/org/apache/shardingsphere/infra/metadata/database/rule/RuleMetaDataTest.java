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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleMetaDataTest {
    
    private final RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(new ShardingSphereRuleFixture()));
    
    @Test
    void assertFindRules() {
        assertThat(ruleMetaData.findRules(ShardingSphereRuleFixture.class).size(), is(1));
    }
    
    @Test
    void assertFindSingleRule() {
        assertTrue(ruleMetaData.findSingleRule(ShardingSphereRuleFixture.class).isPresent());
    }
    
    @Test
    void assertGetSingleRule() {
        assertThat(ruleMetaData.getSingleRule(ShardingSphereRuleFixture.class), instanceOf(ShardingSphereRuleFixture.class));
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMapWhenRulesAreEmpty() {
        Collection<ShardingSphereRule> rules = new ArrayList<>();
        RuleMetaData ruleMetaData = new RuleMetaData(rules);
        Map<String, Collection<Class<? extends ShardingSphereRule>>> actual = ruleMetaData.getInUsedStorageUnitNameAndRulesMap();
        assertThat(actual.size(), is(0));
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMapWhenRulesContainDataNodeContainedRule() {
        Collection<ShardingSphereRule> rules = new ArrayList<>();
        DataNodeContainedRule rule = new MockDataNodeContainedRule();
        rules.add(rule);
        RuleMetaData ruleMetaData = new RuleMetaData(rules);
        Map<String, Collection<Class<? extends ShardingSphereRule>>> actual = ruleMetaData.getInUsedStorageUnitNameAndRulesMap();
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("testDataNodeSourceName"));
        assertThat(actual.get("testDataNodeSourceName").size(), is(1));
        assertThat(actual.get("testDataNodeSourceName").size(), is(1));
        assertTrue(actual.get("testDataNodeSourceName").contains(MockDataNodeContainedRule.class));
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMapWhenRulesContainBothDataSourceContainedRuleAndDataNodeContainedRule() {
        Collection<ShardingSphereRule> rules = new ArrayList<>();
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class);
        when(dataSourceContainedRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("test", Arrays.asList("testDataSourceName")));
        DataNodeContainedRule dataNodeContainedRule = new MockDataNodeContainedRule();
        rules.add(dataSourceContainedRule);
        rules.add(dataNodeContainedRule);
        RuleMetaData ruleMetaData = new RuleMetaData(rules);
        Map<String, Collection<Class<? extends ShardingSphereRule>>> actual = ruleMetaData.getInUsedStorageUnitNameAndRulesMap();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("testDataSourceName"));
        assertTrue(actual.containsKey("testDataNodeSourceName"));
        assertTrue(actual.get("testDataSourceName").contains(dataSourceContainedRule.getClass()));
        assertTrue(actual.get("testDataNodeSourceName").contains(MockDataNodeContainedRule.class));
    }
    
    private static class MockDataNodeContainedRule implements DataNodeContainedRule {
        
        @Override
        public RuleConfiguration getConfiguration() {
            return mock(RuleConfiguration.class);
        }
        
        @Override
        public Map<String, Collection<DataNode>> getAllDataNodes() {
            Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
            result.put("test", Arrays.asList(new DataNode("testDataNodeSourceName", "testTableName")));
            return result;
        }
        
        @Override
        public boolean isNeedAccumulate(final Collection<String> tableNames) {
            return false;
        }
        
        @Override
        public Optional<String> findLogicTableByActualTable(final String actualTable) {
            return Optional.empty();
        }
        
        @Override
        public Optional<String> findFirstActualTable(final String logicTable) {
            return Optional.empty();
        }
        
        @Override
        public Collection<DataNode> getDataNodesByTableName(final String tableName) {
            return null;
        }
        
        @Override
        public Optional<String> findActualTableByCatalog(final String catalog, final String logicTable) {
            return Optional.empty();
        }
    }
}
