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
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetColumnMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetRow;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetsRoot;
import io.shardingsphere.dbtest.common.SQLValue;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.After;
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
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public final class DMLIntegrateTest extends BaseIntegrateTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final DMLIntegrateTestCaseAssertion assertion;
    
    public DMLIntegrateTest(final String sqlCaseId, final String path, final DMLIntegrateTestCaseAssertion assertion, 
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
            DMLIntegrateTestCase integrateTestCase = integrateTestCasesLoader.getDMLIntegrateTestCase(sqlCaseId);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            if (getDatabaseTypes(integrateTestCase.getDatabaseTypes()).contains(databaseType)) {
                result.addAll(getParameters(databaseType, caseType, integrateTestCase));
            }
            
        }
        return result;
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            getDataSetEnvironmentManager().initialize(true);
        }
    }
    
    @After
    public void clearData() throws SQLException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            getDataSetEnvironmentManager().clear();
        }
    }
    
    @Test
    public void assertExecuteUpdate() throws JAXBException, IOException, SQLException, ParseException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertExecuteUpdateForStatement(connection);
            } else {
                assertExecuteUpdateForPreparedStatement(connection);
            }
        }
        assertDataSet();
    }
    
    private void assertExecuteUpdateForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertThat(statement.executeUpdate(String.format(getSql(), assertion.getSQLValues().toArray())), is(assertion.getExpectedUpdate()));
        }
    }
    
    private void assertExecuteUpdateForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql().replaceAll("%s", "?"))) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertThat(preparedStatement.executeUpdate(), is(assertion.getExpectedUpdate()));
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
        assertDataSet();
    }
    
    private void assertExecuteForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray())));
            assertThat(statement.getUpdateCount(), is(assertion.getExpectedUpdate()));
        }
    }
    
    private void assertExecuteForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql().replaceAll("%s", "?"))) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            assertThat(preparedStatement.getUpdateCount(), is(assertion.getExpectedUpdate()));
        }
    }
    
    private void assertDataSet() throws SQLException, IOException, JAXBException {
        DataSetsRoot expected;
        try (FileReader reader = new FileReader(getExpectedDataFile())) {
            expected = (DataSetsRoot) JAXBContext.newInstance(DataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
        assertThat("Only support single table for DML.", expected.getMetadataList().size(), is(1));
        DataSetMetadata expectedDataSetMetadata = expected.getMetadataList().get(0);
        for (String each : new InlineExpressionParser(expectedDataSetMetadata.getDataNodes()).evaluate()) {
            DataNode dataNode = new DataNode(each);
            try (Connection connection = getDataSourceMap().get(dataNode.getDataSourceName()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s", dataNode.getTableName()))) {
                assertDataSet(preparedStatement, expected.findDataSetRows(dataNode), expectedDataSetMetadata);
            }
        }
    }
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final List<DataSetRow> expectedDataSetRows, final DataSetMetadata expectedDataSetMetadata) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetadata.getColumnMetadataList());
            assertDataSets(actualResultSet, expectedDataSetRows);
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final List<DataSetColumnMetadata> columnMetadataList) throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(columnMetadataList.size()));
        int index = 1;
        for (DataSetColumnMetadata each : columnMetadataList) {
            assertThat(actualMetaData.getColumnLabel(index++), is(each.getName()));
        }
    }
    
    private void assertDataSets(final ResultSet actualResultSet, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        while (actualResultSet.next()) {
            int index = 1;
            for (String each : expectedDatSetRows.get(count).getValues()) {
                if (Types.DATE == actualResultSet.getMetaData().getColumnType(index)) {
                    assertThat(new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(actualResultSet.getDate(index).getTime())), is(each));
                } else {
                    assertThat(String.valueOf(actualResultSet.getObject(index)), is(each));
                }
                index++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
}
