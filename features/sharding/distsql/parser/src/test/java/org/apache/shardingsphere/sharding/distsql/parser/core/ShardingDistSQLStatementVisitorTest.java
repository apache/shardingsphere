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

package org.apache.shardingsphere.sharding.distsql.parser.core;

import lombok.SneakyThrows;
import org.apache.shardingsphere.sharding.distsql.parser.facade.ShardingDistSQLParserFacade;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ColumnKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.SequenceKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingKeyGenerateStrategiesStatement;
import org.apache.shardingsphere.sharding.distsql.statement.ShowShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.SQLParserFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingDistSQLStatementVisitorTest {
    
    @Test
    void assertCreateColumnStrategyWithAlgorithm() {
        CreateShardingKeyGenerateStrategyStatement actual = (CreateShardingKeyGenerateStrategyStatement) parse(
                "CREATE SHARDING KEY GENERATE STRATEGY order_id_strategy (TABLE=t_order, COLUMN=order_id, TYPE(NAME='snowflake', PROPERTIES('worker-id'=1)))");
        assertTrue(actual.getKeyGenerateStrategySegment() instanceof ColumnKeyGenerateStrategyDefinitionSegment);
        ColumnKeyGenerateStrategyDefinitionSegment actualSegment = (ColumnKeyGenerateStrategyDefinitionSegment) actual.getKeyGenerateStrategySegment();
        assertThat(actual.getName(), is("order_id_strategy"));
        assertThat(actualSegment.getTableName(), is("t_order"));
        assertThat(actualSegment.getColumnName(), is("order_id"));
        assertTrue(!actualSegment.getKeyGeneratorName().isPresent());
        assertThat(actualSegment.getAlgorithmSegment().get().getName(), is("snowflake"));
        assertThat(actualSegment.getAlgorithmSegment().get().getProps().getProperty("worker-id"), is("1"));
    }
    
    @Test
    void assertCreateKeyGenerator() {
        CreateShardingKeyGeneratorStatement actual = (CreateShardingKeyGeneratorStatement) parse(
                "CREATE SHARDING KEY GENERATOR snowflake_generator (TYPE(NAME='SNOWFLAKE', PROPERTIES('worker-id'=1)))");
        assertThat(actual.getName(), is("snowflake_generator"));
        assertThat(actual.getAlgorithmSegment().getName(), is("SNOWFLAKE"));
        assertThat(actual.getAlgorithmSegment().getProps().getProperty("worker-id"), is("1"));
    }
    
    @Test
    void assertCreateColumnStrategyWithGenerator() {
        CreateShardingKeyGenerateStrategyStatement actual = (CreateShardingKeyGenerateStrategyStatement) parse(
                "CREATE SHARDING KEY GENERATE STRATEGY order_id_strategy (TABLE=t_order, COLUMN=order_id, GENERATOR=snowflake_generator)");
        assertTrue(actual.getKeyGenerateStrategySegment() instanceof ColumnKeyGenerateStrategyDefinitionSegment);
        ColumnKeyGenerateStrategyDefinitionSegment actualSegment = (ColumnKeyGenerateStrategyDefinitionSegment) actual.getKeyGenerateStrategySegment();
        assertThat(actual.getName(), is("order_id_strategy"));
        assertThat(actualSegment.getTableName(), is("t_order"));
        assertThat(actualSegment.getColumnName(), is("order_id"));
        assertThat(actualSegment.getKeyGeneratorName().get(), is("snowflake_generator"));
        assertTrue(!actualSegment.getAlgorithmSegment().isPresent());
    }
    
    @Test
    void assertAlterColumnStrategyWithGenerator() {
        AlterShardingKeyGenerateStrategyStatement actual = (AlterShardingKeyGenerateStrategyStatement) parse(
                "ALTER SHARDING KEY GENERATE STRATEGY order_id_strategy (TABLE=t_order, COLUMN=order_id, GENERATOR=snowflake_generator)");
        assertTrue(actual.getKeyGenerateStrategySegment() instanceof ColumnKeyGenerateStrategyDefinitionSegment);
        ColumnKeyGenerateStrategyDefinitionSegment actualSegment = (ColumnKeyGenerateStrategyDefinitionSegment) actual.getKeyGenerateStrategySegment();
        assertThat(actual.getName(), is("order_id_strategy"));
        assertThat(actualSegment.getTableName(), is("t_order"));
        assertThat(actualSegment.getColumnName(), is("order_id"));
        assertThat(actualSegment.getKeyGeneratorName().get(), is("snowflake_generator"));
        assertTrue(!actualSegment.getAlgorithmSegment().isPresent());
    }
    
    @Test
    void assertAlterKeyGenerator() {
        AlterShardingKeyGeneratorStatement actual = (AlterShardingKeyGeneratorStatement) parse(
                "ALTER SHARDING KEY GENERATOR snowflake_generator (TYPE(NAME='UUID'))");
        assertThat(actual.getName(), is("snowflake_generator"));
        assertThat(actual.getAlgorithmSegment().getName(), is("UUID"));
    }
    
    @Test
    void assertCreateSequenceStrategyWithAlgorithm() {
        CreateShardingKeyGenerateStrategyStatement actual = (CreateShardingKeyGenerateStrategyStatement) parse(
                "CREATE SHARDING KEY GENERATE STRATEGY order_sequence_strategy (SEQUENCE='order_seq', TYPE(NAME='redis_cluster_auto_increment', PROPERTIES('increment'=1)))");
        assertTrue(actual.getKeyGenerateStrategySegment() instanceof SequenceKeyGenerateStrategyDefinitionSegment);
        SequenceKeyGenerateStrategyDefinitionSegment actualSegment = (SequenceKeyGenerateStrategyDefinitionSegment) actual.getKeyGenerateStrategySegment();
        assertThat(actual.getName(), is("order_sequence_strategy"));
        assertThat(actualSegment.getSequenceName(), is("order_seq"));
        assertTrue(!actualSegment.getKeyGeneratorName().isPresent());
        assertThat(actualSegment.getAlgorithmSegment().get().getName(), is("redis_cluster_auto_increment"));
        assertThat(actualSegment.getAlgorithmSegment().get().getProps().getProperty("increment"), is("1"));
    }
    
    @Test
    void assertShowStrategies() {
        ShowShardingKeyGenerateStrategiesStatement actual = (ShowShardingKeyGenerateStrategiesStatement) parse(
                "SHOW SHARDING KEY GENERATE STRATEGIES FROM sharding_db");
        assertTrue(!actual.getName().isPresent());
        assertThat(actual.getFromDatabase().getDatabase().getIdentifier().getValue(), is("sharding_db"));
    }
    
    @Test
    void assertShowKeyGenerators() {
        ShowShardingKeyGeneratorsStatement actual = (ShowShardingKeyGeneratorsStatement) parse("SHOW SHARDING KEY GENERATORS FROM sharding_db");
        assertTrue(!actual.getName().isPresent());
        assertThat(actual.getFromDatabase().getDatabase().getIdentifier().getValue(), is("sharding_db"));
    }
    
    @Test
    void assertShowKeyGenerator() {
        ShowShardingKeyGeneratorsStatement actual = (ShowShardingKeyGeneratorsStatement) parse("SHOW SHARDING KEY GENERATOR snowflake_generator FROM sharding_db");
        assertThat(actual.getName().get(), is("snowflake_generator"));
        assertThat(actual.getFromDatabase().getDatabase().getIdentifier().getValue(), is("sharding_db"));
    }
    
    @Test
    void assertShowStrategy() {
        ShowShardingKeyGenerateStrategiesStatement actual = (ShowShardingKeyGenerateStrategiesStatement) parse(
                "SHOW SHARDING KEY GENERATE STRATEGY order_id_strategy FROM sharding_db");
        assertThat(actual.getName().get(), is("order_id_strategy"));
        assertThat(actual.getFromDatabase().getDatabase().getIdentifier().getValue(), is("sharding_db"));
    }
    
    @Test
    void assertDropStrategy() {
        DropShardingKeyGenerateStrategyStatement actual = (DropShardingKeyGenerateStrategyStatement) parse(
                "DROP SHARDING KEY GENERATE STRATEGY IF EXISTS order_id_strategy, order_sequence_strategy");
        assertTrue(actual.isIfExists());
        assertThat(actual.getNames(), is(Arrays.asList("order_id_strategy", "order_sequence_strategy")));
    }
    
    @Test
    void assertDropKeyGenerator() {
        DropShardingKeyGeneratorStatement actual = (DropShardingKeyGeneratorStatement) parse("DROP SHARDING KEY GENERATOR IF EXISTS snowflake_generator, uuid_generator");
        assertTrue(actual.isIfExists());
        assertThat(actual.getNames(), is(Arrays.asList("snowflake_generator", "uuid_generator")));
    }
    
    @Test
    void assertCreateShardingTableRuleWithKeyGenerator() {
        CreateShardingTableRuleStatement actual = (CreateShardingTableRuleStatement) parse(
                "CREATE SHARDING TABLE RULE t_order(DATANODES('ds_${0..1}.t_order_${0..1}'), "
                        + "TABLE_STRATEGY(TYPE='standard', SHARDING_COLUMN=order_id, SHARDING_ALGORITHM(TYPE(NAME='inline', PROPERTIES('algorithm-expression'='t_order_${order_id % 2}')))), "
                        + "KEY_GENERATE_STRATEGY(COLUMN=order_id, GENERATOR=snowflake_generator))");
        TableRuleSegment actualSegment = (TableRuleSegment) actual.getRules().iterator().next();
        assertThat(actualSegment.getKeyGenerateStrategySegment().getKeyGenerateColumn(), is("order_id"));
        assertThat(actualSegment.getKeyGenerateStrategySegment().getKeyGeneratorName().get(), is("snowflake_generator"));
        assertTrue(!actualSegment.getKeyGenerateStrategySegment().getAlgorithmSegment().isPresent());
    }
    
    @Test
    void assertAlterShardingAutoTableRuleWithKeyGenerator() {
        AlterShardingTableRuleStatement actual = (AlterShardingTableRuleStatement) parse(
                "ALTER SHARDING TABLE RULE t_order(STORAGE_UNITS(ds_0), SHARDING_COLUMN=order_id, "
                        + "TYPE(NAME='hash_mod', PROPERTIES('sharding-count'='2')), "
                        + "KEY_GENERATE_STRATEGY(COLUMN=order_id, GENERATOR=snowflake_generator))");
        AutoTableRuleSegment actualSegment = (AutoTableRuleSegment) actual.getRules().iterator().next();
        assertThat(actualSegment.getKeyGenerateStrategySegment().getKeyGenerateColumn(), is("order_id"));
        assertThat(actualSegment.getKeyGenerateStrategySegment().getKeyGeneratorName().get(), is("snowflake_generator"));
        assertTrue(!actualSegment.getKeyGenerateStrategySegment().getAlgorithmSegment().isPresent());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ASTNode parse(final String sql) {
        ShardingDistSQLParserFacade facade = new ShardingDistSQLParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        SQLVisitor<ASTNode> visitor = facade.getVisitorClass().getDeclaredConstructor().newInstance();
        return visitor.visit(parseASTNode.getRootNode());
    }
}
