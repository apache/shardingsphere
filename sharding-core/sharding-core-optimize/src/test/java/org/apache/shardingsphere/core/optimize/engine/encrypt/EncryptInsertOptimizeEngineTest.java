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

package org.apache.shardingsphere.core.optimize.engine.encrypt;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorRuleConfiguration;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class EncryptInsertOptimizeEngineTest {
    
    private EncryptRule encryptRule;
    
    private final List<Object> parametersWithValues = Arrays.asList((Object) 1, (Object) 2);
    
    private final List<Object> parametersWithoutValues = Collections.emptyList();
    
    @Before
    public void setUp() {
        encryptRule = new EncryptRule(createEncryptRuleConfiguration());
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptorRuleConfiguration encryptorConfig = new EncryptorRuleConfiguration("test", "t_encrypt.col1, t_encrypt.col2", new Properties());
        EncryptorRuleConfiguration encryptorQueryConfig = 
                new EncryptorRuleConfiguration("assistedTest", "t_query_encrypt.col1, t_query_encrypt.col2", "t_query_encrypt.query1, t_query_encrypt.query2", new Properties());
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.getEncryptorRuleConfigs().put("test", encryptorConfig);
        result.getEncryptorRuleConfigs().put("assistedTest", encryptorQueryConfig);
        return result;
    }
    
    @Test
    public void assertInsertStatementWithValuesWithPlaceHolderWithEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithValuesWithPlaceHolderWithEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getColumnNames().size(), is(2));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().size(), is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(2));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], is((Object) 1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], is((Object) 2));
    
    }
    
    private InsertStatement createInsertStatementWithValuesWithPlaceHolderWithEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_encrypt", null));
        result.getColumnNames().add("col1");
        result.getColumnNames().add("col2");
        result.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1))));
        return result;
    }
    
    @Test
    public void assertInsertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithoutValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getColumnNames().size(), is(4));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().size(), is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getColumnValue("col1"), is((Object) 1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getColumnValue("col2"), is((Object) 2));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getColumnValue("query1"), is((Object) 1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getColumnValue("query2"), is((Object) 2));
        
    }
    
    private InsertStatement createInsertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_query_encrypt", null));
        result.getColumnNames().add("col1");
        result.getColumnNames().add("col2");
        result.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 1), new LiteralExpressionSegment(3, 4, 2))));
        return result;
    }
    
    @Test
    public void assertInsertStatementWithSetWithoutPlaceHolderWithEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithSetWithoutPlaceHolderWithEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithoutValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getColumnNames().size(), is(2));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().size(), is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(0));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getColumnValue("col1"), is((Object) 1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getColumnValue("col2"), is((Object) 2));
        
    }
    
    private InsertStatement createInsertStatementWithSetWithoutPlaceHolderWithEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_encrypt", null));
        result.getColumnNames().add("col1");
        result.getColumnNames().add("col2");
        result.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(1, 2, 1), new LiteralExpressionSegment(3, 4, 2))));
        return result;
    }
    
    @Test
    public void assertInsertStatementWithSetWithPlaceHolderWithQueryEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithSetWithPlaceHolderWithQueryEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertTrue(actual.getInsertOptimizeResult().isPresent());
        assertThat(actual.getInsertOptimizeResult().get().getColumnNames().size(), is(4));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().size(), is(1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters().length, is(4));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[0], is((Object) 1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[1], is((Object) 2));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[2], is((Object) 1));
        assertThat(actual.getInsertOptimizeResult().get().getUnits().get(0).getParameters()[3], is((Object) 2));
        
    }
    
    private InsertStatement createInsertStatementWithSetWithPlaceHolderWithQueryEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_query_encrypt", null));
        result.getColumnNames().add("col1");
        result.getColumnNames().add("col2");
        result.getValues().add(new InsertValue(Arrays.<ExpressionSegment>asList(new ParameterMarkerExpressionSegment(1, 2, 0), new ParameterMarkerExpressionSegment(3, 4, 1))));
        return result;
    }
}
