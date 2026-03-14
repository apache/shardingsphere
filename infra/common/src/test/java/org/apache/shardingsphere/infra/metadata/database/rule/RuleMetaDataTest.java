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
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleMetaDataTest {
    
    private RuleMetaDataShardingSphereRuleFixture fixtureRule;
    
    private ShardingSphereRule dataSourceMapperRule;
    
    private ShardingSphereRule dataNodeRule;
    
    private RuleMetaData ruleMetaData;
    
    @BeforeEach
    void setUp() {
        fixtureRule = new RuleMetaDataShardingSphereRuleFixture();
        dataSourceMapperRule = mockDataSourceMapperRule("foo_ds");
        dataNodeRule = mockDataNodeRule();
        ruleMetaData = new RuleMetaData(Arrays.asList(fixtureRule, dataSourceMapperRule, dataNodeRule));
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
    void assertFindRulesWithCachedRules() {
        ruleMetaData.findSingleRule(RuleMetaDataShardingSphereRuleFixture.class);
        assertThat(ruleMetaData.findRules(RuleMetaDataShardingSphereRuleFixture.class).size(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("findSingleRuleArguments")
    void assertFindSingleRule(final String name, final boolean cached, final Class<? extends ShardingSphereRule> ruleClass, final boolean expectedPresent) {
        if (cached) {
            assertThat(ruleMetaData.getSingleRule(RuleMetaDataShardingSphereRuleFixture.class), isA(RuleMetaDataShardingSphereRuleFixture.class));
        }
        assertThat(ruleMetaData.findSingleRule(ruleClass).isPresent(), is(expectedPresent));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSingleRuleArguments")
    void assertGetSingleRule(final String name, final boolean cached,
                             final Class<? extends ShardingSphereRule> ruleClass, final Class<?> expectedType, final Class<? extends Throwable> expectedExceptionClass) {
        if (cached) {
            assertTrue(ruleMetaData.findSingleRule(RuleMetaDataShardingSphereRuleFixture.class).isPresent());
        }
        if (null == expectedExceptionClass) {
            assertThat(ruleMetaData.getSingleRule(ruleClass), isA(expectedType));
        } else {
            assertThat(assertThrows(expectedExceptionClass, () -> ruleMetaData.getSingleRule(ruleClass)), isA(expectedExceptionClass));
        }
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMap() {
        Map<String, Collection<Class<? extends ShardingSphereRule>>> actual = ruleMetaData.getInUsedStorageUnitNameAndRulesMap();
        assertThat(actual.size(), is(2));
        assertTrue(actual.containsKey("foo_ds"));
        assertTrue(actual.containsKey("foo_db"));
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMapWhenRuleClassAddedForExistingStorageUnit() {
        ShardingSphereRule firstRule = mockDataSourceMapperRuleAttributeRule(RuleMetaDataShardingSphereRuleFixture.class, "shared_ds");
        ShardingSphereRule secondRule = mockDataSourceMapperRuleAttributeRule(ShardingSphereRule.class, "shared_ds");
        Map<String, Collection<Class<? extends ShardingSphereRule>>> actual = new RuleMetaData(Arrays.asList(firstRule, secondRule)).getInUsedStorageUnitNameAndRulesMap();
        Collection<Class<? extends ShardingSphereRule>> ruleClasses = actual.get("shared_ds");
        assertThat(ruleClasses.size(), is(2));
        assertTrue(ruleClasses.contains(firstRule.getClass()));
        assertTrue(ruleClasses.contains(secondRule.getClass()));
    }
    
    @Test
    void assertGetInUsedStorageUnitNameAndRulesMapWhenDuplicatedRuleClassSkippedForExistingStorageUnit() {
        ShardingSphereRule duplicatedRule = mockDataSourceMapperRuleAttributeRule(RuleMetaDataShardingSphereRuleFixture.class, "dup_ds");
        RuleMetaData metaData = new RuleMetaData(Arrays.asList(duplicatedRule, duplicatedRule));
        assertThat(metaData.getInUsedStorageUnitNameAndRulesMap().get("dup_ds").size(), is(1));
    }
    
    private <T extends ShardingSphereRule> T mockDataSourceMapperRuleAttributeRule(final Class<T> type, final String storageUnitName) {
        T result = mock(type, RETURNS_DEEP_STUBS);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("logic_db", Collections.singleton(storageUnitName)));
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    @Test
    void assertGetAttributes() {
        assertTrue(ruleMetaData.getAttributes(RuleAttribute.class).isEmpty());
        assertFalse(ruleMetaData.getAttributes(DataSourceMapperRuleAttribute.class).isEmpty());
        assertFalse(ruleMetaData.getAttributes(DataNodeRuleAttribute.class).isEmpty());
    }
    
    @Test
    void assertGetAttributesWithCachedAttributes() {
        ruleMetaData.findAttribute(DataSourceMapperRuleAttribute.class);
        assertThat(ruleMetaData.getAttributes(DataSourceMapperRuleAttribute.class).size(), is(1));
    }
    
    @Test
    void assertFindAttribute() {
        Optional<DataSourceMapperRuleAttribute> actual = ruleMetaData.findAttribute(DataSourceMapperRuleAttribute.class);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(DataSourceMapperRuleAttribute.class));
    }
    
    @Test
    void assertFindEmptyAttribute() {
        assertFalse(new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS))).findAttribute(DataSourceMapperRuleAttribute.class).isPresent());
    }
    
    @Test
    void assertInvalidateCacheWhenRuleAdded() {
        ruleMetaData.findSingleRule(RuleMetaDataShardingSphereRuleFixture.class);
        ruleMetaData.getRules().add(new RuleMetaDataShardingSphereRuleFixture());
        assertThat(ruleMetaData.findRules(RuleMetaDataShardingSphereRuleFixture.class).size(), is(2));
    }
    
    @Test
    void assertInvalidateCacheWhenRulesAdded() {
        ruleMetaData.findAttribute(DataSourceMapperRuleAttribute.class);
        ruleMetaData.getRules().add(mockDataSourceMapperRule("bar_ds"));
        assertThat(ruleMetaData.getAttributes(DataSourceMapperRuleAttribute.class).size(), is(2));
    }
    
    @Test
    void assertInvalidateCacheWhenRuleRemoved() {
        ruleMetaData.findSingleRule(RuleMetaDataShardingSphereRuleFixture.class);
        ruleMetaData.getRules().remove(fixtureRule);
        assertTrue(ruleMetaData.findRules(RuleMetaDataShardingSphereRuleFixture.class).isEmpty());
    }
    
    @Test
    void assertInvalidateCacheWhenRulesRemoved() {
        ruleMetaData.findAttribute(DataSourceMapperRuleAttribute.class);
        ruleMetaData.getRules().removeAll(Collections.singleton(dataSourceMapperRule));
        assertTrue(ruleMetaData.getAttributes(DataSourceMapperRuleAttribute.class).isEmpty());
    }
    
    @Test
    void assertInvalidateCacheWhenRulesRemovedByPredicate() {
        ruleMetaData.getInUsedStorageUnitNameAndRulesMap();
        ruleMetaData.getRules().removeIf(each -> each == dataNodeRule);
        assertThat(ruleMetaData.getInUsedStorageUnitNameAndRulesMap().size(), is(1));
    }
    
    @Test
    void assertInvalidateCacheWhenRulesRetained() {
        ruleMetaData.findAttribute(DataNodeRuleAttribute.class);
        ruleMetaData.getRules().retainAll(Collections.singleton(dataSourceMapperRule));
        assertTrue(ruleMetaData.getAttributes(DataNodeRuleAttribute.class).isEmpty());
    }
    
    @Test
    void assertInvalidateCacheWhenRulesCleared() {
        ruleMetaData.findSingleRule(RuleMetaDataShardingSphereRuleFixture.class);
        ruleMetaData.getRules().clear();
        assertTrue(ruleMetaData.getConfigurations().isEmpty());
    }
    
    private ShardingSphereRule mockDataSourceMapperRule(final String storageUnitName) {
        ShardingSphereRule result = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDataSourceMapper().values()).thenReturn(Collections.singletonList(Collections.singletonList(storageUnitName)));
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
    
    private static Stream<Arguments> findSingleRuleArguments() {
        return Stream.of(
                Arguments.of("single rule present", false, RuleMetaDataShardingSphereRuleFixture.class, true),
                Arguments.of("single rule cached", true, RuleMetaDataShardingSphereRuleFixture.class, true),
                Arguments.of("single rule absent", false, GlobalRule.class, false));
    }
    
    private static Stream<Arguments> getSingleRuleArguments() {
        return Stream.of(
                Arguments.of("single rule present", false, RuleMetaDataShardingSphereRuleFixture.class, RuleMetaDataShardingSphereRuleFixture.class, null),
                Arguments.of("single rule cached", true, RuleMetaDataShardingSphereRuleFixture.class, RuleMetaDataShardingSphereRuleFixture.class, null),
                Arguments.of("single rule absent", false, GlobalRule.class, null, IllegalStateException.class));
    }
}
