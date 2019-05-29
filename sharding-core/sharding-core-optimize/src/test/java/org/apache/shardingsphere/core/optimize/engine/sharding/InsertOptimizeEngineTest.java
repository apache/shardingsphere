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
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
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
    
    private InsertStatement insertValuesStatementWithPlaceholder;
    
    private InsertStatement insertValuesStatementWithoutPlaceholder;
    
    private InsertStatement insertSetStatementWithPlaceholder;
    
    private InsertStatement insertSetStatementWithoutPlaceholder;
    
    private InsertStatement insertValuesStatementWithPlaceholderWithEncrypt;
    
    private InsertStatement insertSetStatementWithoutPlaceholderWithEncrypt;
    
    private InsertStatement insertSetStatementWithPlaceholderWithQueryEncrypt;
    
    private InsertStatement insertValuesStatementWithoutPlaceholderWithQueryEncrypt;
    
    private List<Object> insertValuesParameters;
    
    private List<Object> insertSetParameters;
    
    @Before
    public void setUp() throws IOException {
        URL url = InsertOptimizeEngineTest.class.getClassLoader().getResource("yaml/optimize-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
        initializeInsertValuesWithPlaceholder();
        initializeInsertValuesWithoutPlaceholder();
        initializeInsertSetWithoutPlaceholder();
        initializeInsertSetWithPlaceholder();
        initializeInsertValuesParameters();
        initializeInsertSetParameters();
        initializeInsertValuesWithPlaceholderWithEncrypt();
        initializeInsertSetWithoutPlaceholderWithEncrypt();
        initializeInsertSetWithPlaceholderWithQueryEncrypt();
        initializeInsertValuesWithoutPlaceholderWithQueryEncrypt();
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
    
    private void initializeInsertValuesWithPlaceholder() {
        insertValuesStatementWithPlaceholder = new InsertStatement();
        insertValuesStatementWithPlaceholder.getTables().add(new Table("t_order", null));
        insertValuesStatementWithPlaceholder.setParametersIndex(4);
        AndCondition andCondition1 = new AndCondition();
        andCondition1.getConditions().add(new Condition(new Column("user_id", "t_order"), new ParameterMarkerExpressionSegment(0, 0, 0)));
        insertValuesStatementWithPlaceholder.getRouteCondition().getOrConditions().add(andCondition1);
        AndCondition andCondition2 = new AndCondition();
        andCondition2.getConditions().add(new Condition(new Column("user_id", "t_order"), new ParameterMarkerExpressionSegment(0, 0, 2)));
        insertValuesStatementWithPlaceholder.getRouteCondition().getOrConditions().add(andCondition2);
        insertValuesStatementWithPlaceholder.getColumnNames().add("user_id");
        insertValuesStatementWithPlaceholder.getColumnNames().add("status");
        InsertValue insertValue = new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertValuesStatementWithPlaceholder.getValues().add(insertValue);
        insertValuesStatementWithPlaceholder.getValues().add(insertValue);
    }
    
    private void initializeInsertValuesWithPlaceholderWithEncrypt() {
        insertValuesStatementWithPlaceholderWithEncrypt = new InsertStatement();
        insertValuesStatementWithPlaceholderWithEncrypt.getTables().add(new Table("t_encrypt", null));
        insertValuesStatementWithPlaceholderWithEncrypt.setParametersIndex(4);
        AndCondition andCondition1 = new AndCondition();
        andCondition1.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new ParameterMarkerExpressionSegment(0, 0, 0)));
        insertValuesStatementWithPlaceholderWithEncrypt.getRouteCondition().getOrConditions().add(andCondition1);
        AndCondition andCondition2 = new AndCondition();
        andCondition2.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new ParameterMarkerExpressionSegment(0, 0, 2)));
        insertValuesStatementWithPlaceholderWithEncrypt.getRouteCondition().getOrConditions().add(andCondition2);
        insertValuesStatementWithPlaceholderWithEncrypt.getColumnNames().add("user_id");
        insertValuesStatementWithPlaceholderWithEncrypt.getColumnNames().add("status");
        InsertValue insertValue = new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertValuesStatementWithPlaceholderWithEncrypt.getValues().add(insertValue);
        insertValuesStatementWithPlaceholderWithEncrypt.getValues().add(insertValue);
    }
    
    private void initializeInsertValuesWithoutPlaceholder() {
        insertValuesStatementWithoutPlaceholder = new InsertStatement();
        insertValuesStatementWithoutPlaceholder.getTables().add(new Table("t_order", null));
        insertValuesStatementWithoutPlaceholder.setParametersIndex(0);
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new LiteralExpressionSegment(0, 0, 12)));
        insertValuesStatementWithoutPlaceholder.getRouteCondition().getOrConditions().add(andCondition);
    }
    
    private void initializeInsertValuesWithoutPlaceholderWithQueryEncrypt() {
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt = new InsertStatement();
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", null));
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.setParametersIndex(0);
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt_query"), new LiteralExpressionSegment(0, 0, 12)));
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getRouteCondition().getOrConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithPlaceholder() {
        insertSetStatementWithPlaceholder = new InsertStatement();
        insertSetStatementWithPlaceholder.getTables().add(new Table("t_order", null));
        insertSetStatementWithPlaceholder.setParametersIndex(0);
        insertSetStatementWithPlaceholder.getColumnNames().add("user_id");
        insertSetStatementWithPlaceholder.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new LiteralExpressionSegment(0, 0, 12)));
        insertSetStatementWithPlaceholder.getRouteCondition().getOrConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithPlaceholderWithQueryEncrypt() {
        insertSetStatementWithPlaceholderWithQueryEncrypt = new InsertStatement();
        insertSetStatementWithPlaceholderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", null));
        insertSetStatementWithPlaceholderWithQueryEncrypt.setParametersIndex(0);
        insertSetStatementWithPlaceholderWithQueryEncrypt.getColumnNames().add("user_id");
        insertSetStatementWithPlaceholderWithQueryEncrypt.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt_query"), new LiteralExpressionSegment(0, 0, 12)));
        insertSetStatementWithPlaceholderWithQueryEncrypt.getRouteCondition().getOrConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithoutPlaceholder() {
        insertSetStatementWithoutPlaceholder = new InsertStatement();
        insertSetStatementWithoutPlaceholder.getTables().add(new Table("t_order", null));
        insertSetStatementWithoutPlaceholder.setParametersIndex(0);
        insertSetStatementWithoutPlaceholder.getColumnNames().add("user_id");
        insertSetStatementWithoutPlaceholder.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_order"), new LiteralExpressionSegment(0, 0, 12)));
        insertSetStatementWithoutPlaceholder.getRouteCondition().getOrConditions().add(andCondition);
    }
    
    private void initializeInsertSetWithoutPlaceholderWithEncrypt() {
        insertSetStatementWithoutPlaceholderWithEncrypt = new InsertStatement();
        insertSetStatementWithoutPlaceholderWithEncrypt.getTables().add(new Table("t_encrypt", null));
        insertSetStatementWithoutPlaceholderWithEncrypt.setParametersIndex(0);
        insertSetStatementWithoutPlaceholderWithEncrypt.getColumnNames().add("user_id");
        insertSetStatementWithoutPlaceholderWithEncrypt.getColumnNames().add("status");
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(new Condition(new Column("user_id", "t_encrypt"), new LiteralExpressionSegment(0, 0, 12)));
        insertSetStatementWithoutPlaceholderWithEncrypt.getRouteCondition().getOrConditions().add(andCondition);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceholderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(2);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithPlaceholder, insertValuesParameters, generatedKey).optimize();
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
        assertThat(actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(0), 11);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(1), 2);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceholderWithGeneratedKeyWithEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(2);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithPlaceholderWithEncrypt, insertValuesParameters, generatedKey).optimize();
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
        assertThat(actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(0), 11);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(1), 2);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceholderWithoutGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        generatedKey.getGeneratedKeys().add(1);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithPlaceholder, insertValuesParameters, generatedKey).optimize();
        assertFalse(actual.getShardingConditions().isAlwaysFalse());
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(2));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertThat(actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().size(), is(2));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 10);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(1).getShardingValues().get(0), 11);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceholderWithGeneratedKeyWithQueryEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getColumnNames().add("user_id");
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getColumnNames().add("status");
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getValues().add(
                new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithoutPlaceholderWithQueryEncrypt, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceholderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertValuesStatementWithoutPlaceholder.getColumnNames().add("user_id");
        insertValuesStatementWithoutPlaceholder.getColumnNames().add("status");
        insertValuesStatementWithoutPlaceholder.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertValuesStatementWithoutPlaceholder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceholderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertSetStatementWithPlaceholder.getValues().add(
                new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithPlaceholder, insertSetParameters, generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceholderWithGeneratedKeyWithQueryEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        InsertValue insertValue = new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertSetStatementWithPlaceholderWithQueryEncrypt.getValues().add(insertValue);
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithPlaceholderWithQueryEncrypt, insertSetParameters, generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(4));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[3], CoreMatchers.<Object>is(12));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceholderWithGeneratedKey() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertSetStatementWithoutPlaceholder.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithoutPlaceholder, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceholderWithGeneratedKeyWithEncrypt() {
        GeneratedKey generatedKey = new GeneratedKey("order_id");
        generatedKey.getGeneratedKeys().add(1);
        insertSetStatementWithoutPlaceholderWithEncrypt.getValues().add(
                new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        OptimizeResult actual = new InsertOptimizeEngine(shardingRule, insertSetStatementWithoutPlaceholderWithEncrypt, Collections.emptyList(), generatedKey).optimize();
        assertThat(actual.getShardingConditions().getShardingConditions().size(), is(1));
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(0), 12);
        assertShardingValue((ListRouteValue) actual.getShardingConditions().getShardingConditions().get(0).getShardingValues().get(1), 1);
    }
    
    private void assertShardingValue(final ListRouteValue actual, final int expected) {
        assertThat(actual.getValues().size(), is(1));
        assertThat((int) actual.getValues().iterator().next(), is(expected));
    }
}
