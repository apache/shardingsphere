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

package com.dangdang.ddframe.rdb.common.base;

import com.dangdang.ddframe.rdb.common.env.DatabaseEnvironment;
import com.dangdang.ddframe.rdb.common.env.ShardingTestStrategy;
import com.dangdang.ddframe.rdb.common.util.DBUnitUtil;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLAssertData;
import com.dangdang.ddframe.rdb.integrate.jaxb.SQLShardingRule;
import com.dangdang.ddframe.rdb.integrate.jaxb.helper.SQLAssertJAXBHelper;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.dangdang.ddframe.rdb.common.util.SqlPlaceholderUtil.replacePreparedStatement;
import static com.dangdang.ddframe.rdb.common.util.SqlPlaceholderUtil.replaceStatement;
import static org.dbunit.Assertion.assertEquals;

@RunWith(Parameterized.class)
public abstract class AbstractSQLAssertTest extends AbstractSQLTest {
    
    private final String testCaseName;
    
    @Getter
    private final String sql;
    
    private final DatabaseType type;
    
    private final List<SQLShardingRule> shardingRules;
    
    protected AbstractSQLAssertTest(final String testCaseName, final String sql, final DatabaseType type, final List<SQLShardingRule> shardingRules) {
        this.testCaseName = testCaseName;
        this.sql = sql;
        this.type = type;
        this.shardingRules = shardingRules;
    }
    
    @Parameterized.Parameters(name = "{0}In{2}")
    public static Collection<Object[]> dataParameters() {
        return SQLAssertJAXBHelper.getDataParameters("integrate/assert");
    }
    
    @Override
    public DatabaseType getCurrentDatabaseType() {
        return type;
    }
    
    protected abstract ShardingTestStrategy getShardingStrategy();
    
    protected abstract Map<DatabaseType, ShardingDataSource> getShardingDataSources();
    
    @Test
    public void assertWithPreparedStatement() {
        execute(true);
    }
    
    @Test
    public void assertWithStatement() {
        execute(false);
    }
    
