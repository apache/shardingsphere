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

package org.apache.shardingsphere.example.proxy.distsql.feature.discovery;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.example.proxy.distsql.feature.FeatureExecutor;
import org.apache.shardingsphere.example.proxy.distsql.feature.StatementHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public final class DBDiscoveryExecutor extends StatementHolder implements FeatureExecutor {
    
    private final static String ADD_RULE = "CREATE DB_DISCOVERY RULE ha_group_0 (\n" +
            "RESOURCES(ds_0,ds_1),\n" +
            "TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec',keepAliveCron=''))\n" +
            ");";
    
    private final static String ALTER_RULE = "ALTER DB_DISCOVERY RULE ha_group_0 (\n" +
            "RESOURCES(ds_0),\n" +
            "TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec' ,keepAliveCron=''))\n" +
            ");";
    
    private final static String DROP_RULE = "DROP DB_DISCOVERY RULE ha_group_0;";
    
    private final static String SHOW_RULE = "show db_discovery rules;";
    
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
        executeAlterRule();
        executeShowRule();
        executeDropRule();
        executeShowRule();
    }
    
    private void executeShowRule() throws SQLException {
        log.info("show rule...");
        ResultSet resultSet = statement.executeQuery(SHOW_RULE);
        log.info(new Gson().toJson(getResultData(resultSet)));
    }
    
    private void executeAddRule() throws SQLException, InterruptedException {
        log.info("add rule...");
        statement.execute(ADD_RULE);
        waitingRenew();
    }
    
    private void executeAlterRule() throws SQLException, InterruptedException {
        log.info("alter rule...");
        statement.execute(ALTER_RULE);
        waitingRenew();
    }
    
    private void executeDropRule() throws SQLException, InterruptedException {
        log.info("drop rule...");
        statement.execute(DROP_RULE);
        waitingRenew();
    }
}
