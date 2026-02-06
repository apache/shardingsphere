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
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
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
import org.apache.shardingsphere.sharding.distsql.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.exception.metadata.DuplicateShardingActualDataNodeException;
import org.apache.shardingsphere.sharding.exception.strategy.InvalidShardingStrategyConfigurationException;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateShardingTableRuleExecutorTest {
    
    private final CreateShardingTableRuleExecutor executor = new CreateShardingTableRuleExecutor();
    
    private final ShardingRuleConfiguration currentRuleConfig = createCurrentShardingRuleConfiguration();
    
    @BeforeEach
    void setUp() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("schema");
        ResourceMetaData resourceMetaData = new ResourceMetaData(createDataSource());
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        DataSourceMapperRuleAttribute ruleAttribute = mock(DataSourceMapperRuleAttribute.class);
        when(ruleAttribute.getDataSourceMapper()).thenReturn(Collections.singletonMap("logic_ds", null));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        executor.setDatabase(database);
    }
    
    @Test
    void assertBuildToBeCreatedRuleConfiguration() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        ShardingRuleChecker checker = new ShardingRuleChecker(rule);
        when(rule.getShardingRuleChecker()).thenReturn(checker);
        executor.setRule(rule);
        CreateShardingTableRuleStatement sqlStatement = new CreateShardingTableRuleStatement(false, Arrays.asList(createCompleteAutoTableRule(), createCompleteTableRule()));
        executor.checkBeforeUpdate(sqlStatement);
        ShardingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(sqlStatement);
        assertThat(actual.getTables().size(), is(1));
        Iterator<ShardingTableRuleConfiguration> tableRuleIterator = actual.getTables().iterator();
        ShardingTableRuleConfiguration tableRule = tableRuleIterator.next();
        assertThat(tableRule.getTableShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getLogicTable(), is("t_order_input"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_input_table_core.standard.fixture"));
        assertThat(tableRule.getDatabaseShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_input_database_inline"));
        assertThat(actual.getTables().size(), is(1));
        Iterator<ShardingAutoTableRuleConfiguration> autoTableIterator = actual.getAutoTables().iterator();
        ShardingAutoTableRuleConfiguration autoTableRule = autoTableIterator.next();
        assertThat(autoTableRule.getLogicTable(), is("t_order_item_input"));
        assertThat(autoTableRule.getActualDataSources(), is("logic_ds"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_item_input_foo.distsql.fixture"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_item_input_distsql.fixture"));
    }
    
    @Test
    void assertCheckCreateShardingStatement() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "STORAGE_UNITS(ds_0,ds_1),"
                + "SHARDING_COLUMN=order_id,"
                + "TYPE(NAME='hash_mod',PROPERTIES('sharding-count'='6')),"
                + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake')))";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        executor.checkBeforeUpdate(distSQLStatement);
    }
    
    @Test
    void assertCheckCreateShardingStatementThrows() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "STORAGE_UNITS(ds_0,ds_1),"
                + "SHARDING_COLUMN=order_id,"
                + "TYPE(NAME='inline',PROPERTIES('algorithm-expression'='t_order_item_${order_id % 4}')),"
                + "KEY_GENERATE_STRATEGY(COLUMN=order_id,TYPE(NAME='snowflake')))";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        assertThrows(AlgorithmInitializationException.class, () -> executor.checkBeforeUpdate(distSQLStatement));
    }
    
    @Test
    void assertCheckCreateShardingStatementWithNoneDatabaseStrategy() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "DATANODES('ds_0.t_order_${0..1}'),"
                + "DATABASE_STRATEGY(TYPE='NONE'),"
                + "TABLE_STRATEGY(TYPE='standard',SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='t_order_${order_id % 2}'))))"
                + ");";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        executor.checkBeforeUpdate(distSQLStatement);
    }
    
    @Test
    void assertCheckCreateShardingStatementWithNoneDatabaseStrategyThrows() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "DATANODES('ds_${0..1}.t_order_${0..1}'),"
                + "DATABASE_STRATEGY(TYPE='NONE'),"
                + "TABLE_STRATEGY(TYPE='standard',SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='t_order_${order_id % 2}'))))"
                + ");";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        assertThrows(InvalidShardingStrategyConfigurationException.class, () -> executor.checkBeforeUpdate(distSQLStatement));
    }
    
    @Test
    void assertCheckCreateShardingStatementWithNoneTableStrategy() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "DATANODES('ds_${0..1}.t_order_0'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='ds_${user_id % 2}')))),"
                + "TABLE_STRATEGY(TYPE='NONE')"
                + ");";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        executor.checkBeforeUpdate(distSQLStatement);
    }
    
    @Test
    void assertCheckCreateShardingStatementWithNoneTableStrategyThrows() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "DATANODES('ds_${0..1}.t_order_${0..1}'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='ds_${user_id % 2}')))),"
                + "TABLE_STRATEGY(TYPE='NONE')"
                + ");";
        CreateShardingTableRuleStatement distSQLStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        assertThrows(InvalidShardingStrategyConfigurationException.class, () -> executor.checkBeforeUpdate(distSQLStatement));
    }
    
    @Test
    void assertCheckWithDuplicateDataNodes() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "DATANODES('ds_${0..1}.t_order'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='ds_${user_id % 2}'))))"
                + "), t_order_item("
                + "DATANODES('ds_${0..1}.t_order'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='ds_${user_id % 2}'))))"
                + ");";
        CreateShardingTableRuleStatement sqlStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        assertThrows(DuplicateShardingActualDataNodeException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckWithInvalidDataSourceInlineExpression() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "DATANODES('ds_${0..1}.t_order_${0..1}'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='foo_${user_id % 2}')))),"
                + "TABLE_STRATEGY(TYPE='standard',SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='t_order_${order_id % 2}'))))"
                + ");";
        CreateShardingTableRuleStatement sqlStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        assertThrows(AlgorithmInitializationException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertCheckWithInvalidTableInlineExpression() {
        String sql = "CREATE SHARDING TABLE RULE t_order("
                + "DATANODES('ds_${0..1}.t_order_${0..1}'),"
                + "DATABASE_STRATEGY(TYPE='standard',SHARDING_COLUMN=user_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='ds_${user_id % 2}')))),"
                + "TABLE_STRATEGY(TYPE='standard',SHARDING_COLUMN=order_id,SHARDING_ALGORITHM(TYPE(NAME='inline',PROPERTIES('algorithm-expression'='bar_${order_id % 2}'))))"
                + ");";
        CreateShardingTableRuleStatement sqlStatement = (CreateShardingTableRuleStatement) getDistSQLStatement(sql);
        assertThrows(AlgorithmInitializationException.class, () -> executor.checkBeforeUpdate(sqlStatement));
    }
    
    @Test
    void assertUpdateWithIfNotExistsStatement() {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(currentRuleConfig);
        ShardingRuleChecker checker = new ShardingRuleChecker(rule);
        when(rule.getShardingRuleChecker()).thenReturn(checker);
        executor.setRule(rule);
        Collection<AbstractTableRuleSegment> segments = new LinkedList<>();
        segments.add(createCompleteAutoTableRule());
        segments.add(createCompleteTableRule());
        CreateShardingTableRuleStatement statementWithIfNotExists = new CreateShardingTableRuleStatement(true, segments);
        executor.checkBeforeUpdate(statementWithIfNotExists);
        ShardingRuleConfiguration actual = executor.buildToBeCreatedRuleConfiguration(statementWithIfNotExists);
        assertThat(actual.getTables().size(), is(1));
        Iterator<ShardingTableRuleConfiguration> tableRuleIterator = actual.getTables().iterator();
        ShardingTableRuleConfiguration tableRule = tableRuleIterator.next();
        assertThat(tableRule.getTableShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getLogicTable(), is("t_order_input"));
        assertThat(tableRule.getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertThat(((StandardShardingStrategyConfiguration) tableRule.getTableShardingStrategy()).getShardingColumn(), is("product_id"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_input_table_core.standard.fixture"));
        assertThat(tableRule.getDatabaseShardingStrategy(), isA(StandardShardingStrategyConfiguration.class));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_input_database_inline"));
        assertThat(actual.getTables().size(), is(1));
        Iterator<ShardingAutoTableRuleConfiguration> autoTableIterator = actual.getAutoTables().iterator();
        ShardingAutoTableRuleConfiguration autoTableRule = autoTableIterator.next();
        assertThat(autoTableRule.getLogicTable(), is("t_order_item_input"));
        assertThat(autoTableRule.getActualDataSources(), is("logic_ds"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_item_input_foo.distsql.fixture"));
        assertThat(((StandardShardingStrategyConfiguration) autoTableRule.getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getColumn(), is("product_id"));
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_item_input_distsql.fixture"));
    }
    
    private AutoTableRuleSegment createCompleteAutoTableRule() {
        AutoTableRuleSegment result = new AutoTableRuleSegment("t_order_item_input", Collections.singleton("logic_ds"),
                new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties())), null);
        result.setShardingColumn("order_id");
        result.setShardingAlgorithmSegment(new AlgorithmSegment("FOO.DISTSQL.FIXTURE", PropertiesBuilder.build(new Property("", ""))));
        return result;
    }
    
    private TableRuleSegment createCompleteTableRule() {
        KeyGenerateStrategySegment keyGenerator = new KeyGenerateStrategySegment("product_id", new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TableRuleSegment result = new TableRuleSegment("t_order_input", Collections.singleton("ds_${0..1}.t_order_${0..1}"), keyGenerator, null);
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
