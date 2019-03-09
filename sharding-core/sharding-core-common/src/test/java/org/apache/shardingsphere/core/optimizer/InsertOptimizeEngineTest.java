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

package org.apache.shardingsphere.core.optimizer;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.keygen.GeneratedKey;
import org.apache.shardingsphere.core.optimizer.result.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimizer.engine.sharding.insert.InsertOptimizeEngine;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.context.table.Table;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;
import org.apache.shardingsphere.core.routing.value.ListRouteValue;
import org.apache.shardingsphere.core.rule.ShardingRule;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class InsertOptimizeEngineTest {
    
    private ShardingRule shardingRule;
    
    private InsertStatement insertStatementWithValuesWithPlaceHolder;
    
    private InsertStatement insertStatementWithValuesWithoutPlaceHolder;
    
    private InsertStatement insertStatementWithoutValuesWithPlaceHolder;
    
    private InsertStatement insertStatementWithoutValuesWithoutPlaceHolder;
    
    private InsertStatement insertStatementWithValuesWithPlaceHolderWithEncrypt;
    
    private InsertStatement insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt;
    
    private InsertStatement insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt;
    
    private InsertStatement insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt;
    
    private List<Object> parametersWithValues;
    
    private List<Object> parametersWithoutValues;
    
    @Before
    public void setUp() throws IOException {
        URL url = InsertOptimizeEngineTest.class.getClassLoader().getResource("yaml/optimize-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
        initializeWithValuesWithPlaceHolder();
        initializeInsertWithValuesWithoutPlaceHolder();
        initializeInsertWithoutValuesWithoutPlaceHolder();
        initializeInsertWithoutValuesWithPlaceHolder();
        initializeParametersWithValues();
        initializeParametersWithoutValues();
        initializeWithValuesWithPlaceHolderWithEncrypt();
        initializeInsertWithoutValuesWithoutPlaceHolderWithEncrypt();
        initializeInsertWithoutValuesWithPlaceHolderWithQueryEncrypt();
        initializeInsertWithValuesWithoutPlaceHolderWithQueryEncrypt();
    }
    
    private void initializeParametersWithValues() {
        parametersWithValues = new ArrayList<>(4);
        parametersWithValues.add(10);
        parametersWithValues.add("init");
        parametersWithValues.add(11);
        parametersWithValues.add("init");
    }
    
    private void initializeParametersWithoutValues() {
        parametersWithoutValues = new ArrayList<>(2);
        parametersWithoutValues.add(12);
        parametersWithoutValues.add("a");
    }
    
    private void initializeWithValuesWithPlaceHolder() {
        insertStatementWithValuesWithPlaceHolder = new InsertStatement();
        insertStatementWithValuesWithPlaceHolder.getTables().add(new Table("t_order", Optional.<String>absent()));
        insertStatementWithValuesWithPlaceHolder.setParametersIndex(4);
        insertStatementWithValuesWithPlaceHolder.setInsertValuesListLastIndex(45);
        insertStatementWithValuesWithPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order", "", ""));
        insertStatementWithValuesWithPlaceHolder.addSQLToken(new InsertValuesToken(39, DefaultKeyword.VALUES));
        AndCondition andCondition1 = new AndCondition();
        andCondition1.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLPlaceholderExpression(0)));
        insertStatementWithValuesWithPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition1);
        AndCondition andCondition2 = new AndCondition();
        andCondition2.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLPlaceholderExpression(2)));
        insertStatementWithValuesWithPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition2);
        insertStatementWithValuesWithPlaceHolder.getColumns().add(new Column("user_id", "t_order"));
        insertStatementWithValuesWithPlaceHolder.getColumns().add(new Column("status", "t_order"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, "(?, ?)", 2);
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(0));
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(1));
        insertStatementWithValuesWithPlaceHolder.getInsertValues().getInsertValues().add(insertValue);
        insertStatementWithValuesWithPlaceHolder.getInsertValues().getInsertValues().add(insertValue);
    }
    
    private void initializeWithValuesWithPlaceHolderWithEncrypt() {
        insertStatementWithValuesWithPlaceHolderWithEncrypt = new InsertStatement();
        insertStatementWithValuesWithPlaceHolderWithEncrypt.getTables().add(new Table("t_encrypt", Optional.<String>absent()));
        insertStatementWithValuesWithPlaceHolderWithEncrypt.setParametersIndex(4);
        insertStatementWithValuesWithPlaceHolderWithEncrypt.setInsertValuesListLastIndex(45);
        insertStatementWithValuesWithPlaceHolderWithEncrypt.addSQLToken(new TableToken(12, 0, "t_encrypt", "", ""));
        insertStatementWithValuesWithPlaceHolderWithEncrypt.addSQLToken(new InsertValuesToken(39, DefaultKeyword.VALUES));
        AndCondition andCondition1 = new AndCondition();
        andCondition1.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new SQLPlaceholderExpression(0)));
        insertStatementWithValuesWithPlaceHolderWithEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition1);
        AndCondition andCondition2 = new AndCondition();
        andCondition2.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new SQLPlaceholderExpression(2)));
        insertStatementWithValuesWithPlaceHolderWithEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition2);
        insertStatementWithValuesWithPlaceHolderWithEncrypt.getColumns().add(new Column("user_id", "t_encrypt"));
        insertStatementWithValuesWithPlaceHolderWithEncrypt.getColumns().add(new Column("status", "t_encrypt"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, "(?, ?)", 2);
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(0));
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(1));
        insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValues().getInsertValues().add(insertValue);
        insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValues().getInsertValues().add(insertValue);
    }
    
    private void initializeInsertWithValuesWithoutPlaceHolder() {
        insertStatementWithValuesWithoutPlaceHolder = new InsertStatement();
        insertStatementWithValuesWithoutPlaceHolder.getTables().add(new Table("t_order", Optional.<String>absent()));
        insertStatementWithValuesWithoutPlaceHolder.setParametersIndex(0);
        insertStatementWithValuesWithoutPlaceHolder.setInsertValuesListLastIndex(50);
        insertStatementWithValuesWithoutPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order", "", ""));
        insertStatementWithValuesWithoutPlaceHolder.addSQLToken(new InsertValuesToken(42, DefaultKeyword.VALUES));
        ItemsToken itemsToken = new ItemsToken(34);
        itemsToken.getItems().add("order_id");
        insertStatementWithValuesWithoutPlaceHolder.addSQLToken(itemsToken);
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertStatementWithValuesWithoutPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertWithValuesWithoutPlaceHolderWithQueryEncrypt() {
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt = new InsertStatement();
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", Optional.<String>absent()));
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.setParametersIndex(0);
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.setInsertValuesListLastIndex(50);
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.addSQLToken(new TableToken(12, 0, "t_encrypt_query", "", ""));
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.addSQLToken(new InsertValuesToken(42, DefaultKeyword.VALUES));
        ItemsToken itemsToken = new ItemsToken(34);
        itemsToken.getItems().add("order_id");
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.addSQLToken(itemsToken);
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt_query"), new SQLNumberExpression(12)));
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertWithoutValuesWithPlaceHolder() {
        insertStatementWithoutValuesWithPlaceHolder = new InsertStatement();
        insertStatementWithoutValuesWithPlaceHolder.getTables().add(new Table("t_order", Optional.<String>absent()));
        insertStatementWithoutValuesWithPlaceHolder.setParametersIndex(0);
        insertStatementWithoutValuesWithPlaceHolder.setInsertValuesListLastIndex(47);
        insertStatementWithoutValuesWithPlaceHolder.setColumnsListLastIndex(19);
        insertStatementWithoutValuesWithPlaceHolder.setGenerateKeyColumnIndex(-1);
        insertStatementWithoutValuesWithPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order", "", ""));
        insertStatementWithoutValuesWithPlaceHolder.addSQLToken(new InsertValuesToken(24, DefaultKeyword.SET));
        insertStatementWithoutValuesWithPlaceHolder.getColumns().add(new Column("user_id", "t_order"));
        insertStatementWithoutValuesWithPlaceHolder.getColumns().add(new Column("status", "t_order"));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertStatementWithoutValuesWithPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertWithoutValuesWithPlaceHolderWithQueryEncrypt() {
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt = new InsertStatement();
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", Optional.<String>absent()));
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.setParametersIndex(0);
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.setInsertValuesListLastIndex(47);
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.setColumnsListLastIndex(19);
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.setGenerateKeyColumnIndex(-1);
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.addSQLToken(new TableToken(12, 0, "t_encrypt_query", "", ""));
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.addSQLToken(new InsertValuesToken(24, DefaultKeyword.SET));
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getColumns().add(new Column("user_id", "t_encrypt_query"));
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getColumns().add(new Column("status", "t_encrypt_query"));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt_query"), new SQLNumberExpression(12)));
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertWithoutValuesWithoutPlaceHolder() {
        insertStatementWithoutValuesWithoutPlaceHolder = new InsertStatement();
        insertStatementWithoutValuesWithoutPlaceHolder.getTables().add(new Table("t_order", Optional.<String>absent()));
        insertStatementWithoutValuesWithoutPlaceHolder.setParametersIndex(0);
        insertStatementWithoutValuesWithoutPlaceHolder.setInsertValuesListLastIndex(50);
        insertStatementWithoutValuesWithoutPlaceHolder.setColumnsListLastIndex(19);
        insertStatementWithoutValuesWithoutPlaceHolder.setGenerateKeyColumnIndex(-1);
        insertStatementWithoutValuesWithoutPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order", "", ""));
        insertStatementWithoutValuesWithoutPlaceHolder.addSQLToken(new InsertValuesToken(24, DefaultKeyword.SET));
        insertStatementWithoutValuesWithoutPlaceHolder.getColumns().add(new Column("user_id", "t_order"));
        insertStatementWithoutValuesWithoutPlaceHolder.getColumns().add(new Column("status", "t_order"));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertStatementWithoutValuesWithoutPlaceHolder.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    private void initializeInsertWithoutValuesWithoutPlaceHolderWithEncrypt() {
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt = new InsertStatement();
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.getTables().add(new Table("t_encrypt", Optional.<String>absent()));
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.setParametersIndex(0);
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.setInsertValuesListLastIndex(50);
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.setColumnsListLastIndex(19);
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.setGenerateKeyColumnIndex(-1);
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.addSQLToken(new TableToken(12, 0, "t_encrypt", "", ""));
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.addSQLToken(new InsertValuesToken(24, DefaultKeyword.SET));
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.getColumns().add(new Column("user_id", "t_encrypt"));
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.getColumns().add(new Column("status", "t_encrypt"));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new SQLNumberExpression(12)));
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.getRouteConditions().getOrCondition().getAndConditions().add(andCondition);
    }
    
    @Test
    public void assertOptimizeWithValuesWithPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(2);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithPlaceHolder, parametersWithValues, generatedKey).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(2));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(3));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).getParameters().size(), is(3));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(0), CoreMatchers.<Object>is(10));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(2), CoreMatchers.<Object>is(1));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).getParameters().get(0), CoreMatchers.<Object>is(11));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).getParameters().get(2), CoreMatchers.<Object>is(2));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).toString(), is("(?, ?, ?)"));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).toString(), is("(?, ?, ?)"));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(1).getShardingValues().get(0), 11);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(1).getShardingValues().get(1), 2);
        assertTrue(insertStatementWithValuesWithPlaceHolder.isContainGenerateKey());
    }
    
    @Test
    public void assertOptimizeWithValuesWithPlaceHolderWithGeneratedKeyWithEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_encrypt"));
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(2);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithPlaceHolderWithEncrypt, parametersWithValues, generatedKey).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(2));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(1).getParameters().size(), is(3));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().get(0), CoreMatchers.<Object>is(10));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().get(2), CoreMatchers.<Object>is("encryptValue"));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(1).getParameters().get(0), CoreMatchers.<Object>is(11));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(1).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(1).getParameters().get(2), CoreMatchers.<Object>is("encryptValue"));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(0).toString(), is("(?, ?, ?)"));
        assertThat(insertStatementWithValuesWithPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(1).toString(), is("(?, ?, ?)"));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(1).getShardingValues().get(0), 11);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(1).getShardingValues().get(1), 2);
        assertTrue(insertStatementWithValuesWithPlaceHolderWithEncrypt.isContainGenerateKey());
    }
    
    @Test
    public void assertOptimizeWithValuesWithPlaceHolderWithoutGeneratedKey() {
        insertStatementWithValuesWithPlaceHolder.setGenerateKeyColumnIndex(1);
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(1);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithPlaceHolder, parametersWithValues, generatedKey).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(2));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(3));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).getParameters().size(), is(3));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(0), CoreMatchers.<Object>is(10));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).getParameters().get(0), CoreMatchers.<Object>is(11));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).toString(), is("(?, ?, ?)"));
        assertThat(insertStatementWithValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(1).toString(), is("(?, ?, ?)"));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(1).getShardingValues().get(0), 11);
    }
    
    @Test
    public void assertOptimizeWithValuesWithoutPlaceHolderWithGeneratedKeyWithQueryEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_encrypt_query"));
        generatedKey.getGeneratedKeys().add(1);
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.getColumns().add(new Column("user_id", "t_encrypt_query"));
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.getColumns().add(new Column("status", "t_encrypt_query"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, "(12,'a')", 0);
        insertValue.getColumnValues().add(new SQLNumberExpression(12));
        insertValue.getColumnValues().add(new SQLTextExpression("a"));
        insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.getInsertValues().getInsertValues().add(insertValue);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(0));
        assertThat(insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).toString(), is("('encryptValue', 'a', 1, 'assistedEncryptValue')"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertTrue(insertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt.isContainGenerateKey());
    }
    
    @Test
    public void assertOptimizeWithValuesWithoutPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        insertStatementWithValuesWithoutPlaceHolder.getColumns().add(new Column("user_id", "t_order"));
        insertStatementWithValuesWithoutPlaceHolder.getColumns().add(new Column("status", "t_order"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, "(12,'a')", 0);
        insertValue.getColumnValues().add(new SQLNumberExpression(12));
        insertValue.getColumnValues().add(new SQLTextExpression("a"));
        insertStatementWithValuesWithoutPlaceHolder.getInsertValues().getInsertValues().add(insertValue);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithoutPlaceHolder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(insertStatementWithValuesWithoutPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(0));
        assertThat(insertStatementWithValuesWithoutPlaceHolder.getInsertValuesToken().getColumnValues().get(0).toString(), is("(12, 'a', 1)"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertTrue(insertStatementWithValuesWithoutPlaceHolder.isContainGenerateKey());
    }
    
    @Test
    public void assertOptimizeWithoutValuesWithPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        InsertValue insertValue = new InsertValue(DefaultKeyword.SET, "user_id = ?, status = ?", 2);
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(0));
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(1));
        insertStatementWithoutValuesWithPlaceHolder.getInsertValues().getInsertValues().add(insertValue);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithoutValuesWithPlaceHolder, parametersWithoutValues, generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(insertStatementWithoutValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(3));
        assertThat(insertStatementWithoutValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(0), CoreMatchers.<Object>is(12));
        assertThat(insertStatementWithoutValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(1), CoreMatchers.<Object>is("a"));
        assertThat(insertStatementWithoutValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().get(2), CoreMatchers.<Object>is(1));
        assertThat(insertStatementWithoutValuesWithPlaceHolder.getInsertValuesToken().getColumnValues().get(0).toString(), is("user_id = ?, status = ?, order_id = ?"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 12);
    }
    
    @Test
    public void assertOptimizeWithoutValuesWithPlaceHolderWithGeneratedKeyWithQueryEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_encrypt_query"));
        generatedKey.getGeneratedKeys().add(1);
        InsertValue insertValue = new InsertValue(DefaultKeyword.SET, "user_id = ?, status = ?", 2);
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(0));
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(1));
        insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getInsertValues().getInsertValues().add(insertValue);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt, parametersWithoutValues, generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(4));
        assertThat(insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().get(0), CoreMatchers.<Object>is("encryptValue"));
        assertThat(insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().get(1), CoreMatchers.<Object>is("a"));
        assertThat(insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().get(2), CoreMatchers.<Object>is(1));
        assertThat(insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().get(3), CoreMatchers.<Object>is("assistedEncryptValue"));
        assertThat(insertStatementWithoutValuesWithPlaceHolderWithQueryEncrypt.getInsertValuesToken().getColumnValues().get(0).toString(), 
                is("user_id = ?, status = ?, order_id = ?, assisted_user_id = ?"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 12);
    }
    
    @Test
    public void assertOptimizeWithoutValuesWithoutPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        InsertValue insertValue = new InsertValue(DefaultKeyword.SET, "user_id = 12, status = 'a'", 0);
        insertValue.getColumnValues().add(new SQLNumberExpression(12));
        insertValue.getColumnValues().add(new SQLTextExpression("a"));
        insertStatementWithoutValuesWithoutPlaceHolder.getInsertValues().getInsertValues().add(insertValue);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithoutValuesWithoutPlaceHolder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(insertStatementWithoutValuesWithoutPlaceHolder.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(0));
        assertThat(insertStatementWithoutValuesWithoutPlaceHolder.getInsertValuesToken().getColumnValues().get(0).toString(), is("user_id = 12, status = 'a', order_id = 1"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertTrue(insertStatementWithoutValuesWithoutPlaceHolder.isContainGenerateKey());
    }
    
    @Test
    public void assertOptimizeWithoutValuesWithoutPlaceHolderWithGeneratedKeyWithEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_encrypt"));
        generatedKey.getGeneratedKeys().add(1);
        InsertValue insertValue = new InsertValue(DefaultKeyword.SET, "user_id = 12, status = 'a'", 0);
        insertValue.getColumnValues().add(new SQLNumberExpression(12));
        insertValue.getColumnValues().add(new SQLTextExpression("a"));
        insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.getInsertValues().getInsertValues().add(insertValue);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(0).getParameters().size(), is(0));
        assertThat(insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.getInsertValuesToken().getColumnValues().get(0).toString(), is("user_id = 12, status = 'a', order_id = 'encryptValue'"));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertTrue(insertStatementWithoutValuesWithoutPlaceHolderWithEncrypt.isContainGenerateKey());
    }
    
    private void assertShardingValue(final ListRouteValue actual, final int expected) {
        assertThat(actual.getValues().size(), is(1));
        assertThat((int) actual.getValues().iterator().next(), is(expected));
    }
}
