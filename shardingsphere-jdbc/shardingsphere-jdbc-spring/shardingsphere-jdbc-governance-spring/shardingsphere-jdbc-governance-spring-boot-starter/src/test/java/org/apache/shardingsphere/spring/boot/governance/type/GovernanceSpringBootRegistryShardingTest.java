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

package org.apache.shardingsphere.spring.boot.governance.type;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.spring.boot.governance.registry.TestGovernanceRepository;
import org.apache.shardingsphere.spring.boot.governance.util.EmbedTestingServer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GovernanceSpringBootRegistryShardingTest.class)
@SpringBootApplication
@ActiveProfiles("registry")
public class GovernanceSpringBootRegistryShardingTest {
    
    private static final String SHARDING_DATABASES_FILE = "yaml/sharding-databases.yaml";
    
    private static final String SHARDING_RULE_FILE = "yaml/sharding-rule.yaml";
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        String shardingDatabases = readYAML(SHARDING_DATABASES_FILE);
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        TestGovernanceRepository repository = new TestGovernanceRepository();
        repository.persist("/schemas/logic_db/datasource", shardingDatabases);
        repository.persist("/schemas/logic_db/rule", shardingRule);
        repository.persist("/props", ConfigurationPropertyKey.EXECUTOR_SIZE.getKey() + ": '100'\n" + ConfigurationPropertyKey.SQL_SHOW.getKey() + ": 'true'\n");
        repository.persist("/states/datanodes", "");
    }
    
    @Test
    public void assertWithShardingSphereDataSource() {
        assertTrue(dataSource instanceof GovernanceShardingSphereDataSource);
        MetaDataContexts metaDataContexts = getFieldValue("metaDataContexts", GovernanceShardingSphereDataSource.class, dataSource);
        for (DataSource each : metaDataContexts.getDefaultMetaData().getResource().getDataSources().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(16));
        }
        assertTrue(metaDataContexts.getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertTrue(metaDataContexts.getProps().getValue(ConfigurationPropertyKey.SQL_SHOW));
    }
    
    @Test
    public void assertWithShardingSphereDataSourceNames() {
        MetaDataContexts metaDataContexts = getFieldValue("metaDataContexts", GovernanceShardingSphereDataSource.class, dataSource);
        ShardingRule shardingRule = (ShardingRule) metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().iterator().next();
        assertThat(shardingRule.getDataSourceNames().size(), is(2));
        assertTrue(shardingRule.getDataSourceNames().contains("ds_0"));
        assertTrue(shardingRule.getDataSourceNames().contains("ds_1"));
    }
    
    @Test
    public void assertWithTableRules() {
        MetaDataContexts metaDataContexts = getFieldValue("metaDataContexts", GovernanceShardingSphereDataSource.class, dataSource);
        ShardingRule shardingRule = (ShardingRule) metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().iterator().next();
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
        assertTrue(itemRule.getGenerateKeyColumn().isPresent());
        assertThat(itemRule.getGenerateKeyColumn().get(), is("order_item_id"));
    }
    
    @Test
    public void assertWithBindingTableRules() {
        MetaDataContexts metaDataContexts = getFieldValue("metaDataContexts", GovernanceShardingSphereDataSource.class, dataSource);
        ShardingRule shardingRule = (ShardingRule) metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().iterator().next();
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
        assertTrue(itemRule.getGenerateKeyColumn().isPresent());
        assertThat(itemRule.getGenerateKeyColumn().get(), is("order_item_id"));
        assertTrue(orderRule.getGenerateKeyColumn().isPresent());
        assertThat(orderRule.getGenerateKeyColumn().get(), is("order_id"));
        
    }
    
    @Test
    public void assertWithBroadcastTables() {
        MetaDataContexts metaDataContexts = getFieldValue("metaDataContexts", GovernanceShardingSphereDataSource.class, dataSource);
        ShardingRule shardingRule = (ShardingRule) metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().iterator().next();
        assertThat(shardingRule.getBroadcastTables().size(), is(1));
        assertThat(shardingRule.getBroadcastTables().iterator().next(), is("t_config"));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private <T> T getFieldValue(final String fieldName, final Class<?> fieldClass, final Object target) {
        Field field = fieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private static String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
