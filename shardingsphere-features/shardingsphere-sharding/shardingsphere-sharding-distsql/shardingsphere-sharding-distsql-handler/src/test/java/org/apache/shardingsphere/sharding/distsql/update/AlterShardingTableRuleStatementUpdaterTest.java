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
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.AlterShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
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
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterShardingTableRuleStatementUpdaterTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData shardingSphereRuleMetaData;
    
    private final ShardingRuleConfiguration currentRuleConfiguration = createCurrentShardingRuleConfiguration();
    
    private final ShardingSphereResource shardingSphereResource = new ShardingSphereResource(createDataSource(), null, null, null);
    
    private final AlterShardingTableRuleStatementUpdater updater = new AlterShardingTableRuleStatementUpdater();
    
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
        rules.add(createCompleteAutoTableRule("t_order_item"));
        rules.add(createCompleteTableRule("t_order"));
        AlterShardingTableRuleStatement statement = new AlterShardingTableRuleStatement(rules);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfiguration);
        ShardingRuleConfiguration toBeAlteredRuleConfiguration = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfiguration, toBeAlteredRuleConfiguration);
        assertThat(currentRuleConfiguration.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = currentRuleConfiguration.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order${0..1}"));
        assertTrue(tableRule.getTableShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_algorithm"));
        assertTrue(tableRule.getDatabaseShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_database_inline"));
        assertThat(currentRuleConfiguration.getTables().size(), is(1));
        ShardingAutoTableRuleConfiguration autoTableRule = currentRuleConfiguration.getAutoTables().iterator().next();
        assertThat(autoTableRule.getLogicTable(), is("t_order_item"));
        assertThat(autoTableRule.getActualDataSources(), is("ds_0,ds_1"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_item_MOD_TEST"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_item_snowflake_test"));
    }
    
    @Test
    public void assertUpdateTableType() throws DistSQLException {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createCompleteAutoTableRule("t_order"));
        rules.add(createCompleteTableRule("t_order_item"));
        AlterShardingTableRuleStatement statement = new AlterShardingTableRuleStatement(rules);
        updater.checkSQLStatement(shardingSphereMetaData, statement, currentRuleConfiguration);
        ShardingRuleConfiguration toBeAlteredRuleConfiguration = updater.buildToBeAlteredRuleConfiguration(statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfiguration, toBeAlteredRuleConfiguration);
        assertThat(currentRuleConfiguration.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = currentRuleConfiguration.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order_item"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order${0..1}"));
        assertTrue(tableRule.getTableShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_algorithm"));
        assertTrue(tableRule.getDatabaseShardingStrategy() instanceof StandardShardingStrategyConfiguration);
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_item_database_inline"));
        assertThat(currentRuleConfiguration.getTables().size(), is(1));
        ShardingAutoTableRuleConfiguration autoTableRule = currentRuleConfiguration.getAutoTables().iterator().next();
        assertThat(autoTableRule.getLogicTable(), is("t_order"));
        assertThat(autoTableRule.getActualDataSources(), is("ds_0,ds_1"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_MOD_TEST"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_snowflake_test"));
    }
    
    private AutoTableRuleSegment createCompleteAutoTableRule(final String logicTableName) {
        AutoTableRuleSegment result = new AutoTableRuleSegment(logicTableName, Arrays.asList("ds_0", "ds_1"));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("snowflake_test", newProperties("work", "123"))));
        result.setShardingColumn("order_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("MOD_TEST", newProperties("", "")));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule(final String logicTableName) {
        TableRuleSegment result = new TableRuleSegment(logicTableName, Collections.singletonList("ds_${0..1}.t_order${0..1}"));
        result.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", "t_order_algorithm", null));
        AlgorithmSegment databaseAlgorithmSegment = getAutoCreativeAlgorithmSegment("inline", newProperties("algorithm-expression", "ds_${user_id% 2}"));
        result.setDatabaseStrategySegment(new ShardingStrategySegment("standard", "product_id", null, databaseAlgorithmSegment));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("SNOWFLAKE_TEST", newProperties("work", "123"))));
        return result;
    }
    
    private AlgorithmSegment getAutoCreativeAlgorithmSegment(final String name, final Properties props) {
        return new AlgorithmSegment(name, props);
    }
    
    private ShardingRuleConfiguration createCurrentShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createTableRuleConfiguration());
        result.getAutoTables().add(createAutoTableRuleConfiguration());
        result.getShardingAlgorithms().put("t_order_algorithm", new ShardingSphereAlgorithmConfiguration("hash_mod", newProperties("sharding-count", "4")));
        result.getKeyGenerators().put("t_order_item_snowflake", new ShardingSphereAlgorithmConfiguration("snowflake", new Properties()));
        return result;
    }
    
    private ShardingTableRuleConfiguration createTableRuleConfiguration() {
        ShardingTableRuleConfiguration result = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order${0..1}");
        result.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_algorithm"));
        return result;
    }
    
    private ShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration() {
        ShardingAutoTableRuleConfiguration result = new ShardingAutoTableRuleConfiguration("t_order_item", "ds_0");
        result.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_MOD_TEST"));
        result.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("product_id", "product_id_snowflake_test"));
        return result;
    }
    
    private static Collection<ShardingSphereRule> createShardingSphereRule() {
        return Collections.emptyList();
    }
    
    private static Properties newProperties(final String key, final String value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
    
    private static Map<String, DataSource> createDataSource() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class));
        result.put("ds_1", mock(DataSource.class));
        return result;
    }
}
