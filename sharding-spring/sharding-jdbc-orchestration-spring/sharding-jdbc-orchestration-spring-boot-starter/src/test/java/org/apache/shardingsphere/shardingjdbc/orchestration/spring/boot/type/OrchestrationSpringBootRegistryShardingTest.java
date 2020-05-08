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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.type;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.standard.StandardShardingStrategy;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.registry.TestCenterRepository;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.util.EmbedTestingServer;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.datanode.DataNode;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrchestrationSpringBootRegistryShardingTest.class)
@SpringBootApplication
@ActiveProfiles("registry")
public class OrchestrationSpringBootRegistryShardingTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        TestCenterRepository testCenter = new TestCenterRepository();
        testCenter.persist("/demo_spring_boot_ds_center/config/schema/logic_db/datasource", "ds: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  properties:\n"
            + "    url: jdbc:h2:mem:ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL\n"
            + "    maxTotal: 16\n"
            + "    password: ''\n"
            + "    username: sa\n"
            + "ds_0: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  properties:\n"
            + "    url: jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL\n"
            + "    maxTotal: 16\n"
            + "    password: ''\n"
            + "    username: sa\n"
            + "ds_1: !!org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  properties:\n"
            + "    url: jdbc:h2:mem:ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL\n"
            + "    maxTotal: 16\n"
            + "    password: ''\n"
            + "    username: sa\n");
        testCenter.persist("/demo_spring_boot_ds_center/config/schema/logic_db/rule", "shardingRule:\n"
            + "  bindingTables:\n"
            + "  - t_order\n"
            + "  - t_order_item\n"
            + "  broadcastTables:\n"
            + "  - t_config\n"
            + "  defaultDatabaseStrategy:\n"
            + "    standard:\n"
            + "      shardingAlgorithm:\n" 
            + "        type: INLINE\n"
            + "        props:\n" 
            + "          algorithm.expression: ds_${user_id % 2}\n"                
            + "      shardingColumn: user_id\n"
            + "  tables:\n"
            + "    t_order:\n"
            + "      actualDataNodes: ds_${0..1}.t_order_${0..1}\n"
            + "      keyGenerator:\n"
            + "        column: order_id\n"
            + "        props:\n"
            + "          worker.id: '123'\n"
            + "        type: SNOWFLAKE\n"
            + "      logicTable: t_order\n"
            + "      tableStrategy:\n"
            + "        standard:\n"
            + "          shardingAlgorithm:\n"
            + "            type: INLINE\n"
            + "            props:\n"
            + "              algorithm.expression: t_order_${order_id % 2}\n"    
            + "          shardingColumn: order_id\n"
            + "    t_order_item:\n"
            + "      actualDataNodes: ds_${0..1}.t_order_item_${0..1}\n"
            + "      keyGenerator:\n"
            + "        column: order_item_id\n"
            + "        props:\n"
            + "          worker.id: '123'\n"
            + "        type: SNOWFLAKE\n"
            + "      logicTable: t_order_item\n"
            + "      tableStrategy:\n"
            + "        standard:\n"
            + "          shardingAlgorithm:\n"
            + "            type: INLINE\n"
            + "            props:\n"
            + "              algorithm.expression: t_order_item_${order_id % 2}\n"
            + "          shardingColumn: order_id\n");
        testCenter.persist("/demo_spring_boot_ds_center/config/props", "executor.size: '100'\nsql.show: 'true'\n");
        testCenter.persist("/demo_spring_boot_ds_center/registry/datasources", "");
    }
    
    @Test
    public void assertWithShardingDataSource() {
        assertTrue(dataSource instanceof OrchestrationShardingDataSource);
        ShardingDataSource shardingDataSource = getFieldValue("dataSource", OrchestrationShardingDataSource.class, dataSource);
        RuntimeContext runtimeContext = shardingDataSource.getRuntimeContext();
        for (DataSource each : shardingDataSource.getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(16));
        }
        assertTrue(runtimeContext.getProperties().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        ConfigurationProperties properties = runtimeContext.getProperties();
        assertTrue(properties.getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertThat(properties.getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), is(100));
    }
    
    @Test
    public void assertWithShardingDataSourceNames() {
        ShardingDataSource shardingDataSource = getFieldValue("dataSource", OrchestrationShardingDataSource.class, dataSource);
        RuntimeContext runtimeContext = shardingDataSource.getRuntimeContext();
        ShardingRule shardingRule = (ShardingRule) runtimeContext.getRules().iterator().next();
        assertThat(shardingRule.getDataSourceNames().size(), is(2));
        assertTrue(shardingRule.getDataSourceNames().contains("ds_0"));
        assertTrue(shardingRule.getDataSourceNames().contains("ds_1"));
    }
    
    @Test
    public void assertWithTableRules() {
        ShardingDataSource shardingDataSource = getFieldValue("dataSource", OrchestrationShardingDataSource.class, dataSource);
        RuntimeContext runtimeContext = shardingDataSource.getRuntimeContext();
        ShardingRule shardingRule = (ShardingRule) runtimeContext.getRules().iterator().next();
        assertThat(shardingRule.getTableRules().size(), is(2));
        TableRule orderRule = shardingRule.getTableRule("t_order");
        assertThat(orderRule.getLogicTable(), is("t_order"));
        assertThat(orderRule.getActualDataNodes().size(), is(4));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_0")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_1")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_0")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_1")));
        assertTrue(orderRule.getGenerateKeyColumn().isPresent());
        assertThat(orderRule.getGenerateKeyColumn().get(), is("order_id"));
        TableRule itemRule = shardingRule.getTableRule("t_order_item");
        assertThat(itemRule.getLogicTable(), is("t_order_item"));
        assertThat(itemRule.getActualDataNodes().size(), is(4));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_0")));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_1")));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_0")));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_1")));
        assertThat(itemRule.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(itemRule.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        assertTrue(itemRule.getGenerateKeyColumn().isPresent());
        assertThat(itemRule.getGenerateKeyColumn().get(), is("order_item_id"));
        assertThat(itemRule.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(itemRule.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        
    }
    
    @Test
    public void assertWithBindingTableRules() {
        ShardingDataSource shardingDataSource = getFieldValue("dataSource", OrchestrationShardingDataSource.class, dataSource);
        RuntimeContext runtimeContext = shardingDataSource.getRuntimeContext();
        ShardingRule shardingRule = (ShardingRule) runtimeContext.getRules().iterator().next();
        assertThat(shardingRule.getBindingTableRules().size(), is(2));
        TableRule orderRule = shardingRule.getTableRule("t_order");
        assertThat(orderRule.getLogicTable(), is("t_order"));
        assertThat(orderRule.getActualDataNodes().size(), is(4));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_0")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_1")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_0")));
        assertTrue(orderRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_1")));
        TableRule itemRule = shardingRule.getTableRule("t_order_item");
        assertThat(itemRule.getLogicTable(), is("t_order_item"));
        assertThat(itemRule.getActualDataNodes().size(), is(4));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_0")));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_0", "t_order_item_1")));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_0")));
        assertTrue(itemRule.getActualDataNodes().contains(new DataNode("ds_1", "t_order_item_1")));
        assertThat(itemRule.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(itemRule.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        assertTrue(itemRule.getGenerateKeyColumn().isPresent());
        assertThat(itemRule.getGenerateKeyColumn().get(), is("order_item_id"));
        assertThat(itemRule.getTableShardingStrategy(), instanceOf(StandardShardingStrategy.class));
        assertThat(itemRule.getTableShardingStrategy().getShardingColumns().iterator().next(), is("order_id"));
        assertTrue(orderRule.getGenerateKeyColumn().isPresent());
        assertThat(orderRule.getGenerateKeyColumn().get(), is("order_id"));
        
    }
    
    @Test
    public void assertWithBroadcastTables() {
        ShardingDataSource shardingDataSource = getFieldValue("dataSource", OrchestrationShardingDataSource.class, dataSource);
        RuntimeContext runtimeContext = shardingDataSource.getRuntimeContext();
        ShardingRule shardingRule = (ShardingRule) runtimeContext.getRules().iterator().next();
        assertThat(shardingRule.getBroadcastTables().size(), is(1));
        assertThat(shardingRule.getBroadcastTables().iterator().next(), is("t_config"));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private <T> T getFieldValue(final String fieldName, final Class<?> fieldClass, final Object target) {
        Field field = fieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
