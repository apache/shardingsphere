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

import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateReadwriteSplittingRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResource resource;
    
    private final CreateReadwriteSplittingRuleStatementUpdater updater = new CreateReadwriteSplittingRuleStatementUpdater();
    
    @Before
    public void before() {
        when(database.getResource()).thenReturn(resource);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckSQLStatementWithDuplicateRuleNames() throws DistSQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResource().getDataSources().keySet()).thenReturn(Collections.emptySet());
        updater.checkSQLStatement(database, createSQLStatement("TEST"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateResource() throws DistSQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResource()).thenReturn(resource);
        when(resource.getDataSources()).thenReturn(Collections.singletonMap("write_ds", null));
        updater.checkSQLStatement(database, createSQLStatement("write_ds", "TEST"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertCheckSQLStatementWithoutExistedResources() throws DistSQLException {
        when(resource.getNotExistedResources(any())).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        updater.checkSQLStatement(database, createSQLStatement("TEST"), null);
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertCheckSQLStatementWithoutExistedAutoAwareResources() throws DistSQLException {
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.singletonMap("ms_group", "ds_0")));
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("dynamic_rule", "ha_group", "false", "TEST", new Properties());
        updater.checkSQLStatement(database, new CreateReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment)), null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeCreatedLoadBalancers() throws DistSQLException {
        when(database.getRuleMetaData().findRules(any())).thenReturn(Collections.emptyList());
        updater.checkSQLStatement(database, createSQLStatement("INVALID_TYPE"), null);
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateWriteResourceNamesInStatement() throws DistSQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResource()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatementWithDuplicateWriteResourceNames("write_ds_0", "write_ds_1", "TEST"), null);
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateWriteResourceNames() throws DistSQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResource()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_1", "ds_write", Arrays.asList("read_ds_0", "read_ds_1"), "TEST"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateReadResourceNamesInStatement() throws DistSQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResource()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatementWithDuplicateReadResourceNames("write_ds_0", "write_ds_1", "TEST"), null);
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateReadResourceNames() throws DistSQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResource()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_1", "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"), "TEST"), createCurrentRuleConfiguration());
    }
    
    @Test
    public void assertUpdateSuccess() throws DistSQLException {
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.singletonMap("ms_group", "ds_0")));
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        try (MockedStatic<ReadQueryLoadBalanceAlgorithmFactory> mockedFactory = mockStatic(ReadQueryLoadBalanceAlgorithmFactory.class)) {
            mockedFactory.when(() -> ReadQueryLoadBalanceAlgorithmFactory.contains("TEST")).thenReturn(true);
            ReadwriteSplittingRuleSegment dynamicSegment = new ReadwriteSplittingRuleSegment("dynamic_rule", "ms_group", "false", "TEST", new Properties());
            ReadwriteSplittingRuleSegment staticSegment = new ReadwriteSplittingRuleSegment("static_rule", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), "TEST", new Properties());
            ShardingSphereServiceLoader.register(ReadQueryLoadBalanceAlgorithm.class);
            CreateReadwriteSplittingRuleStatement statement = new CreateReadwriteSplittingRuleStatement(Arrays.asList(dynamicSegment, staticSegment));
            updater.checkSQLStatement(database, statement, null);
            ReadwriteSplittingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(statement);
            ReadwriteSplittingRuleConfiguration currentRuleConfig = new ReadwriteSplittingRuleConfiguration(new ArrayList<>(), new HashMap<>());
            updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
            assertThat(currentRuleConfig.getDataSources().size(), is(2));
        }
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds_0", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        return new CreateReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment(ruleName, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        return new CreateReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String writeDataSource, final Collection<String> readDataSources, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment(ruleName, writeDataSource, readDataSources, loadBalancerName, new Properties());
        return new CreateReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateWriteResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds", Arrays.asList("read_ds_2", "read_ds_3"), loadBalancerName, new Properties());
        return new CreateReadwriteSplittingRuleStatement(Arrays.asList(ruleSegment0, ruleSegment1));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateReadResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        return new CreateReadwriteSplittingRuleStatement(Arrays.asList(ruleSegment0, ruleSegment1));
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds_0",
                new StaticReadwriteSplittingStrategyConfiguration("ds_write", Arrays.asList("read_ds_0", "read_ds_1")), null, "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap());
    }
}
