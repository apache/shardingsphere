/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate;

import com.dangdang.ddframe.rdb.common.sql.base.AbstractSQLTest;
import com.dangdang.ddframe.rdb.integrate.sql.DatabaseTestSQL;
import com.dangdang.ddframe.rdb.integrate.util.DBUnitUtil;
import com.dangdang.ddframe.rdb.integrate.util.DataBaseEnvironment;
import com.dangdang.ddframe.rdb.integrate.util.ShardingJdbcDatabaseTester;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.dbcp.BasicDataSource;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Before;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.H2;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.MySQL;
import static com.dangdang.ddframe.rdb.sharding.constant.DatabaseType.Oracle;
import static org.dbunit.Assertion.assertEquals;

public abstract class AbstractDBUnitTest {
    
    protected static final DatabaseType CURRENT_DB_TYPE = H2;
    
    @Getter(AccessLevel.PROTECTED)
    private static DatabaseTestSQL databaseTestSQL = new DatabaseTestSQL();
    
    private static final Map<String, DataSource> DATA_SOURCES = new HashMap<>();
    
    private static final DataBaseEnvironment DB_ENV = new DataBaseEnvironment(CURRENT_DB_TYPE);
    
    static {
        AbstractSQLTest.createSchema();
    }
    
    @Before
    public final void importDataSet() throws Exception {
        for (String each : getDataSetFiles()) {
            InputStream is = AbstractDBUnitTest.class.getClassLoader().getResourceAsStream(each);
            IDataSet dataSet = new FlatXmlDataSetBuilder().build(new InputStreamReader(is));
            IDatabaseTester databaseTester = new ShardingJdbcDatabaseTester(DB_ENV.getDriverClassName(), DB_ENV.getURL(getDatabaseName(each)), 
                    DB_ENV.getUsername(), DB_ENV.getPassword(), DB_ENV.getSchema(getDatabaseName(each)));
            databaseTester.setDataSet(dataSet);
            databaseTester.onSetup();
        }
    }
    
    protected abstract List<String> getDataSetFiles();
    
    protected final DatabaseType currentDbType() {
        return H2 == CURRENT_DB_TYPE ? DatabaseType.MySQL : CURRENT_DB_TYPE;
    }
    
    protected final boolean isAliasSupport() {
        return H2 == DB_ENV.getDatabaseType() || MySQL == DB_ENV.getDatabaseType();
    }
    
    protected final Map<String, DataSource> createDataSourceMap(final String dataSourceNamePattern) {
        Map<String, DataSource> result = new HashMap<>(getDataSetFiles().size());
        for (String each : getDataSetFiles()) {
            String database = getDatabaseName(each);
            result.put(String.format(dataSourceNamePattern, database), createDataSource(database));
        }
        return result;
    }
    
    private static DataSource createDataSource(final String dataSource) {
        if (DATA_SOURCES.containsKey(dataSource)) {
            return DATA_SOURCES.get(dataSource);
        }
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(DB_ENV.getDriverClassName());
        result.setUrl(DB_ENV.getURL(dataSource));
        result.setUsername(DB_ENV.getUsername());
        result.setPassword(DB_ENV.getPassword());
        result.setMaxActive(1000);
        if (Oracle == DB_ENV.getDatabaseType()) {
            result.setConnectionInitSqls(Collections.singleton("ALTER SESSION SET CURRENT_SCHEMA = " + dataSource));
        }
        DATA_SOURCES.put(dataSource, result);
        return result;
    }
    
    private String getDatabaseName(final String dataSetFile) {
        String fileName = new File(dataSetFile).getName();
        if (-1 == fileName.lastIndexOf(".")) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }
    
    protected void assertDataSet(final String expectedDataSetFile, final Connection connection, final String actualTableName, final String sql, final Object... params) 
            throws SQLException, DatabaseUnitException {
        try (
                Connection conn = connection;
                PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 1;
            for (Object each : params) {
                ps.setObject(i++, each);
            }
            ITable actualTable = DBUnitUtil.getConnection(DB_ENV, connection).createTable(actualTableName, ps); 
            IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(new InputStreamReader(AbstractDBUnitTest.class.getClassLoader().getResourceAsStream(expectedDataSetFile)));
            assertEquals(expectedDataSet.getTable(actualTableName), actualTable);
        }
    }
}
