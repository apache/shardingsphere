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
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.statement.CreateDefaultShadowAlgorithmStatement;
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

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateDefaultShadowAlgorithmExecutorTest {
    
    private final CreateDefaultShadowAlgorithmExecutor executor = (CreateDefaultShadowAlgorithmExecutor) TypedSPILoader.getService(
            DatabaseRuleDefinitionExecutor.class, CreateDefaultShadowAlgorithmStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("shadow_db");
        executor.setDatabase(database);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkBeforeUpdateFailureArguments")
    void assertCheckBeforeUpdateWithInvalidInput(final String name, final CreateDefaultShadowAlgorithmStatement sqlStatement,
                                                 final ShadowRule rule, final Class<? extends Exception> expectedException) {
        executor.setRule(rule);
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdate() {
        executor.setRule(null);
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(createStatement(false, "SQL_HINT")));
    }
    
    @Test
    void assertCheckBeforeUpdateWithIfNotExists() {
        ShadowRule rule = mock(ShadowRule.class);
        executor.setRule(rule);
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(createStatement(true, "SQL_HINT")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("buildToBeCreatedRuleConfigurationArguments")
    void assertBuildToBeCreatedRuleConfiguration(final String name, final ShadowRule rule, final int expectedAlgorithmSize, final String expectedDefaultAlgorithmName) {
        executor.setRule(rule);
        ShadowRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(createStatement(false, "SQL_HINT"));
        assertThat(actual.getShadowAlgorithms().size(), is(expectedAlgorithmSize));
        assertThat(actual.getShadowAlgorithms().containsKey("default_shadow_algorithm"), is(expectedAlgorithmSize > 0));
        assertThat(actual.getDefaultShadowAlgorithmName(), is(expectedDefaultAlgorithmName));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ShadowRule.class));
    }
    
    private static Stream<Arguments> checkBeforeUpdateFailureArguments() {
        ShadowRule duplicatedRule = mock(ShadowRule.class);
        when(duplicatedRule.containsShadowAlgorithm("default_shadow_algorithm")).thenReturn(true);
        return Stream.of(
                Arguments.of("missing algorithm type", createStatement(false, ""), null, MissingRequiredAlgorithmException.class),
                Arguments.of("invalid algorithm type", createStatement(false, "INVALID_TYPE"), null, ServiceProviderNotFoundException.class),
                Arguments.of("duplicate default algorithm", createStatement(false, "SQL_HINT"), duplicatedRule, InUsedAlgorithmException.class));
    }
    
    private static CreateDefaultShadowAlgorithmStatement createStatement(final boolean ifNotExists, final String algorithmType) {
        ShadowAlgorithmSegment shadowAlgorithmSegment = new ShadowAlgorithmSegment("algorithm_name", new AlgorithmSegment(algorithmType, PropertiesBuilder.build(new Property("type", "value"))));
        return new CreateDefaultShadowAlgorithmStatement(ifNotExists, shadowAlgorithmSegment);
    }
    
    private static Stream<Arguments> buildToBeCreatedRuleConfigurationArguments() {
        ShadowRule duplicatedRule = mock(ShadowRule.class);
        when(duplicatedRule.containsShadowAlgorithm("default_shadow_algorithm")).thenReturn(true);
        return Stream.of(
                Arguments.of("rule is null", null, 1, "default_shadow_algorithm"),
                Arguments.of("rule without default algorithm", mock(ShadowRule.class), 1, "default_shadow_algorithm"),
                Arguments.of("rule already has default algorithm", duplicatedRule, 0, null));
    }
}
