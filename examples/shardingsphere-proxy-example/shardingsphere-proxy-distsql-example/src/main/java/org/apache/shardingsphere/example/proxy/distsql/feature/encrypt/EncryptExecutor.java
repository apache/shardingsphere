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

package org.apache.shardingsphere.example.proxy.distsql.feature.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.example.proxy.distsql.feature.AbstractFeatureExecutor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Please ensure that the schema example_db has been added to the Proxy
 */
@Slf4j
public final class EncryptExecutor extends AbstractFeatureExecutor {
    
    private static final String ADD_RULE = "CREATE ENCRYPT RULE t_encrypt (\n" +
            "COLUMNS(\n" +
            "(NAME=user_id,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),\n" +
            "(NAME=order_id, CIPHER =order_cipher,TYPE(NAME='MD5'))\n" +
            ")),\n" +
            "t_encrypt_2 (\n" +
            "COLUMNS(\n" +
            "(NAME=user_id,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),\n" +
            "(NAME=order_id, CIPHER=order_cipher,TYPE(NAME='MD5'))\n" +
            "))";
    
    private static final String ALTER_RULE = "ALTER ENCRYPT RULE t_encrypt (\n" +
            "COLUMNS(\n" +
            "(NAME=user_id,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),\n" +
            "(NAME=order_id,CIPHER=order_cipher,TYPE(NAME='MD5'))\n" +
            "))";
    
    private static final String DROP_RULE = "DROP ENCRYPT RULE t_encrypt,t_encrypt_2";
    
    private static final String SHOW_RULE = "SHOW ENCRYPT RULES";
    
    @Override
    public void init(Statement statement) {
        this.statement = statement;
    }
    
    @Override
    public void execute() throws SQLException {
        executeUseSchema();
        executeShowRule();
        executeAddRule();
        executeShowRule();
        executeAlterRule();
        executeShowRule();
        executeDropRule();
        executeShowRule();
    }
    
    private void executeShowRule() throws SQLException {
        log.info("show rule...");
        ResultSet resultSet = statement.executeQuery(SHOW_RULE);
        log.info(JsonUtils.toJsonString(getResultData(resultSet)));
    }
    
    private void executeAddRule() throws SQLException {
        log.info("add rule...");
        statement.execute(ADD_RULE);
    }
    
    private void executeAlterRule() throws SQLException {
        log.info("alter rule...");
        statement.execute(ALTER_RULE);
    }
    
    private void executeDropRule() throws SQLException {
        log.info("drop rule...");
        statement.execute(DROP_RULE);
    }
}
