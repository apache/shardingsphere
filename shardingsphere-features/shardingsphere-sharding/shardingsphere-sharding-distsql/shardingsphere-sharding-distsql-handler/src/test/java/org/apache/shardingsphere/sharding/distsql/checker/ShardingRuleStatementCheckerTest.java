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

package org.apache.shardingsphere.sharding.distsql.checker;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRuleStatementCheckerTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData shardingSphereRuleMetaData;
    
    private final ShardingRuleConfiguration shardingRuleConfiguration = createShardingRuleConfiguration();
    
    private final ShardingSphereResource shardingSphereResource = new ShardingSphereResource(createDataSource(), null, null, null);
    
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
    public void assertCheckerAutoTableSuccess() throws DistSQLException {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createCompleteAutoTableRule());
        rules.add(createCompleteTableRule());
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
        rules.clear();
        rules.add(new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkAlteration(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckCreationWithDuplicated() throws DistSQLException {
        List<AbstractTableRuleSegment> rules = Arrays.asList(
                new AutoTableRuleSegment("t_order_duplicated", Arrays.asList("ds_0", "ds_1")),
                new AutoTableRuleSegment("t_order_duplicated", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckCreationWithIdentical() throws DistSQLException {
        List<AbstractTableRuleSegment> rules = Arrays.asList(
                new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckAlterationWithRuleRequiredMissed() throws DistSQLException {
        List<AbstractTableRuleSegment> rules = Arrays.asList(
                new AutoTableRuleSegment("t_order_required_missed", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkAlteration(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = RequiredResourceMissedException.class)
    public void assertCheckCreationWithResourceRequiredMissed() throws DistSQLException {
        List<AbstractTableRuleSegment> rules = Arrays.asList(
                new AutoTableRuleSegment("t_product", Arrays.asList("ds_required_missed", "ds_1")));
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckCreationWithInvalidKeyGenerateAlgorithm() throws DistSQLException {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setKeyGenerateSegment(new KeyGenerateSegment("product_id", new AlgorithmSegment("invalid", newProperties("invalid", "invalid"))));
        List<AbstractTableRuleSegment> rules = Arrays.asList(autoTableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckAutoTableWithInvalidShardingAlgorithms() throws DistSQLException {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingColumn("product_id");
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("invalid", newProperties("", "")));
        List<AbstractTableRuleSegment> rules = Arrays.asList(autoTableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckTableWithInvalidShardingStrategyType() throws DistSQLException {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("invalid", "product_id", "t_order_algorithm"));
        List<AbstractTableRuleSegment> rules = Arrays.asList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckTableWithInvalidAlgorithmName() throws DistSQLException {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("hint", "product_id", "invalid"));
        List<AbstractTableRuleSegment> rules = Arrays.asList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(shardingSphereMetaData, rules, shardingRuleConfiguration);
    }
    
    private static ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfiguration = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order${0..1}");
        tableRuleConfiguration.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_algorithm"));
        result.getTables().add(tableRuleConfiguration);
        ShardingAutoTableRuleConfiguration autoTableRuleConfiguration = new ShardingAutoTableRuleConfiguration("t_order_item", "ds_0");
        autoTableRuleConfiguration.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_item_algorithm"));
        autoTableRuleConfiguration.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "t_order_item_snowflake"));
        result.getAutoTables().add(autoTableRuleConfiguration);
        result.getShardingAlgorithms().put("t_order_algorithm", new ShardingSphereAlgorithmConfiguration("hash_mod", newProperties("sharding-count", "4")));
        result.getKeyGenerators().put("t_order_item_snowflake", new ShardingSphereAlgorithmConfiguration("snowflake", newProperties("worker-id", "123")));
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
    
    private AutoTableRuleSegment createCompleteAutoTableRule() {
        AutoTableRuleSegment result = new AutoTableRuleSegment("t_product_0", Arrays.asList("ds_0", "ds_1"));
        result.setKeyGenerateSegment(new KeyGenerateSegment("product_id", new AlgorithmSegment("snowflake_test", newProperties("work", "123"))));
        result.setShardingColumn("product_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("MOD_TEST", newProperties("", "")));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule() {
        TableRuleSegment result = new TableRuleSegment("t_product_1", Collections.singletonList("ds_${0..1}.t_order${0..1}"));
        result.setTableStrategySegment(new ShardingStrategySegment("hint", "product_id", "t_order_algorithm"));
        result.setDatabaseStrategySegment(new ShardingStrategySegment("hint", "product_id", "t_order_algorithm"));
        result.setKeyGenerateSegment(new KeyGenerateSegment("product_id", new AlgorithmSegment("SNOWFLAKE_TEST", newProperties("work", "123"))));
        return result;
    }
    
    private static Collection<ShardingSphereRule> createShardingSphereRule() {
        return Collections.emptyList();
    }
}
