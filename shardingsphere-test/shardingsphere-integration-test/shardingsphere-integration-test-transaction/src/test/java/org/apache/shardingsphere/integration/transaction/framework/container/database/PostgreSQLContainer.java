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

package org.apache.shardingsphere.integration.transaction.framework.container.database;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.transaction.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.transaction.env.enums.TransactionITEnvTypeEnum;
import org.apache.shardingsphere.test.integration.env.container.wait.JDBCConnectionWaitStrategy;
import org.apache.shardingsphere.test.integration.env.runtime.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;

import java.sql.DriverManager;

/**
 * PostgreSQL container for Transaction IT.
 */
public final class PostgreSQLContainer extends DatabaseContainer {
    
    private static final DatabaseType DATABASE_TYPE = new PostgreSQLDatabaseType();
    
    private final String username = "root";
    
    private final String password = "root";
    
    private final int port = 5432;
    
    public PostgreSQLContainer(final String dockerImageName) {
        super(DATABASE_TYPE, dockerImageName);
    }
    
    @Override
    protected void configure() {
        withCommand("--max_connections=600 --max_prepared_transactions=600 --wal_level=logical");
        addEnv("POSTGRES_USER", username);
        addEnv("POSTGRES_PASSWORD", password);
        withClasspathResourceMapping("/env/postgresql/postgresql.conf", "/etc/postgresql/postgresql.conf", BindMode.READ_ONLY);
        withExposedPorts(port);
        if (TransactionITEnvTypeEnum.NATIVE == IntegrationTestEnvironment.getInstance().getItEnvType()) {
            addFixedExposedPort(port, port);
        }
        setWaitStrategy(new JDBCConnectionWaitStrategy(() -> DriverManager.getConnection(DataSourceEnvironment.getURL(DATABASE_TYPE, "localhost", getFirstMappedPort(), "postgres"),
                username, password)));
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
    
    @Override
    public String getAbbreviation() {
        return "postgresql";
    }
}
