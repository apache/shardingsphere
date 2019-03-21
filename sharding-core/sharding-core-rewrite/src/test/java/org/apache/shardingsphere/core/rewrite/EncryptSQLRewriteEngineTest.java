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

package org.apache.shardingsphere.core.rewrite;

import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptorConfiguration;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.optimize.OptimizeEngineFactory;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.EncryptSQLParsingEngine;
import org.apache.shardingsphere.core.parse.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EncryptSQLRewriteEngineTest {
    
    private final DatabaseType databaseType = DatabaseType.MySQL;
    
    private EncryptRule encryptRule;
    
    private EncryptSQLParsingEngine sqlParsingEngine;
    
    private List<Object> parameters;
    
    @Before
    public void setUp() {
        encryptRule = new EncryptRule(createEncryptRuleConfiguration());
        parameters = new LinkedList<>();
        parameters.add(1);
        parameters.add(2);
        sqlParsingEngine = new EncryptSQLParsingEngine(databaseType, encryptRule, createShardingTableMetaData());
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
    
    private ShardingTableMetaData createShardingTableMetaData() {
        ColumnMetaData columnMetaData1 = new ColumnMetaData("col1", "VARCHAR(10)", false);
        ColumnMetaData columnMetaData2 = new ColumnMetaData("col2", "VARCHAR(10)", false);
        ColumnMetaData queryColumnMetaData1 = new ColumnMetaData("query1", "VARCHAR(10)", false);
        ColumnMetaData queryColumnMetaData2 = new ColumnMetaData("query2", "VARCHAR(10)", false);
        TableMetaData encryptTableMetaData = new TableMetaData(Arrays.asList(columnMetaData1, columnMetaData2));
        TableMetaData queryTableMetaData = new TableMetaData(Arrays.asList(columnMetaData1, columnMetaData2, queryColumnMetaData1, queryColumnMetaData2));
        Map<String, TableMetaData> tables = new LinkedHashMap<>();
        tables.put("t_encrypt", encryptTableMetaData);
        tables.put("t_query_encrypt", queryTableMetaData);
        return new ShardingTableMetaData(tables);
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
    
    private SQLUnit getSQLUnit(final String sql, final List<Object> parameters) {
        SQLStatement sqlStatement = sqlParsingEngine.parse(false, sql);
        OptimizeResult optimizeResult = OptimizeEngineFactory.newInstance(encryptRule, sqlStatement, parameters).optimize();
        return new EncryptSQLRewriteEngine(encryptRule, sql, databaseType, sqlStatement, parameters, optimizeResult).rewrite().toSQL();
    }
}
