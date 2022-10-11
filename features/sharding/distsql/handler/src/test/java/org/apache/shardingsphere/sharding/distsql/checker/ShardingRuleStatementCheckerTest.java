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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
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

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingRuleStatementCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereRuleMetaData shardingSphereRuleMetaData;
    
    private final ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();
    
    private final ShardingSphereResourceMetaData shardingSphereResource = new ShardingSphereResourceMetaData("sharding_db", createDataSource());
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("schema");
        when(database.getResourceMetaData()).thenReturn(shardingSphereResource);
        when(database.getRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
        when(shardingSphereRuleMetaData.getRules()).thenReturn(Collections.emptyList());
    }
    
    @Test
    public void assertCheckerAutoTableSuccess() {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createCompleteAutoTableRule());
        rules.add(createCompleteTableRule());
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
        rules.clear();
        rules.add(new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkAlteration(database, rules, shardingRuleConfig);
    }
    
    @Test
    public void assertCheckerBindingTableSuccess() {
        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();
        shardingRuleConfig.getBindingTableGroups().add("t_order,t_order_item");
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1"), "order_id",
                new AlgorithmSegment("MOD", newProperties("sharding-count", "2")), null, null));
        rules.add(new AutoTableRuleSegment("t_order_item", Arrays.asList("ds_0", "ds_1"), "order_id",
                new AlgorithmSegment("MOD", newProperties("sharding-count", "2")), null, null));
        ShardingTableRuleStatementChecker.checkAlteration(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckCreationWithDuplicated() {
        List<AbstractTableRuleSegment> rules = Arrays.asList(
                new AutoTableRuleSegment("t_order_duplicated", Arrays.asList("ds_0", "ds_1")),
                new AutoTableRuleSegment("t_order_duplicated", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckCreationWithIdentical() {
        List<AbstractTableRuleSegment> rules = Collections.singletonList(new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckAlterationWithRuleRequiredMissed() {
        List<AbstractTableRuleSegment> rules = Collections.singletonList(new AutoTableRuleSegment("t_order_required_missed", Arrays.asList("ds_0", "ds_1")));
        ShardingTableRuleStatementChecker.checkAlteration(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = MissingRequiredResourcesException.class)
    public void assertCheckCreationWithResourceRequiredMissed() {
        List<AbstractTableRuleSegment> rules = Collections.singletonList(new AutoTableRuleSegment("t_product", Arrays.asList("ds_required_missed", "ds_1")));
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckCreationWithInvalidKeyGenerateAlgorithm() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("invalid", newProperties("invalid", "invalid"))));
        ShardingTableRuleStatementChecker.checkCreation(database, Collections.singleton(autoTableRuleSegment), shardingRuleConfig);
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertCheckCreationWithMissedAuditAlgorithm() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setAuditStrategySegment(new AuditStrategySegment(Collections.singleton("invalid"),
                Collections.singletonList(new ShardingAuditorSegment("invalid", new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()))), true));
        ShardingTableRuleStatementChecker.checkCreation(database, Collections.singleton(autoTableRuleSegment), shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckCreationWithInvalidAuditAlgorithm() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setAuditStrategySegment(new AuditStrategySegment(Collections.singleton("sharding_key_required_auditor"),
                Collections.singletonList(new ShardingAuditorSegment("sharding_key_required_auditor", new AlgorithmSegment("invalid", new Properties()))), true));
        ShardingTableRuleStatementChecker.checkCreation(database, Collections.singleton(autoTableRuleSegment), shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckAutoTableWithNotExistShardingAlgorithms() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingColumn("product_id");
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("not_exist", newProperties("", "")));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(autoTableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("complex", newProperties("", "")));
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckAutoTableWithComplexShardingAlgorithms() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingColumn("product_id");
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("complex", newProperties("", "")));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(autoTableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckTableWithInvalidShardingStrategyType() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("invalid", "product_id", "t_order_algorithm", null));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckTableWithUnmatchedShardingStrategyType1() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("complex", "product_id", "t_order_algorithm", null));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckTableWithUnmatchedShardingStrategyType2() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id,user_id", "t_order_algorithm", null));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckTableWithInvalidAlgorithmName() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("hint", "product_id", "invalid", null));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckTableWithInvalidAlgorithmNameWhenCurrentRuleConfigIsNull() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("hint", "product_id", "invalid", null));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, null);
    }
    
    @Test
    public void assertCheckNullAlgorithmNameAndAlgorithmSegment() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", newProperties("algorithm-expression", "ds_${product_id% 2}"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", null, databaseAlgorithmSegment));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckNullAlgorithmNameAndNullAlgorithmSegment() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", null, null));
        List<AbstractTableRuleSegment> rules = Collections.singletonList(tableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, shardingRuleConfig);
    }
    
    private static ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order${0..1}");
        tableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_algorithm"));
        result.getTables().add(tableRuleConfig);
        ShardingAutoTableRuleConfiguration autoTableRuleConfig = new ShardingAutoTableRuleConfiguration("t_order_item", "ds_0");
        autoTableRuleConfig.setShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "t_order_algorithm"));
        autoTableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("order_item_id", "t_order_item_snowflake"));
        result.getAutoTables().add(autoTableRuleConfig);
        result.getShardingAlgorithms().put("t_order_algorithm", new AlgorithmConfiguration("hash_mod", newProperties("sharding-count", "4")));
        result.getKeyGenerators().put("t_order_item_snowflake", new AlgorithmConfiguration("snowflake", new Properties()));
        result.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        return result;
    }
    
    private static Properties newProperties(final String key, final String value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
    
    private static Map<String, DataSource> createDataSource() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
    
    private AutoTableRuleSegment createCompleteAutoTableRule() {
        AutoTableRuleSegment result = new AutoTableRuleSegment("t_product_0", Arrays.asList("ds_0", "ds_1"));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        result.setShardingColumn("product_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("FOO.DISTSQL.FIXTURE", newProperties("", "")));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule() {
        TableRuleSegment result = new TableRuleSegment("t_product_1", Collections.singletonList("ds_${0..1}.t_order${0..1}"));
        result.setTableStrategySegment(new ShardingStrategySegment("hint", "product_id", "t_order_algorithm", null));
        result.setDatabaseStrategySegment(new ShardingStrategySegment("hint", "product_id", "t_order_algorithm", null));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        return result;
    }
}
