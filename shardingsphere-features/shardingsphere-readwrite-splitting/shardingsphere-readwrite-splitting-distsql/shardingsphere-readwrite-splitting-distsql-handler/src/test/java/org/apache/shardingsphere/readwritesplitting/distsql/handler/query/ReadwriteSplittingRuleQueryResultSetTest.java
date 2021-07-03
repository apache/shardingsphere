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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfiguration()));
        ReadwriteSplittingRuleQueryResultSet resultSet = new ReadwriteSplittingRuleQueryResultSet();
        resultSet.init(metaData, mock(ShowReadwriteSplittingRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("pr_ds"));
        assertTrue(actual.contains("ms_group"));
        assertTrue(actual.contains("ds_primary"));
        assertTrue(actual.contains("ds_slave_0,ds_slave_1"));
        assertTrue(actual.contains("random"));
        assertTrue(actual.contains("read_weight=2:1"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("pr_ds", "ms_group", "ds_primary", Arrays.asList("ds_slave_0", "ds_slave_1"), "test");
        Properties props = new Properties();
        props.setProperty("read_weight", "2:1");
        ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration = new ShardingSphereAlgorithmConfiguration("random", props);
        Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = new HashMap<>();
        loadBalancers.put("test", shardingSphereAlgorithmConfiguration);
        return new ReadwriteSplittingRuleConfiguration(Collections.singleton(dataSourceRuleConfig), loadBalancers);
    }
}
