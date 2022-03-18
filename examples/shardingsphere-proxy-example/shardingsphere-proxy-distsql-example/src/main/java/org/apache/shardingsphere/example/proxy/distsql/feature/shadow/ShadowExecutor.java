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

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.example.proxy.distsql.feature.AbstractFeatureExecutor;

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
            "t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES(\"shadow\"=\"true\", foo=\"bar\"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES(\"operation\"=\"insert\",\"column\"=\"user_id\", \"regex\"='[1]')))), \n" +
            "t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES(\"shadow\"=\"true\", \"foo\"=\"bar\")))));";
    
    private static final String ALTER_RULE = "ALTER SHADOW RULE shadow_rule(\n" +
            "SOURCE=ds_1,\n" +
            "SHADOW=ds_0,\n" +
            "t_order((simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES(\"shadow\"=\"true\", foo=\"bar\"))),(TYPE(NAME=COLUMN_REGEX_MATCH, PROPERTIES(\"operation\"=\"insert\",\"column\"=\"user_id\", \"regex\"='[1]')))), \n" +
            "t_order_item((TYPE(NAME=SIMPLE_NOTE, PROPERTIES(\"shadow\"=\"true\", \"foo\"=\"bar\")))))";
    
    private static final String ALTER_ALGORITHM = "ALTER SHADOW ALGORITHM \n" +
            "(simple_note_algorithm, TYPE(NAME=SIMPLE_NOTE, PROPERTIES(\"shadow\"=\"true\", \"foo\"=\"bar\"))), \n" +
            "(shadow_rule_t_order_column_regex_match, TYPE(NAME=COLUMN_REGEX_MATCH,PROPERTIES(\"operation\"=\"insert\", \"column\"=\"user_id\", \"regex\"='[1]')));";
    
    private static final String DROP_RULE = "DROP SHADOW RULE shadow_rule;";
    
    private static final String DROP_ALGORITHM = "DROP shadow algorithm simple_note_algorithm,shadow_rule_t_order_column_regex_match,shadow_rule_t_order_item_simple_note;";
    
    private static final String SHOW_RULE = "show shadow rules;";
    
    private static final String SHOW_TABLE_RULE = "show shadow table rules;";
    
    private static final String SHOW_ALGORITHM = "show shadow algorithms";
    
    @Override
    public void init(Statement statement) {
        this.statement = statement;
    }
    
    @Override
    public void execute() throws SQLException, InterruptedException {
        executeUseSchema();
        executeShowRule();
        executeAddRule();
        executeShowRule();
        executeShowTableRule();
        executeShowAlgorithm();
        executeAlterRule();
        executeAlterAlgorithm();
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
        log.info(new Gson().toJson(getResultData(resultSet)));
    }
    
    private void executeShowTableRule() throws SQLException {
        log.info("show table rule...");
        ResultSet resultSet = statement.executeQuery(SHOW_TABLE_RULE);
        log.info(new Gson().toJson(getResultData(resultSet)));
    }
    
    private void executeShowAlgorithm() throws SQLException {
        log.info("show algorithm...");
        ResultSet resultSet = statement.executeQuery(SHOW_ALGORITHM);
        log.info(new Gson().toJson(getResultData(resultSet)));
    }
    
    private void executeAddRule() throws SQLException {
        log.info("add rule...");
        statement.execute(ADD_RULE);
    }
    
    private void executeAlterRule() throws SQLException {
        log.info("alter rule...");
        statement.execute(ALTER_RULE);
    }
    
    private void executeAlterAlgorithm() throws SQLException {
        log.info("alter algorithm...");
        statement.execute(ALTER_ALGORITHM);
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
