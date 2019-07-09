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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingInsertOptimizeEngineTest {
    
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
        URL url = ShardingInsertOptimizeEngineTest.class.getClassLoader().getResource("yaml/optimize-rule.yaml");
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
        insertValuesStatementWithPlaceholder.getColumnNames().add("user_id");
        insertValuesStatementWithPlaceholder.getColumnNames().add("status");
        InsertValue insertValue = new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertValuesStatementWithPlaceholder.getValues().add(insertValue);
        insertValuesStatementWithPlaceholder.getValues().add(insertValue);
    }
    
    private void initializeInsertValuesWithPlaceholderWithEncrypt() {
        insertValuesStatementWithPlaceholderWithEncrypt = new InsertStatement();
        insertValuesStatementWithPlaceholderWithEncrypt.getTables().add(new Table("t_encrypt", null));
        insertValuesStatementWithPlaceholderWithEncrypt.getColumnNames().add("user_id");
        insertValuesStatementWithPlaceholderWithEncrypt.getColumnNames().add("status");
        InsertValue insertValue = new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertValuesStatementWithPlaceholderWithEncrypt.getValues().add(insertValue);
        insertValuesStatementWithPlaceholderWithEncrypt.getValues().add(insertValue);
    }
    
    private void initializeInsertValuesWithoutPlaceholder() {
        insertValuesStatementWithoutPlaceholder = new InsertStatement();
        insertValuesStatementWithoutPlaceholder.getTables().add(new Table("t_order", null));
    }
    
    private void initializeInsertValuesWithoutPlaceholderWithQueryEncrypt() {
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt = new InsertStatement();
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", null));
    }
    
    private void initializeInsertSetWithPlaceholder() {
        insertSetStatementWithPlaceholder = new InsertStatement();
        insertSetStatementWithPlaceholder.getTables().add(new Table("t_order", null));
        insertSetStatementWithPlaceholder.getColumnNames().add("user_id");
        insertSetStatementWithPlaceholder.getColumnNames().add("status");
    }
    
    private void initializeInsertSetWithPlaceholderWithQueryEncrypt() {
        insertSetStatementWithPlaceholderWithQueryEncrypt = new InsertStatement();
        insertSetStatementWithPlaceholderWithQueryEncrypt.getTables().add(new Table("t_encrypt_query", null));
        insertSetStatementWithPlaceholderWithQueryEncrypt.getColumnNames().add("user_id");
        insertSetStatementWithPlaceholderWithQueryEncrypt.getColumnNames().add("status");
    }
    
    private void initializeInsertSetWithoutPlaceholder() {
        insertSetStatementWithoutPlaceholder = new InsertStatement();
        insertSetStatementWithoutPlaceholder.getTables().add(new Table("t_order", null));
        insertSetStatementWithoutPlaceholder.getColumnNames().add("user_id");
        insertSetStatementWithoutPlaceholder.getColumnNames().add("status");
    }
    
    private void initializeInsertSetWithoutPlaceholderWithEncrypt() {
        insertSetStatementWithoutPlaceholderWithEncrypt = new InsertStatement();
        insertSetStatementWithoutPlaceholderWithEncrypt.getTables().add(new Table("t_encrypt", null));
        insertSetStatementWithoutPlaceholderWithEncrypt.getColumnNames().add("user_id");
        insertSetStatementWithoutPlaceholderWithEncrypt.getColumnNames().add("status");
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceholderWithGeneratedKey() {
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertValuesStatementWithPlaceholder, insertValuesParameters).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getUnits().get(1).getParameters()[2], CoreMatchers.<Object>is(1));
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceholderWithGeneratedKeyWithEncrypt() {
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertValuesStatementWithPlaceholderWithEncrypt, insertValuesParameters).optimize();
        assertThat(actual.getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getUnits().get(1).getParameters()[2], CoreMatchers.<Object>is(1));
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceholderWithoutGeneratedKey() {
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertValuesStatementWithPlaceholder, insertValuesParameters).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceholderWithGeneratedKeyWithQueryEncrypt() {
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getColumnNames().add("user_id");
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getColumnNames().add("status");
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getValues().add(
                new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertValuesStatementWithoutPlaceholderWithQueryEncrypt, Collections.emptyList()).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceholderWithGeneratedKey() {
        insertValuesStatementWithoutPlaceholder.getColumnNames().add("user_id");
        insertValuesStatementWithoutPlaceholder.getColumnNames().add("status");
        insertValuesStatementWithoutPlaceholder.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertValuesStatementWithoutPlaceholder, Collections.emptyList()).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceholderWithGeneratedKey() {
        insertSetStatementWithPlaceholder.getValues().add(
                new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1))));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertSetStatementWithPlaceholder, insertSetParameters).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceholderWithGeneratedKeyWithQueryEncrypt() {
        InsertValue insertValue = new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertSetStatementWithPlaceholderWithQueryEncrypt.getValues().add(insertValue);
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertSetStatementWithPlaceholderWithQueryEncrypt, insertSetParameters).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(4));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getUnits().get(0).getParameters()[3], CoreMatchers.<Object>is(12));
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceholderWithGeneratedKey() {
        insertSetStatementWithoutPlaceholder.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertSetStatementWithoutPlaceholder, Collections.emptyList()).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceholderWithGeneratedKeyWithEncrypt() {
        insertSetStatementWithoutPlaceholderWithEncrypt.getValues().add(
                new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine(
                shardingRule, mock(ShardingTableMetaData.class), insertSetStatementWithoutPlaceholderWithEncrypt, Collections.emptyList()).optimize();
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
}
