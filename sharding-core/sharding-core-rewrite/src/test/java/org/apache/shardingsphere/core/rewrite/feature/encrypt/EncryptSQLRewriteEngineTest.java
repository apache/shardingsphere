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

package org.apache.shardingsphere.core.rewrite.feature.encrypt;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.preprocessor.SQLStatementContextFactory;
import org.apache.shardingsphere.core.preprocessor.statement.SQLStatementContext;
import org.apache.shardingsphere.core.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.core.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.core.rewrite.engine.impl.DefaultSQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlRootEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class EncryptSQLRewriteEngineTest {
    
    private EncryptRule encryptRule;
    
    private List<Object> parametersOfEqual;
    
    private List<Object> parametersOfIn;
    
    private SQLParseEngine parseEngine;
    
    @Before
    public void setUp() throws IOException {
        encryptRule = createEncryptRule();
        parametersOfEqual = Arrays.<Object>asList(1, 2);
        parametersOfIn = Arrays.<Object>asList(1, 2, 3, 4);
        parseEngine = new SQLParseEngine(DatabaseTypes.getActualDatabaseType("MySQL"));
    }
    
    private EncryptRule createEncryptRule() throws IOException {
        URL url = EncryptSQLRewriteEngineTest.class.getClassLoader().getResource("yaml/encrypt-rewrite-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootEncryptRuleConfiguration yamlEncryptConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootEncryptRuleConfiguration.class);
        return new EncryptRule(new EncryptRuleConfigurationYamlSwapper().swap(yamlEncryptConfig.getEncryptRule()));
    }

    @Test
    public void assertSelectWithoutPlaceholderWithEncrypt() {
        String sql = "SELECT * FROM t_encrypt WHERE encrypt_col_1 = 1 or encrypt_col_2 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT * FROM t_encrypt WHERE cipher_col_1 = 'encryptValue' or cipher_col_2 = 'encryptValue'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertSelectWithoutPlaceholderWithPlainEncrypt() {
        String sql = "SELECT * FROM t_plain_encrypt WHERE col3 = 1 or col4 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT * FROM t_plain_encrypt WHERE col1 = 'encryptValue' or col2 = 'encryptValue'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertSelectWithoutPlaceholderWithPlainEncryptWithLogicColumn() {
        String sql = "SELECT col3, col4 FROM t_plain_encrypt WHERE col3 = 1 or col4 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT col1, col2 FROM t_plain_encrypt WHERE col1 = 'encryptValue' or col2 = 'encryptValue'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertSelectWithPlaceholderWithQueryEncrypt() {
        String sql = "SELECT * FROM t_plain_query WHERE col3 in (?, ?) and col4 in (?, ?)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfIn, true);
        assertThat(actual.getSql(), is("SELECT * FROM t_plain_query WHERE query1 IN (?, ?) and query2 IN (?, ?)"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertSelectWithPlaceholderWithQueryEncryptWithLogicColumn() {
        String sql = "SELECT col3 as alias FROM t_plain_query WHERE col3 in (?, ?) and col4 in (?, ?)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfIn, true);
        assertThat(actual.getSql(), is("SELECT col1 as alias FROM t_plain_query WHERE query1 IN (?, ?) and query2 IN (?, ?)"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertSelectWithPlaceholderWithQueryPlainEncrypt() {
        String sql = "SELECT * FROM t_plain_query WHERE col3 = ? or col4 = ?";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, false);
        assertThat(actual.getSql(), is("SELECT * FROM t_plain_query WHERE plain1 = ? or plain2 = ?"));
        assertThat(actual.getParameters().size(), is(2));
        assertThat(actual.getParameters().get(0), is((Object) 1));
        assertThat(actual.getParameters().get(1), is((Object) 2));
    }
    
    @Test
    public void assertDeleteWithPlaceholderWithEncrypt() {
        String sql = "DELETE FROM t_encrypt WHERE encrypt_col_1 = ? and encrypt_col_2 = ?";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, true);
        assertThat(actual.getSql(), is("DELETE FROM t_encrypt WHERE cipher_col_1 = ? and cipher_col_2 = ?"));
        assertThat(actual.getParameters().size(), is(2));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "encryptValue"));
        
    }
    
    @Test
    public void assertDeleteWithPlaceholderWithPlainEncrypt() {
        String sql = "DELETE FROM t_plain_encrypt WHERE col3 in (?, ?) or col4 in (?, ?)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfIn, false);
        assertThat(actual.getSql(), is("DELETE FROM t_plain_encrypt WHERE plain1 IN (?, ?) or plain2 IN (?, ?)"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) 1));
        assertThat(actual.getParameters().get(1), is((Object) 2));
        assertThat(actual.getParameters().get(2), is((Object) 3));
        assertThat(actual.getParameters().get(3), is((Object) 4));
        
    }
    
    @Test
    public void assertDeleteWithoutPlaceholderWithQueryEncrypt() {
        String sql = "DELETE FROM t_query_encrypt WHERE col1 = 1 and col2 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("DELETE FROM t_query_encrypt WHERE query1 = 'assistedEncryptValue' and query2 = 'assistedEncryptValue'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertDeleteWithoutPlaceholderWithQueryPlainEncrypt() {
        String sql = "DELETE FROM t_plain_query WHERE col3 = 1 or col4 IN (2,3,4)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("DELETE FROM t_plain_query WHERE query1 = 'assistedEncryptValue' or query2 IN ('assistedEncryptValue', 'assistedEncryptValue', 'assistedEncryptValue')"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertUpdateWithoutPlaceholderWithEncrypt() {
        String sql = "UPDATE t_encrypt set encrypt_col_1 = 3 where encrypt_col_2 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("UPDATE t_encrypt set cipher_col_1 = 'encryptValue' where cipher_col_2 = 'encryptValue'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertUpdateWithoutPlaceholderWithPlainEncrypt() {
        String sql = "UPDATE t_plain_encrypt set col3 = 3 where col4 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), false);
        assertThat(actual.getSql(), is("UPDATE t_plain_encrypt set col1 = 'encryptValue', plain1 = 3 where plain2 = '2'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertUpdateWithPlaceholderWithQueryEncrypt() {
        String sql = "UPDATE t_query_encrypt set col1 = ? where col2 = ?";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, true);
        assertThat(actual.getSql(), is("UPDATE t_query_encrypt set col1 = ?, query1 = ? where query2 = ?"));
        assertThat(actual.getParameters().size(), is(3));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertUpdateWithPlaceholderWithQueryPlainEncrypt() {
        String sql = "UPDATE t_plain_query set col3 = ? where col4 = ?";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, true);
        assertThat(actual.getSql(), is("UPDATE t_plain_query set col1 = ?, query1 = ?, plain1 = ? where query2 = ?"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) 1));
        assertThat(actual.getParameters().get(3), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertUpdateMultipleEncryptColumnsWithPlaceholder() {
        String sql = "UPDATE t_plain_query set col3 = ?, col4 = ?, other_col1 = ? where other_col2 = ?";
        List<Object> parameters = Arrays.<Object>asList(1, 2, "update_regular", "query_regular");
        SQLRewriteResult actual = getSQLRewriteResult(sql, parameters, true);
        assertThat(actual.getSql(), is("UPDATE t_plain_query set col1 = ?, query1 = ?, plain1 = ?, col2 = ?, query2 = ?, plain2 = ?, other_col1 = ? where other_col2 = ?"));
        assertThat(actual.getParameters().size(), is(8));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) 1));
        assertThat(actual.getParameters().get(3), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(4), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(5), is((Object) 2));
        assertThat(actual.getParameters().get(6), is((Object) "update_regular"));
        assertThat(actual.getParameters().get(7), is((Object) "query_regular"));
    }
    
    @Test
    public void assertInsertWithValuesWithPlaceholderWithEncrypt() {
        String sql = "INSERT INTO t_encrypt(encrypt_col_1, encrypt_col_2) VALUES (?, ?), (3, 4)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, true);
        assertThat(actual.getSql(), is("INSERT INTO t_encrypt(cipher_col_1, cipher_col_2) VALUES (?, ?), ('encryptValue', 'encryptValue')"));
        assertThat(actual.getParameters().size(), is(2));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "encryptValue"));
    }
    
    @Test
    public void assertInsertWithValuesWithPlaceholderWithPlainEncrypt() {
        String sql = "INSERT INTO t_plain_encrypt(col3, col4) VALUES (?, ?), (3, 4)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, true);
        assertThat(actual.getSql(), is("INSERT INTO t_plain_encrypt(col1, col2, plain1, plain2) VALUES (?, ?, ?, ?), ('encryptValue', 'encryptValue', 3, 4)"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) 1));
        assertThat(actual.getParameters().get(3), is((Object) 2));
    }
    
    @Test
    public void assertInsertWithValuesWithoutPlaceholderWithQueryEncrypt() {
        String sql = "INSERT INTO t_query_encrypt(col1, col2) VALUES (1, 2), (3, 4)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("INSERT INTO t_query_encrypt(col1, col2, query1, query2) " 
                + "VALUES ('encryptValue', 'encryptValue', 'assistedEncryptValue', 'assistedEncryptValue'), ('encryptValue', 'encryptValue', 'assistedEncryptValue', 'assistedEncryptValue')"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertInsertWithValuesWithoutPlaceholderWithQueryPlainEncrypt() {
        String sql = "INSERT INTO t_plain_query(col3, col4) VALUES (1, 2), (3, 4)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("INSERT INTO t_plain_query(col1, col2, query1, plain1, query2, plain2) " 
                + "VALUES ('encryptValue', 'encryptValue', 'assistedEncryptValue', 1, 'assistedEncryptValue', 2), " 
                + "('encryptValue', 'encryptValue', 'assistedEncryptValue', 3, 'assistedEncryptValue', 4)"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertInsertWithSetWithoutPlaceholderWithEncrypt() {
        String sql = "INSERT INTO t_encrypt SET encrypt_col_1 = 1, encrypt_col_2 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("INSERT INTO t_encrypt SET cipher_col_1 = 'encryptValue', cipher_col_2 = 'encryptValue'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertInsertWithSetWithoutPlaceholderWithPlainEncrypt() {
        String sql = "INSERT INTO t_plain_encrypt SET col3 = 1, col4 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("INSERT INTO t_plain_encrypt SET col1 = 'encryptValue', plain1 = 1, col2 = 'encryptValue', plain2 = 2"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertInsertWithSetWithPlaceholderWithQueryEncrypt() {
        String sql = "INSERT INTO t_query_encrypt SET col1 = ?, col2 = ?";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, true);
        assertThat(actual.getSql(), is("INSERT INTO t_query_encrypt SET col1 = ?, query1 = ?, col2 = ?, query2 = ?"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(3), is((Object) "assistedEncryptValue"));
    }
    
    @Test
    public void assertInsertWithSetWithPlaceholderWithQueryPlainEncrypt() {
        String sql = "INSERT INTO t_plain_query SET col3 = ?, col4 = ?";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, false);
        assertThat(actual.getSql(), is("INSERT INTO t_plain_query SET col1 = ?, query1 = ?, plain1 = ?, col2 = ?, query2 = ?, plain2 = ?"));
        assertThat(actual.getParameters().size(), is(6));
        assertThat(actual.getParameters().get(0), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(1), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(2), is((Object) 1));
        assertThat(actual.getParameters().get(3), is((Object) "encryptValue"));
        assertThat(actual.getParameters().get(4), is((Object) "assistedEncryptValue"));
        assertThat(actual.getParameters().get(5), is((Object) 2));
    }
    
    @SuppressWarnings("unchecked")
    private SQLRewriteResult getSQLRewriteResult(final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        // TODO panjuan: should mock sqlStatement, do not call parse module on rewrite test case
        SQLStatement sqlStatement = parseEngine.parse(sql, false);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(mock(TableMetas.class), sql, parameters, sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(TableMetas.class), sqlStatementContext, sql, parameters);
        new EncryptSQLRewriteContextDecorator(encryptRule, isQueryWithCipherColumn).decorate(sqlRewriteContext); 
        return new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext);
    }
}
