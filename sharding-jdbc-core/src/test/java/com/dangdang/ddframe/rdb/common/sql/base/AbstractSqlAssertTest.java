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

import com.dangdang.ddframe.rdb.common.jaxb.SqlAssert;
import com.dangdang.ddframe.rdb.common.jaxb.SqlAssertData;
import com.dangdang.ddframe.rdb.common.jaxb.SqlAsserts;
import com.dangdang.ddframe.rdb.integrate.util.DBUnitUtil;
import com.dangdang.ddframe.rdb.integrate.util.DataBaseEnvironment;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;
import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replaceStatement;
import static org.dbunit.Assertion.assertEquals;

public abstract class AbstractSqlAssertTest extends AbstractBaseSqlTest {
    
    private final String sql;
    
    private final Set<DatabaseType> types;
    
    private final List<SqlAssertData> data;
    
    protected AbstractSqlAssertTest(final String testCaseName, final String sql, final Set<DatabaseType> types, final List<SqlAssertData> data) {
        this.sql = sql;
        this.types = types;
        this.data = data;
    }
    
    protected abstract List<String> getDataSetFiles();
    
    protected abstract Map<DatabaseType, ShardingDataSource> getShardingDataSources();
    
    protected static Collection<Object[]> dataParameters(final String path) {
        Collection<Object[]> result = new ArrayList<>();
        URL url = AbstractSqlAssertTest.class.getClassLoader().getResource(path);
        if (null == url) {
            return result;
        }
        File filePath = new File(url.getPath());
        if (filePath.exists()) {
            File[] files = filePath.listFiles();
            if (null != files) {
                for (File each : files) {
                    result.addAll(dataParameters(each));
                }
            }
        }
        return result;
    }
    
    private static Collection<Object[]> dataParameters(final File file) {
        SqlAsserts asserts = loadSqlAsserts(file);
        Object[][] result = new Object[asserts.getSqlAsserts().size()][1];
        for (int i = 0; i < asserts.getSqlAsserts().size(); i++) {
            result[i] = getDataParameter(asserts.getSqlAsserts().get(i));
        }
        return Arrays.asList(result);
    }
    
    private static SqlAsserts loadSqlAsserts(final File file) {
        try {
            return (SqlAsserts) JAXBContext.newInstance(SqlAsserts.class).createUnmarshaller().unmarshal(file);
        } catch (final JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static Object[] getDataParameter(final SqlAssert sqlAssert) {
        final Object[] result = new Object[4];
        result[0] = sqlAssert.getId();
        result[1] = sqlAssert.getSql(); 
        if (null == sqlAssert.getTypes()) {
            result[2] = Collections.emptySet();
        } else {
            Set<DatabaseType> types = new HashSet<>();
            for (String each : sqlAssert.getTypes().split(",")) {
                types.add(DatabaseType.valueOf(each));
            }
            result[2] = types;
        }
        result[3] = sqlAssert.getData();
        return result;
    }
    
    @Test
    public void assertWithPreparedStatement() throws Exception {
        executeAndAssertResult(true);
    }
    
    @Test
    public void assertWithStatement() throws Exception {
        executeAndAssertResult(false);
    }
    
    private void executeAndAssertResult(final boolean isPreparedStatement) throws Exception {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : getShardingDataSources().entrySet()) {
            if (types.size() == 0 || types.contains(each.getKey())) {
                assertSql(isPreparedStatement, each.getValue());
            }
        }
    }
    
    private void assertSql(final boolean isPreparedStatement, final ShardingDataSource shardingDataSource) throws Exception {
        for (SqlAssertData each : data) {
            if (isPreparedStatement) {
                executePreparedStatement(shardingDataSource, getParameters(each));
            } else {
                executeStatement(shardingDataSource, getParameters(each));
            }
            for (Connection conn : shardingDataSource.getConnection().getConnections()) {
                assertResult(conn, each.getExpected());
            }
        }
    }
    
    private List<String> getParameters(final SqlAssertData data) {
        return Strings.isNullOrEmpty(data.getParameter()) ? Collections.<String>emptyList() : Lists.newArrayList(data.getParameter().split(","));
    }
    
    private void executePreparedStatement(final ShardingDataSource dataSource, final List<String> parameters) throws SQLException {
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
    
    private void executeStatement(final ShardingDataSource dataSource, final List<String> parameters) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(replaceStatement(sql, parameters.toArray()));
        }
    }
    
    private void assertResult(final Connection connection, final String expectedDataSet) throws Exception {
        File file = new File(AbstractSqlAssertTest.class.getClassLoader().getResource(expectedDataSet).getPath());
        ITableIterator expectedTableIterator = new FlatXmlDataSetBuilder().build(file).iterator();
        try (Connection conn = connection) {
            while (expectedTableIterator.next()) {
                ITable expectedTable = expectedTableIterator.getTable();
                String actualTableName = expectedTable.getTableMetaData().getTableName();
                String status = file.getParentFile().getName();
                String verifySql = "SELECT * FROM " + actualTableName + " WHERE status = '" + status + "'";
                ITable actualTable = DBUnitUtil.getConnection(new DataBaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createQueryTable(actualTableName, verifySql);
                assertEquals(expectedTable, actualTable);
            }
        }
    }
}
