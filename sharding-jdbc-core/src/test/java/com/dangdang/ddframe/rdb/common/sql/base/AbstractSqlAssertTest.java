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

import com.dangdang.ddframe.rdb.common.jaxb.ExpectedData;
import com.dangdang.ddframe.rdb.common.jaxb.SqlAssert;
import com.dangdang.ddframe.rdb.common.jaxb.SqlAsserts;
import com.dangdang.ddframe.rdb.common.jaxb.SqlParameter;
import com.dangdang.ddframe.rdb.common.jaxb.SqlParameters;
import com.dangdang.ddframe.rdb.integrate.util.DBUnitUtil;
import com.dangdang.ddframe.rdb.integrate.util.DataBaseEnvironment;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
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
import java.util.List;

import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replacePreparedStatement;
import static com.dangdang.ddframe.rdb.integrate.util.SqlPlaceholderUtil.replaceStatement;
import static org.dbunit.Assertion.assertEquals;

public abstract class AbstractSqlAssertTest extends AbstractBaseSqlTest {
    
    private final String sql;
    
    private final List<DatabaseType> types;
    
    private final String expectedDataSet;
    
    private final SqlParameters sqlParameters;
    
    protected AbstractSqlAssertTest(final String testCaseName, final String sql, final List<DatabaseType> types, final ExpectedData expectedData, final SqlParameters sqlParameters) {
        this.sql = sql;
        this.types = types;
        this.expectedDataSet = expectedData.getFile();
        this.sqlParameters = sqlParameters;
    }
    
    protected abstract List<String> getDataSetFiles();
    
    protected abstract List<ShardingDataSource> getShardingDataSources();
    
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
        final Object[] result = new Object[5];
        result[0] = sqlAssert.getId();
        result[1] = sqlAssert.getSql(); 
        if (null == sqlAssert.getTypes()) {
            result[2] = null;
        } else {
            List<DatabaseType> types = new ArrayList<>();
            for (String each : sqlAssert.getTypes().split(",")) {
                types.add(DatabaseType.valueOf(each));
            }
            result[2] = types;
        }
        result[3] = sqlAssert.getExpectedData();
        result[4] = sqlAssert.getParameters();
        return result;
    }
    
    @Test
    public void assertWithPreparedStatement() throws Exception {
        for (ShardingDataSource each : getShardingDataSources()) {
            executePreparedStatement(each);
            for (Connection conn : each.getConnection().getConnections()) {
                assertResult(conn);
            }
        }
    }
    
    private void executePreparedStatement(final ShardingDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(sql))) {
            for (SqlParameter each : sqlParameters.getParameter()) {
                int index = 1;
                for (String value : each.getValue().split(",")) {
                    if (value.contains("'")) {
                        preparedStatement.setString(index++, value.replace("'", ""));
                    } else {
                        preparedStatement.setInt(index++, Integer.valueOf(value));
                    }
                }
                preparedStatement.execute();
            }
        }
    }
    
    @Test
    public void assertWithStatement() throws Exception {
        for (ShardingDataSource each : getShardingDataSources()) {
            executeStatement(each);
            for (Connection conn : each.getConnection().getConnections()) {
                assertResult(conn);
            }
        }
    }
    
    private void executeStatement(final ShardingDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (SqlParameter each : sqlParameters.getParameter()) {
                statement.execute(replaceStatement(sql, each.getValue().split(",")));
            }
        }
    }
    
    private void assertResult(final Connection connection) throws Exception {
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
