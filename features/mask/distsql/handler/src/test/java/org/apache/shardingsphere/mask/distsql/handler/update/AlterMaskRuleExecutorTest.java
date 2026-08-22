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

package org.apache.shardingsphere.mask.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.AlterMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterMaskRuleExecutorTest {
    
    private final AlterMaskRuleExecutor executor = (AlterMaskRuleExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, AlterMaskRuleStatement.class);
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("checkBeforeUpdateArguments")
    void assertCheckBeforeUpdate(final String name, final AlterMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig, final Class<? extends Exception> expectedException) {
        executor.setRule(createRule(currentRuleConfig));
        if (null == expectedException) {
            assertDoesNotThrow(() -> executor.checkBeforeUpdate(sqlStatement));
            return;
        }
        when(database.getName()).thenReturn("foo_db");
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertBuildToBeAlteredRuleConfiguration() {
        AlterMaskRuleStatement sqlStatement = new AlterMaskRuleStatement(Collections.singleton(createRuleSegment("t_order", "order_id", "MD5")));
        MaskRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(actual.getTables().size(), is(1));
        MaskTableRuleConfiguration actualTable = actual.getTables().iterator().next();
        assertThat(actualTable.getName(), is("t_order"));
        assertThat(actualTable.getColumns().iterator().next().getLogicColumn(), is("order_id"));
        assertThat(actual.getMaskAlgorithms().size(), is(1));
        assertTrue(actual.getMaskAlgorithms().containsKey("t_order_order_id_md5"));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        executor.setRule(createRule(createCurrentRuleConfigurationWithAlgorithms()));
        MaskTableRuleConfiguration toBeAlteredTable = new MaskTableRuleConfiguration("t_order", Collections.singleton(new MaskColumnRuleConfiguration("order_id", "order_mask")));
        MaskRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(new MaskRuleConfiguration(Collections.singleton(toBeAlteredTable), Collections.emptyMap()));
        assertTrue(actual.getTables().isEmpty());
        assertThat(actual.getMaskAlgorithms().size(), is(1));
        assertTrue(actual.getMaskAlgorithms().containsKey("unused_mask"));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfigurationWithDifferentTableNameCase() {
        executor.setRule(createRule(createCurrentRuleConfigurationWithAlgorithms()));
        MaskTableRuleConfiguration toBeAlteredTable = new MaskTableRuleConfiguration("T_ORDER", Collections.singleton(new MaskColumnRuleConfiguration("order_id", "t_order_order_id_sm3")));
        MaskRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(new MaskRuleConfiguration(Collections.singleton(toBeAlteredTable), Collections.emptyMap()));
        assertThat(actual.getTables().size(), is(1));
        assertThat(actual.getTables().iterator().next().getName(), is("t_order"));
        assertThat(actual.getMaskAlgorithms().size(), is(2));
        assertTrue(actual.getMaskAlgorithms().containsKey("order_mask"));
        assertTrue(actual.getMaskAlgorithms().containsKey("unused_mask"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertUpdatePathWithDifferentTableNameCase() {
        MaskRuleConfiguration currentRuleConfig = createCurrentRuleConfigurationWithAlgorithms();
        executor.setRule(createRule(currentRuleConfig));
        MaskTableRuleConfiguration toBeAlteredTable = new MaskTableRuleConfiguration("T_ORDER", Collections.singleton(new MaskColumnRuleConfiguration("order_id", "t_order_order_id_sm3")));
        MaskRuleConfiguration toBeDroppedRuleConfig = executor.buildToBeDroppedRuleConfiguration(new MaskRuleConfiguration(Collections.singleton(toBeAlteredTable), Collections.emptyMap()));
        RuleItemConfigurationChangedProcessor<MaskRuleConfiguration, MaskTableRuleConfiguration> processor = TypedSPILoader.getService(
                RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("mask", "tables"));
        processor.changeRuleItemConfiguration("T_ORDER", currentRuleConfig, toBeAlteredTable);
        for (MaskTableRuleConfiguration each : toBeDroppedRuleConfig.getTables()) {
            processor.dropRuleItemConfiguration(each.getName(), currentRuleConfig);
        }
        Collection<MaskTableRuleConfiguration> actual = currentRuleConfig.getTables().stream().filter(each -> "t_order".equalsIgnoreCase(each.getName())).collect(Collectors.toList());
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("T_ORDER"));
        assertThat(actual.iterator().next().getColumns().iterator().next().getMaskAlgorithm(), is("t_order_order_id_sm3"));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(MaskRule.class));
    }
    
    private static Stream<Arguments> checkBeforeUpdateArguments() {
        return Stream.of(
                Arguments.of("all tables exist", new AlterMaskRuleStatement(Collections.singleton(createRuleSegment("t_order", "order_id", "MD5"))),
                        createCurrentRuleConfiguration(Collections.singleton("t_order")), null),
                Arguments.of("single missing table", new AlterMaskRuleStatement(Collections.singleton(createRuleSegment("t_missing", "order_id", "MD5"))),
                        createCurrentRuleConfiguration(Collections.singleton("t_order")), MissingRequiredRuleException.class),
                Arguments.of("one table exists and one missing",
                        new AlterMaskRuleStatement(Arrays.asList(createRuleSegment("t_order", "order_id", "MD5"), createRuleSegment("t_missing", "user_id", "AES"))),
                        createCurrentRuleConfiguration(Collections.singleton("t_order")), MissingRequiredRuleException.class),
                Arguments.of("existing table in different case", new AlterMaskRuleStatement(Collections.singleton(createRuleSegment("T_ORDER", "order_id", "MD5"))),
                        createCurrentRuleConfiguration(Collections.singleton("t_order")), null));
    }
    
    private static MaskRuleSegment createRuleSegment(final String tableName, final String columnName, final String algorithmType) {
        return new MaskRuleSegment(tableName, Collections.singleton(new MaskColumnSegment(columnName, new AlgorithmSegment(algorithmType, new Properties()))));
    }
    
    private static MaskRuleConfiguration createCurrentRuleConfiguration(final Collection<String> tableNames) {
        Collection<MaskTableRuleConfiguration> tableRuleConfigs = tableNames.stream().map(each -> new MaskTableRuleConfiguration(each, Collections.emptyList())).collect(Collectors.toList());
        return new MaskRuleConfiguration(tableRuleConfigs, Collections.emptyMap());
    }
    
    private static MaskRuleConfiguration createCurrentRuleConfigurationWithAlgorithms() {
        Map<String, AlgorithmConfiguration> algorithmConfigs = new LinkedHashMap<>(3, 1F);
        algorithmConfigs.put("order_mask", new AlgorithmConfiguration("MD5", new Properties()));
        algorithmConfigs.put("user_mask", new AlgorithmConfiguration("MD5", new Properties()));
        algorithmConfigs.put("unused_mask", new AlgorithmConfiguration("SM3", new Properties()));
        return new MaskRuleConfiguration(new LinkedList<>(Arrays.asList(
                new MaskTableRuleConfiguration("t_order", Collections.singleton(new MaskColumnRuleConfiguration("order_id", "order_mask"))),
                new MaskTableRuleConfiguration("t_user", Collections.singleton(new MaskColumnRuleConfiguration("user_id", "user_mask"))))), algorithmConfigs);
    }
    
    private MaskRule createRule(final MaskRuleConfiguration ruleConfig) {
        MaskRule result = mock(MaskRule.class);
        when(result.getConfiguration()).thenReturn(ruleConfig);
        return result;
    }
}
