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

package org.apache.shardingsphere.test.integration.framework.container.atomic.storage.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.DockerStorageContainer;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * PostgreSQL container.
 */
public final class PostgreSQLContainer extends DockerStorageContainer {
    
    public PostgreSQLContainer(final String scenario) {
        super(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"), "postgres:12.6", scenario);
    }
    
    @Override
    protected void configure() {
        withCommand("--max_connections=200");
        addEnv("POSTGRES_USER", "root");
        addEnv("POSTGRES_PASSWORD", "root");
        super.configure();
    }
    
    @Override
    @SneakyThrows({ClassNotFoundException.class, SQLException.class, InterruptedException.class})
    protected void execute() {
        Class.forName(DataSourceEnvironment.getDriverClassName(getDatabaseType()));
        String url = DataSourceEnvironment.getURL(getDatabaseType(), getHost(), getMappedPort(getPort()));
        boolean connected = false;
        while (!connected) {
            try (Connection ignored = DriverManager.getConnection(url, "root", "root")) {
                connected = true;
                break;
            } catch (final PSQLException ex) {
                Thread.sleep(500L);
            }
        }
    }
    
    @Override
    public int getPort() {
        return 5432;
    }
}
