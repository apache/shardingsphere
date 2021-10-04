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

package org.apache.shardingsphere.sharding.distsql.update;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingTableRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereResource resource;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    private final CreateShardingTableRuleStatementUpdater updater = new CreateShardingTableRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertExecuteWithDuplicateTables() throws DistSQLException {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Collections.emptyList(), null, null, null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment, ruleSegment), null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckWithShardingAlgorithmsIncomplete() throws DistSQLException {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Collections.emptyList(), null, null, null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment), null);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckSQLStatementWithoutToBeCreatedShardingAlgorithms() throws DistSQLException {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Collections.emptyList(), "order_id", new AlgorithmSegment("INVALID_TYPE", new Properties()), null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment), null);
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertExecuteWithNotExistsResource() throws DistSQLException {
        List<String> dataSources = Arrays.asList("ds0", "ds1");
        when(resource.getNotExistedResources(any())).thenReturn(dataSources);
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", dataSources, null, null, null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment), null);
    }
    
    @Test
    public void assertExecuteWithExtraResources() throws DistSQLException {
        List<String> dataSources = Arrays.asList("ds0", "ds1");
        when(resource.getNotExistedResources(any())).thenReturn(new HashSet<>(dataSources));
        DataSourceContainedRule dataSourceContainedRule = mock(DataSourceContainedRule.class);
        when(ruleMetaData.getRules()).thenReturn(Collections.singletonList(dataSourceContainedRule));
        when(dataSourceContainedRule.getDataSourceMapper()).thenReturn(getDataSourceMapper());
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", dataSources, "order_id", new AlgorithmSegment("MOD_TEST", new Properties()), null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment), null);
    }
    
    @Test
    public void assertExecuteWithInlineExpression() throws DistSQLException {
        TableRuleSegment ruleSegment = new TableRuleSegment("t_order", Arrays.asList("ds_${0..1}", "ds2"), "order_id", new AlgorithmSegment("MOD_TEST", new Properties()), null, null);
        updater.checkSQLStatement(shardingSphereMetaData, createSQLStatement(ruleSegment), null);
    }
    
    private Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> dataSourceMapper = new HashMap<>(2, 1);
        dataSourceMapper.put("ds0", null);
        dataSourceMapper.put("ds1", null);
        return dataSourceMapper;
    }
    
    private CreateShardingTableRuleStatement createSQLStatement(final TableRuleSegment... ruleSegments) {
        return new CreateShardingTableRuleStatement(Arrays.asList(ruleSegments));
    }
}
