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
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.statement.AlterShadowRuleStatement;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
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
class AlterShadowRuleExecutorTest {
    
    private final AlterShadowRuleExecutor executor = (AlterShadowRuleExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, AlterShadowRuleStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @Mock
    private ShadowRuleConfiguration currentConfig;
    
    @BeforeEach
    void setUp() {
        Collection<ShadowDataSourceConfiguration> shadowDataSource = new LinkedList<>();
        shadowDataSource.add(new ShadowDataSourceConfiguration("initRuleName1", "ds1", "ds_shadow1"));
        shadowDataSource.add(new ShadowDataSourceConfiguration("initRuleName2", "ds2", "ds_shadow2"));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getName()).thenReturn("shadow_db");
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        when(currentConfig.getDataSources()).thenReturn(shadowDataSource);
        executor.setDatabase(database);
        executor.setRule(createRule(currentConfig));
    }
    
    private ShadowRule createRule(final ShadowRuleConfiguration ruleConfig) {
        ShadowRule result = mock(ShadowRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkBeforeUpdateFailureArguments")
    void assertCheckBeforeUpdateWithInvalidInput(final String name, final AlterShadowRuleStatement sqlStatement,
                                                 final List<String> notExistedStorageUnits, final Class<? extends Exception> expectedException) {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(notExistedStorageUnits);
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckBeforeUpdate() {
        Properties props = PropertiesBuilder.build(new Property("type", "value"));
        ShadowAlgorithmSegment segment1 = new ShadowAlgorithmSegment("algorithmName1", new AlgorithmSegment("SQL_HINT", props));
        ShadowAlgorithmSegment segment2 = new ShadowAlgorithmSegment("algorithmName2", new AlgorithmSegment("SQL_HINT", props));
        AlterShadowRuleStatement sqlStatement = createAlterStatement(Arrays.asList(
                new ShadowRuleSegment("initRuleName1", "ds_0", null, Collections.singletonMap("t_order", Collections.singleton(segment1))),
                new ShadowRuleSegment("initRuleName2", "ds_1", null, Collections.singletonMap("t_order_1", Collections.singleton(segment2)))));
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuildToBeAlteredRuleConfiguration() {
        ShadowAlgorithmSegment algorithmSegment = new ShadowAlgorithmSegment("algorithm_name", new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value"))));
        AlterShadowRuleStatement sqlStatement = createAlterStatement(
                Collections.singleton(new ShadowRuleSegment("initRuleName1", "ds_0", "ds_0_shadow", Collections.singletonMap("t_order", Collections.singleton(algorithmSegment)))));
        ShadowRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(actual.getDataSources().size(), is(1));
        assertThat(actual.getShadowAlgorithms().size(), is(1));
        assertThat(actual.getTables().size(), is(1));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ShadowRuleConfiguration ruleConfig = new ShadowRuleConfiguration();
        ruleConfig.getShadowAlgorithms().put("used_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        ruleConfig.getShadowAlgorithms().put("unused_algorithm", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        ruleConfig.getTables().put("t_order", new ShadowTableConfiguration(new ArrayList<>(Collections.singleton("initRuleName1")), new ArrayList<>(Collections.singleton("used_algorithm"))));
        ruleConfig.setDefaultShadowAlgorithmName("used_algorithm");
        executor.setRule(createRule(ruleConfig));
        ShadowRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(new ShadowRuleConfiguration());
        assertThat(actual.getShadowAlgorithms().size(), is(1));
        assertTrue(actual.getShadowAlgorithms().containsKey("unused_algorithm"));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ShadowRule.class));
    }
    
    private static Stream<Arguments> checkBeforeUpdateFailureArguments() {
        ShadowAlgorithmSegment duplicatedAlgorithmSegment = new ShadowAlgorithmSegment("duplicated_algorithm",
                new AlgorithmSegment("SQL_HINT", PropertiesBuilder.build(new Property("type", "value"))));
        ShadowAlgorithmSegment invalidTypeSegment = new ShadowAlgorithmSegment("invalid_type_algorithm",
                new AlgorithmSegment("INVALID_TYPE", PropertiesBuilder.build(new Property("type", "value"))));
        return Stream.of(
                Arguments.of("duplicate rule name", createAlterStatement(Arrays.asList(
                        new ShadowRuleSegment("rule_name", null, null, null),
                        new ShadowRuleSegment("rule_name", null, null, null))), Collections.emptyList(), DuplicateRuleException.class),
                Arguments.of("rule not found", createAlterStatement(Collections.singleton(
                        new ShadowRuleSegment("missing_rule", null, null, null))), Collections.emptyList(), MissingRequiredRuleException.class),
                Arguments.of("storage unit not found", createAlterStatement(Collections.singleton(
                        new ShadowRuleSegment("initRuleName1", "missing_storage_unit", null, null))), Collections.singletonList("missing_storage_unit"),
                        MissingRequiredStorageUnitsException.class),
                Arguments.of("duplicate algorithm name", createAlterStatement(Arrays.asList(
                        new ShadowRuleSegment("initRuleName1", "ds_0", null, Collections.singletonMap("t_order", Collections.singleton(duplicatedAlgorithmSegment))),
                        new ShadowRuleSegment("initRuleName2", "ds_1", null, Collections.singletonMap("t_order_1", Collections.singleton(duplicatedAlgorithmSegment))))),
                        Collections.emptyList(), InUsedAlgorithmException.class),
                Arguments.of("invalid algorithm type", createAlterStatement(Collections.singleton(
                        new ShadowRuleSegment("initRuleName1", "ds_0", null, Collections.singletonMap("t_order", Collections.singleton(invalidTypeSegment))))),
                        Collections.emptyList(), ServiceProviderNotFoundException.class));
    }
    
    private static AlterShadowRuleStatement createAlterStatement(final Collection<ShadowRuleSegment> rules) {
        AlterShadowRuleStatement result = new AlterShadowRuleStatement(rules);
        result.buildAttributes();
        return result;
    }
}
