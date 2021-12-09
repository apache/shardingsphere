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

package org.apache.shardingsphere.example.proxy.distsql.feature.sharding;

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
public final class ShardingExecutor extends AbstractFeatureExecutor {
    
    private final static String ADD_RULE = "CREATE SHARDING TABLE RULE t_order (\n" +
            "RESOURCES(ds_0,ds_1),\n" +
            "SHARDING_COLUMN=order_id,\n" +
            "TYPE(NAME=hash_mod,PROPERTIES(\"sharding-count\"=4)),\n" +
            "GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES(\"worker-id\"=123)))\n" +
            ");";
    
    private final static String ALTER_RULE = "ALTER SHARDING TABLE RULE t_order (\n" +
            "RESOURCES(ds_0,ds_1),\n" +
            "SHARDING_COLUMN=order_id,\n" +
            "TYPE(NAME=hash_mod,PROPERTIES(\"sharding-count\"=5)),\n" +
            "GENERATED_KEY(COLUMN=another_id,TYPE(NAME=snowflake,PROPERTIES(\"worker-id\"=123)))\n" +
            ");";
    
    private final static String DROP_RULE = "DROP SHARDING TABLE RULE t_order;\n";
    
    private final static String DROP_ALGORITHM = "DROP SHARDING ALGORITHM t_order_hash_mod";
    
    private final static String SHOW_RULE = "show sharding table rules;";
    
    private final static String SHOW_ALGORITHM = "show sharding algorithms";
    
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
        executeShowAlgorithm();
        executeAlterRule();
        executeShowRule();
        executeShowAlgorithm();
        executeDropRule();
        executeDropAlgorithm();
        executeShowRule();
        executeShowAlgorithm();
    }
    
    private void executeShowRule() throws SQLException {
        log.info("show rule...");
        ResultSet resultSet = statement.executeQuery(SHOW_RULE);
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
    
    private void executeDropRule() throws SQLException {
        log.info("drop rule...");
        statement.execute(DROP_RULE);
    }
    
    private void executeDropAlgorithm() throws SQLException {
        log.info("drop algorithm...");
        statement.execute(DROP_ALGORITHM);
    }
}
