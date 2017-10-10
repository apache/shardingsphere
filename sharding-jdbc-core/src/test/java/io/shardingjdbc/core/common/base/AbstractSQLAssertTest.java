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

import io.shardingjdbc.core.common.env.ShardingTestStrategy;
import io.shardingjdbc.core.common.util.SQLAssertHelper;
import io.shardingjdbc.core.integrate.jaxb.SQLAssertData;
import io.shardingjdbc.core.integrate.jaxb.SQLShardingRule;
import io.shardingjdbc.core.integrate.jaxb.helper.SQLAssertJAXBHelper;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.jdbc.adapter.AbstractDataSourceAdapter;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.dbunit.DatabaseUnitException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(Parameterized.class)
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
    
    @Parameterized.Parameters(name = "{0}In{2}")
    public static Collection<Object[]> dataParameters() {
        return SQLAssertJAXBHelper.getDataParameters("integrate/assert");
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
                    assertDqlSql(isPreparedStatement, isExecute, abstractDataSourceAdapter, each, expectedDataSetFile);
                } else  {
                    assertDmlAndDdlSql(isPreparedStatement, isExecute, abstractDataSourceAdapter, each, expectedDataSetFile);
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
    
    private void assertDqlSql(final boolean isPreparedStatement, final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, 
                              final SQLAssertData data, final File expectedDataSetFile)
            throws MalformedURLException, SQLException, DatabaseUnitException {
        if (isPreparedStatement) {
            sqlAssertHelper.executeQueryWithPreparedStatement(isExecute, abstractDataSourceAdapter, getParameters(data), expectedDataSetFile);
        } else {
            sqlAssertHelper.executeQueryWithStatement(abstractDataSourceAdapter, getParameters(data), expectedDataSetFile);
        }
    }
    
    private void assertDmlAndDdlSql(final boolean isPreparedStatement, final boolean isExecute, final AbstractDataSourceAdapter abstractDataSourceAdapter, 
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
