/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.common.base;

import com.google.common.collect.Sets;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingjdbc.common.env.DatabaseEnvironment;
import io.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingConnection;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.h2.tools.RunScript;
import org.junit.AfterClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractSQLTest {
    
    protected static ShardingDataSource shardingDataSource;
    
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
    
    static Set<DatabaseType> getDatabaseTypes() {
        return databaseTypes;
    }
    
    private static String getDatabaseName(final String dataSetFile) {
        String fileName = new File(dataSetFile).getName();
        if (-1 == fileName.lastIndexOf(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
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
    
    protected abstract List<String> getInitDataSetFiles();
    
    protected final Map<DatabaseType, Map<String, DataSource>> createDataSourceMap() {
        for (String each : getInitDataSetFiles()) {
            String dbName = getDatabaseName(each);
            for (DatabaseType type : getDatabaseTypes()) {
                createDataSources(dbName, type);
            }
        }
        return databaseTypeMap;
    }
    
    private static Connection initialConnection(final String dbName, final DatabaseType type) throws SQLException {
        return buildDataSource(dbName, type).getConnection();
    }
    
    @AfterClass
    public static void clear() {
        if (shardingDataSource == null) {
            return;
        }
        shardingDataSource.close();
        shardingDataSource = null;
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
    
    protected final void importDataSet() {
        try {
            ShardingConnection conn = shardingDataSource.getConnection();
            RunScript.execute(conn, new InputStreamReader(AbstractSQLTest.class.getClassLoader().getResourceAsStream("integrate/cases/jdbc/jdbc_data.sql")));
            conn.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
}
