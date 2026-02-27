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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DropShadowRuleExecutorTest {
    
    private final DropShadowRuleExecutor executor = (DropShadowRuleExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, DropShadowRuleStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("shadow_db");
        executor.setDatabase(database);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkBeforeUpdateWithMissingRuleArguments")
    void assertCheckBeforeUpdateWithMissingRule(final String name, final DropShadowRuleStatement sqlStatement) {
        executor.setRule(createRule(new ShadowRuleConfiguration()));
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdateWithIfExists() {
        executor.setRule(createRule(new ShadowRuleConfiguration()));
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(createStatement(true, "missing_rule")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("hasAnyOneToBeDroppedArguments")
    void assertHasAnyOneToBeDropped(final String name, final ShadowRule rule, final DropShadowRuleStatement sqlStatement, final boolean expected) {
        executor.setRule(rule);
        assertThat(executor.hasAnyOneToBeDropped(sqlStatement), is(expected));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ShadowRuleConfiguration ruleConfig = createRuleConfigurationForDrop();
        executor.setRule(createRule(ruleConfig));
        DropShadowRuleStatement sqlStatement = createStatement(false, "shadow_group");
        executor.checkBeforeUpdate(sqlStatement);
        ShadowRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getDataSources().iterator().next().getName(), is("shadow_group"));
        assertThat(actual.getTables().size(), is(1));
        assertTrue(actual.getTables().containsKey("t_order"));
        assertThat(actual.getShadowAlgorithms().size(), is(2));
        assertTrue(actual.getShadowAlgorithms().containsKey("only_drop_algorithm"));
        assertTrue(actual.getShadowAlgorithms().containsKey("unused_algorithm"));
        assertThat(ruleConfig.getDataSources().size(), is(1));
        assertFalse(ruleConfig.getTables().containsKey("t_order"));
        assertThat(ruleConfig.getTables().get("t_order_item").getDataSourceNames().size(), is(1));
        assertThat(ruleConfig.getTables().get("t_order_item").getDataSourceNames().iterator().next(), is("shadow_group_1"));
    }
    
    @Test
    void assertBuildToBeAlteredRuleConfiguration() {
        ShadowRuleConfiguration ruleConfig = createRuleConfigurationForDrop();
        executor.setRule(createRule(ruleConfig));
        ShadowRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(createStatement(false, "shadow_group"));
        assertThat(actual.getTables().size(), is(1));
        assertFalse(actual.getTables().containsKey("t_order"));
        assertTrue(actual.getTables().containsKey("t_order_item"));
        assertThat(actual.getTables().get("t_order_item").getDataSourceNames().size(), is(1));
        assertThat(actual.getTables().get("t_order_item").getDataSourceNames().iterator().next(), is("shadow_group_1"));
    }
    
    private ShadowRuleConfiguration createRuleConfigurationForDrop() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("shadow_group", "production_0", "shadow_0"));
        result.getDataSources().add(new ShadowDataSourceConfiguration("shadow_group_1", "production_1", "shadow_1"));
        result.getTables().put("t_order",
                new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("shadow_group")), new ArrayList<>(Collections.singleton("only_drop_algorithm"))));
        result.getTables().put("t_order_item",
                new ShadowTableConfiguration(new ArrayList<>(Arrays.asList("shadow_group", "shadow_group_1")), new ArrayList<>(Collections.singleton("shared_algorithm"))));
        result.getShadowAlgorithms().put("only_drop_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        result.getShadowAlgorithms().put("shared_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        result.getShadowAlgorithms().put("unused_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        result.setDefaultShadowAlgorithmName("shared_algorithm");
        return result;
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ShadowRule.class));
    }
    
    private static Stream<Arguments> checkBeforeUpdateWithMissingRuleArguments() {
        return Stream.of(
                Arguments.of("single missing rule", createStatement(false, "missing_rule_0")),
                Arguments.of("multiple missing rules", createStatement(false, "missing_rule_0", "missing_rule_1")),
                Arguments.of("another missing rule", createStatement(false, "missing_rule_2")));
    }
    
    private static Stream<Arguments> hasAnyOneToBeDroppedArguments() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getDataSources().add(new ShadowDataSourceConfiguration("shadow_group", "production_0", "shadow_0"));
        ruleConfig.getDataSources().add(new ShadowDataSourceConfiguration("shadow_group_1", "production_1", "shadow_1"));
        return Stream.of(
                Arguments.of("contains dropped rule", createRule(ruleConfig), createStatement(false, "shadow_group"), true),
                Arguments.of("contains no dropped rules", createRule(ruleConfig), createStatement(false, "missing_rule"), false),
                Arguments.of("contains one dropped rule", createRule(ruleConfig), createStatement(false, "missing_rule", "shadow_group_1"), true));
    }
    
    private static DropShadowRuleStatement createStatement(final boolean ifExists, final String... ruleNames) {
        DropShadowRuleStatement result = new DropShadowRuleStatement(ifExists, Arrays.asList(ruleNames));
        result.buildAttributes();
        return result;
    }
    
    private static ShadowRule createRule(final ShadowRuleConfiguration ruleConfig) {
        ShadowRule result = mock(ShadowRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
}
