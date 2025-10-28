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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.exception.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.exception.actual.DuplicateReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TypedSPILoader.class)
class CreateReadwriteSplittingRuleExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    private final CreateReadwriteSplittingRuleExecutor executor = new CreateReadwriteSplittingRuleExecutor();
    
    @BeforeEach
    void setUp() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateRuleNames() {
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(DuplicateRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateDataSources() {
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("write_ds", null));
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(InvalidRuleConfigurationException.class, () -> executor.checkBeforeUpdate(createSQLStatement("write_ds", "TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithNotExistedDataSources() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(createSQLStatement("TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateLogicDataSource() {
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("duplicate_ds", Collections.singleton("ds_0")));
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("duplicate_ds", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(null, new Properties()));
        executor.setDatabase(database);
        assertThrows(InvalidRuleConfigurationException.class, () -> executor.checkBeforeUpdate(createSQLStatement(false, ruleSegment)));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateWriteDataSourcesInStatement() {
        assertThrows(DuplicateReadwriteSplittingActualDataSourceException.class,
                () -> executor.checkBeforeUpdate(createSQLStatementWithDuplicateWriteDataSources("write_ds_0", "write_ds_1", "TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateWriteDataSources() {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(DuplicateReadwriteSplittingActualDataSourceException.class,
                () -> executor.checkBeforeUpdate(createSQLStatement("readwrite_ds_1", "ds_write", Arrays.asList("read_ds_0", "read_ds_1"), "TEST", new Properties())));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateReadDataSourcesInStatement() {
        assertThrows(DuplicateReadwriteSplittingActualDataSourceException.class,
                () -> executor.checkBeforeUpdate(createSQLStatementWithDuplicateReadDataSources("write_ds_0", "write_ds_1", "TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateReadDataSources() {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(DuplicateReadwriteSplittingActualDataSourceException.class,
                () -> executor.checkBeforeUpdate(createSQLStatement("readwrite_ds_1", "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"), "TEST", new Properties())));
    }
    
    @Test
    void assertCheckSQLStatementWithInvalidLoadBalancerProperties() {
        assertThrows(InvalidAlgorithmConfigurationException.class,
                () -> executor.checkBeforeUpdate(createSQLStatement("readwrite_group", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "weight",
                        PropertiesBuilder.build(new Property("read_ds_0", "5"), new Property("read_ds_2", "5")))));
    }
    
    @Test
    void assertCheckSQLStatementWithIfNotExists() {
        ReadwriteSplittingRuleSegment staticSegment = new ReadwriteSplittingRuleSegment("readwrite_ds_0", "write_ds_0", Arrays.asList("read_ds_2", "read_ds_3"),
                new AlgorithmSegment(null, new Properties()));
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        executor.checkBeforeUpdate(createSQLStatement(true, staticSegment));
    }
    
    @Test
    void assertUpdateSuccess() {
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class, RETURNS_DEEP_STUBS);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("ms_group", Collections.singleton("ds_0")));
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.singleton(ruleAttribute));
        ReadwriteSplittingRuleSegment staticSegment = new ReadwriteSplittingRuleSegment(
                "static_rule", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), new AlgorithmSegment("TEST", new Properties()));
        CreateReadwriteSplittingRuleStatement sqlStatement = createSQLStatement(false, staticSegment);
        executor.setDatabase(database);
        executor.checkBeforeUpdate(sqlStatement);
        executor.setRule(mock(ReadwriteSplittingRule.class));
        ReadwriteSplittingRuleConfiguration toBeCreatedRuleConfig = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(toBeCreatedRuleConfig.getDataSourceGroups().size(), is(1));
        assertThat(toBeCreatedRuleConfig.getLoadBalancers().size(), is(1));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment("readwrite_ds_0", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties())));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment(ruleName, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties())));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String writeDataSource, final Collection<String> readDataSources,
                                                                     final String loadBalancerName, final Properties props) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment(ruleName, writeDataSource, readDataSources, new AlgorithmSegment(loadBalancerName, props)));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final boolean ifNotExists, final ReadwriteSplittingRuleSegment... ruleSegments) {
        return new CreateReadwriteSplittingRuleStatement(ifNotExists, Arrays.asList(ruleSegments));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateWriteDataSources(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds", Arrays.asList("read_ds_2", "read_ds_3"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        return createSQLStatement(false, ruleSegment0, ruleSegment1);
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateReadDataSources(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        return createSQLStatement(false, ruleSegment0, ruleSegment1);
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration("readwrite_ds_0", "ds_write",
                Arrays.asList("read_ds_0", "read_ds_1"), "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceGroupConfig)), Collections.emptyMap());
    }
}
