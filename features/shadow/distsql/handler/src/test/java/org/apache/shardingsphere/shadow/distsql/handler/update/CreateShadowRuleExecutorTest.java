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
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.CreateShadowRuleStatement;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateShadowRuleExecutorTest {
    
    private final CreateShadowRuleExecutor executor = (CreateShadowRuleExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, CreateShadowRuleStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @BeforeEach
    void setUp() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(database.getName()).thenReturn("shadow_db");
        executor.setDatabase(database);
        executor.setRule(createRule(createCurrentRuleConfiguration()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkBeforeUpdateFailureArguments")
    void assertCheckBeforeUpdateWithInvalidInput(final String name, final CreateShadowRuleStatement sqlStatement, final ShadowRule rule,
                                                 final Collection<String> notExistedStorageUnits, final Collection<DataSourceMapperRuleAttribute> ruleAttributes,
                                                 final Class<? extends Exception> expectedException) {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(notExistedStorageUnits);
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(ruleAttributes);
        executor.setRule(rule);
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdate() {
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(createStatement(false, Collections.singleton(createShadowRuleSegment("rule_name", "ds_0", "algorithm_name", "SQL_HINT", "t_order")))));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfiguration() {
        ShadowRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(
                createStatement(false, Collections.singleton(createShadowRuleSegment("rule_name", "ds_0", "algorithm_name", "SQL_HINT", "t_order"))));
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getShadowAlgorithms().size(), is(1));
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfigurationWithIfNotExists() {
        CreateShadowRuleStatement sqlStatement = createStatement(true, Arrays.asList(
                createShadowRuleSegment("initRuleName", "ds_0", "algorithm_name_0", "SQL_HINT", "t_order"),
                createShadowRuleSegment("new_rule_name", "ds_1", "algorithm_name_1", "SQL_HINT", "t_order_1")));
        executor.setRule(createRule(createCurrentRuleConfiguration()));
        ShadowRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getDataSources().iterator().next().getName(), is("new_rule_name"));
        assertTrue(actual.getTables().containsKey("t_order_1"));
        assertTrue(actual.getShadowAlgorithms().containsKey("algorithm_name_1"));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ShadowRule.class));
    }
    
    private static Stream<Arguments> checkBeforeUpdateFailureArguments() {
        DataSourceMapperRuleAttribute duplicatedLogicDataSourceAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(duplicatedLogicDataSourceAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("duplicate_ds", Collections.singleton("ds_0")));
        ShadowAlgorithmSegment duplicatedAlgorithmSegment = createShadowAlgorithmSegment("duplicated_algorithm", "SQL_HINT");
        return Stream.of(
                Arguments.of("duplicate rule name", createStatement(false, Arrays.asList(
                        new ShadowRuleSegment("rule_name", null, null, null),
                        new ShadowRuleSegment("rule_name", null, null, null))), createRule(createCurrentRuleConfiguration()),
                        Collections.emptyList(), Collections.emptyList(), DuplicateRuleException.class),
                Arguments.of("duplicate current rule", createStatement(false, Collections.singleton(
                        createShadowRuleSegment("initRuleName", "ds_0", "algorithm_name", "SQL_HINT", "t_order"))), createRule(createCurrentRuleConfiguration()),
                        Collections.emptyList(), Collections.emptyList(), DuplicateRuleException.class),
                Arguments.of("duplicate logic datasource", createStatement(false, Collections.singleton(
                        createShadowRuleSegment("duplicate_ds", "ds_0", "algorithm_name", "SQL_HINT", "t_order"))), createRule(createCurrentRuleConfiguration()),
                        Collections.emptyList(), Collections.singleton(duplicatedLogicDataSourceAttribute), InvalidRuleConfigurationException.class),
                Arguments.of("missing storage unit", createStatement(false, Collections.singleton(
                        createShadowRuleSegment("rule_name", "missing_ds", "algorithm_name", "SQL_HINT", "t_order"))), createRule(createCurrentRuleConfiguration()),
                        Collections.singleton("missing_ds"), Collections.emptyList(), MissingRequiredStorageUnitsException.class),
                Arguments.of("duplicate algorithm", createStatement(false, Arrays.asList(
                        new ShadowRuleSegment("rule_name_0", "ds_0", null, Collections.singletonMap("t_order", Collections.singleton(duplicatedAlgorithmSegment))),
                        new ShadowRuleSegment("rule_name_1", "ds_1", null, Collections.singletonMap("t_order_1", Collections.singleton(duplicatedAlgorithmSegment))))),
                        createRule(createCurrentRuleConfiguration()), Collections.emptyList(), Collections.emptyList(), DuplicateRuleException.class),
                Arguments.of("invalid algorithm type", createStatement(false, Collections.singleton(
                        createShadowRuleSegment("rule_name", "ds_0", "algorithm_name", "INVALID_TYPE", "t_order"))), createRule(createCurrentRuleConfiguration()),
                        Collections.emptyList(), Collections.emptyList(), ServiceProviderNotFoundException.class));
    }
    
    private static CreateShadowRuleStatement createStatement(final boolean ifNotExists, final Collection<ShadowRuleSegment> rules) {
        CreateShadowRuleStatement result = new CreateShadowRuleStatement(ifNotExists, new LinkedList<>(rules));
        result.buildAttributes();
        return result;
    }
    
    private static ShadowRuleSegment createShadowRuleSegment(final String ruleName, final String source, final String algorithmName, final String algorithmType, final String tableName) {
        return new ShadowRuleSegment(ruleName, source, null, Collections.singletonMap(tableName, Collections.singleton(createShadowAlgorithmSegment(algorithmName, algorithmType))));
    }
    
    private static ShadowAlgorithmSegment createShadowAlgorithmSegment(final String algorithmName, final String algorithmType) {
        return new ShadowAlgorithmSegment(algorithmName, new AlgorithmSegment(algorithmType, PropertiesBuilder.build(new Property("type", "value"))));
    }
    
    private static ShadowRuleConfiguration createCurrentRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("initRuleName", "init_ds_0", "init_ds_0_shadow"));
        return result;
    }
    
    private static ShadowRule createRule(final ShadowRuleConfiguration ruleConfig) {
        ShadowRule result = mock(ShadowRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
}
