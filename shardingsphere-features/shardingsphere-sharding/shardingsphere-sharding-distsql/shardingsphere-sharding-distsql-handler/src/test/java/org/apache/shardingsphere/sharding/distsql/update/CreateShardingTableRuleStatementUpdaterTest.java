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
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
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
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingTableRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData shardingSphereRuleMetaData;
    
    private final ShardingRuleConfiguration currentRuleConfig = createCurrentShardingRuleConfiguration();
    
    private final ShardingSphereResource shardingSphereResource = new ShardingSphereResource(createDataSource(), null, null, null);
    
    private final CreateShardingTableRuleStatementUpdater updater = new CreateShardingTableRuleStatementUpdater();
    
    @Before
    public void before() {
        when(shardingSphereMetaData.getDatabaseName()).thenReturn("schema");
        when(shardingSphereMetaData.getResource()).thenReturn(shardingSphereResource);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        when(shardingSphereRuleMetaData.getRules()).thenReturn(Collections.singleton(new MockDataSourceContainedRule()));
    }
    
    @Test
    public void assertUpdate() throws DistSQLException {
        CreateShardingTableRuleStatement statement = new CreateShardingTableRuleStatement(Arrays.asList(createCompleteAutoTableRule(), createCompleteTableRule()));
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeAlteredRuleConfig = updater.buildToBeCreatedRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        assertThat(currentRuleConfig.getTables().size(), is(2));
        Iterator<ShardingTableRuleConfiguration> tableRuleIterator = currentRuleConfig.getTables().iterator();
        ShardingTableRuleConfiguration tableRule = tableRuleIterator.next();
        assertThat(tableRule.getTableShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_algorithm"));
        tableRule = tableRuleIterator.next();
        assertThat(tableRule.getTableShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getLogicTable(), is("t_order_input"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_algorithm"));
        assertThat(tableRule.getDatabaseShardingStrategy(), instanceOf(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_input_database_inline"));
        assertThat(currentRuleConfig.getTables().size(), is(2));
        Iterator<ShardingAutoTableRuleConfiguration> autoTableIterator = currentRuleConfig.getAutoTables().iterator();
        ShardingAutoTableRuleConfiguration autoTableRule = autoTableIterator.next();
        assertThat(autoTableRule.getLogicTable(), is("t_order_item"));
        assertThat(autoTableRule.getActualDataSources(), is("ds_0"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_mod_test"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("product_id_DISTSQL.FIXTURE"));
        autoTableRule = autoTableIterator.next();
        assertThat(autoTableRule.getLogicTable(), is("t_order_item_input"));
        assertThat(autoTableRule.getActualDataSources(), is("logic_ds"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_item_input_foo.distsql.fixture"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_item_input_distsql.fixture"));
    }
    
    private AutoTableRuleSegment createCompleteAutoTableRule() {
        AutoTableRuleSegment result = new AutoTableRuleSegment("t_order_item_input", Collections.singletonList("logic_ds"));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        result.setShardingColumn("order_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("FOO.DISTSQL.FIXTURE", createProperties("", "")));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule() {
        TableRuleSegment result = new TableRuleSegment("t_order_input", Collections.singletonList("ds_${0..1}.t_order_${0..1}"));
        result.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", "t_order_algorithm", null));
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", createProperties("algorithm-expression", "ds_${user_id% 2}"));
        result.setDatabaseStrategySegment(new ShardingStrategySegment("standard", "product_id", null, databaseAlgorithmSegment));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        return result;
    }
    
    private ShardingRuleConfiguration createCurrentShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createTableRuleConfiguration());
        result.getAutoTables().add(createAutoTableRuleConfiguration());
        result.getShardingAlgorithms().put("t_order_algorithm", new ShardingSphereAlgorithmConfiguration("hash_mod", createProperties("sharding-count", "4")));
        result.getKeyGenerators().put("t_order_item_snowflake", new ShardingSphereAlgorithmConfiguration("snowflake", new Properties()));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..1}");
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_algorithm"));
        return result;
    }
    
    private ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("t_order_item", "ds_0");
        result.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_mod_test"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("product_id", "product_id_DISTSQL.FIXTURE"));
        return result;
    }
    
    private Properties createProperties(final String key, final String value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
    
    private Map<String, DataSource> createDataSource() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class));
        result.put("ds_1", mock(DataSource.class));
        return result;
    }
    
    private static class MockDataSourceContainedRule implements DataSourceContainedRule {
        
        @Override
        public Map<String, Collection<String>> getDataSourceMapper() {
            return Collections.singletonMap("logic_ds", null);
        }
        
        @Override
        public String getType() {
            return "mock";
        }
    }
}
