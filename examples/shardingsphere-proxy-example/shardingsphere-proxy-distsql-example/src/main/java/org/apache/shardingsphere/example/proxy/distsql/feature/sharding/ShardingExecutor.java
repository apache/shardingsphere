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
public final class ShardingExecutor extends AbstractFeatureExecutor {
    
    private static final String ADD_RULE = "CREATE SHARDING TABLE RULE t_order (\n" +
            "STORAGE_UNITS(ds_0,ds_1),\n" +
            "SHARDING_COLUMN=order_id,\n" +
            "TYPE(NAME=hash_mod,PROPERTIES(\"sharding-count\"=4)),\n" +
            "KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME=snowflake))\n" +
            ")";
    
    private static final String ALTER_RULE = "ALTER SHARDING TABLE RULE t_order (\n" +
            "STORAGE_UNITS(ds_0,ds_1),\n" +
            "SHARDING_COLUMN=order_id,\n" +
            "TYPE(NAME=hash_mod,PROPERTIES(\"sharding-count\"=5)),\n" +
            "KEY_GENERATE_STRATEGY(COLUMN=another_id,TYPE(NAME=snowflake))\n" +
            ")";
    
    private static final String DROP_RULE = "DROP SHARDING TABLE RULE t_order";
    
    private static final String DROP_ALGORITHM = "DROP SHARDING ALGORITHM t_order_hash_mod";
    
    private static final String SHOW_RULE = "SHOW SHARDING TABLE RULES";
    
    private static final String SHOW_ALGORITHM = "SHOW SHARDING ALGORITHMS";
    
    private static final String DROP_KEY_GENERATOR = "DROP SHARDING KEY GENERATOR t_order_snowflake";
    
    private static final String SHOW_KEY_GENERATORS = "SHOW SHARDING KEY GENERATORS";
    
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
        executeShowAlgorithm();
        executeAlterRule();
        executeShowRule();
        executeShowAlgorithm();
        executeDropRule();
        executeDropAlgorithm();
        executeShowRule();
        executeShowAlgorithm();
        executeShowKeyGenerators();
        executeDropKeyGenerator();
        executeShowKeyGenerators();
    }
    
    private void executeShowRule() throws SQLException {
        log.info("show rule...");
        ResultSet resultSet = statement.executeQuery(SHOW_RULE);
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
    
    private void executeShowKeyGenerators() throws SQLException {
        log.info("show sharding key generators...");
        ResultSet resultSet = statement.executeQuery(SHOW_KEY_GENERATORS);
        log.info(JsonUtils.toJsonString(getResultData(resultSet)));
    }
    
    private void executeDropKeyGenerator() throws SQLException {
        log.info("drop sharding key generator...");
        statement.execute(DROP_KEY_GENERATOR);
    }
}
