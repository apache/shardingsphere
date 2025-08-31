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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.facade.ShardingDistSQLParserFacade;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.exception.metadata.DuplicateShardingActualDataNodeException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.checker.ShardingRuleChecker;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.SQLParserFactory;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AlterShardingTableRuleExecutorTest {
    
    private final AlterShardingTableRuleExecutor executor = new AlterShardingTableRuleExecutor();
    
    private final ShardingRuleConfiguration currentRuleConfig = createCurrentShardingRuleConfiguration();
    
    private final ResourceMetaData resourceMetaData = new ResourceMetaData(createDataSource());
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("schema");
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        executor.setDatabase(database);
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        ShardingRuleChecker checker = new ShardingRuleChecker(rule);
        when(rule.getShardingRuleChecker()).thenReturn(checker);
        executor.setRule(rule);
    }
    
    @Test
    void assertCheckWithDuplicateDataNodes() {
        String sql = "ALTER SHARDING TABLE RULE t_order("
                + "DATANODES('ds_${0..1}.t_order'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='ds_${user_id % 2}'))))"
                + "), t_order_item("
                + "DATANODES('ds_${0..1}.t_order'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='ds_${user_id % 2}'))))"
                + ");";
        AlterShardingTableRuleStatement sqlStatement = (AlterShardingTableRuleStatement) getDistSQLStatement(sql);
        assertThrows(DuplicateShardingActualDataNodeException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertUpdate() {
        AlterShardingTableRuleStatement sqlStatement = new AlterShardingTableRuleStatement(Arrays.asList(createCompleteAutoTableRule("t_order_item"), createCompleteTableRule("t_order")));
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(toBeAlteredRuleConfig.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = toBeAlteredRuleConfig.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order${0..1}"));
        assertThat(tableRule.getTableShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_table_core.standard.fixture"));
        assertThat(tableRule.getDatabaseShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_database_inline"));
        assertThat(toBeAlteredRuleConfig.getTables().size(), is(1));
        ShardingAutoTableRuleConfiguration autoTableRule = toBeAlteredRuleConfig.getAutoTables().iterator().next();
        assertThat(autoTableRule.getLogicTable(), is("t_order_item"));
        assertThat(autoTableRule.getActualDataSources(), is("ds_0,ds_1"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_item_foo.distsql.fixture"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_item_distsql.fixture"));
    }
    
    @Test
    void assertUpdateWithDifferentCase() {
        AlterShardingTableRuleStatement sqlStatement = new AlterShardingTableRuleStatement(Arrays.asList(createCompleteAutoTableRule("T_ORDER_ITEM"), createCompleteTableRule("T_ORDER")));
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(toBeAlteredRuleConfig.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = toBeAlteredRuleConfig.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("T_ORDER"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order${0..1}"));
        assertThat(tableRule.getTableShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_table_core.standard.fixture"));
        assertThat(tableRule.getDatabaseShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_database_inline"));
        assertThat(toBeAlteredRuleConfig.getTables().size(), is(1));
        ShardingAutoTableRuleConfiguration autoTableRule = toBeAlteredRuleConfig.getAutoTables().iterator().next();
        assertThat(autoTableRule.getLogicTable(), is("T_ORDER_ITEM"));
        assertThat(autoTableRule.getActualDataSources(), is("ds_0,ds_1"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_item_foo.distsql.fixture"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_item_distsql.fixture"));
    }
    
    @Test
    void assertUpdateTableType() {
        AlterShardingTableRuleStatement sqlStatement = new AlterShardingTableRuleStatement(Arrays.asList(createCompleteAutoTableRule("t_order"), createCompleteTableRule("t_order_item")));
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration toBeAlteredRuleConfig = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
        assertThat(toBeAlteredRuleConfig.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = toBeAlteredRuleConfig.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order_item"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order${0..1}"));
        assertThat(tableRule.getTableShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_item_table_core.standard.fixture"));
        assertThat(tableRule.getDatabaseShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_item_database_inline"));
        assertThat(toBeAlteredRuleConfig.getTables().size(), is(1));
        ShardingAutoTableRuleConfiguration autoTableRule = toBeAlteredRuleConfig.getAutoTables().iterator().next();
        assertThat(autoTableRule.getLogicTable(), is("t_order"));
        assertThat(autoTableRule.getActualDataSources(), is("ds_0,ds_1"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_foo.distsql.fixture"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_distsql.fixture"));
    }
    
    private AutoTableRuleSegment createCompleteAutoTableRule(final String logicTableName) {
        AutoTableRuleSegment result = new AutoTableRuleSegment(logicTableName, Arrays.asList("ds_0", "ds_1"));
        result.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())));
        result.setShardingColumn("order_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("FOO.DISTSQL.FIXTURE", PropertiesBuilder.build(new Property("", ""))));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule(final String logicTableName) {
        KeyGenerateStrategySegment keyGenerator = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment result = new TableRuleSegment(logicTableName, Collections.singleton("ds_${0..1}.t_order${0..1}"), keyGenerator, null);
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
    
    private Map<String, DataSource> createDataSource() {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private DistSQLStatement getDistSQLStatement(final String sql) {
        ShardingDistSQLParserFacade facade = new ShardingDistSQLParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        SQLVisitor visitor = facade.getVisitorClass().getDeclaredConstructor().newInstance();
        return (DistSQLStatement) visitor.visit(parseASTNode.getRootNode());
    }
}
