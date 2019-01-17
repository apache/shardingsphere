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
import org.apache.shardingsphere.api.algorithm.sharding.ListShardingValue;
import org.apache.shardingsphere.core.optimizer.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimizer.insert.InsertOptimizeEngine;
import org.apache.shardingsphere.core.optimizer.insert.InsertShardingCondition;
import org.apache.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.context.table.Table;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parsing.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.parsing.parser.token.TableToken;
import org.apache.shardingsphere.core.routing.router.sharding.GeneratedKey;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.sharding.YamlShardingConfiguration;
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

public final class InsertOptimizeEngineTest {
    
    private ShardingRule shardingRule;
    
    private InsertStatement insertStatementWithValuesWithPlaceHolder;
    
    private InsertStatement insertStatementWithValuesWithoutPlaceHolder;
    
    private InsertStatement insertStatementWithoutValuesWithPlaceHolder;
    
    private InsertStatement insertStatementWithoutValuesWithoutPlaceHolder;
    
    private List<Object> parametersWithValues;
    
    private List<Object> parametersWithoutValues;
    
    @Before
    public void setUp() throws IOException {
        URL url = InsertOptimizeEngineTest.class.getClassLoader().getResource("yaml/optimize-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlShardingConfiguration yamlShardingConfig = YamlShardingConfiguration.unmarshal(new File(url.getFile()));
        shardingRule = new ShardingRule(yamlShardingConfig.getShardingRule().getShardingRuleConfiguration(), yamlShardingConfig.getDataSources().keySet());
        initializeWithValuesWithPlaceHolder();
        initializeInsertWithValuesWithoutPlaceHolder();
        initializeInsertWithoutValuesWithoutPlaceHolder();
        initializeInsertWithoutValuesWithPlaceHolder();
        initializeParametersWithValues();
        initializeParametersWithoutValues();
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
        insertStatementWithValuesWithPlaceHolder.setInsertValuesListLastPosition(45);
        insertStatementWithValuesWithPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order"));
        insertStatementWithValuesWithPlaceHolder.addSQLToken(new InsertValuesToken(39, "t_order"));
        AndCondition andCondition1 = new AndCondition();
        andCondition1.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLPlaceholderExpression(0)));
        insertStatementWithValuesWithPlaceHolder.getConditions().getOrCondition().getAndConditions().add(andCondition1);
        AndCondition andCondition2 = new AndCondition();
        andCondition2.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLPlaceholderExpression(2)));
        insertStatementWithValuesWithPlaceHolder.getConditions().getOrCondition().getAndConditions().add(andCondition2);
        insertStatementWithValuesWithPlaceHolder.getInsertValues().getInsertValues().add(new InsertValue(DefaultKeyword.VALUES, "(?, ?)", 2));
        insertStatementWithValuesWithPlaceHolder.getInsertValues().getInsertValues().add(new InsertValue(DefaultKeyword.VALUES, "(?, ?)", 2));
    }
    
    private void initializeInsertWithValuesWithoutPlaceHolder() {
        insertStatementWithValuesWithoutPlaceHolder = new InsertStatement();
        insertStatementWithValuesWithoutPlaceHolder.getTables().add(new Table("t_order", Optional.<String>absent()));
        insertStatementWithValuesWithoutPlaceHolder.setParametersIndex(0);
        insertStatementWithValuesWithoutPlaceHolder.setInsertValuesListLastPosition(50);
        insertStatementWithValuesWithoutPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order"));
        insertStatementWithValuesWithoutPlaceHolder.addSQLToken(new InsertValuesToken(42, "t_order"));
        ItemsToken itemsToken = new ItemsToken(34);
        itemsToken.getItems().add("order_id");
        insertStatementWithValuesWithoutPlaceHolder.addSQLToken(itemsToken);
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertStatementWithValuesWithoutPlaceHolder.getConditions().getOrCondition().getAndConditions().add(andCondition);
        insertStatementWithValuesWithoutPlaceHolder.getInsertValues().getInsertValues().add(new InsertValue(DefaultKeyword.VALUES, "(12,'a')", 0));
    }
    
    private void initializeInsertWithoutValuesWithPlaceHolder() {
        insertStatementWithoutValuesWithPlaceHolder = new InsertStatement();
        insertStatementWithoutValuesWithPlaceHolder.getTables().add(new Table("t_order", Optional.<String>absent()));
        insertStatementWithoutValuesWithPlaceHolder.setParametersIndex(0);
        insertStatementWithoutValuesWithPlaceHolder.setInsertValuesListLastPosition(47);
        insertStatementWithoutValuesWithPlaceHolder.setColumnsListLastPosition(19);
        insertStatementWithoutValuesWithPlaceHolder.setGenerateKeyColumnIndex(-1);
        insertStatementWithoutValuesWithPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order"));
        insertStatementWithoutValuesWithPlaceHolder.addSQLToken(new InsertValuesToken(24, "t_order"));
        insertStatementWithoutValuesWithPlaceHolder.getColumns().add(new Column("order_id", "t_order"));
        insertStatementWithoutValuesWithPlaceHolder.getColumns().add(new Column("status", "t_order"));
        insertStatementWithoutValuesWithPlaceHolder.getColumns().add(new Column("user_id", "t_order"));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertStatementWithoutValuesWithPlaceHolder.getConditions().getOrCondition().getAndConditions().add(andCondition);
        insertStatementWithoutValuesWithPlaceHolder.getInsertValues().getInsertValues().add(new InsertValue(DefaultKeyword.SET, "user_id = ?, status = ?", 2));
    }
    
    private void initializeInsertWithoutValuesWithoutPlaceHolder() {
        insertStatementWithoutValuesWithoutPlaceHolder = new InsertStatement();
        insertStatementWithoutValuesWithoutPlaceHolder.getTables().add(new Table("t_order", Optional.<String>absent()));
        insertStatementWithoutValuesWithoutPlaceHolder.setParametersIndex(0);
        insertStatementWithoutValuesWithoutPlaceHolder.setInsertValuesListLastPosition(50);
        insertStatementWithoutValuesWithoutPlaceHolder.setColumnsListLastPosition(19);
        insertStatementWithoutValuesWithoutPlaceHolder.setGenerateKeyColumnIndex(-1);
        insertStatementWithoutValuesWithoutPlaceHolder.addSQLToken(new TableToken(12, 0, "t_order"));
        insertStatementWithoutValuesWithoutPlaceHolder.addSQLToken(new InsertValuesToken(24, "t_order"));
        insertStatementWithoutValuesWithoutPlaceHolder.getColumns().add(new Column("order_id", "t_order"));
        insertStatementWithoutValuesWithoutPlaceHolder.getColumns().add(new Column("status", "t_order"));
        insertStatementWithoutValuesWithoutPlaceHolder.getColumns().add(new Column("user_id", "t_order"));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new SQLNumberExpression(12)));
        insertStatementWithoutValuesWithoutPlaceHolder.getConditions().getOrCondition().getAndConditions().add(andCondition);
        insertStatementWithoutValuesWithoutPlaceHolder.getInsertValues().getInsertValues().add(new InsertValue(DefaultKeyword.SET, "user_id = 12, status = 'a'", 0));
    }
    
    @Test
    public void assertOptimizeWithValuesWithPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(2);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithPlaceHolder, parametersWithValues, generatedKey).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(2));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().size(), is(3));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getParameters().size(), is(3));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(0), CoreMatchers.<Object>is(10));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(2), CoreMatchers.<Object>is(1));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getParameters().get(0), CoreMatchers.<Object>is(11));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getParameters().get(2), CoreMatchers.<Object>is(2));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getInsertValueExpression(), is("(?, ?, ?)"));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getInsertValueExpression(), is("(?, ?, ?)"));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 1);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 10);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(1).getShardingValues().get(0), 2);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(1).getShardingValues().get(1), 11);
    }
    
    @Test
    public void assertOptimizeWithValuesWithPlaceHolderWithoutGeneratedKey() {
        insertStatementWithValuesWithPlaceHolder.setGenerateKeyColumnIndex(1);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithPlaceHolder, parametersWithValues, null).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(2));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().size(), is(2));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getParameters().size(), is(2));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(0), CoreMatchers.<Object>is(10));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getParameters().get(0), CoreMatchers.<Object>is(11));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getParameters().get(1), CoreMatchers.<Object>is("init"));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getInsertValueExpression(), is("(?, ?)"));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(1)).getInsertValueExpression(), is("(?, ?)"));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(1));
        assertThat(actual.getShardingConditions().get(1).getShardingValues().size(), is(1));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(1).getShardingValues().get(0), 11);
    }
    
    @Test
    public void assertOptimizeWithValuesWithoutPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithValuesWithoutPlaceHolder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().size(), is(0));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getInsertValueExpression(), is("(12,'a', 1)"));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 1);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 12);
    }
    
    @Test
    public void assertOptimizeWithoutValuesWithPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithoutValuesWithPlaceHolder, parametersWithoutValues, generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().size(), is(3));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(0), CoreMatchers.<Object>is(1));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(1), CoreMatchers.<Object>is(12));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().get(2), CoreMatchers.<Object>is("a"));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getInsertValueExpression(), is("order_id = ?, user_id = ?, status = ?"));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 1);
    }
    
    @Test
    public void assertOptimizeWithoutValuesWithoutPlaceHolderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey(new Column("order_id", "t_order"));
        generatedKey.getGeneratedKeys().add(1);
        ShardingConditions actual = new InsertOptimizeEngine(shardingRule, insertStatementWithoutValuesWithoutPlaceHolder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getParameters().size(), is(0));
        assertThat(((InsertShardingCondition) actual.getShardingConditions().get(0)).getInsertValueExpression(), is("order_id = 1, user_id = 12, status = 'a'"));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 1);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 12);
    }
    
    private void assertShardingValue(final ListShardingValue actual, final int expected) {
        assertThat(actual.getValues().size(), is(1));
        assertThat((int) actual.getValues().iterator().next(), is(expected));
    }
}
