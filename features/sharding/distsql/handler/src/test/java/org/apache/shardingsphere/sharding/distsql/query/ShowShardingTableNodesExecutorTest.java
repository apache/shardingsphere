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
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.query.ShowShardingTableNodesExecutor;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingRuleNotFoundException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ShowShardingTableNodesExecutorTest {
    
    @Test
    void assertGetRowData() {
        ShardingRule rule = createShardingRule();
        assertOrder(rule);
        assertOrderItem(rule);
        assertAll(rule);
    }
    
    @SneakyThrows(IOException.class)
    private ShardingRule createShardingRule() {
        URL url = getClass().getClassLoader().getResource("yaml/config_sharding_for_table_nodes.yaml");
        assertNotNull(url);
        YamlRootConfiguration yamlRootConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
        ShardingRuleConfiguration shardingRuleConfig = ShardingRuleConfigurationConverter.findAndConvertShardingRuleConfiguration(yamlRootConfig.getRules())
                .orElseThrow(ShardingRuleNotFoundException::new);
        return new ShardingRule(shardingRuleConfig, Maps.of("ds_1", new MockedDataSource(), "ds_2", new MockedDataSource(), "ds_3", new MockedDataSource()), mock(InstanceContext.class));
    }
    
    private void assertOrder(final ShardingRule rule) {
        ShowShardingTableNodesExecutor executor = new ShowShardingTableNodesExecutor();
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowShardingTableNodesStatement("t_order", null), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        assertThat(row.getCell(2), is("ds_1.t_order_0, ds_2.t_order_1, ds_1.t_order_2, ds_2.t_order_3, ds_1.t_order_4, ds_2.t_order_5"));
    }
    
    private void assertOrderItem(final ShardingRule rule) {
        ShowShardingTableNodesExecutor executor = new ShowShardingTableNodesExecutor();
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowShardingTableNodesStatement("t_order_item", null), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
        assertThat(row.getCell(2), is("ds_2.t_order_item_0, ds_3.t_order_item_1, ds_2.t_order_item_2, ds_3.t_order_item_3, ds_2.t_order_item_4, ds_3.t_order_item_5"));
    }
    
    private void assertAll(final ShardingRule rule) {
        ShowShardingTableNodesExecutor executor = new ShowShardingTableNodesExecutor();
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowShardingTableNodesStatement(null, null), mock(ContextManager.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        assertThat(row.getCell(2), is("ds_1.t_order_0, ds_2.t_order_1, ds_1.t_order_2, ds_2.t_order_3, ds_1.t_order_4, ds_2.t_order_5"));
        row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
        assertThat(row.getCell(2), is("ds_2.t_order_item_0, ds_3.t_order_item_1, ds_2.t_order_item_2, ds_3.t_order_item_3, ds_2.t_order_item_4, ds_3.t_order_item_5"));
    }
}
