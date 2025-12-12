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

package org.apache.shardingsphere.sharding.distsql.handler.checker;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.core.exception.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.exception.strategy.InvalidShardingStrategyConfigurationException;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingTableRuleStatementCheckerTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    private final ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();
    
    private final ResourceMetaData resourceMetaData = new ResourceMetaData(createDataSource());
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("schema");
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
    }
    
    @Test
    void assertCheckCreateSuccess() {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createCompleteAutoTableRule());
        rules.add(createCompleteTableRule());
        ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig);
    }
    
    @Test
    void assertCheckAlterSuccess() {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingColumn("order_id");
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("CORE.AUTO.FIXTURE", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        rules.add(autoTableRuleSegment);
        ShardingTableRuleStatementChecker.checkAlteration(database, rules, shardingRuleConfig);
    }
    
    @Test
    void assertCheckerBindingTableSuccess() {
        ShardingRuleConfiguration shardingRuleConfig = createShardingRuleConfiguration();
        shardingRuleConfig.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("reference_0", "t_order,t_order_item"));
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        AutoTableRuleSegment autoTable1 = new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1"));
        autoTable1.setShardingColumn("order_id");
        autoTable1.setShardingAlgorithmSegment(new AlgorithmSegment("CORE.AUTO.FIXTURE", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        rules.add(autoTable1);
        AutoTableRuleSegment autoTable2 = new AutoTableRuleSegment("t_order_item", Arrays.asList("ds_0", "ds_1"));
        autoTable2.setShardingColumn("order_id");
        autoTable2.setShardingAlgorithmSegment(new AlgorithmSegment("CORE.AUTO.FIXTURE", PropertiesBuilder.build(new Property("sharding-count", "2"))));
        rules.add(autoTable2);
        ShardingTableRuleStatementChecker.checkAlteration(database, rules, shardingRuleConfig);
    }
    
    @Test
    void assertCheckCreationWithDuplicated() {
        List<AbstractTableRuleSegment> rules = Arrays.asList(
                new AutoTableRuleSegment("t_order_duplicated", Arrays.asList("ds_0", "ds_1")),
                new AutoTableRuleSegment("t_order_duplicated", Arrays.asList("ds_0", "ds_1")));
        assertThrows(DuplicateRuleException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckCreationWithIdentical() {
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(new AutoTableRuleSegment("t_order", Arrays.asList("ds_0", "ds_1")));
        assertThrows(DuplicateRuleException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckAlterationWithRuleRequiredMissed() {
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(new AutoTableRuleSegment("t_order_required_missed", Arrays.asList("ds_0", "ds_1")));
        assertThrows(MissingRequiredRuleException.class, () -> ShardingTableRuleStatementChecker.checkAlteration(database, rules, shardingRuleConfig));
    }
    
    @Test
    void assertCheckCreationWithResourceRequiredMissed() {
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(new AutoTableRuleSegment("t_product", Arrays.asList("ds_required_missed", "ds_1")));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckCreationWithInvalidKeyGenerateAlgorithm() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("invalid", PropertiesBuilder.build(new Property("invalid", "invalid")))));
        assertThrows(ServiceProviderNotFoundException.class,
                () -> ShardingTableRuleStatementChecker.checkCreation(database, Collections.singleton(autoTableRuleSegment), false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckCreationWithInvalidAuditAlgorithm() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setAuditStrategySegment(new AuditStrategySegment(Collections.singleton(new ShardingAuditorSegment("sharding_key_required_auditor",
                new AlgorithmSegment("invalid", new Properties()))), true));
        assertThrows(ServiceProviderNotFoundException.class,
                () -> ShardingTableRuleStatementChecker.checkCreation(database, Collections.singleton(autoTableRuleSegment), false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckAutoTableWithNotExistShardingAlgorithms() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingColumn("product_id");
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("not_exist", PropertiesBuilder.build(new Property("", ""))));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(autoTableRuleSegment);
        assertThrows(ServiceProviderNotFoundException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckAutoTableWithComplexShardingAlgorithms() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingColumn("product_id");
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("complex", PropertiesBuilder.build(new Property("", ""))));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(autoTableRuleSegment);
        assertThrows(ServiceProviderNotFoundException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckTableWithInvalidShardingStrategyType() {
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("invalid", "product_id", null));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(UnsupportedSQLOperationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckTableWithUnmatchedShardingStrategyType1() {
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("complex", "product_id", null));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckTableWithUnmatchedShardingStrategyType2() {
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id,user_id", null));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckTableWithUnmatchedShardingStrategyType3() {
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("CORE.AUTO.FIXTURE", PropertiesBuilder.build(new Property("sharding-count", "4")));
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "user_id", databaseAlgorithmSegment));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(AlgorithmInitializationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckTableWithInvalidAlgorithmName() {
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("hint", "product_id", null));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckTableWithInvalidAlgorithmNameWhenCurrentRuleConfigIsNull() {
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("hint", "product_id", null));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, null));
    }
    
    @Test
    void assertCheckTableWithInvalidDataNodeFormat() {
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${product_id % 2}")));
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", databaseAlgorithmSegment));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(InvalidDataNodeFormatException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, null));
    }
    
    @Test
    void assertCheckNullAlgorithmNameAndNullAlgorithmSegment() {
        KeyGenerateStrategySegment keyGenerateStrategy = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"), keyGenerateStrategy, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", null));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(tableRuleSegment);
        assertThrows(InvalidAlgorithmConfigurationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckAutoTableRuleWithStandardShardingAlgorithm() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("INLINE", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${product_id % 2}"))));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(autoTableRuleSegment);
        assertThrows(AlgorithmInitializationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckAutoTableRuleWithAutoShardingAlgorithm() {
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_product", Arrays.asList("ds_0", "ds_1"));
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("CORE.AUTO.FIXTURE", PropertiesBuilder.build(new Property("sharding-count", "4"))));
        Collection<AbstractTableRuleSegment> rules = Collections.singleton(autoTableRuleSegment);
        ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig);
    }
    
    @Test
    void assertCheckStatementWithIfNotExists() {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createCompleteAutoTableRule());
        rules.add(createCompleteTableRule());
        ShardingTableRuleStatementChecker.checkCreation(database, rules, true, shardingRuleConfig);
    }
    
    @Test
    void assertCheckTableRuleWithNoneStrategyTypeThrows() {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createWrongTableRuleWithNoneTypeStrategy());
        assertThrows(InvalidShardingStrategyConfigurationException.class, () -> ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig));
    }
    
    @Test
    void assertCheckTableRuleWithNoneStrategyTypeSuccess() {
        Collection<AbstractTableRuleSegment> rules = new LinkedList<>();
        rules.add(createCompleteTableRuleWithNoneTypeStrategy());
        ShardingTableRuleStatementChecker.checkCreation(database, rules, false, shardingRuleConfig);
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
        result.getShardingAlgorithms().put("t_order_algorithm", new AlgorithmConfiguration("hash_mod", PropertiesBuilder.build(new Property("sharding-count", "4"))));
        result.getKeyGenerators().put("t_order_item_snowflake", new AlgorithmConfiguration("snowflake", new Properties()));
        result.getAuditors().put("sharding_key_required_auditor", new AlgorithmConfiguration("DML_SHARDING_CONDITIONS", new Properties()));
        return result;
    }
    
    private Map<String, DataSource> createDataSource() {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
    
    private AutoTableRuleSegment createCompleteAutoTableRule() {
        AutoTableRuleSegment result = new AutoTableRuleSegment("t_product_0", Arrays.asList("ds_0", "ds_1"));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        result.setShardingColumn("product_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("FOO.DISTSQL.FIXTURE", PropertiesBuilder.build(new Property("", ""))));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule() {
        Properties props = new Properties();
        KeyGenerateStrategySegment keyGenerator = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", props));
        TableRuleSegment result = new TableRuleSegment("t_product_1", Collections.singleton("ds_${0..1}.t_order${0..1}"), keyGenerator, null);
        result.setTableStrategySegment(new ShardingStrategySegment("hint", null, new AlgorithmSegment("CORE.HINT.FIXTURE", props)));
        result.setDatabaseStrategySegment(new ShardingStrategySegment("hint", null, new AlgorithmSegment("CORE.HINT.FIXTURE", props)));
        return result;
    }
    
    private TableRuleSegment createWrongTableRuleWithNoneTypeStrategy() {
        Properties props = new Properties();
        KeyGenerateStrategySegment keyGenerator = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", props));
        TableRuleSegment result = new TableRuleSegment("t_product_1", Collections.singleton("ds_${0..1}.t_order${0..1}"), keyGenerator, null);
        result.setDatabaseStrategySegment(new ShardingStrategySegment("none", null, null));
        result.setTableStrategySegment(new ShardingStrategySegment("none", null, null));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRuleWithNoneTypeStrategy() {
        Properties props = PropertiesBuilder.build(new Property("algorithm-expression", "t_order_${order_id % 2}"));
        KeyGenerateStrategySegment keyGenerator = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", props));
        TableRuleSegment result = new TableRuleSegment("t_product_1", Collections.singleton("ds_0.t_order${0..1}"), keyGenerator, null);
        result.setDatabaseStrategySegment(new ShardingStrategySegment("none", null, null));
        result.setTableStrategySegment(new ShardingStrategySegment("standard", "order_id", new AlgorithmSegment("inline", props)));
        return result;
    }
}
