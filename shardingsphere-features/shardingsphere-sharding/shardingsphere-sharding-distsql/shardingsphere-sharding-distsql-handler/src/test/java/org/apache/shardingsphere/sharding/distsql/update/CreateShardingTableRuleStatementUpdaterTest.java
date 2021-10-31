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

package org.apache.shardingsphere.sharding.distsql.update;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingTableRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData shardingSphereRuleMetaData;
    
    private final ShardingRuleConfiguration currentRuleConfiguration = createCurrentShardingRuleConfiguration();
    
    private final ShardingSphereResource shardingSphereResource = new ShardingSphereResource(createDataSource(), null, null, null);
    
    private final CreateShardingTableRuleStatementUpdater updater = new CreateShardingTableRuleStatementUpdater();
    
    @Before
    public void before() {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
        when(shardingSphereMetaData.getName()).thenReturn("schema");
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        when(shardingSphereRuleMetaData.getRules()).thenReturn(createShardingSphereRule());
    }
    
    @Test
    public void assertUpdate() throws DistSQLException {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createCompleteAutoTableRule());
        rules.add(createCompleteTableRule());
        CreateShardingTableRuleStatement statement = new CreateShardingTableRuleStatement(rules);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfiguration);
        ShardingRuleConfiguration toBeAlteredRuleConfiguration = updater.buildToBeCreatedRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfiguration, toBeAlteredRuleConfiguration);
        Assert.assertEquals(2, currentRuleConfiguration.getTables().size());
        Iterator<ShardingTableRuleConfiguration> tableRuleIterator = currentRuleConfiguration.getTables().iterator();
        ShardingTableRuleConfiguration tableRule = tableRuleIterator.next();
        Assert.assertTrue(tableRule.getTableShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        Assert.assertEquals("t_order", tableRule.getLogicTable());
        Assert.assertEquals("ds_${0..1}.t_order${0..1}", tableRule.getActualDataNodes());
        Assert.assertEquals("order_id", ((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn());
        Assert.assertEquals("t_order_algorithm", tableRule.getTableShardingStrategy().getShardingAlgorithmName());
        tableRule = tableRuleIterator.next();
        Assert.assertTrue(tableRule.getTableShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        Assert.assertEquals("t_order_input", tableRule.getLogicTable());
        Assert.assertEquals("ds_${0..1}.t_order${0..1}", tableRule.getActualDataNodes());
        Assert.assertEquals("product_id", ((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn());
        Assert.assertEquals("t_order_algorithm", tableRule.getTableShardingStrategy().getShardingAlgorithmName());
        Assert.assertTrue(tableRule.getDatabaseShardingStrategy() instanceof HintShardingStrategyConfiguration);
        Assert.assertEquals("t_order_algorithm", tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName());
        Assert.assertEquals(2, currentRuleConfiguration.getTables().size());
        Iterator<ShardingAutoTableRuleConfiguration> autoTableIterator = currentRuleConfiguration.getAutoTables().iterator();
        ShardingAutoTableRuleConfiguration autoTableRule = autoTableIterator.next();
        Assert.assertEquals("t_order_item", autoTableRule.getLogicTable());
        Assert.assertEquals("ds_0", autoTableRule.getActualDataSources());
        Assert.assertEquals("t_order_MOD_TEST", autoTableRule.getShardingStrategy().getShardingAlgorithmName());
        Assert.assertEquals("order_id", ((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn());
        Assert.assertEquals("product_id", autoTableRule.getKeyGenerateStrategy().getColumn());
        Assert.assertEquals("product_id_snowflake_test", autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName());
        autoTableRule = autoTableIterator.next();
        Assert.assertEquals("t_order_item_input", autoTableRule.getLogicTable());
        Assert.assertEquals("logic_ds", autoTableRule.getActualDataSources());
        Assert.assertEquals("t_order_item_input_MOD_TEST", autoTableRule.getShardingStrategy().getShardingAlgorithmName());
        Assert.assertEquals("order_id", ((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn());
        Assert.assertEquals("product_id", autoTableRule.getKeyGenerateStrategy().getColumn());
        Assert.assertEquals("t_order_item_input_snowflake_test", autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName());
    }
    
    private AutoTableRuleSegment createCompleteAutoTableRule() {
        AutoTableRuleSegment result = new AutoTableRuleSegment("t_order_item_input", Arrays.asList("logic_ds"));
        result.setKeyGenerateSegment(new KeyGenerateSegment("product_id", new AlgorithmSegment("snowflake_test", newProperties("work", "123"))));
        result.setShardingColumn("order_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("MOD_TEST", newProperties("", "")));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule() {
        TableRuleSegment result = new TableRuleSegment("t_order_input", Collections.singletonList("ds_${0..1}.t_order${0..1}"));
        result.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", "t_order_algorithm"));
        result.setDatabaseStrategySegment(new ShardingStrategySegment("hint", "product_id", "t_order_algorithm"));
        result.setKeyGenerateSegment(new KeyGenerateSegment("product_id", new AlgorithmSegment("SNOWFLAKE_TEST", newProperties("work", "123"))));
        return result;
    }
    
    private ShardingRuleConfiguration createCurrentShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createTableRuleConfiguration());
        result.getAutoTables().add(createAutoTableRuleConfiguration());
        result.getShardingAlgorithms().put("t_order_algorithm", new ShardingSphereAlgorithmConfiguration("hash_mod", newProperties("sharding-count", "4")));
        result.getKeyGenerators().put("t_order_item_snowflake", new ShardingSphereAlgorithmConfiguration("snowflake", newProperties("worker-id", "123")));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration() {
        ShardingTableRuleConfiguration tableRuleConfiguration = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order${0..1}");
        tableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_algorithm"));
        return tableRuleConfiguration;
    }
    
    private ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration autoTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("t_order_item", "ds_0");
        autoTableRuleConfiguration.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_MOD_TEST"));
        autoTableRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("product_id", "product_id_snowflake_test"));
        return autoTableRuleConfiguration;
    }
    
    private static Collection<ShardingSphereRule> createShardingSphereRule() {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        result.add(new MockDataSourceContainedRule());
        return result;
    }
    
    private static Properties newProperties(final String key, final String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return properties;
    }
    
    private static Map<String, DataSource> createDataSource() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds_0", mock(DataSource.class));
        result.put("ds_1", mock(DataSource.class));
        return result;
    }
    
    private static class MockDataSourceContainedRule implements DataSourceContainedRule {
        
        @Override
        public String getType() {
            return "mock";
        }
        
        @Override
        public Map<String, Collection<String>> getDataSourceMapper() {
            Map<String, Collection<String>> result = new HashMap<>();
            result.put("logic_ds", null);
            return result;
        }
    }
}
