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

package io.shardingsphere.dbtest.engine;

import com.google.common.base.Strings;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.ddl.DDLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.dataset.expected.ddl.DDLDataSet;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetColumn;
import io.shardingsphere.dbtest.cases.dataset.metadata.DataSetMetadata;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.dataset.DataSetEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public final class DDLIntegrateTest extends BaseIntegrateTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final DDLIntegrateTestCaseAssertion assertion;
    
    private final DatabaseType databaseType;
    
    public DDLIntegrateTest(final String sqlCaseId, final String path, final DDLIntegrateTestCaseAssertion assertion, final String shardingRuleType, 
                            final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException, ParseException {
        super(sqlCaseId, path, assertion, shardingRuleType, databaseTypeEnvironment, caseType);
        this.assertion = assertion;
        databaseType = databaseTypeEnvironment.getDatabaseType();
    }
    
    @Parameters(name = "{0} -> Rule:{3} -> {4} -> {5}")
    public static Collection<Object[]> getParameters() {
        // TODO sqlCasesLoader size should eq integrateTestCasesLoader size
        // assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(integrateTestCasesLoader.countAllDataSetTestCases()));
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            if (SQLType.DDL != new SQLJudgeEngine(sqlCasesLoader.getSupportedSQL(sqlCaseId, SQLCaseType.Placeholder, Collections.emptyList())).judge().getType()) {
                continue;
            }
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            DDLIntegrateTestCase integrateTestCase = integrateTestCasesLoader.getDDLIntegrateTestCase(sqlCaseId);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            result.addAll(getParameters(databaseType, caseType, integrateTestCase));
        }
        return result;
    }
    
    @Before
    public void insertData() throws SQLException, ParseException, IOException, JAXBException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(getShardingRuleType()), getDataSourceMap()).initialize();
        }
    }
    
    @Test
    public void assertExecuteUpdate() throws JAXBException, IOException, SQLException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            dropTableIfExisted(connection);
            if (!Strings.isNullOrEmpty(assertion.getInitSql())) {
                connection.prepareStatement(assertion.getInitSql()).executeUpdate();
            }
            if (SQLCaseType.Literal == getCaseType()) {
                connection.createStatement().executeUpdate(getSql());
            } else {
                connection.prepareStatement(getSql()).executeUpdate();
            }
            assertMetadata(connection);
            dropTableIfExisted(connection);
        }
    }
    
    @Test
    public void assertExecute() throws JAXBException, IOException, SQLException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            dropTableIfExisted(connection);
            if (!Strings.isNullOrEmpty(assertion.getInitSql())) {
                connection.prepareStatement(assertion.getInitSql()).executeUpdate();
            }
            if (SQLCaseType.Literal == getCaseType()) {
                connection.createStatement().execute(getSql());
            } else {
                connection.prepareStatement(getSql()).execute();
            }
            assertMetadata(connection);
            dropTableIfExisted(connection);
        }
    }
    
    private void assertMetadata(final Connection connection) throws IOException, JAXBException, SQLException {
        // TODO case for drop (index) and truncate, add assert later
        if (null == assertion.getExpectedDataFile()) {
            return;
        }
        DDLDataSet expected;
        try (FileReader reader = new FileReader(getExpectedDataFile())) {
            expected = (DDLDataSet) JAXBContext.newInstance(DDLDataSet.class).createUnmarshaller().unmarshal(reader);
        }
        String tableName = assertion.getTable();
        List<DataSetColumn> actualColumns = getActualColumns(connection, tableName);
        assertMetadata(actualColumns, expected.find(tableName));
    }
    
    private void assertMetadata(final List<DataSetColumn> actual, final DataSetMetadata expected) {
        for (DataSetColumn each : expected.getColumns()) {
            assertMetadata(actual, each);
        }
    }
    
    private void assertMetadata(final List<DataSetColumn> actual, final DataSetColumn expect) {
        for (DataSetColumn each : actual) {
            if (expect.getName().equals(each.getName())) {
                if (DatabaseType.MySQL == databaseType && "integer".equals(expect.getType())) {
                    assertThat(each.getType(), is("int"));
                } else if (DatabaseType.PostgreSQL == databaseType && "integer".equals(expect.getType())) {
                    assertThat(each.getType(), is("int4"));
                } else {
                    assertThat(each.getType(), is(expect.getType()));
                }
            }
        }
    }
    
    private List<DataSetColumn> getActualColumns(final Connection connection, final String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        boolean isTableExisted = metaData.getTables(null, null, tableName, new String[] {"TABLE"}).next();
        if (!isTableExisted) {
            return Collections.emptyList();
        }
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, null)) {
            List<DataSetColumn> result = new LinkedList<>();
            while (resultSet.next()) {
                DataSetColumn each = new DataSetColumn();
                each.setName(resultSet.getString("COLUMN_NAME"));
                each.setType(resultSet.getString("TYPE_NAME").toLowerCase());
                result.add(each);
            }
            return result;
        }
    }
    
    private void dropTableIfExisted(final Connection connection) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(String.format("DROP TABLE %s", assertion.getTable()))) {
            preparedStatement.executeUpdate();
            // CHECKSTYLE: OFF
        } catch (final SQLException ignored) {
            // CHECKSTYLE: ON
        }
    }
}
