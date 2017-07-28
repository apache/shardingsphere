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

import com.dangdang.ddframe.rdb.common.jaxb.SqlAssertData;
import com.dangdang.ddframe.rdb.common.jaxb.SqlShardingRule;
import com.dangdang.ddframe.rdb.common.sql.common.ShardingTestStrategy;
import com.dangdang.ddframe.rdb.integrate.util.DBUnitUtil;
import com.dangdang.ddframe.rdb.integrate.util.DataBaseEnvironment;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dangdang.ddframe.rdb.common.sql.common.ShardingTestStrategy.masterslave;
import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;
import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replaceStatement;
import static org.dbunit.Assertion.assertEquals;

public abstract class AbstractSqlAssertTest extends AbstractBaseSqlTest {
    
    private final String sql;
    
    private final Set<DatabaseType> types;
    
    private final List<SqlShardingRule> shardingRules;
    
    protected AbstractSqlAssertTest(final String testCaseName, final String sql, final Set<DatabaseType> types, final List<SqlShardingRule> shardingRules) {
        this.sql = sql;
        this.types = types;
        this.shardingRules = shardingRules;
    }
    
    protected abstract ShardingTestStrategy getShardingStrategy();
    
    protected abstract Map<DatabaseType, ShardingDataSource> getShardingDataSources();
    
    @Test
    public void assertWithPreparedStatement() throws Exception {
        execute(true);
    }
    
    @Test
    public void assertWithStatement() throws Exception {
        execute(false);
    }
    
    private void execute(final boolean isPreparedStatement) throws Exception {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : getShardingDataSources().entrySet()) {
            if (types.size() == 0 || types.contains(each.getKey())) {
                try {
                    executeAndAssertSql(isPreparedStatement, each.getValue());
                    //CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    //CHECKSTYLE:ON
                    if (ex.getMessage().startsWith("Dynamic table")) {
                        continue;
                    }
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    private void executeAndAssertSql(final boolean isPreparedStatement, final ShardingDataSource shardingDataSource) throws Exception {
        for (SqlShardingRule sqlShardingRule : shardingRules) {
            if (needAssert(sqlShardingRule)) {
                for (SqlAssertData each : sqlShardingRule.getData()) {
                    String expected = each.getExpected() == null ? "integrate/dataset/EmptyTable.xml"
                            : String.format("integrate/dataset/%s/expect/" + each.getExpected(), getShardingStrategy().name(), getShardingStrategy().name());
                    URL url = AbstractSqlAssertTest.class.getClassLoader().getResource(expected);
                    if (null == url) {
                        throw new Exception("Wrong expected file:" + expected);
                    }
                    File expectedDataSetFile = new File(url.getPath());
                    if (sql.toUpperCase().startsWith("SELECT")) {
                        assertSelectSql(isPreparedStatement, shardingDataSource, each, expectedDataSetFile);
                    } else {
                        assertDmlSql(isPreparedStatement, shardingDataSource, each, expectedDataSetFile);
                    }
                }
            }
        }
    }
    
    private boolean needAssert(final SqlShardingRule sqlShardingRule) {
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
    
    private void assertSelectSql(final boolean isPreparedStatement, final ShardingDataSource shardingDataSource, final SqlAssertData data, final File expectedDataSetFile) throws Exception {
        if (isPreparedStatement) {
            executeQueryWithPreparedStatement(shardingDataSource, getParameters(data), expectedDataSetFile);
        } else {
            executeQueryWithStatement(shardingDataSource, getParameters(data), expectedDataSetFile);
        }
    }
    
    private void assertDmlSql(final boolean isPreparedStatement, final ShardingDataSource shardingDataSource, final SqlAssertData data, final File expectedDataSetFile) throws Exception {
        if (isPreparedStatement) {
            executeWithPreparedStatement(shardingDataSource, getParameters(data));
        } else {
            executeWithStatement(shardingDataSource, getParameters(data));
        }
        String dataSourceName = getDataSourceName(data.getExpected());
        SQLType sqlType = getSqlType();
        try (Connection conn = shardingDataSource.getConnection().getConnection(dataSourceName, sqlType)) {
            assertResult(conn, expectedDataSetFile);
        }
    }
    
    private SQLType getSqlType() {
        return masterslave == getShardingStrategy() ? SQLType.INSERT : SQLType.SELECT;
    }
    
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
        } else {
            result = "dataSource_" + result;
        }
        return result;
    }
    
    private List<String> getParameters(final SqlAssertData data) {
        return Strings.isNullOrEmpty(data.getParameter()) ? Collections.<String>emptyList() : Lists.newArrayList(data.getParameter().split(","));
    }
    
    private void executeWithPreparedStatement(final ShardingDataSource dataSource, final List<String> parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(sql))) {
            int index = 1;
            for (String each : parameters) {
                if (each.contains("'")) {
                    preparedStatement.setString(index++, each.replace("'", ""));
                } else {
                    preparedStatement.setInt(index++, Integer.valueOf(each));
                }
            }
            preparedStatement.execute();
        }
    }
    
