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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.database;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.wait.JDBCConnectionWaitStrategy;
import org.testcontainers.containers.BindMode;

import java.sql.DriverManager;

public final class PostgreSQLContainer extends DatabaseContainer {
    
    private static final DatabaseType DATABASE_TYPE = new PostgreSQLDatabaseType();
    
    private final String username = "scaling";
    
    private final String password = "root";
    
    private final int port = 5432;
    
    public PostgreSQLContainer(final String dockerImageName) {
        super(DATABASE_TYPE, dockerImageName);
    }
    
    @Override
    protected void configure() {
        withCommand("--max_connections=600");
        withCommand("--wal_level=logical");
        String rootUsername = "root";
        String rootPassword = "root";
        addEnv("POSTGRES_USER", rootUsername);
        addEnv("POSTGRES_PASSWORD", rootPassword);
        withClasspathResourceMapping("/env/postgresql/postgresql.conf", "/etc/postgresql/postgresql.conf", BindMode.READ_ONLY);
        withClasspathResourceMapping("/env/postgresql/initdb.sql", "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
        withExposedPorts(port);
        setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(DATABASE_TYPE, "localhost", getFirstMappedPort(), "postgres"),
                rootUsername, rootPassword)));
    }
    
    @Override
    public String getJdbcUrl(final String databaseName) {
        return DataSourceEnvironment.getURL(DATABASE_TYPE, getHost(), getFirstMappedPort(), databaseName);
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public int getPort() {
        return port;
    }
}
