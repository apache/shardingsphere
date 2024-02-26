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
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecuteEngine;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingTableNodesStatement;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingRuleNotFoundException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationConverter;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowShardingTableNodesExecutorTest {
    
    private DistSQLQueryExecuteEngine engine;
    
    DistSQLQueryExecuteEngine setUp(final ShardingRule rule, final ShowShardingTableNodesStatement statement) {
        return new DistSQLQueryExecuteEngine(statement, "foo_db", mockContextManager(rule), mock(DistSQLConnectionContext.class));
    }
    
    private ContextManager mockContextManager(final ShardingRule rule) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(database.getRuleMetaData().findSingleRule(ShardingRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    @Test
    void assertGetRowData() throws SQLException {
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
    
    private void assertOrder(final ShardingRule rule) throws SQLException {
        engine = setUp(rule, new ShowShardingTableNodesStatement("t_order", null));
        engine.executeQuery();
        Collection<LocalDataQueryResultRow> actual = engine.getRows();
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        assertThat(row.getCell(2), is("ds_1.t_order_0, ds_2.t_order_1, ds_1.t_order_2, ds_2.t_order_3, ds_1.t_order_4, ds_2.t_order_5"));
    }
    
    private void assertOrderItem(final ShardingRule rule) throws SQLException {
        engine = setUp(rule, new ShowShardingTableNodesStatement("t_order_item", null));
        engine.executeQuery();
        Collection<LocalDataQueryResultRow> actual = engine.getRows();
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
        assertThat(row.getCell(2), is("ds_2.t_order_item_0, ds_3.t_order_item_1, ds_2.t_order_item_2, ds_3.t_order_item_3, ds_2.t_order_item_4, ds_3.t_order_item_5"));
    }
    
    private void assertAll(final ShardingRule rule) throws SQLException {
        engine = setUp(rule, new ShowShardingTableNodesStatement(null, null));
        engine.executeQuery();
        Collection<LocalDataQueryResultRow> actual = engine.getRows();
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
