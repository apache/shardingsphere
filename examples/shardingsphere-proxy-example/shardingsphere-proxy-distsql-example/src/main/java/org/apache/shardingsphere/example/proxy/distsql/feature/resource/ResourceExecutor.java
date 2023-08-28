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
public final class ResourceExecutor extends AbstractFeatureExecutor {
    
    private static final String ADD_RESOURCE = "ADD RESOURCE resource_0 (\n" +
            "    URL=\"jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false\",\n" +
            "    USER=root,\n" +
            "    PASSWORD=root,\n" +
            "    PROPERTIES(\"maximumPoolSize\"=10,\"idleTimeout\"=\"30000\")\n" +
            "),resource_1 (\n" +
            "    URL=\"jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false\",\n" +
            "    USER=root,\n" +
            "    PASSWORD=root,\n" +
            "    PROPERTIES(\"maximumPoolSize\"=10,\"idleTimeout\"=\"30000\")\n" +
            ")";
    
    private static final String ALTER_RESOURCE = "ALTER RESOURCE resource_0 (\n" +
            "    HOST=127.0.0.1,\n" +
            "    PORT=3306,\n" +
            "    DB=demo_ds,\n" +
            "    USER=root,\n" +
            "    PASSWORD=root\n" +
            ")";
    
    private static final String DROP_RESOURCE = "DROP RESOURCE resource_0, resource_1";
    
    private static final String SHOW_RESOURCE = "SHOW SCHEMA RESOURCES";
    
    @Override
    public void init(Statement statement) {
        this.statement = statement;
    }
    
    @Override
    public void execute() throws SQLException {
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
        log.info("show schema resources...");
        ResultSet resultSet = statement.executeQuery(SHOW_RESOURCE);
        log.info(JsonUtils.toJsonString(getResultData(resultSet)));
    }
    
    private void executeAddResource() throws SQLException {
        log.info("add resource...");
        statement.execute(ADD_RESOURCE);
    }
    
    private void executeAlterResource() throws SQLException {
        log.info("alter resource...");
        statement.execute(ALTER_RESOURCE);
    }
    
    private void executeDropResource() throws SQLException {
        log.info("drop resource...");
        statement.execute(DROP_RESOURCE);
    }
}
