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

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShardingTableNodesResultSet;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingTableNodesResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingRule shardingRule = createShardingRule();
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(shardingRule));
        assertOrder(database);
        assertOrderItem(database);
    }
    
    @SneakyThrows(IOException.class)
    private ShardingRule createShardingRule() {
        URL url = getClass().getClassLoader().getResource("yaml/config_sharding_for_table_nodes.yaml");
        assertNotNull(url);
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
        ShardingRuleConfiguration shardingRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(yamlRootConfig.getRules());
        return new ShardingRule(shardingRuleConfig, Arrays.asList("ds_1", "ds_2", "ds_3"), null);
    }
    
    private void assertOrder(final ShardingSphereDatabase database) {
        DatabaseDistSQLResultSet resultSet = new ShardingTableNodesResultSet();
        resultSet.init(database, new ShowShardingTableNodesStatement("t_order", null));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("t_order"));
        assertThat(actual.get(1), is("ds_1.t_order_0, ds_2.t_order_1, ds_1.t_order_2, ds_2.t_order_3, ds_1.t_order_4, ds_2.t_order_5"));
    }
    
    private void assertOrderItem(final ShardingSphereDatabase database) {
        DatabaseDistSQLResultSet resultSet = new ShardingTableNodesResultSet();
        resultSet.init(database, new ShowShardingTableNodesStatement("t_order_item", null));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0), is("t_order_item"));
        assertThat(actual.get(1), is("ds_2.t_order_item_0, ds_3.t_order_item_1, ds_2.t_order_item_2, ds_3.t_order_item_3, ds_2.t_order_item_4, ds_3.t_order_item_5"));
    }
}
