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

import com.google.common.base.Optional;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parse.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EncryptInsertOptimizeEngineTest {
    
    private EncryptRule encryptRule;
    
    private final List<Object> parametersWithValues = Arrays.asList((Object) 1, (Object) 2);
    
    private final List<Object> parametersWithoutValues = Collections.emptyList();
    
    @Before
    public void setUp() {
        encryptRule = new EncryptRule(createEncryptRuleConfiguration());
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptorConfiguration encryptorConfig = new EncryptorConfiguration("test", "col1, col2", new Properties());
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptTableRuleConfig.setTable("t_encrypt");
        encryptTableRuleConfig.setEncryptorConfig(encryptorConfig);
        EncryptorConfiguration encryptorQueryConfig = new EncryptorConfiguration("assistedTest", "col1, col2", "query1, query2", new Properties());
        EncryptTableRuleConfiguration encryptQueryTableRuleConfig = new EncryptTableRuleConfiguration();
        encryptQueryTableRuleConfig.setTable("t_query_encrypt");
        encryptQueryTableRuleConfig.setEncryptorConfig(encryptorQueryConfig);
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.getTableRuleConfigs().add(encryptTableRuleConfig);
        result.getTableRuleConfigs().add(encryptQueryTableRuleConfig);
        return result;
    }
    
    @Test
    public void assertInsertStatementWithValuesWithPlaceHolderWithEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithValuesWithPlaceHolderWithEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertThat(actual.getInsertColumnValues().get().getColumnNames().size(), is(2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().size(), is(1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().size(), is(2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().get(0), is((Object) 1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().get(1), is((Object) 2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).toString(), is("(?, ?)"));
    
    }
    
    private InsertStatement createInsertStatementWithValuesWithPlaceHolderWithEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_encrypt", Optional.<String>absent()));
        result.addSQLToken(new TableToken(12, 0, "t_encrypt", QuoteCharacter.NONE));
        result.addSQLToken(new InsertValuesToken(34, DefaultKeyword.VALUES));
        result.getColumns().add(new Column("col1", "t_encrypt"));
        result.getColumns().add(new Column("col2", "t_encrypt"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, 2);
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(0));
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(1));
        result.getInsertValues().getInsertValues().add(insertValue);
        return result;
    }
    
    @Test
    public void assertInsertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithoutValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertThat(actual.getInsertColumnValues().get().getColumnNames().size(), is(4));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().size(), is(1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().size(), is(0));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getColumnValue("col1"), is((Object) 1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getColumnValue("col2"), is((Object) 2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getColumnValue("query1"), is((Object) 1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getColumnValue("query2"), is((Object) 2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).toString(), is("(1, 2, 1, 2)"));
        
    }
    
    private InsertStatement createInsertStatementWithValuesWithoutPlaceHolderWithQueryEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_query_encrypt", Optional.<String>absent()));
        result.addSQLToken(new TableToken(12, 0, "t_query_encrypt", QuoteCharacter.NONE));
        result.addSQLToken(new InsertValuesToken(40, DefaultKeyword.VALUES));
        result.getColumns().add(new Column("col1", "t_query_encrypt"));
        result.getColumns().add(new Column("col2", "t_query_encrypt"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.VALUES, 0);
        insertValue.getColumnValues().add(new SQLNumberExpression(1));
        insertValue.getColumnValues().add(new SQLNumberExpression(2));
        result.getInsertValues().getInsertValues().add(insertValue);
        return result;
    }
    
    @Test
    public void assertInsertStatementWithSetWithoutPlaceHolderWithEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithSetWithoutPlaceHolderWithEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithoutValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertThat(actual.getInsertColumnValues().get().getColumnNames().size(), is(2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().size(), is(1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().size(), is(0));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getColumnValue("col1"), is((Object) 1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getColumnValue("col2"), is((Object) 2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).toString(), is("col1 = 1, col2 = 2"));
        
    }
    
    private InsertStatement createInsertStatementWithSetWithoutPlaceHolderWithEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_encrypt", Optional.<String>absent()));
        result.addSQLToken(new TableToken(12, 0, "t_encrypt", QuoteCharacter.NONE));
        result.addSQLToken(new InsertValuesToken(34, DefaultKeyword.SET));
        result.getColumns().add(new Column("col1", "t_encrypt"));
        result.getColumns().add(new Column("col2", "t_encrypt"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.SET, 0);
        insertValue.getColumnValues().add(new SQLNumberExpression(1));
        insertValue.getColumnValues().add(new SQLNumberExpression(2));
        result.getInsertValues().getInsertValues().add(insertValue);
        return result;
    }
    
    @Test
    public void assertInsertStatementWithSetWithPlaceHolderWithQueryEncrypt() {
        InsertStatement insertStatement = createInsertStatementWithSetWithPlaceHolderWithQueryEncrypt();
        EncryptInsertOptimizeEngine optimizeEngine = new EncryptInsertOptimizeEngine(encryptRule, insertStatement, parametersWithValues);
        OptimizeResult actual = optimizeEngine.optimize();
        assertThat(actual.getInsertColumnValues().get().getColumnNames().size(), is(4));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().size(), is(1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().size(), is(4));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().get(0), is((Object) 1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().get(1), is((Object) 2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().get(2), is((Object) 1));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).getParameters().get(3), is((Object) 2));
        assertThat(actual.getInsertColumnValues().get().getColumnValues().get(0).toString(), is("col1 = ?, col2 = ?, query1 = ?, query2 = ?"));
        
    }
    
    private InsertStatement createInsertStatementWithSetWithPlaceHolderWithQueryEncrypt() {
        InsertStatement result = new InsertStatement();
        result.getTables().add(new Table("t_query_encrypt", Optional.<String>absent()));
        result.addSQLToken(new TableToken(12, 0, "t_query_encrypt", QuoteCharacter.NONE));
        result.addSQLToken(new InsertValuesToken(40, DefaultKeyword.SET));
        result.getColumns().add(new Column("col1", "t_query_encrypt"));
        result.getColumns().add(new Column("col2", "t_query_encrypt"));
        InsertValue insertValue = new InsertValue(DefaultKeyword.SET, 2);
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(0));
        insertValue.getColumnValues().add(new SQLPlaceholderExpression(1));
        result.getInsertValues().getInsertValues().add(insertValue);
        return result;
    }
}
