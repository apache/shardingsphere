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

package io.shardingsphere.core.common.base;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.shardingsphere.core.common.env.DatabaseEnvironment;
import io.shardingsphere.core.common.env.ShardingJdbcDatabaseTester;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.tools.RunScript;
import org.junit.AfterClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class AbstractSQLTest {
    
    private static Map<DatabaseType, ShardingDataSource> shardingDataSources = new HashMap<>();
    
    private static Set<DatabaseType> databaseTypes = Sets.newHashSet(DatabaseType.H2);
    
    private final Map<DatabaseType, Map<String, DataSource>> databaseTypeMap = new HashMap<>();
    
    static {
        init();
    }
    
    protected static Map<DatabaseType, ShardingDataSource> getShardingDataSources() {
        return shardingDataSources;
    }
    
    private static synchronized void init() {
        try {
            Properties prop = new Properties();
            prop.load(AbstractSQLTest.class.getClassLoader().getResourceAsStream("integrate/env.properties"));
            boolean initialized = null == prop.getProperty("initialized") ? false : Boolean.valueOf(prop.getProperty("initialized"));
            String databases = prop.getProperty("databases");
            if (!Strings.isNullOrEmpty(databases)) {
                for (String each : databases.split(",")) {
                    databaseTypes.add(findDatabaseType(each.trim()));
                }
            }
            if (initialized) {
                createJdbcSchema(DatabaseType.H2);
            } else {
                for (DatabaseType each : getDatabaseTypes()) {
                    createJdbcSchema(each);
                }
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private static DatabaseType findDatabaseType(final String databaseType) {
        for (DatabaseType each : DatabaseType.values()) {
            if (each.name().equalsIgnoreCase(databaseType)) {
                return each;
            }
        }
        throw new RuntimeException("Can't find database type of:" + databaseType);
    }
    
    private static void createJdbcSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 2; i++) {
                conn = initialConnection("jdbc_" + i, dbType);
                RunScript.execute(conn, new InputStreamReader(AbstractSQLTest.class.getClassLoader().getResourceAsStream("integrate/schema/table/jdbc.sql")));
                conn.close();
            }
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    static Set<DatabaseType> getDatabaseTypes() {
        return databaseTypes;
    }
    
    protected static String getDatabaseName(final String dataSetFile) {
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
        result.setMaxTotal(1);
        if (DatabaseType.Oracle == dbEnv.getDatabaseType()) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dbName));
        }
        return result;
    }
    
    protected abstract List<String> getInitDataSetFiles();
    
    protected abstract DatabaseType getCurrentDatabaseType();
    
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
    public static void clear() throws SQLException, ReflectiveOperationException {
        if (!shardingDataSources.isEmpty()) {
            for (ShardingDataSource each : shardingDataSources.values()) {
                each.close();
                closeDataSources(getDataSourceMap(each).values());
            }
            shardingDataSources.clear();
        }
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
    
    private static Map<String, DataSource> getDataSourceMap(final ShardingDataSource shardingDataSource)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field field = shardingDataSource.getClass().getDeclaredField("shardingContext");
        field.setAccessible(true);
        ShardingContext shardingContext = (ShardingContext) field.get(shardingDataSource);
        return shardingContext.getDataSourceMap();
    }
    
    private static void closeDataSources(final Collection<DataSource> dataSources) throws SQLException {
        for (DataSource each : dataSources) {
            if (each instanceof BasicDataSource) {
                ((BasicDataSource) each).close();
            } else if (each instanceof MasterSlaveDataSource) {
                closeDataSources(((MasterSlaveDataSource) each).getAllDataSources().values());
            }
        }
    }
    
    protected final void importDataSet() throws Exception {
        for (DatabaseType databaseType : getDatabaseTypes()) {
            if (databaseType == getCurrentDatabaseType() || null == getCurrentDatabaseType()) {
                DatabaseEnvironment dbEnv = new DatabaseEnvironment(databaseType);
                for (String each : getInitDataSetFiles()) {
                    InputStream is = AbstractSQLTest.class.getClassLoader().getResourceAsStream(each);
                    IDataSet dataSet = new FlatXmlDataSetBuilder().build(new InputStreamReader(is));
                    IDatabaseTester databaseTester = new ShardingJdbcDatabaseTester(dbEnv.getDriverClassName(), dbEnv.getURL(getDatabaseName(each)),
                            dbEnv.getUsername(), dbEnv.getPassword(), dbEnv.getSchema(getDatabaseName(each)));
                    databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
                    databaseTester.setDataSet(dataSet);
                    databaseTester.onSetup();
                }
            }
        }
    }
}
