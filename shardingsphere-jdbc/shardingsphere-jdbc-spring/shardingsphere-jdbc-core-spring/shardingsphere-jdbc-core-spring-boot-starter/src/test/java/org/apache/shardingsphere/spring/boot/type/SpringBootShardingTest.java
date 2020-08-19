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

package org.apache.shardingsphere.spring.boot.type;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.strategy.standard.StandardShardingStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootShardingTest.class)
@SpringBootApplication
@ActiveProfiles("sharding")
public class SpringBootShardingTest {
    
    @Resource
    private DataSource dataSource;
    
    @Test
    public void assertWithShardingSphereDataSource() {
        assertThat(dataSource, instanceOf(ShardingSphereDataSource.class));
        SchemaContexts schemaContexts = ((ShardingSphereDataSource) dataSource).getSchemaContexts();
        assertThat(((ShardingSphereDataSource) dataSource).getDataSourceMap().size(), is(3));
        assertTrue(schemaContexts.getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertTrue(schemaContexts.getProps().getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertThat(schemaContexts.getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), is(100));
    }
    
    @Test
    public void assertWithShardingSphereDataSourceNames() {
        SchemaContexts schemaContexts = ((ShardingSphereDataSource) dataSource).getSchemaContexts();
        ShardingRule shardingRule = (ShardingRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
        assertThat(shardingRule.getDataSourceNames().size(), is(2));
        assertTrue(shardingRule.getDataSourceNames().contains("ds_0"));
        assertTrue(shardingRule.getDataSourceNames().contains("ds_1"));
    }
    
    @Test
    public void assertWithTableRules() {
        SchemaContexts schemaContexts = ((ShardingSphereDataSource) dataSource).getSchemaContexts();
        ShardingRule shardingRule = (ShardingRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
        assertThat(shardingRule.getTableRules().size(), is(2));
        TableRule tableRule1 = shardingRule.getTableRule("t_order");
        assertThat(tableRule1.getActualDataNodes().size(), is(4));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_0", "t_order_0")));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_0", "t_order_1")));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_1", "t_order_0")));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_1", "t_order_1")));
        assertThat(tableRule1.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(tableRule1.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        assertTrue(tableRule1.getGenerateKeyColumn().isPresent());
        assertThat(tableRule1.getGenerateKeyColumn().get(), is("order_id"));
        TableRule tableRule2 = shardingRule.getTableRule("t_order_item");
        assertThat(tableRule2.getActualDataNodes().size(), is(4));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_0")));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_1")));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_0")));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_1")));
        assertThat(tableRule1.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(tableRule1.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        assertTrue(tableRule2.getGenerateKeyColumn().isPresent());
        assertThat(tableRule2.getGenerateKeyColumn().get(), is("order_item_id"));
    }
    
    @Test
    public void assertWithBindingTableRules() {
        SchemaContexts schemaContexts = ((ShardingSphereDataSource) dataSource).getSchemaContexts();
        ShardingRule shardingRule = (ShardingRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
        assertThat(shardingRule.getBindingTableRules().size(), is(2));
        TableRule tableRule1 = shardingRule.getTableRule("t_order");
        assertThat(tableRule1.getLogicTable(), is("t_order"));
        assertThat(tableRule1.getActualDataNodes().size(), is(4));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_0", "t_order_0")));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_0", "t_order_1")));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_1", "t_order_0")));
        assertTrue(tableRule1.getActualDataNodes().contains(new DataNode("ds_1", "t_order_1")));
        assertThat(tableRule1.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(tableRule1.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        assertTrue(tableRule1.getGenerateKeyColumn().isPresent());
        assertThat(tableRule1.getGenerateKeyColumn().get(), is("order_id"));
        TableRule tableRule2 = shardingRule.getTableRule("t_order_item");
        assertThat(tableRule2.getLogicTable(), is("t_order_item"));
        assertThat(tableRule2.getActualDataNodes().size(), is(4));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_0")));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_1")));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_0")));
        assertTrue(tableRule2.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_1")));
        assertThat(tableRule1.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(tableRule1.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        assertTrue(tableRule2.getGenerateKeyColumn().isPresent());
        assertThat(tableRule2.getGenerateKeyColumn().get(), is("order_item_id"));
    }
    
    @Test
    public void assertWithBroadcastTables() {
        SchemaContexts schemaContexts = ((ShardingSphereDataSource) dataSource).getSchemaContexts();
        ShardingRule shardingRule = (ShardingRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
        assertThat(shardingRule.getBroadcastTables().size(), is(1));
        assertThat(shardingRule.getBroadcastTables().iterator().next(), is("t_config"));
    }
}
