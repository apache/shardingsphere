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
        URL url = EncryptSQLRewriteEngineTest.class.getClassLoader().getResource("yaml/encrypt-rewrite-rule-query-with-cipher.yaml");
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        YamlRootEncryptRuleConfiguration yamlEncryptConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootEncryptRuleConfiguration.class);
        return new EncryptRule(new EncryptRuleConfigurationYamlSwapper().swap(yamlEncryptConfig.getEncryptRule()));
    }

    @Test
    public void assertSelectWithoutPlaceholderWithEncrypt() {
        String sql = "SELECT * FROM t_cipher WHERE encrypt_1 = 1 or encrypt_2 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT * FROM t_cipher WHERE cipher_1 = 'encrypt_1' or cipher_2 = 'encrypt_2'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertSelectWithoutPlaceholderWithPlainEncrypt() {
        String sql = "SELECT * FROM t_cipher_plain WHERE encrypt_1 = 1 or encrypt_2 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT * FROM t_cipher_plain WHERE cipher_1 = 'encrypt_1' or cipher_2 = 'encrypt_2'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertSelectWithoutPlaceholderWithPlainEncryptWithLogicColumn() {
        String sql = "SELECT encrypt_1, encrypt_2 FROM t_cipher_plain WHERE encrypt_1 = 1 or encrypt_2 = 2";
        SQLRewriteResult actual = getSQLRewriteResult(sql, Collections.emptyList(), true);
        assertThat(actual.getSql(), is("SELECT cipher_1, cipher_2 FROM t_cipher_plain WHERE cipher_1 = 'encrypt_1' or cipher_2 = 'encrypt_2'"));
        assertTrue(actual.getParameters().isEmpty());
    }
    
    @Test
    public void assertSelectWithPlaceholderWithQueryEncrypt() {
        String sql = "SELECT * FROM t_cipher_assisted_query_plain WHERE encrypt_1 in (?, ?) and encrypt_2 in (?, ?)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfIn, true);
        assertThat(actual.getSql(), is("SELECT * FROM t_cipher_assisted_query_plain WHERE assisted_query_1 IN (?, ?) and assisted_query_2 IN (?, ?)"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "assisted_query_1"));
        assertThat(actual.getParameters().get(1), is((Object) "assisted_query_2"));
        assertThat(actual.getParameters().get(2), is((Object) "assisted_query_3"));
        assertThat(actual.getParameters().get(3), is((Object) "assisted_query_4"));
    }
    
    @Test
    public void assertSelectWithPlaceholderWithQueryEncryptWithLogicColumn() {
        String sql = "SELECT encrypt_1 as alias FROM t_cipher_assisted_query_plain WHERE encrypt_1 in (?, ?) and encrypt_2 in (?, ?)";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfIn, true);
        assertThat(actual.getSql(), is("SELECT cipher_1 as alias FROM t_cipher_assisted_query_plain WHERE assisted_query_1 IN (?, ?) and assisted_query_2 IN (?, ?)"));
        assertThat(actual.getParameters().size(), is(4));
        assertThat(actual.getParameters().get(0), is((Object) "assisted_query_1"));
        assertThat(actual.getParameters().get(1), is((Object) "assisted_query_2"));
        assertThat(actual.getParameters().get(2), is((Object) "assisted_query_3"));
        assertThat(actual.getParameters().get(3), is((Object) "assisted_query_4"));
    }
    
    @Test
    public void assertSelectWithPlaceholderWithQueryPlainEncrypt() {
        String sql = "SELECT * FROM t_cipher_assisted_query_plain WHERE encrypt_1 = ? or encrypt_2 = ?";
        SQLRewriteResult actual = getSQLRewriteResult(sql, parametersOfEqual, false);
        assertThat(actual.getSql(), is("SELECT * FROM t_cipher_assisted_query_plain WHERE plain_1 = ? or plain_2 = ?"));
        assertThat(actual.getParameters().size(), is(2));
        assertThat(actual.getParameters().get(0), is((Object) 1));
        assertThat(actual.getParameters().get(1), is((Object) 2));
    }
    
    private SQLRewriteResult getSQLRewriteResult(final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        SQLStatement sqlStatement = parseEngine.parse(sql, false);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(mock(TableMetas.class), sql, parameters, sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(TableMetas.class), sqlStatementContext, sql, parameters);
        new EncryptSQLRewriteContextDecorator(encryptRule, isQueryWithCipherColumn).decorate(sqlRewriteContext); 
        return new DefaultSQLRewriteEngine().rewrite(sqlRewriteContext);
    }
}
