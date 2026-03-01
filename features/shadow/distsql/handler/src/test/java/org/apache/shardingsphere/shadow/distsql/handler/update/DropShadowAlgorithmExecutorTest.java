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
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowAlgorithmStatement;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DropShadowAlgorithmExecutorTest {
    
    private final DropShadowAlgorithmExecutor executor = (DropShadowAlgorithmExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, DropShadowAlgorithmStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("shadow_db");
        executor.setDatabase(database);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkBeforeUpdateFailureArguments")
    void assertCheckBeforeUpdateWithInvalidInput(final String name, final ShadowRuleConfiguration ruleConfig,
                                                 final DropShadowAlgorithmStatement sqlStatement, final Class<? extends Exception> expectedException) {
        executor.setRule(createRule(ruleConfig));
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdate() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getShadowAlgorithms().put("droppable_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        ruleConfig.getTables().put("t_order_empty", new ShadowTableConfiguration(new LinkedList<>(), new LinkedList<>(Collections.singleton("droppable_algorithm"))));
        executor.setRule(createRule(ruleConfig));
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(new DropShadowAlgorithmStatement(false, Collections.singleton("droppable_algorithm"))));
    }
    
    @Test
    void assertCheckBeforeUpdateWithIfExists() {
        executor.setRule(createRule(new ShadowRuleConfiguration()));
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(new DropShadowAlgorithmStatement(true, Collections.singleton("missing_algorithm"))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("hasAnyOneToBeDroppedArguments")
    void assertHasAnyOneToBeDropped(final String name, final DropShadowAlgorithmStatement sqlStatement, final boolean expected) {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getShadowAlgorithms().put("algorithm_0", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        ruleConfig.getShadowAlgorithms().put("algorithm_1", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        executor.setRule(createRule(ruleConfig));
        assertThat(executor.hasAnyOneToBeDropped(sqlStatement), is(expected));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getShadowAlgorithms().put("algorithm_0", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        ruleConfig.getShadowAlgorithms().put("algorithm_1", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        executor.setRule(createRule(ruleConfig));
        DropShadowAlgorithmStatement sqlStatement = new DropShadowAlgorithmStatement(false, Arrays.asList("algorithm_0", "algorithm_1"));
        ShadowRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(sqlStatement);
        assertThat(actual.getShadowAlgorithms().size(), is(2));
        assertTrue(actual.getShadowAlgorithms().keySet().containsAll(sqlStatement.getNames()));
    }
    
    private ShadowRule createRule(final ShadowRuleConfiguration ruleConfig) {
        ShadowRule result = mock(ShadowRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ShadowRule.class));
    }
    
    private static Stream<Arguments> checkBeforeUpdateFailureArguments() {
        ShadowRuleConfiguration notRegisteredConfig = new ShadowRuleConfiguration();
        notRegisteredConfig.getShadowAlgorithms().put("registered_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        ShadowRuleConfiguration inUsedAlgorithmConfig = new ShadowRuleConfiguration();
        inUsedAlgorithmConfig.getShadowAlgorithms().put("in_used_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        inUsedAlgorithmConfig.getShadowAlgorithms().put("algorithm_from_empty_table", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        inUsedAlgorithmConfig.getTables().put("t_order",
                new ShadowTableConfiguration(new LinkedList<>(Collections.singleton("shadow_group")), new LinkedList<>(Collections.singleton("in_used_algorithm"))));
        inUsedAlgorithmConfig.getTables().put("t_order_empty", new ShadowTableConfiguration(new LinkedList<>(), new LinkedList<>(Collections.singleton("algorithm_from_empty_table"))));
        ShadowRuleConfiguration defaultAlgorithmConfig = new ShadowRuleConfiguration();
        defaultAlgorithmConfig.getShadowAlgorithms().put("default_shadow_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        defaultAlgorithmConfig.setDefaultShadowAlgorithmName("default_shadow_algorithm");
        return Stream.of(
                Arguments.of("algorithm not registered", notRegisteredConfig,
                        new DropShadowAlgorithmStatement(false, Collections.singleton("missing_algorithm")), UnregisteredAlgorithmException.class),
                Arguments.of("algorithm in use", inUsedAlgorithmConfig,
                        new DropShadowAlgorithmStatement(false, Collections.singleton("in_used_algorithm")), InUsedAlgorithmException.class),
                Arguments.of("default algorithm", defaultAlgorithmConfig,
                        new DropShadowAlgorithmStatement(false, Collections.singleton("default_shadow_algorithm")), InUsedAlgorithmException.class));
    }
    
    private static Stream<Arguments> hasAnyOneToBeDroppedArguments() {
        return Stream.of(
                Arguments.of("contains dropped algorithm", new DropShadowAlgorithmStatement(false, Collections.singleton("algorithm_0")), true),
                Arguments.of("contains no dropped algorithm", new DropShadowAlgorithmStatement(false, Collections.singleton("algorithm_9")), false),
                Arguments.of("contains one of dropped algorithms", new DropShadowAlgorithmStatement(false, Arrays.asList("algorithm_9", "algorithm_1")), true));
    }
}
