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

package org.apache.shardingsphere.encrypt.distsql.parser;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.distsql.parser.core.featured.FeaturedDistSQLStatementParserFacadeFactory;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.facade.EncryptDistSQLStatementParserFacade;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.SQLParserFactory;
import org.junit.Test;

import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class EncryptDistSqlTest {
    
    @Test
    public void assertCreateEncryptRule() {
        String sql = "CREATE ENCRYPT RULE t_encrypt (COLUMNS("
                + " (NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))"
                + ",(NAME=order_id, CIPHER =order_cipher,TYPE(NAME='MD5')))"
                + ",QUERY_WITH_CIPHER_COLUMN=true)";
        CreateEncryptRuleStatement createEncryptRuleStatement = (CreateEncryptRuleStatement) getEncryptDistSQLStatement(sql);
        assertThat(createEncryptRuleStatement.getRules().size(), is(1));
        assertEncryptRule(createEncryptRuleStatement.getRules().iterator().next());
    }
    
    @Test
    public void assertAlterEncryptRule() {
        String sql = "ALTER ENCRYPT RULE t_encrypt (COLUMNS("
                + " (NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),"
                + " (NAME=order_id,CIPHER=order_cipher,TYPE(NAME='MD5'))), QUERY_WITH_CIPHER_COLUMN=TRUE)";
        AlterEncryptRuleStatement alterEncryptRule = (AlterEncryptRuleStatement) getEncryptDistSQLStatement(sql);
        assertThat(alterEncryptRule.getRules().size(), is(1));
        assertEncryptRule(alterEncryptRule.getRules().iterator().next());
    }
    
    private void assertEncryptRule(final EncryptRuleSegment encryptRuleSegment) {
        assertThat(encryptRuleSegment.getTableName(), is("t_encrypt"));
        assertThat(encryptRuleSegment.getColumns().size(), is(2));
        assertThat(encryptRuleSegment.getQueryWithCipherColumn(), is(true));
        Iterator<EncryptColumnSegment> encryptRuleColumns = encryptRuleSegment.getColumns().iterator();
        EncryptColumnSegment userEncryptColumn = encryptRuleColumns.next();
        assertThat(userEncryptColumn.getName(), is("user_id"));
        assertThat(userEncryptColumn.getPlainColumn(), is("user_plain"));
        assertThat(userEncryptColumn.getCipherColumn(), is("user_cipher"));
        assertThat(userEncryptColumn.getEncryptor().getName(), is("AES"));
        Properties properties = new Properties();
        properties.setProperty("aes-key-value", "123456abc");
        assertThat(userEncryptColumn.getEncryptor().getProps(), is(properties));
        EncryptColumnSegment orderEncryptColumn = encryptRuleColumns.next();
        assertThat(orderEncryptColumn.getName(), is("order_id"));
        assertNull(orderEncryptColumn.getPlainColumn());
        assertThat(orderEncryptColumn.getCipherColumn(), is("order_cipher"));
        assertThat(orderEncryptColumn.getEncryptor().getName(), is("MD5"));
        assertThat(orderEncryptColumn.getEncryptor().getProps().size(), is(0));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private DistSQLStatement getEncryptDistSQLStatement(final String sql) {
        EncryptDistSQLStatementParserFacade facade = new EncryptDistSQLStatementParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        SQLVisitor visitor = FeaturedDistSQLStatementParserFacadeFactory.getInstance(facade.getType()).getVisitorClass().getDeclaredConstructor().newInstance();
        return (DistSQLStatement) ((ParseTreeVisitor) visitor).visit(parseASTNode.getRootNode());
    }
}
