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
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingTableNodesQueryResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
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

public final class ShardingTableNodesQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(createRuleConfiguration()));
        DistSQLResultSet resultSet = new ShardingTableNodesQueryResultSet();
        resultSet.init(metaData, mock(ShowShardingTableNodesStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("t_order"));
        assertThat(actual.get(1), is("ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1"));
        resultSet.next();
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.get(0), is("t_product"));
        assertThat(actual.get(1), is("ds_2.t_product_0, ds_3.t_product_1"));
        actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.get(0), is("t_user"));
        assertThat(actual.get(1), is("ds_2.t_user_0, ds_3.t_user_1, ds_2.t_user_2, ds_3.t_user_3"));
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createShardingTableRuleConfiguration());
        result.getAutoTables().add(createProductAutoTableConfiguration());
        result.getAutoTables().add(createUserAutoTableConfiguration());
        Properties properties = new Properties();
        properties.put("sharding-count", 2);
        result.getShardingAlgorithms().put("t_product_algorithm", new ShardingSphereAlgorithmConfiguration("hash_mod", properties));
        result.setDefaultTableShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "t_product_algorithm"));
        result.setDefaultDatabaseShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "t_product_algorithm"));
        return result;
    }
    
    private ShardingTableRuleConfiguration createShardingTableRuleConfiguration() {
        return new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
    }
    
    private ShardingAutoTableRuleConfiguration createProductAutoTableConfiguration() {
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("t_product", "ds_2,ds_3");
        shardingAutoTableRuleConfiguration.setShardingStrategy(new StandardShardingStrategyConfiguration("user_id", "t_product_algorithm"));
        return shardingAutoTableRuleConfiguration;
    }
    
    private ShardingAutoTableRuleConfiguration createUserAutoTableConfiguration() {
        ShardingAutoTableRuleConfiguration shardingAutoTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("t_user", "ds_2,ds_3");
        shardingAutoTableRuleConfiguration.setShardingStrategy(new StandardShardingStrategyConfiguration("user_id", ""));
        return shardingAutoTableRuleConfiguration;
    }
}
