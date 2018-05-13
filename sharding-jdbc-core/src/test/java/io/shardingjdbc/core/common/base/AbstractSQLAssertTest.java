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

package io.shardingjdbc.core.common.base;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.shardingjdbc.core.common.env.DatabaseEnvironment;
import io.shardingjdbc.core.common.env.ShardingJdbcDatabaseTester;
import io.shardingjdbc.core.common.env.ShardingTestStrategy;
import io.shardingjdbc.core.common.util.SQLAssertHelper;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.integrate.jaxb.SQLAssertData;
import io.shardingjdbc.core.integrate.jaxb.SQLShardingRule;
import io.shardingjdbc.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import lombok.Getter;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractSQLAssertTest extends AbstractSQLTest {
    
    private final String testCaseName;
    
    @Getter
    private final String sql;
    
    private final DatabaseType type;
    
    private final List<SQLShardingRule> shardingRules;
    
    private final SQLAssertHelper sqlAssertHelper;
    
    protected AbstractSQLAssertTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> shardingRules) {
        this.testCaseName = testCaseName;
        this.sql = sql;
        this.type = type;
        this.shardingRules = shardingRules;
        sqlAssertHelper = new SQLAssertHelper(sql);
    }
    
    protected static void importAllDataSet(final List<String> dataSetFiles) throws Exception {
        for (DatabaseType databaseType : getDatabaseTypes()) {
            DatabaseEnvironment dbEnv = new DatabaseEnvironment(databaseType);
            for (String each : dataSetFiles) {
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
    
    protected abstract ShardingTestStrategy getShardingStrategy();
    
    protected abstract Map<DatabaseType, ? extends AbstractDataSourceAdapter> getDataSources() throws SQLException;
    
    protected File getExpectedFile(final String expected) {
        String strategyName = getShardingStrategy().name();
        String expectedFile = null == expected ? "integrate/dataset/EmptyTable.xml" : String.format("integrate/dataset/sharding/%s/expect/" + expected, strategyName, strategyName);
        URL url = AbstractSQLAssertTest.class.getClassLoader().getResource(expectedFile);
        if (null == url) {
            throw new RuntimeException("Wrong expected file:" + expectedFile);
        }
        return new File(url.getPath());
    }
    
    @Override
    public DatabaseType getCurrentDatabaseType() {
        return type;
    }
    
    @Before
    public void initDDLTables() throws SQLException {
        if (getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE") || getSql().startsWith("DROP TABLE") || getSql().startsWith("CREATE UNIQUE INDEX") || getSql().startsWith("CREATE INDEX")) {
            if (getSql().contains("TEMP")) {
                executeSQL("CREATE TEMPORARY TABLE t_temp_log(id int, status varchar(10))");
            } else {
                executeSQL("CREATE TABLE t_log(id int, status varchar(10))");
            }
        }
        if (getSql().startsWith("DROP INDEX")) {
            executeSQL("CREATE TABLE t_log(id int, status varchar(10))");
            executeSQL("CREATE INDEX t_log_index ON t_log(status)");
        }
    }
    
    @After
    public void cleanupDDLTables() throws SQLException {
        if (getSql().startsWith("CREATE UNIQUE INDEX") || getSql().startsWith("CREATE INDEX")) {
            executeSQL("DROP TABLE t_log");
        } else if (getSql().startsWith("ALTER") || getSql().startsWith("TRUNCATE") || getSql().startsWith("CREATE") || getSql().startsWith("DROP INDEX")) {
            if (getSql().contains("TEMP")) {
                executeSQL("DROP TABLE t_temp_log");
            } else {
                executeSQL("DROP TABLE t_log");
            }
        }
    }
    
    private void executeSQL(final String sql) throws SQLException {
        for (Map.Entry<DatabaseType, ? extends AbstractDataSourceAdapter> each : getDataSources().entrySet()) {
            if (getCurrentDatabaseType() == each.getKey()) {
                try (Connection conn = each.getValue().getConnection();
                     Statement statement = conn.createStatement()) {
                    statement.execute(sql);
                    //CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    //CHECKSTYLE:ON
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    @Test
    public void assertExecuteWithPreparedStatement() throws SQLException {
        execute(true, false);
    }
    
    @Test
    public void assertExecuteWithStatement() throws SQLException {
        execute(false, false);
    }
    
    @Test
    public void assertExecuteQueryWithPreparedStatement() throws SQLException {
        execute(true, true);
    }
    
    @Test
    public void assertExecuteQueryWithStatement() throws SQLException {
        execute(false, true);
    }
    
    private void execute(final boolean isPreparedStatement, final boolean isExecute) throws SQLException {
        for (Map.Entry<DatabaseType, ? extends AbstractDataSourceAdapter> each : getDataSources().entrySet()) {
            if (getCurrentDatabaseType() == each.getKey()) {
                try {
                    executeAndAssertSQL(isPreparedStatement, isExecute, each.getValue());
                    //CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    //CHECKSTYLE:ON
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    private void executeAndAssertSQL(final boolean isPreparedStatement, final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter) 
            throws MalformedURLException, SQLException, DatabaseUnitException {
        for (SQLShardingRule sqlShardingRule : shardingRules) {
            if (!needAssert(sqlShardingRule)) {
                continue;
            }
            for (SQLAssertData each : sqlShardingRule.getData()) {
                File expectedDataSetFile = getExpectedFile(each.getExpected());
                if (sql.toUpperCase().startsWith("SELECT")) {
                    assertDQL(isPreparedStatement, isExecute, abstractDataSourceAdapter, each, expectedDataSetFile);
                } else  {
                    assertDMLAndDDL(isPreparedStatement, isExecute, abstractDataSourceAdapter, each, expectedDataSetFile);
                }
            }
        }
    }
    
    private boolean needAssert(final SQLShardingRule sqlShardingRule) {
        String shardingRules = sqlShardingRule.getValue();
        if (null == shardingRules) {
            return true;
        }
        for (String each : shardingRules.split(",")) {
            if (getShardingStrategy().name().equals(each)) {
                return true;
            }
        }
        return false;
    }
    
    private void assertDQL(final boolean isPreparedStatement, final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter,
                           final SQLAssertData data, final File expectedDataSetFile)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        if (isPreparedStatement) {
            sqlAssertHelper.executeQueryWithPreparedStatement(isExecute, abstractDataSourceAdapter, getParameters(data), expectedDataSetFile);
        } else {
            sqlAssertHelper.executeQueryWithStatement(abstractDataSourceAdapter, getParameters(data), expectedDataSetFile);
        }
    }
    
    private void assertDMLAndDDL(final boolean isPreparedStatement, final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter,
                                 final SQLAssertData data, final File expectedDataSetFile)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        if (isPreparedStatement) {
            sqlAssertHelper.executeWithPreparedStatement(isExecute, abstractDataSourceAdapter, getParameters(data));
        } else {
            sqlAssertHelper.executeWithStatement(isExecute, abstractDataSourceAdapter, getParameters(data));
        }
        try (Connection conn = abstractDataSourceAdapter instanceof MasterSlaveDataSource ? abstractDataSourceAdapter.getConnection() 
                : ((ShardingDataSource) abstractDataSourceAdapter).getConnection().getConnection(getDataSourceName(data.getExpected()), getSqlType())) {
            sqlAssertHelper.assertResult(conn, expectedDataSetFile);
        }
    }
    
    private SQLType getSqlType() {
        return ShardingTestStrategy.masterslave == getShardingStrategy() ? SQLType.DML : SQLType.DQL;
    }
    
    // TODO 标准化文件名
    private String getDataSourceName(final String expected) {
        String result = String.format(expected.split("/")[1].split(".xml")[0], getShardingStrategy().name());
        if (!result.contains("_")) {
            result = result + "_0";
        }
        if (result.startsWith("tbl")) {
            result = "tbl";
        }
        if (result.contains("masterslave")) {
            result = result.replace("masterslave", "ms");
        } else if (result.contains("hint")) {
            result = "dataSource_" + result.replace("hint", "db");
        } else {
            result = "dataSource_" + result;
        }
        return result;
    }
    
    private List<String> getParameters(final SQLAssertData data) {
        return Strings.isNullOrEmpty(data.getParameter()) ? Collections.<String>emptyList() : Lists.newArrayList(data.getParameter().split(","));
    }
}
