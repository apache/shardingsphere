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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.dql.DQLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.dql.DQLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.dataset.expected.dataset.ExpectedDataSetRow;
import io.shardingsphere.dbtest.cases.dataset.expected.dataset.ExpectedDataSetsRoot;
import io.shardingsphere.dbtest.common.SQLValue;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.dbtest.env.EnvironmentPath;
import io.shardingsphere.dbtest.env.IntegrateTestEnvironment;
import io.shardingsphere.dbtest.env.dataset.DataSetEnvironmentManager;
import io.shardingsphere.dbtest.env.datasource.DataSourceUtil;
import io.shardingsphere.dbtest.env.schema.SchemaEnvironmentManager;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public final class DQLIntegrateTest extends BaseIntegrateTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final DQLIntegrateTestCaseAssertion assertion;
    
    public DQLIntegrateTest(final String sqlCaseId, final String path, final DQLIntegrateTestCaseAssertion assertion,
                            final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType, final int countInSameCase) throws IOException, JAXBException, SQLException {
        super(sqlCaseId, path, assertion, databaseTypeEnvironment, caseType, countInSameCase);
        this.assertion = assertion;
    }
    
    @Parameters(name = "{0}.{5} -> {2} -> {3} -> {4}")
    public static Collection<Object[]> getParameters() {
        // TODO sqlCasesLoader size should eq integrateTestCasesLoader size
        // assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(integrateTestCasesLoader.countAllDataSetTestCases()));
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            DQLIntegrateTestCase integrateTestCase = integrateTestCasesLoader.getDQLIntegrateTestCase(sqlCaseId);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            if (integrateTestCase.getDatabaseTypes().contains(databaseType)) {
                result.addAll(getParameters(databaseType, caseType, integrateTestCase));
            }
        }
        
        return result;
    }
    
    @BeforeClass
    public static void insertData() throws IOException, JAXBException, SQLException, ParseException {
        for (DatabaseType each : integrateTestCasesLoader.getDatabaseTypes()) {
            if (IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(each)) {
                insertData(each);
            }
        }
    }
    
    private static void insertData(final DatabaseType databaseType) throws SQLException, ParseException, IOException, JAXBException {
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(each), createDataSourceMap(databaseType, each)).initialize();
        }
    }
    
    @AfterClass
    public static void clearData() throws IOException, JAXBException, SQLException {
        for (DatabaseType each : integrateTestCasesLoader.getDatabaseTypes()) {
            if (IntegrateTestEnvironment.getInstance().getDatabaseTypes().contains(each)) {
                clearData(each);
            }
        }
    }
    
    private static void clearData(final DatabaseType databaseType) throws SQLException, IOException, JAXBException {
        for (String each : integrateTestCasesLoader.getShardingRuleTypes()) {
            new DataSetEnvironmentManager(EnvironmentPath.getDataInitializeResourceFile(each), createDataSourceMap(databaseType, each)).clear();
        }
    }
    
    private static Map<String, DataSource> createDataSourceMap(final DatabaseType databaseType, final String shardingRuleType) throws IOException, JAXBException {
        Collection<String> dataSourceNames = SchemaEnvironmentManager.getDataSourceNames(shardingRuleType);
        Map<String, DataSource> result = new HashMap<>(dataSourceNames.size(), 1);
        for (String each : dataSourceNames) {
            result.put(each, DataSourceUtil.createDataSource(databaseType, each));
        }
        return result;
    }
    
    @Test
    public void assertExecuteQuery() throws JAXBException, IOException, SQLException, ParseException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertExecuteQueryForStatement(connection);
            } else {
                assertExecuteQueryForPreparedStatement(connection);
            }
        }
    }
    
    private void assertExecuteQueryForStatement(final Connection connection) throws SQLException, JAXBException, IOException, ParseException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format(getSql(), assertion.getSQLValues().toArray()))) {
            assertResultSet(resultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatement(final Connection connection) throws SQLException, ParseException, JAXBException, IOException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql().replaceAll("%s", "?"))) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    @Test
    public void assertExecute() throws JAXBException, IOException, SQLException, ParseException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertExecuteForStatement(connection);
            } else {
                assertExecuteForPreparedStatement(connection);
            }
        }
    }
    
    private void assertExecuteForStatement(final Connection connection) throws SQLException, ParseException, JAXBException, IOException {
        try (Statement statement = connection.createStatement()) {
            assertTrue("Not a DQL statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray())));
            try (ResultSet resultSet = statement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatement(final Connection connection) throws SQLException, ParseException, JAXBException, IOException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql().replaceAll("%s", "?"))) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a DQL statement.", preparedStatement.execute());
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertResultSet(resultSet);
            }
        }
    }
    
    private void assertResultSet(final ResultSet resultSet) throws SQLException, JAXBException, IOException {
        ExpectedDataSetsRoot expected;
        try (FileReader reader = new FileReader(getExpectedDataFile())) {
            expected = (ExpectedDataSetsRoot) JAXBContext.newInstance(ExpectedDataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
        List<String> expectedColumnNames = expected.getColumns().getValues();
        assertMetaData(resultSet.getMetaData(), expectedColumnNames);
        assertDataSets(resultSet, expected.getDataSetRows());
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final List<String> expectedColumnNames) throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(expectedColumnNames.size()));
        int index = 1;
        for (String each : expectedColumnNames) {
            assertThat(actualMetaData.getColumnLabel(index++), is(each));
        }
    }
    
    private void assertDataSets(final ResultSet actualResultSet, final List<ExpectedDataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        ResultSetMetaData actualMetaData = actualResultSet.getMetaData();
        while (actualResultSet.next()) {
            int index = 1;
            for (String each : expectedDatSetRows.get(count).getValues()) {
                if (Types.DATE == actualResultSet.getMetaData().getColumnType(index)) {
                    assertThat(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(actualResultSet.getDate(index).getTime())), is(each));
                    assertThat(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(actualResultSet.getDate(actualMetaData.getColumnLabel(index)).getTime())), is(each));
                } else {
                    assertThat(String.valueOf(actualResultSet.getObject(index)), is(each));
                    assertThat(String.valueOf(actualResultSet.getObject(actualMetaData.getColumnLabel(index))), is(each));
                }
                index++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
}
