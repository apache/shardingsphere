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

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
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
class CreateReadwriteSplittingRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    private final CreateReadwriteSplittingRuleStatementUpdater updater = new CreateReadwriteSplittingRuleStatementUpdater();
    
    @BeforeEach
    void before() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.emptyList());
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateRuleNames() {
        when(resourceMetaData.getDataSources()).thenReturn(Collections.emptyMap());
        assertThrows(DuplicateRuleException.class, () -> updater.checkSQLStatement(database, createSQLStatement("TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateResource() {
        when(resourceMetaData.getDataSources()).thenReturn(Collections.singletonMap("write_ds", null));
        assertThrows(InvalidRuleConfigurationException.class, () -> updater.checkSQLStatement(database, createSQLStatement("write_ds", "TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedResources() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> updater.checkSQLStatement(database, createSQLStatement("TEST"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateLogicResource() {
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class);
        when(dataSourceContainedRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("duplicate_ds", Collections.singleton("ds_0")));
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(dataSourceContainedRule));
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("duplicate_ds", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(null, new Properties()));
        assertThrows(InvalidRuleConfigurationException.class, () -> updater.checkSQLStatement(database, createSQLStatement(false, ruleSegment), null));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateWriteResourceNamesInStatement() {
        assertThrows(InvalidRuleConfigurationException.class,
                () -> updater.checkSQLStatement(database, createSQLStatementWithDuplicateWriteResourceNames("write_ds_0", "write_ds_1", "TEST"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateWriteResourceNames() {
        assertThrows(InvalidRuleConfigurationException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_1", "ds_write", Arrays.asList("read_ds_0", "read_ds_1"), "TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateReadResourceNamesInStatement() {
        assertThrows(InvalidRuleConfigurationException.class, () -> updater.checkSQLStatement(database, createSQLStatementWithDuplicateReadResourceNames("write_ds_0", "write_ds_1", "TEST"), null));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateReadResourceNames() {
        assertThrows(InvalidRuleConfigurationException.class,
                () -> updater.checkSQLStatement(database, createSQLStatement("readwrite_ds_1", "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"), "TEST"), createCurrentRuleConfiguration()));
    }
    
    @Test
    void assertCheckSQLStatementWithIfNotExists() {
        ReadwriteSplittingRuleSegment staticSegment = new ReadwriteSplittingRuleSegment("readwrite_ds_0", "write_ds_0", Arrays.asList("read_ds_2", "read_ds_3"),
                new AlgorithmSegment(null, new Properties()));
        updater.checkSQLStatement(database, createSQLStatement(true, staticSegment), createCurrentRuleConfiguration());
    }
    
    @Test
    void assertUpdateSuccess() {
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class);
        when(dataSourceContainedRule.getDataSourceMapper()).thenReturn(Collections.singletonMap("ms_group", Collections.singleton("ds_0")));
        when(database.getRuleMetaData().findRules(DataSourceContainedRule.class)).thenReturn(Collections.singleton(dataSourceContainedRule));
        ReadwriteSplittingRuleSegment staticSegment = new ReadwriteSplittingRuleSegment("static_rule", "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment("TEST", new Properties()));
        CreateReadwriteSplittingRuleStatement statement = createSQLStatement(false, staticSegment);
        updater.checkSQLStatement(database, statement, null);
        ReadwriteSplittingRuleConfiguration currentRuleConfig = new ReadwriteSplittingRuleConfiguration(new ArrayList<>(), new HashMap<>());
        ReadwriteSplittingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        assertThat(currentRuleConfig.getDataSources().size(), is(1));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment("readwrite_ds_0", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties())));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment(ruleName, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties())));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String writeDataSource, final Collection<String> readDataSources, final String loadBalancerName) {
        return createSQLStatement(false, new ReadwriteSplittingRuleSegment(ruleName, writeDataSource, readDataSources, new AlgorithmSegment(loadBalancerName, new Properties())));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatement(final boolean ifNotExists, final ReadwriteSplittingRuleSegment... ruleSegments) {
        return new CreateReadwriteSplittingRuleStatement(ifNotExists, Arrays.asList(ruleSegments));
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateWriteResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds", Arrays.asList("read_ds_2", "read_ds_3"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        return createSQLStatement(false, ruleSegment0, ruleSegment1);
    }
    
    private CreateReadwriteSplittingRuleStatement createSQLStatementWithDuplicateReadResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        return createSQLStatement(false, ruleSegment0, ruleSegment1);
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds_0", "ds_write",
                Arrays.asList("read_ds_0", "read_ds_1"), "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap());
    }
}
