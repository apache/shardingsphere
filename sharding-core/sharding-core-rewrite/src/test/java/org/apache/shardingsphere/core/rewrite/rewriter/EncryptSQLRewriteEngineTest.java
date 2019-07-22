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

package org.apache.shardingsphere.core.rewrite.rewriter;

import org.apache.shardingsphere.api.config.encrypt.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptorRuleConfiguration;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.EncryptOptimizeEngineFactory;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class EncryptSQLRewriteEngineTest {
    
    private EncryptRule encryptRule;
    
    private List<Object> parameters;
    
    private SQLParseEngine parseEngine;
    
    @Before
    public void setUp() {
        encryptRule = new EncryptRule(createEncryptRuleConfiguration());
        parameters = Arrays.<Object>asList(1, 2);
        parseEngine = new SQLParseEngine(DatabaseTypes.getActualDatabaseType("MySQL"));
    }
    
    private EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptorRuleConfiguration encryptorConfig = new EncryptorRuleConfiguration("test", new Properties());
        EncryptorRuleConfiguration encryptorQueryConfig = new EncryptorRuleConfiguration("assistedTest", new Properties());
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.getEncryptors().put("test", encryptorConfig);
        result.getEncryptors().put("assistedTest", encryptorQueryConfig);
        result.getTables().put("t_encrypt", createEncryptTableConfig1());
        result.getTables().put("t_query_encrypt", createEncryptTableConfig2());
        return result;
    }
    
    private EncryptTableRuleConfiguration createEncryptTableConfig1() {
        EncryptColumnRuleConfiguration columnConfig1 = new EncryptColumnRuleConfiguration("", "col1", "", "test");
        EncryptColumnRuleConfiguration columnConfig2 = new EncryptColumnRuleConfiguration("", "col2", "", "test");
        Map<String, EncryptColumnRuleConfiguration> columns1 = new LinkedHashMap<>();
        columns1.put("col1", columnConfig1);
        columns1.put("col2", columnConfig2);
        return new EncryptTableRuleConfiguration(columns1);
    }
    
    private EncryptTableRuleConfiguration createEncryptTableConfig2() {
        EncryptColumnRuleConfiguration columnConfig1 = new EncryptColumnRuleConfiguration("", "col1", "query1", "assistedTest");
        EncryptColumnRuleConfiguration columnConfig2 = new EncryptColumnRuleConfiguration("", "col2", "query2", "assistedTest");
        Map<String, EncryptColumnRuleConfiguration> columns2 = new LinkedHashMap<>();
        columns2.put("col1", columnConfig1);
        columns2.put("col2", columnConfig2);
        return new EncryptTableRuleConfiguration(columns2);
    }

    @Test
    public void assertSelectWithoutPlaceholderWithEncrypt() {
        String sql = "SELECT * FROM t_encrypt WHERE col1 = 1 or col2 = 2";
        SQLUnit actual = getSQLUnit(sql, Collections.emptyList());
        assertThat(actual.getSql(), is("SELECT * FROM t_encrypt WHERE col1 = 'encryptValue' or col2 = 'encryptValue'"));
        assertThat(actual.getParameters().size(), is(0));
    }
    
    @Test
    public void assertSelectWithPlaceholderWithQueryEncrypt() {
        String sql = "SELECT * FROM t_query_encrypt WHERE col1 = ? or col2 = ?";
        SQLUnit actual = getSQLUnit(sql, parameters);
        assertThat(actual.getSql(), is("SELECT * FROM t_query_encrypt WHERE query1 = ? or query2 = ?"));
        assertThat(actual.getParameters().size(), is(2));
        assertThat(actual.getParameters().get(0), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertDeleteWithPlaceholderWithEncrypt() {
        String sql = "DELETE FROM t_encrypt WHERE col1 = ? and col2 = ?";
        SQLUnit actual = getSQLUnit(sql, parameters);
        assertThat(actual.getSql(), is("DELETE FROM t_encrypt WHERE col1 = ? and col2 = ?"));
        assertThat(actual.getParameters().size(), is(2));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "encryptValue"));
        
    }
    
    @Test
    public void assertDeleteWithoutPlaceholderWithQueryEncrypt() {
        String sql = "DELETE FROM t_query_encrypt WHERE col1 = 1 and col2 = 2";
        SQLUnit actual = getSQLUnit(sql, Collections.emptyList());
        assertThat(actual.getSql(), is("DELETE FROM t_query_encrypt WHERE query1 = 'assistedEncryptValue' and query2 = 'assistedEncryptValue'"));
        assertThat(actual.getParameters().size(), is(0));
    }
    
    @Test
    public void assertUpdateWithoutPlaceholderWithEncrypt() {
        String sql = "UPDATE t_encrypt set col1 = 3 where col2 = 2";
        SQLUnit actual = getSQLUnit(sql, Collections.emptyList());
        assertThat(actual.getSql(), is("UPDATE t_encrypt set col1 = 'encryptValue' where col2 = 'encryptValue'"));
        assertThat(actual.getParameters().size(), is(0));
    }
    
    @Test
    public void assertUpdateWithPlaceholderWithQueryEncrypt() {
        String sql = "UPDATE t_query_encrypt set col1 = ? where col2 = ?";
        SQLUnit actual = getSQLUnit(sql, parameters);
        assertThat(actual.getSql(), is("UPDATE t_query_encrypt set col1 = ?, query1 = ? where query2 = ?"));
        assertThat(actual.getParameters().size(), is(3));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertInsertWithValuesWithPlaceholderWithEncrypt() {
        String sql = "INSERT INTO t_encrypt(col1, col2) VALUES (?, ?), (3, 4)";
        SQLUnit actual = getSQLUnit(sql, parameters);
        assertThat(actual.getSql(), is("INSERT INTO t_encrypt(col1, col2) VALUES (?, ?), ('encryptValue', 'encryptValue')"));
        assertThat(actual.getParameters().size(), is(2));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertInsertWithValuesWithoutPlaceholderWithQueryEncrypt() {
        String sql = "INSERT INTO t_query_encrypt(col1, col2) VALUES (1, 2), (3, 4)";
        SQLUnit actual = getSQLUnit(sql, Collections.emptyList());
        assertThat(actual.getSql(), is("INSERT INTO t_query_encrypt(col1, col2, query1, query2) " 
                + "VALUES ('encryptValue', 'encryptValue', 'assistedEncryptValue', 'assistedEncryptValue'), ('encryptValue', 'encryptValue', 'assistedEncryptValue', 'assistedEncryptValue')"));
        assertThat(actual.getParameters().size(), is(0));
    }
    
    @Test
    public void assertInsertWithSetWithoutPlaceholderWithEncrypt() {
        String sql = "INSERT INTO t_encrypt SET col1 = 1, col2 = 2";
        SQLUnit actual = getSQLUnit(sql, Collections.emptyList());
        assertThat(actual.getSql(), is("INSERT INTO t_encrypt SET col1 = 'encryptValue', col2 = 'encryptValue'"));
        assertThat(actual.getParameters().size(), is(0));
    }
    
    @Test
    public void assertInsertWithSetWithPlaceholderWithQueryEncrypt() {
        String sql = "INSERT INTO t_query_encrypt SET col1 = ?, col2 = ?";
        SQLUnit actual = getSQLUnit(sql, parameters);
        assertThat(actual.getSql(), is("INSERT INTO t_query_encrypt SET col1 = ?, col2 = ?, query1 = ?, query2 = ?"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(3), is((Object) "assistedEncryptValue"));
    }
    
    @SuppressWarnings("unchecked")
    private SQLUnit getSQLUnit(final String sql, final List<Object> parameters) {
        // TODO panjuan: should mock sqlStatement, do not call parse module on rewrite test case
        SQLStatement sqlStatement = parseEngine.parse(sql, false);
        OptimizedStatement optimizedStatement = EncryptOptimizeEngineFactory.newInstance(sqlStatement).optimize(encryptRule, mock(ShardingTableMetaData.class), sql, parameters, sqlStatement);
        SQLRewriteEngine sqlRewriteEngine = new SQLRewriteEngine(encryptRule, optimizedStatement, sql, parameters, true);
        return sqlRewriteEngine.generateSQL();
    }
}
