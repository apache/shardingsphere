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
import com.dangdang.ddframe.rdb.common.jaxb.SqlParameterValue;
import com.dangdang.ddframe.rdb.common.jaxb.SqlParameters;
import com.dangdang.ddframe.rdb.integrate.util.DBUnitUtil;
import com.dangdang.ddframe.rdb.integrate.util.DataBaseEnvironment;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.InputStreamReader;
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
    
    @Getter(AccessLevel.PROTECTED)
    private final String sql;
    
    private final String expectedDataSet;
    
    private final SqlParameters sqlParameters;
    
    protected AbstractSqlAssertTest(final String testCaseName, final String sql, final ExpectedData expectedData, final SqlParameters sqlParameters) {
        this.sql = sql;
        this.expectedDataSet = expectedData.getFile();
        this.sqlParameters = sqlParameters;
    }
    
    protected abstract List<String> getDataSetFiles();
    
    protected abstract ShardingDataSource getShardingDataSource();
    
    protected abstract Connection getConnection() throws SQLException;
    
    protected static Collection<Object[]> dataParameters(final String path) {
        Collection<Object[]> result = new ArrayList<>();
        URL url = AbstractSqlAssertTest.class.getClassLoader().getResource(path);
        if (null != url) {
            File filePath = new File(url.getPath());
            if (filePath.exists()) {
                File[] files = filePath.listFiles();
                if (null != files) {
                    for (File each : files) {
                        result.addAll(dataParameters(each));
                    }
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
        result[2] = sqlAssert.getExpectedData();
        result[3] = sqlAssert.getParameters();
        return result;
    }
    
    @Test
    public void assertWithPreparedStatement() throws Exception {
        executePreparedStatement();
        assertResult(getConnection());
    }
    
    private void executePreparedStatement() throws SQLException {
        try (Connection connection = getShardingDataSource().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(sql))) {
            for (SqlParameter each : sqlParameters.getParameter()) {
                int index = 1;
                for (SqlParameterValue value : each.getValues()) {
                    preparedStatement.setObject(index++, value.getValueWithType());
                }
                preparedStatement.execute();
            }
        }
    }
    
    @Test
    public void assertWithStatement() throws Exception {
        executeStatement();
        assertResult(getConnection());
    }
    
    private void executeStatement() throws SQLException {
        try (Connection connection = getShardingDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            for (SqlParameter each : sqlParameters.getParameter()) {
                List<String> parameters = new ArrayList<>();
                for (SqlParameterValue value : each.getValues()) {
                    String literal = value.getLiteral();
                    if (null == value.getType()) {
                        literal = "'" + literal + "'"; 
                    }
                    parameters.add(literal);
                }
                statement.execute(replaceStatement(sql, parameters.toArray()));
            }
        }
    }
    
    private void assertResult(final Connection connection) throws Exception {
        ITableIterator expectedTableIterator = new FlatXmlDataSetBuilder().build(new InputStreamReader(AbstractSqlAssertTest.class.getClassLoader().getResourceAsStream(expectedDataSet))).iterator();
        try (Connection conn = connection) {
            while (expectedTableIterator.next()) {
                ITable expectedTable = expectedTableIterator.getTable();
                String actualTableName = expectedTable.getTableMetaData().getTableName();
                ITable actualTable = DBUnitUtil.getConnection(new DataBaseEnvironment(DatabaseType.valueFrom(conn.getMetaData().getDatabaseProductName())), conn)
                        .createQueryTable(actualTableName, "SELECT * FROM " + actualTableName + " WHERE status = 'insert'");
                assertEquals(expectedTable, actualTable);
            }
        }
    }
}
