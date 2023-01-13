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

import lombok.SneakyThrows;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.distsql.handler.exception.DistSQLException;
import org.apache.shardingsphere.distsql.parser.engine.spi.FeaturedDistSQLStatementParserFacade;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.update.CreateShardingTableRuleStatementUpdater;
import org.apache.shardingsphere.sharding.distsql.parser.facade.ShardingDistSQLStatementParserFacade;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.SQLParserFactory;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingTableRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    private final ShardingRuleConfiguration currentRuleConfig = createCurrentShardingRuleConfiguration();
    
    private final ShardingSphereResourceMetaData resourceMetaData = new ShardingSphereResourceMetaData("sharding_db", createDataSource());
    
    private final CreateShardingTableRuleStatementUpdater updater = new CreateShardingTableRuleStatementUpdater();
    
    @Before
    public void before() {
        when(database.getName()).thenReturn("schema");
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        when(ruleMetaData.getRules()).thenReturn(Collections.singleton(new MockDataSourceContainedRule()));
    }
    
    @Test
    public void assertUpdate() {
        CreateShardingTableRuleStatement statement = new CreateShardingTableRuleStatement(false, Arrays.asList(createCompleteAutoTableRule(), createCompleteTableRule()));
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
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
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_input_table_core.standard.fixture"));
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
    
    @Test
    public void assertCheckCreateShardingStatement() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "STORAGE_UNITS(ds_0,ds_1),"
                + "SHARDING_COLUMN=order_id,"
                + "TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='6')),"
                + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake')))";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        updater.checkSQLStatement(database, distSQLStatement, null);
    }
    
    @Test(expected = DistSQLException.class)
    public void assertCheckCreateShardingStatementThrows() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "STORAGE_UNITS(ds_0,ds_1),"
                + "SHARDING_COLUMN=order_id,"
                + "TYPE(NAME='inline',PROPERTIES('algorithm-expression'='t_order_item_${order_id % 4}')),"
                + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake')))";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        updater.checkSQLStatement(database, distSQLStatement, null);
    }
    
    @Test
    public void assertUpdateWithIfNotExistsStatement() {
        CreateShardingTableRuleStatement statement = new CreateShardingTableRuleStatement(false, Arrays.asList(createCompleteAutoTableRule(), createCompleteTableRule()));
        updater.checkSQLStatement(database, statement, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfig = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfig);
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_order_item_input", Collections.singletonList("logic_ds"));
        autoTableRuleSegment.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("test_product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        autoTableRuleSegment.setShardingColumn("custom_id");
        autoTableRuleSegment.setShardingAlgorithmSegment(new AlgorithmSegment("FOO.DISTSQL.FIXTURE", PropertiesBuilder.build(new Property("", ""))));
        KeyGenerateStrategySegment keyGenerator = new KeyGenerateStrategySegment("order_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_order_input", Collections.singletonList("ds_${0..1}.t_order_${0..5}"), keyGenerator, null);
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "user_id", new AlgorithmSegment("CORE.STANDARD.FIXTURE", new Properties())));
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        tableRuleSegment.setDatabaseStrategySegment(new ShardingStrategySegment("standard", "order_id", databaseAlgorithmSegment));
        CreateShardingTableRuleStatement statementWithIfNotExists = new CreateShardingTableRuleStatement(true, Arrays.asList(autoTableRuleSegment, tableRuleSegment));
        updater.checkSQLStatement(database, statementWithIfNotExists, currentRuleConfig);
        ShardingRuleConfiguration toBeCreatedRuleConfigWithIfNotExists = updater.buildToBeCreatedRuleConfiguration(currentRuleConfig, statement);
        updater.updateCurrentRuleConfiguration(currentRuleConfig, toBeCreatedRuleConfigWithIfNotExists);
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
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_input_table_core.standard.fixture"));
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
        result.setShardingAlgorithmSegment(new AlgorithmSegment("FOO.DISTSQL.FIXTURE", PropertiesBuilder.build(new Property("", ""))));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule() {
        KeyGenerateStrategySegment keyGenerator = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment result = new TableRuleSegment("t_order_input", Collections.singletonList("ds_${0..1}.t_order_${0..1}"), keyGenerator, null);
        result.setTableStrategySegment(new ShardingStrategySegment("standard", "product_id", new AlgorithmSegment("CORE.STANDARD.FIXTURE", new Properties())));
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${user_id % 2}")));
        result.setDatabaseStrategySegment(new ShardingStrategySegment("standard", "product_id", databaseAlgorithmSegment));
        return result;
    }
    
    private ShardingRuleConfiguration createCurrentShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(createTableRuleConfiguration());
        result.getAutoTables().add(createAutoTableRuleConfiguration());
        result.getShardingAlgorithms().put("t_order_algorithm", new AlgorithmConfiguration("hash_mod", PropertiesBuilder.build(new Property("sharding-count", "4"))));
        result.getKeyGenerators().put("t_order_item_snowflake", new AlgorithmConfiguration("snowflake", new Properties()));
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
    
    private Map<String, DataSource> createDataSource() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private DistSQLStatement getDistSQLStatement(final String sql) {
        ShardingDistSQLStatementParserFacade facade = new ShardingDistSQLStatementParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        SQLVisitor visitor = TypedSPIRegistry.getRegisteredService(FeaturedDistSQLStatementParserFacade.class, facade.getType()).getVisitorClass().getDeclaredConstructor().newInstance();
        return (DistSQLStatement) ((ParseTreeVisitor) visitor).visit(parseASTNode.getRootNode());
    }
    
    private static class MockDataSourceContainedRule implements DataSourceContainedRule {
        
        @Override
        public RuleConfiguration getConfiguration() {
            return mock(RuleConfiguration.class);
        }
        
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
