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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.checker;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.exception.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredStrategyException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TypedSPILoader.class)
class ReadwriteSplittingRuleStatementCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        lenient().when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        lenient().when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Collections.emptySet());
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
    }
    
    @Test
    void assertCheckCreationWithDuplicateRuleNames() {
        Collection<ReadwriteSplittingRuleSegment> segments = Arrays.asList(new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), null, null),
                new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_2", Arrays.asList("read_ds_2", "read_ds_3"), null, null),
                new ReadwriteSplittingRuleSegment("bar_rule_0", "write_ds_4", Arrays.asList("read_ds_4", "read_ds_5"), null, null));
        assertThrows(DuplicateRuleException.class, () -> ReadwriteSplittingRuleStatementChecker.checkCreation(database, segments, null, false));
    }
    
    @Test
    void assertCheckCreationWithInvalidTransactionalReadQueryStrategy() {
        ReadwriteSplittingRuleSegment segment = new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0", createReadDataSources(), "invalid", null);
        assertThrows(MissingRequiredStrategyException.class, () -> ReadwriteSplittingRuleStatementChecker.checkCreation(database, Collections.singleton(segment), null, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("validTransactionalReadQueryStrategyArguments")
    void assertCheckCreationWithValidTransactionalReadQueryStrategy(final String name, final String transactionalReadQueryStrategy) {
        ReadwriteSplittingRuleSegment segment = new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0",
                Arrays.asList("read_ds_0", "read_ds_1"), transactionalReadQueryStrategy, null);
        assertDoesNotThrow(() -> ReadwriteSplittingRuleStatementChecker.checkCreation(database, Collections.singleton(segment), null, false));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidWeightConfigurationArguments")
    void assertCheckCreationWithInvalidWeightConfiguration(final String name, final ReadwriteSplittingRuleSegment segment) {
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> ReadwriteSplittingRuleStatementChecker.checkCreation(database, Collections.singleton(segment), null, false), name);
    }
    
    @Test
    void assertCheckCreationWithValidWeightConfiguration() {
        AlgorithmSegment loadBalancer = new AlgorithmSegment("weight", PropertiesBuilder.build(new Property("read_ds_0", "1"), new Property("read_ds_1", "1")));
        ReadwriteSplittingRuleSegment segment = new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), null, loadBalancer);
        assertDoesNotThrow(() -> ReadwriteSplittingRuleStatementChecker.checkCreation(database, Collections.singleton(segment), null, false));
    }
    
    @Test
    void assertCheckAlteration() {
        ReadwriteSplittingRuleSegment segment = new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), null, null);
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> dataSourceGroups = Arrays.asList(
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_rule_0", "write_ds_9", Arrays.asList("read_ds_9", "read_ds_10"), "RANDOM"),
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("bar_rule_0", "write_ds_2", Arrays.asList("read_ds_2", "read_ds_3"), "RANDOM"));
        ReadwriteSplittingRuleConfiguration currentRuleConfig = new ReadwriteSplittingRuleConfiguration(dataSourceGroups, Collections.emptyMap());
        assertDoesNotThrow(() -> ReadwriteSplittingRuleStatementChecker.checkAlteration(database, Collections.singleton(segment), currentRuleConfig));
    }
    
    private static Stream<Arguments> invalidWeightConfigurationArguments() {
        return Stream.of(
                Arguments.of("empty weight properties",
                        new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), null,
                                new AlgorithmSegment("weight", new Properties()))),
                Arguments.of("weight property has unknown read storage unit",
                        new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), null,
                                new AlgorithmSegment("weight", PropertiesBuilder.build(new Property("read_ds_0", "1"), new Property("read_ds_2", "1"))))),
                Arguments.of("weight property misses required read storage unit",
                        new ReadwriteSplittingRuleSegment("foo_rule_0", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), null,
                                new AlgorithmSegment("weight", PropertiesBuilder.build(new Property("read_ds_0", "1"))))));
    }
    
    private static Stream<Arguments> validTransactionalReadQueryStrategyArguments() {
        return Stream.of(
                Arguments.of("lowercase strategy", "primary"),
                Arguments.of("uppercase strategy", "PRIMARY"),
                Arguments.of("mixed-case strategy", "PrImArY"));
    }
    
    private static Collection<String> createReadDataSources() {
        return Arrays.asList("read_ds_0", "read_ds_1");
    }
}
