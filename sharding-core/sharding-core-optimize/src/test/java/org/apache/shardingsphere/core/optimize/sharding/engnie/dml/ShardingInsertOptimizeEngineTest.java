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

package org.apache.shardingsphere.core.optimize.sharding.engnie.dml;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
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
        insertValuesStatementWithPlaceholder.setTable(new TableSegment(0, 0, "t_order"));
        insertValuesStatementWithPlaceholder.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertValuesStatementWithPlaceholder.getColumns().add(new ColumnSegment(0, 0, "status"));
        InsertValuesSegment insertValuesSegment = new InsertValuesSegment(
                0, 0, Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertValuesStatementWithPlaceholder.getValues().add(insertValuesSegment);
        insertValuesStatementWithPlaceholder.getValues().add(insertValuesSegment);
    }
    
    private void initializeInsertValuesWithPlaceholderWithEncrypt() {
        insertValuesStatementWithPlaceholderWithEncrypt = new InsertStatement();
        insertValuesStatementWithPlaceholderWithEncrypt.setTable(new TableSegment(0, 0, "t_encrypt"));
        insertValuesStatementWithPlaceholderWithEncrypt.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertValuesStatementWithPlaceholderWithEncrypt.getColumns().add(new ColumnSegment(0, 0, "status"));
        InsertValuesSegment insertValuesSegment = new InsertValuesSegment(
                0, 0, Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1)));
        insertValuesStatementWithPlaceholderWithEncrypt.getValues().add(insertValuesSegment);
        insertValuesStatementWithPlaceholderWithEncrypt.getValues().add(insertValuesSegment);
    }
    
    private void initializeInsertValuesWithoutPlaceholder() {
        insertValuesStatementWithoutPlaceholder = new InsertStatement();
        insertValuesStatementWithoutPlaceholder.setTable(new TableSegment(0, 0, "t_order"));
    }
    
    private void initializeInsertValuesWithoutPlaceholderWithQueryEncrypt() {
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt = new InsertStatement();
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.setTable(new TableSegment(0, 0, "t_encrypt_query"));
    }
    
    private void initializeInsertSetWithPlaceholder() {
        insertSetStatementWithPlaceholder = new InsertStatement();
        insertSetStatementWithPlaceholder.setTable(new TableSegment(0, 0, "t_order"));
        insertSetStatementWithPlaceholder.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertSetStatementWithPlaceholder.getColumns().add(new ColumnSegment(0, 0, "status"));
    }
    
    private void initializeInsertSetWithPlaceholderWithQueryEncrypt() {
        insertSetStatementWithPlaceholderWithQueryEncrypt = new InsertStatement();
        insertSetStatementWithPlaceholderWithQueryEncrypt.setTable(new TableSegment(0, 0, "t_encrypt_query"));
        insertSetStatementWithPlaceholderWithQueryEncrypt.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertSetStatementWithPlaceholderWithQueryEncrypt.getColumns().add(new ColumnSegment(0, 0, "status"));
    }
    
    private void initializeInsertSetWithoutPlaceholder() {
        insertSetStatementWithoutPlaceholder = new InsertStatement();
        insertSetStatementWithoutPlaceholder.setTable(new TableSegment(0, 0, "t_order"));
        insertSetStatementWithoutPlaceholder.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertSetStatementWithoutPlaceholder.getColumns().add(new ColumnSegment(0, 0, "status"));
    }
    
    private void initializeInsertSetWithoutPlaceholderWithEncrypt() {
        insertSetStatementWithoutPlaceholderWithEncrypt = new InsertStatement();
        insertSetStatementWithoutPlaceholderWithEncrypt.setTable(new TableSegment(0, 0, "t_encrypt"));
        insertSetStatementWithoutPlaceholderWithEncrypt.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertSetStatementWithoutPlaceholderWithEncrypt.getColumns().add(new ColumnSegment(0, 0, "status"));
    }
    
    @Test
    public void assertOptimizeInsertValuesWithPlaceholderWithGeneratedKey() {
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", insertValuesParameters, insertValuesStatementWithPlaceholder);
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
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", insertValuesParameters, insertValuesStatementWithPlaceholderWithEncrypt);
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
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", insertValuesParameters, insertValuesStatementWithPlaceholder);
        assertThat(actual.getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getUnits().get(1).getParameters().length, is(3));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(10));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("init"));
        assertThat(actual.getUnits().get(1).getParameters()[0], CoreMatchers.<Object>is(11));
        assertThat(actual.getUnits().get(1).getParameters()[1], CoreMatchers.<Object>is("init"));
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceholderWithGeneratedKeyWithQueryEncrypt() {
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getColumns().add(new ColumnSegment(0, 0, "status"));
        insertValuesStatementWithoutPlaceholderWithQueryEncrypt.getValues().add(
                new InsertValuesSegment(0, 0, Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", Collections.emptyList(), insertValuesStatementWithoutPlaceholderWithQueryEncrypt);
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
    
    @Test
    public void assertOptimizeInsertValuesWithoutPlaceholderWithGeneratedKey() {
        insertValuesStatementWithoutPlaceholder.getColumns().add(new ColumnSegment(0, 0, "user_id"));
        insertValuesStatementWithoutPlaceholder.getColumns().add(new ColumnSegment(0, 0, "status"));
        insertValuesStatementWithoutPlaceholder.getValues().add(
                new InsertValuesSegment(0, 0, Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 12), new LiteralExpressionSegment(3, 4, "a"))));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", Collections.emptyList(), insertValuesStatementWithoutPlaceholder);
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceholderWithGeneratedKey() {
        AssignmentSegment assignmentSegment1 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col1"), new ParameterMarkerExpressionSegment(1, 2, 0));
        AssignmentSegment assignmentSegment2 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col2"), new ParameterMarkerExpressionSegment(3, 4, 1));
        insertSetStatementWithPlaceholder.setSetAssignment(new SetAssignmentsSegment(0, 0, Arrays.asList(assignmentSegment1, assignmentSegment2)));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", insertSetParameters, insertSetStatementWithPlaceholder);
        assertThat(actual.getUnits().get(0).getParameters().length, is(3));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
    }
    
    @Test
    public void assertOptimizeInsertSetWithPlaceholderWithGeneratedKeyWithQueryEncrypt() {
        AssignmentSegment assignmentSegment1 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col1"), new ParameterMarkerExpressionSegment(1, 2, 0));
        AssignmentSegment assignmentSegment2 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col2"), new ParameterMarkerExpressionSegment(3, 4, 1));
        insertSetStatementWithPlaceholderWithQueryEncrypt.setSetAssignment(new SetAssignmentsSegment(0, 0, Arrays.asList(assignmentSegment1, assignmentSegment2)));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", insertSetParameters, insertSetStatementWithPlaceholderWithQueryEncrypt);
        assertThat(actual.getUnits().get(0).getParameters().length, is(4));
        assertThat(actual.getUnits().get(0).getParameters()[0], CoreMatchers.<Object>is(12));
        assertThat(actual.getUnits().get(0).getParameters()[1], CoreMatchers.<Object>is("a"));
        assertThat(actual.getUnits().get(0).getParameters()[2], CoreMatchers.<Object>is(1));
        assertThat(actual.getUnits().get(0).getParameters()[3], CoreMatchers.<Object>is(12));
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceholderWithGeneratedKey() {
        AssignmentSegment assignmentSegment1 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col1"), new LiteralExpressionSegment(1, 2, 12));
        AssignmentSegment assignmentSegment2 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col2"), new LiteralExpressionSegment(3, 4, "a"));
        insertSetStatementWithoutPlaceholder.setSetAssignment(new SetAssignmentsSegment(0, 0, Arrays.asList(assignmentSegment1, assignmentSegment2)));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", Collections.emptyList(), insertSetStatementWithoutPlaceholder);
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
    
    @Test
    public void assertOptimizeInsertSetWithoutPlaceholderWithGeneratedKeyWithEncrypt() {
        AssignmentSegment assignmentSegment1 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col1"), new LiteralExpressionSegment(1, 2, 12));
        AssignmentSegment assignmentSegment2 = new AssignmentSegment(0, 0, new ColumnSegment(0, 0, "col2"), new LiteralExpressionSegment(3, 4, "a"));
        insertSetStatementWithoutPlaceholderWithEncrypt.setSetAssignment(new SetAssignmentsSegment(0, 0, Arrays.asList(assignmentSegment1, assignmentSegment2)));
        ShardingInsertOptimizedStatement actual = new ShardingInsertOptimizeEngine().optimize(
                shardingRule, mock(ShardingTableMetaData.class), "", Collections.emptyList(), insertSetStatementWithoutPlaceholderWithEncrypt);
        assertThat(actual.getUnits().get(0).getParameters().length, is(0));
    }
}
