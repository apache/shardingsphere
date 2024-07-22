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

package org.apache.shardingsphere.test.e2e.agent.engine.container;

import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 * MySQL container.
 */
// TODO Merge test container: merge with org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.MySQLContainer
public final class MySQLContainer extends DockerITContainer {
    
    private static final int EXPOSED_PORT = 3306;
    
    private static final String READY_USER = "root";
    
    private static final String READY_USER_PASSWORD = "123456";
    
    public MySQLContainer(final String image) {
        super("mysql", image);
    }
    
    @Override
    protected void configure() {
        withClasspathResourceMapping("/env/mysql/init.sql", "/docker-entrypoint-initdb.d/init.sql", BindMode.READ_ONLY);
        withExposedPorts(EXPOSED_PORT);
        setCommand("--sql_mode= --default-authentication-plugin=mysql_native_password");
        getContainerEnvironments().forEach(this::addEnv);
        setWaitStrategy(new JdbcConnectionWaitStrategy(
                () -> DriverManager.getConnection(DataSourceEnvironment.getURL(new MySQLDatabaseType(), getHost(), getFirstMappedPort()), READY_USER, READY_USER_PASSWORD)));
    }
    
    private Map<String, String> getContainerEnvironments() {
        Map<String, String> result = new HashMap<>(3, 1F);
        result.put("LANG", "C.UTF-8");
        result.put("MYSQL_ROOT_PASSWORD", READY_USER_PASSWORD);
        result.put("MYSQL_ROOT_HOST", "%");
        return result;
    }
    
    @Override
    public String getAbbreviation() {
        return "mysql";
    }
}
