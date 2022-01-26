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

package org.apache.shardingsphere.sharding.distsql.query;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.UnusedShardingAlgorithmsQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingAlgorithmsStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UnusedShardingAlgorithmsQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfiguration()));
        UnusedShardingAlgorithmsQueryResultSet resultSet = new UnusedShardingAlgorithmsQueryResultSet();
        resultSet.init(metaData, mock(ShowShardingAlgorithmsStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("database_inline"));
        assertThat(actual.get(1), is("INLINE"));
        assertThat(actual.get(2), is("algorithm-expression=ds_${user_id % 2}"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("database_inline", createShardingInlineAlgorithmConfiguration());
        result.getShardingAlgorithms().put("t_order_hash_mod", createShardingHashModAlgorithmConfiguration());
        result.getAutoTables().add(createShardingAutoTableRuleConfiguration());
        return result;
    }
    
    private ShardingSphereAlgorithmConfiguration createShardingInlineAlgorithmConfiguration() {
        Properties props = new Properties();
        props.put("algorithm-expression", "ds_${user_id % 2}");
        return new ShardingSphereAlgorithmConfiguration("INLINE", props);
    }
    
    private ShardingSphereAlgorithmConfiguration createShardingHashModAlgorithmConfiguration() {
        Properties props = new Properties();
        props.put("sharding-count", 4);
        return new ShardingSphereAlgorithmConfiguration("hash_mod", props);
    }
    
    private ShardingAutoTableRuleConfiguration createShardingAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("auto_table");
        result.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "hash_mod"));
        return result;
    }
}
