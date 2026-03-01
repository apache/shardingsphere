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

import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterReadwriteSplittingRuleExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    private final AlterReadwriteSplittingRuleExecutor executor = (AlterReadwriteSplittingRuleExecutor) TypedSPILoader.getService(
            DatabaseRuleDefinitionExecutor.class, AlterReadwriteSplittingRuleStatement.class);
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckBeforeUpdate() {
        mockCheckBeforeUpdateDependencies();
        setRule(createCurrentRuleConfiguration());
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(createSQLStatement(Arrays.asList("read_ds_0", "read_ds_1"), new Properties())));
    }
    
    @Test
    void assertBuildToBeAlteredRuleConfiguration() {
        ReadwriteSplittingRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(createSQLStatement(Arrays.asList("read_ds_0", "read_ds_1"), new Properties()));
        assertThat(actual.getDataSourceGroups().size(), is(1));
        assertThat(actual.getLoadBalancers().size(), is(1));
        assertTrue(actual.getLoadBalancers().containsKey("readwrite_ds_RANDOM"));
    }
    
    @Test
    void assertBuildToBeDroppedRuleConfiguration() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createCurrentRuleConfiguration();
        setRule(ruleConfig);
        ReadwriteSplittingRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(ruleConfig);
        assertThat(actual.getDataSourceGroups().size(), is(0));
        assertThat(actual.getLoadBalancers().size(), is(1));
        assertTrue(actual.getLoadBalancers().containsKey("unused_lb"));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(ReadwriteSplittingRule.class));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final Collection<String> readDataSources, final Properties props) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "write_ds", readDataSources, new AlgorithmSegment("RANDOM", props));
        AlterReadwriteSplittingRuleStatement result = new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
        result.buildAttributes();
        return result;
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "used_lb");
        Map<String, AlgorithmConfiguration> loadBalancers = new HashMap<>(2, 1F);
        loadBalancers.put("used_lb", new AlgorithmConfiguration("RANDOM", new Properties()));
        loadBalancers.put("unused_lb", new AlgorithmConfiguration("RANDOM", new Properties()));
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceGroupConfig)), loadBalancers);
    }
    
    private void setRule(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        executor.setRule(rule);
    }
    
    private void mockCheckBeforeUpdateDependencies() {
        when(database.getName()).thenReturn("test_db");
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        lenient().when(resourceMetaData.getStorageUnits()).thenReturn(Collections.emptyMap());
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Collections.emptySet());
        when(database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
    }
}
