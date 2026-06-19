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

package org.apache.shardingsphere.infra.rule.builder.database;

import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.fixture.FixtureRule;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.fixture.FixtureDatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.rule.builder.fixture.ToggleFixtureDatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.rule.builder.fixture.ToggleFixtureRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

class DatabaseRulesBuilderTest {
    
    private static final ResourceMetaData EMPTY_RESOURCE_META_DATA = new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap());
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("buildWithRuleConfigurationsArguments")
    void assertBuildWithRuleConfigurations(final String name, final Collection<RuleConfiguration> ruleConfigs, final Collection<Class<?>> expectedRuleTypes) {
        List<ShardingSphereRule> actual = new ArrayList<>(DatabaseRulesBuilder.build("foo_db", null,
                new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), ruleConfigs), null, EMPTY_RESOURCE_META_DATA));
        assertThat(actual.size(), is(expectedRuleTypes.size()));
        for (Class<?> each : expectedRuleTypes) {
            assertTrue(actual.stream().anyMatch(each::isInstance));
        }
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertBuildWithChecker() {
        DatabaseRuleConfigurationChecker<FixtureDatabaseRuleConfiguration> checker = mock(DatabaseRuleConfigurationChecker.class);
        doAnswer(invocation -> {
            Collection<ShardingSphereRule> actualBuiltRules = invocation.getArgument(3);
            assertTrue(actualBuiltRules.isEmpty());
            return null;
        }).when(checker).check(eq("foo_db"), argThat(FixtureDatabaseRuleConfiguration.class::isInstance), eq(EMPTY_RESOURCE_META_DATA.getDataSourceMap()), anyCollection());
        try (MockedStatic<OrderedSPILoader> mockedLoader = mockStatic(OrderedSPILoader.class, CALLS_REAL_METHODS)) {
            mockedLoader.when(() -> OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class,
                    Collections.singleton(FixtureDatabaseRuleConfiguration.class))).thenReturn(Collections.singletonMap(FixtureDatabaseRuleConfiguration.class, checker));
            List<ShardingSphereRule> actual = new ArrayList<>(DatabaseRulesBuilder.build("foo_db", null,
                    new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList()), null, EMPTY_RESOURCE_META_DATA));
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0), isA(FixtureRule.class));
            verify(checker).check(eq("foo_db"), argThat(FixtureDatabaseRuleConfiguration.class::isInstance), eq(EMPTY_RESOURCE_META_DATA.getDataSourceMap()), anyCollection());
        }
    }
    
    @Test
    void assertBuildSingleRule() {
        assertThat(DatabaseRulesBuilder.build("foo_db", null, Collections.emptyList(), new FixtureDatabaseRuleConfiguration(), null, EMPTY_RESOURCE_META_DATA), isA(FixtureRule.class));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertBuildSingleRuleWithChecker() {
        DatabaseRuleConfigurationChecker<FixtureDatabaseRuleConfiguration> checker = mock(DatabaseRuleConfigurationChecker.class);
        FixtureDatabaseRuleConfiguration ruleConfig = new FixtureDatabaseRuleConfiguration();
        Collection<ShardingSphereRule> rules = Collections.emptyList();
        try (MockedStatic<OrderedSPILoader> mockedLoader = mockStatic(OrderedSPILoader.class, CALLS_REAL_METHODS)) {
            mockedLoader.when(() -> OrderedSPILoader.getServicesByClass(DatabaseRuleConfigurationChecker.class,
                    Collections.singleton(FixtureDatabaseRuleConfiguration.class))).thenReturn(Collections.singletonMap(FixtureDatabaseRuleConfiguration.class, checker));
            assertThat(DatabaseRulesBuilder.build("foo_db", null, rules, ruleConfig, null, EMPTY_RESOURCE_META_DATA), isA(FixtureRule.class));
            verify(checker).check(eq("foo_db"), eq(ruleConfig), eq(EMPTY_RESOURCE_META_DATA.getDataSourceMap()), eq(rules));
        }
    }
    
    private static Collection<Arguments> buildWithRuleConfigurationsArguments() {
        return Arrays.asList(
                Arguments.of("default rule builder added for non-database rule config",
                        Collections.<RuleConfiguration>singleton(new FixtureRuleConfiguration()), Collections.<Class<?>>singletonList(FixtureRule.class)),
                Arguments.of("empty database rule configuration is filtered",
                        Collections.<RuleConfiguration>singleton(new ToggleFixtureDatabaseRuleConfiguration(true)), Collections.<Class<?>>singletonList(FixtureRule.class)),
                Arguments.of("non-empty database rule configuration keeps enhanced rule",
                        Collections.<RuleConfiguration>singleton(new ToggleFixtureDatabaseRuleConfiguration(false)), Arrays.<Class<?>>asList(FixtureRule.class, ToggleFixtureRule.class)));
    }
}
