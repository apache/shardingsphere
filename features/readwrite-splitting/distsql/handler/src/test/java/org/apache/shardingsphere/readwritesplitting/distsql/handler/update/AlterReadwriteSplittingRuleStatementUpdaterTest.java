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
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResources;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterReadwriteSplittingRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResources resource;
    
    private final AlterReadwriteSplittingRuleStatementUpdater updater = new AlterReadwriteSplittingRuleStatementUpdater();
    
    @Before
    public void before() {
        when(database.getResources()).thenReturn(resource);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() {
        updater.checkSQLStatement(database, createSQLStatement("TEST"), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredRules() {
        updater.checkSQLStatement(database, createSQLStatement("TEST"), new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertCheckSQLStatementWithoutExistedResources() {
        when(resource.getNotExistedResources(any())).thenReturn(Collections.singleton("read_ds_0"));
        updater.checkSQLStatement(database, createSQLStatement("TEST"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertCheckSQLStatementWithoutExistedAutoAwareResources() {
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.singletonMap("ms_group", "ds_0")));
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "ha_group", "false", "TEST", new Properties());
        updater.checkSQLStatement(database, new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment)), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredLoadBalancers() {
        when(database.getRuleMetaData().findRules(any())).thenReturn(Collections.emptyList());
        updater.checkSQLStatement(database, createSQLStatement("INVALID_TYPE"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateWriteResourceNamesInStatement() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResources()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatementWithDuplicateWriteResourceNames("readwrite_ds_0", "readwrite_ds_1", "TEST"), createCurrentRuleConfigurationWithMultipleRules());
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateWriteResourceNames() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResources()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_0", "ds_write_1", Arrays.asList("read_ds_0", "read_ds_1"), "TEST"), createCurrentRuleConfigurationWithMultipleRules());
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateReadResourceNamesInStatement() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResources()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatementWithDuplicateReadResourceNames("readwrite_ds_0", "readwrite_ds_1", "TEST"), createCurrentRuleConfigurationWithMultipleRules());
    }
    
    @Test(expected = InvalidRuleConfigurationException.class)
    public void assertCheckSQLStatementWithDuplicateReadResourceNames() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResources()).thenReturn(resource);
        updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_1", "write_ds_1", Arrays.asList("read_ds_0_0", "read_ds_0_1"), "TEST"), createCurrentRuleConfigurationWithMultipleRules());
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerTypeName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "ds_read_ds_1"), loadBalancerTypeName, new Properties());
        return new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String writeDataSource, final Collection<String> readDataSources, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment(ruleName, writeDataSource, readDataSources, loadBalancerName, new Properties());
        return new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatementWithDuplicateWriteResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds", Arrays.asList("read_ds_2", "read_ds_3"), loadBalancerName, new Properties());
        return new AlterReadwriteSplittingRuleStatement(Arrays.asList(ruleSegment0, ruleSegment1));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatementWithDuplicateReadResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        return new AlterReadwriteSplittingRuleStatement(Arrays.asList(ruleSegment0, ruleSegment1));
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds",
                        new StaticReadwriteSplittingStrategyConfiguration("ds_write", Arrays.asList("read_ds_0", "read_ds_1")), null, "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap());
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfigurationWithMultipleRules() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig0 =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds_0",
                        new StaticReadwriteSplittingStrategyConfiguration("ds_write_0", Arrays.asList("read_ds_0_0", "read_ds_0_1")), null, "TEST");
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig1 =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds_1",
                        new StaticReadwriteSplittingStrategyConfiguration("ds_write_1", Arrays.asList("read_ds_1_0", "read_ds_1_1")), null, "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Arrays.asList(dataSourceRuleConfig0, dataSourceRuleConfig1)), Collections.emptyMap());
    }
}
