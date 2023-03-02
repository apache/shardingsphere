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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TypedSPILoader.class)
public final class CreateReadwriteSplittingRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResourceMetaData resourceMetaData;
    
    private final CreateReadwriteSplittingRuleStatementUpdater updater = new CreateReadwriteSplittingRuleStatementUpdater();
    
    @BeforeEach
    public void before() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
    }
    
    @Test
    public void assertCheckSQLStatementWithDuplicateRuleNames() {
        when(resourceMetaData.getDataSources()).thenReturn(Collections.emptyMap());
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement("TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    public void assertCheckSQLStatementWithDuplicateResource() {
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("write_ds", null));
        assertThrows(InvalidRuleConfigurationException.class, () -> updater.checkSQLStatement(database, createSQLStatement("write_ds", "TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutExistedResources() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> updater.checkSQLStatement(database, createSQLStatement("TEST"), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutExistedAutoAwareResources() {
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.singletonMap("ms_group", "ds_0")));
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("dynamic_rule", "ha_group", "TEST", new Properties());
        assertThrows(MissingRequiredStorageUnitsException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, ruleSegment), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutToBeCreatedLoadBalancers() {
        when(database.getRuleMetaData().findRules(any())).thenReturn(Collections.emptyList());
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> updater.checkSQLStatement(database, createSQLStatement("INVALID_TYPE"), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithDuplicateWriteResourceNamesInStatement() {
        assertThrows(InvalidRuleConfigurationException.class,
                () -> updater.checkSQLStatement(database, createSQLStatementWithDuplicateWriteResourceNames("write_ds_0", "write_ds_1", "TEST"), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithDuplicateWriteResourceNames() {
        assertThrows(InvalidRuleConfigurationException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_1", "ds_write", Arrays.asList("read_ds_0", "read_ds_1"), "TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    public void assertCheckSQLStatementWithDuplicateReadResourceNamesInStatement() {
        assertThrows(InvalidRuleConfigurationException.class, () -> updater.checkSQLStatement(database, createSQLStatementWithDuplicateReadResourceNames("write_ds_0", "write_ds_1", "TEST"), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithDuplicateReadResourceNames() {
        assertThrows(InvalidRuleConfigurationException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_1", "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"), "TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    public void assertCheckSQLStatementWithIfNotExists() {
        ReadwriteSplittingRuleSegment staticSegment = new ReadwriteSplittingRuleSegment("readwrite_ds_0", "write_ds_0", Arrays.asList("read_ds_2", "read_ds_3"), null, new Properties());
        updater.checkSQLStatement(database, createSQLStatement(true, staticSegment), createCurrentRuleConfiguration());
    }
    
    @Test
    public void assertUpdateSuccess() {
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.singletonMap("ms_group", "ds_0")));
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        when(TypedSPILoader.contains(ReadQueryLoadBalanceAlgorithm.class, "TEST")).thenReturn(true);
        ReadwriteSplittingRuleSegment dynamicSegment = new ReadwriteSplittingRuleSegment("dynamic_rule", "ms_group", "TEST", new Properties());
        ReadwriteSplittingRuleSegment staticSegment = new ReadwriteSplittingRuleSegment("static_rule", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), "TEST", new Properties());
        CreateReadwriteSplittingRuleStatement statement = createSQLStatement(false, dynamicSegment, staticSegment);
        updater.checkSQLStatement(database, statement, null);
        ReadwriteSplittingRuleConfiguration currentRuleConfig = new ReadwriteSplittingRuleConfiguration(new ArrayList<>(), new HashMap<>());
        ReadwriteSplittingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        assertThat(currentRuleConfig.getDataSources().size(), is(2));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment("readwrite_ds_0", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties()));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment(ruleName, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties()));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String writeDataSource, final Collection<String> readDataSources, final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment(ruleName, writeDataSource, readDataSources, loadBalancerName, new Properties()));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final boolean ifNotExists, final ReadwriteSplittingRuleSegment... ruleSegments) {
        return new CreateReadwriteSplittingRuleStatement(ifNotExists, Arrays.asList(ruleSegments));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateWriteResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds", Arrays.asList("read_ds_2", "read_ds_3"), loadBalancerName, new Properties());
        return createSQLStatement(false, ruleSegment0, ruleSegment1);
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateReadResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"), loadBalancerName, new Properties());
        return createSQLStatement(false, ruleSegment0, ruleSegment1);
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds_0",
                new StaticReadwriteSplittingStrategyConfiguration("ds_write", Arrays.asList("read_ds_0", "read_ds_1")), null, "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap());
    }
}