    private void executeWithStatement(final ShardingDataSource dataSource, final List<String> parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(replaceStatement(sql, parameters.toArray()));
        }
    }

    private void executeQueryWithPreparedStatement(final ShardingDataSource dataSource, final List<String> parameters, final File file) throws Exception {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(replacePreparedStatement(sql))) {
            int index = 1;
            for (String param : parameters) {
                if (param.contains("'")) {
                    preparedStatement.setString(index++, param.replace("'", ""));
                } else {
                    preparedStatement.setInt(index++, Integer.valueOf(param));
                }
            }
            ITableIterator expectedTableIterator = new FlatXmlDataSetBuilder().build(file).iterator();
            while (expectedTableIterator.next()) {
                ITable expectedTable = expectedTableIterator.getTable();
                String actualTableName = expectedTable.getTableMetaData().getTableName();
                ITable actualTable = DBUnitUtil.getConnection(new DataBaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createTable(actualTableName, preparedStatement);
                IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(file);
                assertEquals(expectedDataSet.getTable(actualTableName), actualTable);
            }
        }
    }

    private void executeQueryWithStatement(final ShardingDataSource dataSource, final List<String> parameters, final File file) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            String querySql = replaceStatement(sql, parameters.toArray());
            ITableIterator expectedTableIterator = new FlatXmlDataSetBuilder().build(file).iterator();
            while (expectedTableIterator.next()) {
                ITable expectedTable = expectedTableIterator.getTable();
                String actualTableName = expectedTable.getTableMetaData().getTableName();
                ITable actualTable = DBUnitUtil.getConnection(new DataBaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createQueryTable(actualTableName, querySql);
                IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(file);
                assertEquals(expectedDataSet.getTable(actualTableName), actualTable);
            }
        }
    }

    private void assertResult(final Connection connection, final File file) throws Exception {
        ITableIterator expectedTableIterator = new FlatXmlDataSetBuilder().build(file).iterator();
        try (Connection conn = connection) {
            while (expectedTableIterator.next()) {
                ITable expectedTable = expectedTableIterator.getTable();
                String actualTableName = expectedTable.getTableMetaData().getTableName();
                String verifySql = "SELECT * FROM " + actualTableName + " WHERE status = '" + getStatus(file) + "'";
                ITable actualTable = DBUnitUtil.getConnection(new DataBaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createQueryTable(actualTableName, verifySql);
                assertEquals(expectedTable, actualTable);
            }
        }
    }
    
    private String getStatus(final File file) {
        if (sql.toUpperCase().startsWith("DELETE")) {
            return masterslave == getShardingStrategy() ? "init_master" : "init";
        }
        return file.getParentFile().getName();
    }
}