    private void execute(final boolean isPreparedStatement) {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : getShardingDataSources().entrySet()) {
            if (getCurrentDatabaseType() == each.getKey()) {
                try {
                    executeAndAssertSQL(isPreparedStatement, each.getValue());
                    //CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    //CHECKSTYLE:ON
                    if (ex.getMessage().startsWith("Dynamic table")) {
                        continue;
                    }
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    private void executeAndAssertSQL(final boolean isPreparedStatement, final ShardingDataSource shardingDataSource) throws MalformedURLException, SQLException, DatabaseUnitException {
        for (SQLShardingRule sqlShardingRule : shardingRules) {
            if (!needAssert(sqlShardingRule)) {
                continue;
            }
            for (SQLAssertData each : sqlShardingRule.getData()) {
                File expectedDataSetFile = getExpectedFile(each.getExpected());
                if (sql.toUpperCase().startsWith("SELECT")) {
                    assertDqlSql(isPreparedStatement, shardingDataSource, each, expectedDataSetFile);
                } else  {
                    assertDmlAndDdlSql(isPreparedStatement, shardingDataSource, each, expectedDataSetFile);
                }
            }
        }
    }
    
    private File getExpectedFile(final String expected) {
        String strategyName = getShardingStrategy().name();
        // TODO DML和DQL保持一直，去掉DML中XML名称里面的placeholder
        String expectedFile = null == expected ? "integrate/dataset/EmptyTable.xml"
                : String.format("integrate/dataset/%s/expect/" + expected, strategyName, strategyName);
        URL url = AbstractSQLAssertTest.class.getClassLoader().getResource(expectedFile);
        if (null == url) {
            throw new RuntimeException("Wrong expected file:" + expectedFile);
        }
        return new File(url.getPath());
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
    
    private void assertDqlSql(final boolean isPreparedStatement, final ShardingDataSource shardingDataSource, final SQLAssertData data, final File expectedDataSetFile)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        if (isPreparedStatement) {
            executeQueryWithPreparedStatement(shardingDataSource, getParameters(data), expectedDataSetFile);
        } else {
            executeQueryWithStatement(shardingDataSource, getParameters(data), expectedDataSetFile);
        }
    }
    
    private void assertDmlAndDdlSql(final boolean isPreparedStatement, final ShardingDataSource shardingDataSource, final SQLAssertData data, final File expectedDataSetFile)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        if (isPreparedStatement) {
            executeWithPreparedStatement(shardingDataSource, getParameters(data));
        } else {
            executeWithStatement(shardingDataSource, getParameters(data));
        }
        try (Connection conn = shardingDataSource.getConnection().getConnection(getDataSourceName(data.getExpected()), getSqlType())) {
            assertResult(conn, expectedDataSetFile);
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
    
    private void executeWithPreparedStatement(final ShardingDataSource dataSource, final List<String> parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(sql))) {
            setParameters(preparedStatement, parameters);
            preparedStatement.execute();
        }
    }
    
    private void executeWithStatement(final ShardingDataSource dataSource, final List<String> parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(replaceStatement(sql, parameters.toArray()));
        }
    }
    
    private void executeQueryWithPreparedStatement(final ShardingDataSource dataSource, final List<String> parameters, final File file)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(replacePreparedStatement(sql))) {
            setParameters(preparedStatement, parameters);
            ReplacementDataSet expectedDataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(file));
            expectedDataSet.addReplacementObject("[null]", null);
            for (ITable each : expectedDataSet.getTables()) {
                String tableName = each.getTableMetaData().getTableName();
                ITable actualTable = DBUnitUtil.getConnection(new DatabaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createTable(tableName, preparedStatement);
                assertEquals(expectedDataSet.getTable(tableName), actualTable);
            }
        }
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final List<String> parameters) throws SQLException {
        int index = 1;
        for (String each : parameters) {
            if (each.contains("'")) {
                preparedStatement.setString(index++, each.replace("'", ""));
            } else {
                preparedStatement.setInt(index++, Integer.valueOf(each));
            }
        }
    }
    
    private void executeQueryWithStatement(final ShardingDataSource dataSource, final List<String> parameters, final File file) throws MalformedURLException, SQLException, DatabaseUnitException {
        try (Connection conn = dataSource.getConnection()) {
            String querySql = replaceStatement(sql, parameters.toArray());
            ReplacementDataSet expectedDataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(file));
            expectedDataSet.addReplacementObject("[null]", null);
            for (ITable each : expectedDataSet.getTables()) {
                String tableName = each.getTableMetaData().getTableName();
                ITable actualTable = DBUnitUtil.getConnection(new DatabaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createQueryTable(tableName, querySql);
                assertEquals(expectedDataSet.getTable(tableName), actualTable);
            }
        }
    }
    
    private void assertResult(final Connection connection, final File file) throws MalformedURLException, SQLException, DatabaseUnitException {
        if (sql.contains("TEMP")) {
            return;
        }
        ITableIterator expectedTableIterator = new FlatXmlDataSetBuilder().build(file).iterator();
        try (Connection conn = connection) {
            while (expectedTableIterator.next()) {
                ITable expectedTable = expectedTableIterator.getTable();
                String actualTableName = expectedTable.getTableMetaData().getTableName();
                String verifySql = "SELECT * FROM " + actualTableName + " WHERE status = '" + getStatus(file) + "'";
                ITable actualTable = DBUnitUtil.getConnection(new DatabaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createQueryTable(actualTableName, verifySql);
                assertEquals(expectedTable, actualTable);
            }
        }
    }
    
    private String getStatus(final File file) {
        if (sql.toUpperCase().startsWith("DELETE")) {
            return ShardingTestStrategy.masterslave == getShardingStrategy() ? "init_master" : "init";
        }
        return file.getParentFile().getName();
    }
}
