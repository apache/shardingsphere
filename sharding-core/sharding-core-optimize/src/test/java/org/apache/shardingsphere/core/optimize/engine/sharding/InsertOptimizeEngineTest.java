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

package org.apache.shardingsphere.core.optimize.engine.sharding;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.optimize.GeneratedKey;
import org.apache.shardingsphere.core.optimize.engine.sharding.insert.InsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.InsertSetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.ItemsToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.TableToken;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class InsertOptimizeEngineTest {
    
    private ShardingRule shardingRule;
    
    private InsertStatement insertValuesStatementWithPlaceHolder;
    
    private InsertStatement insertValuesStatementWithoutPlaceHolder;
    
    private InsertStatement insertSetStatementWithPlaceHolder;
    
    private InsertStatement insertSetStatementWithoutPlaceHolder;
    
    private InsertStatement insertValuesStatementWithPlaceHolderWithEncrypt;
    
    private InsertStatement insertSetStatementWithoutPlaceHolderWithEncrypt;
    
    private InsertStatement insertSetStatementWithPlaceHolderWithQueryEncrypt;
    
    private InsertStatement insertValuesStatementWithoutPlaceHolderWithQueryEncrypt;
    
    private List<Object> insertValuesParameters;
    
    private List<Object> insertSetParameters;
    
    @Before
    public void setUp() throws IOException {
        URL url = InsertOptimizeEngineTest.class.getClassLoader().getResource("yaml/optimize-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
        initializeInsertValuesWithPlaceHolder();
        initializeInsertValuesWithoutPlaceHolder();
        initializeInsertSetWithoutPlaceHolder();
        initializeInsertSetWithPlaceHolder();
        initializeInsertValuesParameters();
        initializeInsertSetParameters();
        initializeInsertValuesWithPlaceHolderWithEncrypt();
        initializeInsertSetWithoutPlaceHolderWithEncrypt();
        initializeInsertSetWithPlaceHolderWithQueryEncrypt();
        initializeInsertValuesWithoutPlaceHolderWithQueryEncrypt();
    }
    
    private void initializeInsertValuesParameters() {
        insertValuesParameters = new ArrayList<>(4);
        insertValuesParameters.add(10);
        insertValuesParameters.add("init");
        insertValuesParameters.add(11);
        insertValuesParameters.add("init");
    }
    
    private void initializeInsertSetParameters() {
        insertSetParameters = new ArrayList<>(2);
        insertSetParameters.add(12);
        insertSetParameters.add("a");
    }
    
    private void initializeInsertValuesWithPlaceHolder() {
        insertValuesStatementWithPlaceHolder = new InsertStatement();
        insertValuesStatementWithPlaceHolder.getTables().add(new Table("t_order", null));
        insertValuesStatementWithPlaceHolder.setParametersIndex(4);
        insertValuesStatementWithPlaceHolder.addSQLToken(new TableToken(12, "t_order", QuoteCharacter.NONE, 0));
        insertValuesStatementWithPlaceHolder.addSQLToken(new InsertValuesToken(39));
        AndCondition andCondition1 = new AndCondition();
        andCondition1.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLPlaceholderExpression(0)));
        insertValuesStatementWithPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition1);
        AndCondition andCondition2 = new AndCondition();
        andCondition2.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLPlaceholderExpression(2)));
        insertValuesStatementWithPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition2);
        insertValuesStatementWithPlaceHolder.getColumnNames().add("user_id");
        insertValuesStatementWithPlaceHolder.getColumnNames().add("status");
        InsertValue insertValue = new InsertValue(Arrays.<SQLExpression>asList(new SQLPlaceholderExpression(0), new SQLPlaceholderExpression(1)));
        insertValuesStatementWithPlaceHolder.getValues().add(insertValue);
        insertValuesStatementWithPlaceHolder.getValues().add(insertValue);
    }
    
    private void initializeInsertValuesWithPlaceHolderWithEncrypt() {
        insertValuesStatementWithPlaceHolderWithEncrypt = new InsertStatement();
        insertValuesStatementWithPlaceHolderWithEncrypt.getTables().add(new Table("t_encrypt", null));
        insertValuesStatementWithPlaceHolderWithEncrypt.setParametersIndex(4);
        insertValuesStatementWithPlaceHolderWithEncrypt.addSQLToken(new TableToken(12, "t_encrypt", QuoteCharacter.NONE, 0));
        insertValuesStatementWithPlaceHolderWithEncrypt.addSQLToken(new InsertValuesToken(39));
        AndCondition andCondition1 = new AndCondition();
        andCondition1.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new SQLPlaceholderExpression(0)));
        insertValuesStatementWithPlaceHolderWithEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition1);
        AndCondition andCondition2 = new AndCondition();
        andCondition2.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new SQLPlaceholderExpression(2)));
        insertValuesStatementWithPlaceHolderWithEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition2);
        insertValuesStatementWithPlaceHolderWithEncrypt.getColumnNames().add("user_id");
        insertValuesStatementWithPlaceHolderWithEncrypt.getColumnNames().add("status");
        InsertValue insertValue = new InsertValue(Arrays.<SQLExpression>asList(new SQLPlaceholderExpression(0), new SQLPlaceholderExpression(1)));
        insertValuesStatementWithPlaceHolderWithEncrypt.getValues().add(insertValue);
        insertValuesStatementWithPlaceHolderWithEncrypt.getValues().add(insertValue);
    }
    
    private void initializeInsertValuesWithoutPlaceHolder() {
        insertValuesStatementWithoutPlaceHolder = new InsertStatement();
        insertValuesStatementWithoutPlaceHolder.getTables().add(new Table("t_order", null));
        insertValuesStatementWithoutPlaceHolder.setParametersIndex(0);
        insertValuesStatementWithoutPlaceHolder.addSQLToken(new TableToken(12, "t_order", QuoteCharacter.NONE, 0));
        insertValuesStatementWithoutPlaceHolder.addSQLToken(new InsertValuesToken(42));
        ItemsToken itemsToken = new ItemsToken(34);
        itemsToken.getItems().add("order_id");
        insertValuesStatementWithoutPlaceHolder.addSQLToken(itemsToken);
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertValuesStatementWithoutPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertValuesWithoutPlaceHolderWithQueryEncrypt() {
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt = new InsertStatement();
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", null));
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.setParametersIndex(0);
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.addSQLToken(new TableToken(12, "t_encrypt_query", QuoteCharacter.NONE, 0));
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.addSQLToken(new InsertValuesToken(42));
        ItemsToken itemsToken = new ItemsToken(34);
        itemsToken.getItems().add("order_id");
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.addSQLToken(itemsToken);
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt_query"), new SQLNumberExpression(12)));
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithPlaceHolder() {
        insertSetStatementWithPlaceHolder = new InsertStatement();
        insertSetStatementWithPlaceHolder.getTables().add(new Table("t_order", null));
        insertSetStatementWithPlaceHolder.setParametersIndex(0);
        insertSetStatementWithPlaceHolder.addSQLToken(new TableToken(12, "t_order", QuoteCharacter.NONE, 0));
        insertSetStatementWithPlaceHolder.addSQLToken(new InsertSetToken(24));
        insertSetStatementWithPlaceHolder.getColumnNames().add("user_id");
        insertSetStatementWithPlaceHolder.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertSetStatementWithPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithPlaceHolderWithQueryEncrypt() {
        insertSetStatementWithPlaceHolderWithQueryEncrypt = new InsertStatement();
        insertSetStatementWithPlaceHolderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", null));
        insertSetStatementWithPlaceHolderWithQueryEncrypt.setParametersIndex(0);
        insertSetStatementWithPlaceHolderWithQueryEncrypt.addSQLToken(new TableToken(12, "t_encrypt_query", QuoteCharacter.NONE, 0));
        insertSetStatementWithPlaceHolderWithQueryEncrypt.addSQLToken(new InsertSetToken(24));
        insertSetStatementWithPlaceHolderWithQueryEncrypt.getColumnNames().add("user_id");
        insertSetStatementWithPlaceHolderWithQueryEncrypt.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt_query"), new SQLNumberExpression(12)));
        insertSetStatementWithPlaceHolderWithQueryEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithoutPlaceHolder() {
        insertSetStatementWithoutPlaceHolder = new InsertStatement();
        insertSetStatementWithoutPlaceHolder.getTables().add(new Table("t_order", null));
        insertSetStatementWithoutPlaceHolder.setParametersIndex(0);
        insertSetStatementWithoutPlaceHolder.addSQLToken(new TableToken(12, "t_order", QuoteCharacter.NONE, 0));
        insertSetStatementWithoutPlaceHolder.addSQLToken(new InsertSetToken(24));
        insertSetStatementWithoutPlaceHolder.getColumnNames().add("user_id");
        insertSetStatementWithoutPlaceHolder.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertSetStatementWithoutPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithoutPlaceHolderWithEncrypt() {
        insertSetStatementWithoutPlaceHolderWithEncrypt = new InsertStatement();
        insertSetStatementWithoutPlaceHolderWithEncrypt.getTables().add(new Table("t_encrypt", null));
        insertSetStatementWithoutPlaceHolderWithEncrypt.setParametersIndex(0);
        insertSetStatementWithoutPlaceHolderWithEncrypt.addSQLToken(new TableToken(12, "t_encrypt", QuoteCharacter.NONE, 0));
        insertSetStatementWithoutPlaceHolderWithEncrypt.addSQLToken(new InsertSetToken(24));
        insertSetStatementWithoutPlaceHolderWithEncrypt.getColumnNames().add("user_id");
        insertSetStatementWithoutPlaceHolderWithEncrypt.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new SQLNumberExpression(12)));
        insertSetStatementWithoutPlaceHolderWithEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(2);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithPlaceHolder, insertValuesParameters, generatedKey).optimize();
        assertFalse(actual.getShardingConditions().isAlwaysFalse());
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(2));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[2], CoreMatchers.<Object>is(2));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("(?, ?, ?)"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).toString(), is("(?, ?, ?)"));
        assertThat(actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(0), 11);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(1), 2);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceHolderWithGeneratedKeyWithEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(2);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithPlaceHolderWithEncrypt, insertValuesParameters, generatedKey).optimize();
        assertFalse(actual.getShardingConditions().isAlwaysFalse());
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(2));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[2], CoreMatchers.<Object>is(2));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("(?, ?, ?)"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).toString(), is("(?, ?, ?)"));
        assertThat(actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(0), 11);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(1), 2);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceHolderWithoutGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(1);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithPlaceHolder, insertValuesParameters, generatedKey).optimize();
        assertFalse(actual.getShardingConditions().isAlwaysFalse());
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(2));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("(?, ?, ?)"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).toString(), is("(?, ?, ?)"));
        assertThat(actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(0), 11);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceHolderWithGeneratedKeyWithQueryEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.getColumnNames().add("user_id");
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.getColumnNames().add("status");
        insertValuesStatementWithoutPlaceHolderWithQueryEncrypt.getValues().add(new InsertValue(Arrays.asList(new SQLNumberExpression(12), new SQLTextExpression("a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithoutPlaceHolderWithQueryEncrypt, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("(12, 'a', 1, 12)"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertValuesStatementWithoutPlaceHolder.getColumnNames().add("user_id");
        insertValuesStatementWithoutPlaceHolder.getColumnNames().add("status");
        insertValuesStatementWithoutPlaceHolder.getValues().add(new InsertValue(Arrays.asList(new SQLNumberExpression(12), new SQLTextExpression("a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithoutPlaceHolder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("(12, 'a', 1)"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertSetStatementWithPlaceHolder.getValues().add(new InsertValue(Arrays.<SQLExpression>asList(new SQLPlaceholderExpression(0), new SQLPlaceholderExpression(1))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithPlaceHolder, insertSetParameters, generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("user_id = ?, status = ?, order_id = ?"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceHolderWithGeneratedKeyWithQueryEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        InsertValue insertValue = new InsertValue(Arrays.<SQLExpression>asList(new SQLPlaceholderExpression(0), new SQLPlaceholderExpression(1)));
        insertSetStatementWithPlaceHolderWithQueryEncrypt.getValues().add(insertValue);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithPlaceHolderWithQueryEncrypt, insertSetParameters, generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(4));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[3], CoreMatchers.<Object>is(12));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), 
                is("user_id = ?, status = ?, order_id = ?, assisted_user_id = ?"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertSetStatementWithoutPlaceHolder.getValues().add(new InsertValue(Arrays.asList(new SQLNumberExpression(12), new SQLTextExpression("a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithoutPlaceHolder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("user_id = 12, status = 'a', order_id = 1"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceHolderWithGeneratedKeyWithEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertSetStatementWithoutPlaceHolderWithEncrypt.getValues().add(new InsertValue(Arrays.asList(new SQLNumberExpression(12), new SQLTextExpression("a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithoutPlaceHolderWithEncrypt, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).toString(), is("user_id = 12, status = 'a', order_id = 1"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    private void assertShardingValue(final ListRouteValue actual, final int expected) {
        assertThat(actual.getValues().size(), is(1));
        assertThat((int) actual.getValues().iterator().next(), is(expected));
    }
}
