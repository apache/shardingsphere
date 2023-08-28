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

package org.apache.shardingsphere.example.proxy.distsql.feature.shadow;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.example.proxy.distsql.feature.AbstractFeatureExecutor;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * Please make sure that resource ds_0, ds_1 and schema example_db have been added to Proxy
 */
@Slf4j
public final class ShadowExecutor extends AbstractFeatureExecutor {
    
    private static final String ADD_RULE = "CREATE SHADOW RULE shadow_rule(\n" +
            "SOURCE=ds_0,\n" +
            "SHADOW=ds_1,\n" +
            "t_order(TYPE(NAME=SQL_HINT),TYPE(NAME=REGEX_MATCH, PROPERTIES(\"operation\"=\"insert\",\"column\"=\"user_id\", \"regex\"='[1]'))), \n" +
            "t_order_item(TYPE(NAME=SQL_HINT)));";
    
    private static final String ALTER_RULE = "ALTER SHADOW RULE shadow_rule(\n" +
            "SOURCE=ds_1,\n" +
            "SHADOW=ds_0,\n" +
            "t_order(TYPE(NAME=SQL_HINT),TYPE(NAME=REGEX_MATCH, PROPERTIES(\"operation\"=\"insert\",\"column\"=\"user_id\", \"regex\"='[1]'))), \n" +
            "t_order_item(TYPE(NAME=SQL_HINT)))";
    
    private static final String DROP_RULE = "DROP SHADOW RULE shadow_rule";
    
    private static final String DROP_ALGORITHM = "DROP SHADOW ALGORITHM sql_hint_algorithm,shadow_rule_t_order_regex_match,shadow_rule_t_order_item_sql_hint";
    
    private static final String SHOW_RULE = "SHOW SHADOW RULES";
    
    private static final String SHOW_TABLE_RULE = "SHOW SHADOW TABLE RULES";
    
    private static final String SHOW_ALGORITHM = "SHOW SHADOW ALGORITHMS";
    
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
        executeShowTableRule();
        executeShowAlgorithm();
        executeAlterRule();
        executeShowRule();
        executeShowTableRule();
        executeShowAlgorithm();
        executeDropRule();
        executeDropAlgorithm();
        executeShowRule();
        executeShowTableRule();
        executeShowAlgorithm();
    }
    
    private void executeShowRule() throws SQLException {
        log.info("show rule...");
        ResultSet resultSet = statement.executeQuery(SHOW_RULE);
        log.info(JsonUtils.toJsonString(getResultData(resultSet)));
    }
    
    private void executeShowTableRule() throws SQLException {
        log.info("show table rule...");
        ResultSet resultSet = statement.executeQuery(SHOW_TABLE_RULE);
        log.info(JsonUtils.toJsonString(getResultData(resultSet)));
    }
    
    private void executeShowAlgorithm() throws SQLException {
        log.info("show algorithm...");
        ResultSet resultSet = statement.executeQuery(SHOW_ALGORITHM);
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
    
    private void executeDropAlgorithm() throws SQLException {
        log.info("drop algorithm...");
        statement.execute(DROP_ALGORITHM);
    }
}
