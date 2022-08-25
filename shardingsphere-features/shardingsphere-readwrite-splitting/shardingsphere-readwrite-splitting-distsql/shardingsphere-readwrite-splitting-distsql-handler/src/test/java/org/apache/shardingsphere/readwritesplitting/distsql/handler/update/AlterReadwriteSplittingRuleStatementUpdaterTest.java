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
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterReadwriteSplittingRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereResource resource;
    
    private final AlterReadwriteSplittingRuleStatementUpdater updater = new AlterReadwriteSplittingRuleStatementUpdater();
    
    @Before
    public void before() {
        when(database.getResource()).thenReturn(resource);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(database, createSQLStatement("TEST"), null);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredRules() throws DistSQLException {
        updater.checkSQLStatement(database, createSQLStatement("TEST"), new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckSQLStatementWithoutExistedResources() throws DistSQLException {
        when(resource.getNotExistedResources(any())).thenReturn(Collections.singleton("read_ds_0"));
        updater.checkSQLStatement(database, createSQLStatement("TEST"), createCurrentRuleConfiguration());
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckSQLStatementWithoutExistedAutoAwareResources() throws DistSQLException {
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.singletonMap("ms_group", "ds_0")));
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "ha_group", "false", "TEST", new Properties());
        updater.checkSQLStatement(database, new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment)), createCurrentRuleConfiguration());
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeAlteredLoadBalancers() throws DistSQLException {
        when(database.getRuleMetaData().findRules(any())).thenReturn(Collections.emptyList());
        updater.checkSQLStatement(database, createSQLStatement("INVALID_TYPE"), createCurrentRuleConfiguration());
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerTypeName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "ds_read_ds_1"), loadBalancerTypeName, new Properties());
        return new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds",
                        new StaticReadwriteSplittingStrategyConfiguration("ds_write", Arrays.asList("read_ds_0", "read_ds_1")), null, "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap());
    }
}
