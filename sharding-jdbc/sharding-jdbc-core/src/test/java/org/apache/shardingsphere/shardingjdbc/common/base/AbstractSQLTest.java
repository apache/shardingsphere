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

package org.apache.shardingsphere.shardingjdbc.common.base;

import com.google.common.collect.Sets;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingjdbc.common.env.DatabaseEnvironment;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractSQLTest {
    
    private static Set<DatabaseType> databaseTypes = Sets.newHashSet(DatabaseType.H2);
    
    private final Map<DatabaseType, Map<String, DataSource>> databaseTypeMap = new HashMap<>();
    
    static {
        init();
    }
    
    private static synchronized void init() {
        try {
            Properties prop = new Properties();
            prop.load(AbstractSQLTest.class.getClassLoader().getResourceAsStream("integrate/env.properties"));
            createJdbcSchema(DatabaseType.H2);
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static void createJdbcSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 2; i++) {
                conn = initialConnection("jdbc_" + i, dbType);
                RunScript.execute(conn, new InputStreamReader(AbstractSQLTest.class.getClassLoader().getResourceAsStream("integrate/cases/jdbc/jdbc_init.sql")));
                conn.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static Connection initialConnection(final String dbName, final DatabaseType type) throws SQLException {
        return buildDataSource(dbName, type).getConnection();
    }
    
    private static BasicDataSource buildDataSource(final String dbName, final DatabaseType type) {
        DatabaseEnvironment dbEnv = new DatabaseEnvironment(type);
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(dbEnv.getDriverClassName());
        result.setUrl(dbEnv.getURL(dbName));
        result.setUsername(dbEnv.getUsername());
        result.setPassword(dbEnv.getPassword());
        result.setMaxTotal(50);
        return result;
    }
    
    protected final Map<DatabaseType, Map<String, DataSource>> createDataSourceMap(final Collection<String> databaseNames) {
        for (String each : databaseNames) {
            String dbName = getDatabaseName(each);
            for (DatabaseType type : databaseTypes) {
                createDataSources(dbName, type);
            }
        }
        return databaseTypeMap;
    }
    
    private void createDataSources(final String dbName, final DatabaseType type) {
        String dataSource = "dataSource_" + dbName;
        Map<String, DataSource> dataSourceMap = databaseTypeMap.get(type);
        if (null == dataSourceMap) {
            dataSourceMap = new HashMap<>();
            databaseTypeMap.put(type, dataSourceMap);
        }
        BasicDataSource result = buildDataSource(dbName, type);
        dataSourceMap.put(dataSource, result);
    }
}
