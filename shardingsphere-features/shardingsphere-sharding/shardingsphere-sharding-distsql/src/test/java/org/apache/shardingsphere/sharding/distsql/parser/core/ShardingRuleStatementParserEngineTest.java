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

import org.apache.shardingsphere.distsql.parser.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ShardingBindingTableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO use Parameterized + XML instead of static test
public final class ShardingRuleStatementParserEngineTest {
    
    private static final String RDL_CREATE_SHARDING_TABLE_RULE = "CREATE SHARDING TABLE RULE t_order (" 
            + "RESOURCES(ms_group_0,ms_group_1)," 
            + "SHARDING_COLUMN=order_id," 
            + "TYPE(NAME=hash_mod,PROPERTIES('sharding-count'=4))," 
            + "GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES(\"worker-id\"=123))))";
    
    private static final String RDL_CREATE_SHARDING_BINDING_TABLE_RULES = "CREATE SHARDING BINDING TABLE RULES (" 
            + "(t_order,t_order_item), (t_1,t_2))";
    
    private static final String RDL_CREATE_SHARDING_BROADCAST_TABLE_RULES = "CREATE SHARDING BROADCAST TABLE RULES(t_1,t_2)";
    
    private static final String RDL_ALTER_SHARDING_TABLE_RULE = "ALTER SHARDING TABLE RULE t_order ("
            + "RESOURCES(ms_group_0,ms_group_1),"
            + "SHARDING_COLUMN=order_id,"
            + "TYPE(NAME=hash_mod,PROPERTIES('sharding-count'=4)),"
            + "GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES(\"worker-id\"=123))))";
    
    private static final String RDL_ALTER_SHARDING_BINDING_TABLE_RULES = "ALTER SHARDING BINDING TABLE RULES ("
            + "(t_order,t_order_item), (t_1,t_2))";
    
    private static final String RDL_ALTER_SHARDING_BROADCAST_TABLE_RULES = "ALTER SHARDING BROADCAST TABLE RULES(t_1,t_2)";
    
    private static final String RDL_DROP_SHARDING_TABLE_RULE = "DROP SHARDING TABLE RULE t_order,t_order_item";
    
    private static final String RDL_DROP_SHARDING_BINDING_TABLE_RULES = "DROP SHARDING BINDING TABLE RULES";
    
    private static final String RDL_DROP_SHARDING_BROADCAST_TABLE_RULES = "DROP SHARDING BROADCAST TABLE RULES";
    
    private static final String RQL_SHOW_SHARDING_BINDING_TABLE_RULES = "SHOW SHARDING BINDING TABLE RULES FROM sharding_db";
    
    private static final String RQL_SHOW_SHARDING_BROADCAST_TABLE_RULES = "SHOW SHARDING BROADCAST TABLE RULES FROM sharding_db";
    
    private static final String RQL_SHOW_SHARDING_TABLE_RULES = "SHOW SHARDING TABLE RULES FROM schemaName";
    
    private static final String RQL_SHOW_SHARDING_TABLE_RULE = "SHOW SHARDING TABLE RULE t_order";
    
    private static final String RQL_SHOW_SHARDING_TABLE_RULE_FROM = "SHOW SHARDING TABLE RULE t_order FROM schemaName";
    
    private final DistSQLStatementParserEngine engine = new DistSQLStatementParserEngine();
    
    @Test
    public void assertParseCreateShardingTableRule() {
        SQLStatement sqlStatement = engine.parse(RDL_CREATE_SHARDING_TABLE_RULE);
        assertTrue(sqlStatement instanceof CreateShardingTableRuleStatement);
        assertThat(((CreateShardingTableRuleStatement) sqlStatement).getRules().size(), is(1));
        TableRuleSegment tableRuleSegment = ((CreateShardingTableRuleStatement) sqlStatement).getRules().iterator().next();
        assertThat(tableRuleSegment.getLogicTable(), is("t_order"));
        assertTrue(tableRuleSegment.getDataSources().containsAll(Arrays.asList("ms_group_0", "ms_group_1")));
        assertThat(tableRuleSegment.getTableStrategyColumn(), is("order_id"));
        assertThat(tableRuleSegment.getKeyGenerateStrategy().getAlgorithmName(), is("snowflake"));
        assertThat(tableRuleSegment.getKeyGenerateStrategy().getAlgorithmProps().getProperty("worker-id"), is("123"));
        assertThat(tableRuleSegment.getKeyGenerateStrategyColumn(), is("another_id"));
        assertThat(tableRuleSegment.getTableStrategy().getAlgorithmName(), is("hash_mod"));
        assertThat(tableRuleSegment.getTableStrategy().getAlgorithmProps().getProperty("sharding-count"), is("4"));
    }
    
    @Test
    public void assertParseCreateShardingBindingTableRules() {
        SQLStatement sqlStatement = engine.parse(RDL_CREATE_SHARDING_BINDING_TABLE_RULES);
        assertTrue(sqlStatement instanceof CreateShardingBindingTableRulesStatement);
        List<ShardingBindingTableRuleSegment> shardingBindingTableRuleSegments = new ArrayList<>(((CreateShardingBindingTableRulesStatement) sqlStatement).getRules());
        assertThat(shardingBindingTableRuleSegments.size(), is(2));
        ShardingBindingTableRuleSegment segment = shardingBindingTableRuleSegments.get(0);
        assertThat(segment.getTables(), is("t_order,t_order_item"));
        segment = shardingBindingTableRuleSegments.get(1);
        assertThat(segment.getTables(), is("t_1,t_2"));
    }
    
