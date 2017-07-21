/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.common.sql.base;

import com.dangdang.ddframe.rdb.common.sql.DatabaseMode;
import com.dangdang.ddframe.rdb.integrate.AbstractDBUnitTest;
import com.dangdang.ddframe.rdb.integrate.util.DataBaseEnvironment;
import com.dangdang.ddframe.rdb.integrate.util.ShardingJdbcDatabaseTester;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.h2.tools.RunScript;
import org.junit.Before;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dangdang.ddframe.rdb.common.sql.DatabaseMode.Local;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.H2;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.Oracle;

public abstract class AbstractBaseSqlTest {
    
    private static final DatabaseMode CURRENT_DB_TYPE = Local;
    
    private static final Map<String, Map<DatabaseType, DataSource>> DATA_SOURCES = new HashMap<>();
    
    //TODO add back after finished refactor
    static {
        createSchema();
    }

    private static void createSchema() {
        for (DatabaseType each : CURRENT_DB_TYPE.databaseTypes()) {
            if (H2 == each) {
                createSchema(each);
            }
        }
    }

    private static void createSchema(final DatabaseType dbType) {
        try {
            Connection conn;
            for (int i = 0; i < 10; i++) {
                for (String database : Arrays.asList("db", "dbtbl", "nullable", "master", "slave")) {
                    conn = createDataSource(database + "_" + i, dbType).getConnection();
                    RunScript.execute(conn, new InputStreamReader(AbstractDBUnitTest.class.getClassLoader().getResourceAsStream("integrate/schema/table/" + database + ".sql")));
                    conn.close();
                }
            }
            String database = "tbl";
            conn = createDataSource(database, dbType).getConnection();
            RunScript.execute(conn, new InputStreamReader(AbstractDBUnitTest.class.getClassLoader().getResourceAsStream("integrate/schema/table/tbl.sql")));
            conn.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    @Before
    public final void importDataSet() throws Exception {
        for (DatabaseType databaseType : CURRENT_DB_TYPE.databaseTypes()) {
            DataBaseEnvironment dbEnv = new DataBaseEnvironment(databaseType);
            for (String each : getDataSetFiles()) {
                InputStream is = AbstractDBUnitTest.class.getClassLoader().getResourceAsStream(each);
                IDataSet dataSet = new FlatXmlDataSetBuilder().build(new InputStreamReader(is));
                IDatabaseTester databaseTester = new ShardingJdbcDatabaseTester(dbEnv.getDriverClassName(), dbEnv.getURL(getDatabaseName(each)),
                        dbEnv.getUsername(), dbEnv.getPassword(), dbEnv.getSchema(getDatabaseName(each)));
                databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
                databaseTester.setDataSet(dataSet);
                databaseTester.onSetup();
            }
        }
    }
    
    protected abstract List<String> getDataSetFiles();
    
    protected final Map<String, Map<DatabaseType, DataSource>> createDataSourceMap() {
        for (String each : getDataSetFiles()) {
            String dbName = getDatabaseName(each);
            for (DatabaseType type : CURRENT_DB_TYPE.databaseTypes()) {
                createDataSource(dbName, type);
            }
        }
        return DATA_SOURCES;
    }
    
    private static DataSource createDataSource(final String dbName, final DatabaseType type) {
        String dataSource = "dataSource_" + dbName;
        if (DATA_SOURCES.containsKey(dataSource) && DATA_SOURCES.get(dataSource).containsKey(type.name())) {
            return DATA_SOURCES.get(dataSource).get(type);
        }
        Map<DatabaseType, DataSource> dbTypeMap = DATA_SOURCES.get(dataSource);
        if (null == dbTypeMap) {
            dbTypeMap = new HashMap<>();
            DATA_SOURCES.put(dataSource, dbTypeMap);
        }
        DataBaseEnvironment dbEnv = new DataBaseEnvironment(type);
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(dbEnv.getDriverClassName());
        result.setUrl(dbEnv.getURL(dbName));
        result.setUsername(dbEnv.getUsername());
        result.setPassword(dbEnv.getPassword());
        result.setMaxActive(1000);
        if (Oracle == dbEnv.getDatabaseType()) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dbName));
        }
        dbTypeMap.put(type, result);
        return result;
    }
    
    private String getDatabaseName(final String dataSetFile) {
        String fileName = new File(dataSetFile).getName();
        if (-1 == fileName.lastIndexOf(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
}
