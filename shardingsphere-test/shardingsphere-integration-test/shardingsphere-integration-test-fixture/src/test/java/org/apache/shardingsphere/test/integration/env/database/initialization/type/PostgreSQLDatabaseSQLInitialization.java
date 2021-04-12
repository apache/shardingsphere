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

package org.apache.shardingsphere.test.integration.env.database.initialization.type;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.database.initialization.DatabaseSQLInitialization;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Database SQL initialization for PostgreSQL.
 */
public final class PostgreSQLDatabaseSQLInitialization implements DatabaseSQLInitialization {
    
    @Override
    public void executeInitSQLs(final String scenario, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) throws IOException, JAXBException, SQLException {
        File file = new File(EnvironmentPath.getInitSQLFile(databaseType, scenario));
        for (Map.Entry<String, DataSource> each : dataSourceMap.entrySet()) {
            try (Connection connection = each.getValue().getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(String.format("SELECT pg_terminate_backend (pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '%s';", each));
                statement.execute(String.format("DROP DATABASE IF EXISTS %s;", each.getKey()));
                statement.execute(String.format("CREATE DATABASE %s;", each.getKey()));
            }
            try (Connection connection = each.getValue().getConnection(); FileReader reader = new FileReader(file)) {
                RunScript.execute(connection, reader);
            }
        }
    }
    
    @Override
    public String getType() {
        return new PostgreSQLDatabaseType().getName();
    }
}