    @Test
    public void assertParseCreateShardingBroadcastTableRules() {
        SQLStatement sqlStatement = engine.parse(RDL_CREATE_SHARDING_BROADCAST_TABLE_RULES);
        System.out.println(sqlStatement);
        assertTrue(sqlStatement instanceof CreateShardingBroadcastTableRulesStatement);
        assertThat(((CreateShardingBroadcastTableRulesStatement) sqlStatement).getTables(), is(Arrays.asList("t_1", "t_2")));
    }
    
    @Test
    public void assertParseAlterShardingTableRule() {
        SQLStatement sqlStatement = engine.parse(RDL_ALTER_SHARDING_TABLE_RULE);
        assertTrue(sqlStatement instanceof AlterShardingTableRuleStatement);
        assertThat(((AlterShardingTableRuleStatement) sqlStatement).getRules().size(), is(1));
        TableRuleSegment tableRuleSegment = ((AlterShardingTableRuleStatement) sqlStatement).getRules().iterator().next();
        assertThat(tableRuleSegment.getLogicTable(), is("t_order"));
        assertTrue(tableRuleSegment.getDataSources().containsAll(Arrays.asList("ms_group_0", "ms_group_1")));
        assertThat(tableRuleSegment.getTableStrategyColumn(), is("order_id"));
        assertThat(tableRuleSegment.getKeyGenerateStrategy().getAlgorithmName(), is("snowflake"));
        assertThat(tableRuleSegment.getKeyGenerateStrategy().getAlgorithmProps().getProperty("worker-id"), is("123"));
        assertThat(tableRuleSegment.getKeyGenerateStrategyColumn(), is("another_id"));
        assertThat(tableRuleSegment.getTableStrategy().getAlgorithmName(), is("hash_mod"));
        assertThat(tableRuleSegment.getTableStrategy().getAlgorithmProps().getProperty("sharding-count"), is("4"));
    }
    
    @Test
    public void assertParseAlterShardingBindingTableRules() {
        SQLStatement sqlStatement = engine.parse(RDL_ALTER_SHARDING_BINDING_TABLE_RULES);
        assertTrue(sqlStatement instanceof AlterShardingBindingTableRulesStatement);
        List<ShardingBindingTableRuleSegment> shardingBindingTableRuleSegments = new ArrayList<>(((AlterShardingBindingTableRulesStatement) sqlStatement).getRules());
        assertThat(shardingBindingTableRuleSegments.size(), is(2));
        ShardingBindingTableRuleSegment segment = shardingBindingTableRuleSegments.get(0);
        assertThat(segment.getTables(), is("t_order,t_order_item"));
        segment = shardingBindingTableRuleSegments.get(1);
        assertThat(segment.getTables(), is("t_1,t_2"));
    }

    @Test
    public void assertParseAlterShardingBroadcastTableRules() {
        SQLStatement sqlStatement = engine.parse(RDL_ALTER_SHARDING_BROADCAST_TABLE_RULES);
        assertTrue(sqlStatement instanceof AlterShardingBroadcastTableRulesStatement);
        assertThat(((AlterShardingBroadcastTableRulesStatement) sqlStatement).getTables(), is(Arrays.asList("t_1", "t_2")));
    }
    
    @Test
    public void assertParseDropShardingTableRule() {
        SQLStatement sqlStatement = engine.parse(RDL_DROP_SHARDING_TABLE_RULE);
        assertTrue(sqlStatement instanceof DropShardingTableRuleStatement);
        assertThat(((DropShardingTableRuleStatement) sqlStatement).getTableNames().stream().map(each -> each.getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("t_order", "t_order_item")));
    }
    
    @Test
    public void assertParseDropShardingBindingTableRules() {
        SQLStatement sqlStatement = engine.parse(RDL_DROP_SHARDING_BINDING_TABLE_RULES);
        assertTrue(sqlStatement instanceof DropShardingBindingTableRulesStatement);
    }
    
    @Test
    public void assertParseDropShardingBroadcastTableRules() {
        SQLStatement sqlStatement = engine.parse(RDL_DROP_SHARDING_BROADCAST_TABLE_RULES);
        assertTrue(sqlStatement instanceof DropShardingBroadcastTableRulesStatement);
    }
    
    @Test
    public void assertParseShowShardingTableRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_TABLE_RULES);
        assertTrue(sqlStatement instanceof ShowShardingTableRulesStatement);
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("schemaName"));
    }
    
    @Test
    public void assertParseShowShardingTableRule() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_TABLE_RULE);
        assertTrue(sqlStatement instanceof ShowShardingTableRulesStatement);
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getTableName(), is("t_order"));
    }
    
    @Test
    public void assertParseShowShardingTableRuleFrom() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_TABLE_RULE_FROM);
        assertTrue(sqlStatement instanceof ShowShardingTableRulesStatement);
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getTableName(), is("t_order"));
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("schemaName"));
    }
    
    @Test
    public void assertParseShowShardingBindingTableRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_BINDING_TABLE_RULES);
        assertTrue(sqlStatement instanceof ShowShardingBindingTableRulesStatement);
        assertThat(((ShowShardingBindingTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("sharding_db"));
    }
    
    @Test
    public void assertParseShowShardingBroadcastTableRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_BROADCAST_TABLE_RULES);
        assertTrue(sqlStatement instanceof ShowShardingBroadcastTableRulesStatement);
        assertThat(((ShowShardingBroadcastTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("sharding_db"));
    }
}
