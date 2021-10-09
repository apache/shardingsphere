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

package org.apache.shardingsphere.example.proxy.distsql.feature.resource;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.example.proxy.distsql.feature.FeatureExecutor;
import org.apache.shardingsphere.example.proxy.distsql.feature.StatementHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public final class ResourceExecutor extends StatementHolder implements FeatureExecutor {
    
    private final static String ADD_RESOURCE = "ADD RESOURCE resource_0 (\n" +
            "    URL=\"jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false\",\n" +
            "    USER=root,\n" +
            "    PASSWORD=root,\n" +
            "    PROPERTIES(\"maximumPoolSize\"=10,\"idleTimeout\"=\"30000\")\n" +
            "),resource_1 (\n" +
            "    URL=\"jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false\",\n" +
            "    USER=root,\n" +
            "    PASSWORD=root,\n" +
            "    PROPERTIES(\"maximumPoolSize\"=10,\"idleTimeout\"=\"30000\")\n" +
            ");";
    
    private final static String ALTER_RESOURCE = "ALTER RESOURCE resource_0 (\n" +
            "    HOST=127.0.0.1,\n" +
            "    PORT=3306,\n" +
            "    DB=demo_ds_1,\n" +
            "    USER=root,\n" +
            "    PASSWORD=root\n" +
            ");";
    
    private final static String DROP_RESOURCE = "DROP RESOURCE resource_0, resource_1;";
    
    private final static String SHOW_RESOURCE = "show resources;";
    
    @Override
    public void init(Statement statement) {
        this.statement = statement;
    }
    
    @Override
    public void execute() throws SQLException, InterruptedException {
        executeUseSchema();
        executeShowResources();
        executeAddResource();
        executeShowResources();
        executeAlterResource();
        executeShowResources();
        executeDropResource();
        executeShowResources();
    }
    
    private void executeShowResources() throws SQLException {
        log.info("show resource...");
        ResultSet resultSet = statement.executeQuery(SHOW_RESOURCE);
        log.info(new Gson().toJson(getResultData(resultSet)));
    }
    
    private void executeAddResource() throws SQLException, InterruptedException {
        log.info("add resource...");
        statement.execute(ADD_RESOURCE);
        waitingRenew();
    }
    
    private void executeAlterResource() throws SQLException, InterruptedException {
        log.info("alter resource...");
        statement.execute(ALTER_RESOURCE);
        waitingRenew();
    }
    
    private void executeDropResource() throws SQLException, InterruptedException {
        log.info("drop resource...");
        statement.execute(DROP_RESOURCE);
        waitingRenew();
    }
}
