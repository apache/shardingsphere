/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.engine.type;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseSettings;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@E2ETestCaseSettings(SQLCommandType.RDL)
class RDLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        
        if (null == containerComposer.getAssertions()) {
            return;
        }
        int dataSetIndex = 0;
        String mode = testParam.getMode();
        
        for (IntegrationTestCaseAssertion each : containerComposer.getAssertions()) {
            if (null != each.getInitialSQL() && null != each.getAssertionSQL()) {
                init(containerComposer, each, dataSetIndex, mode);
            }
            if (null != each.getAssertionSQL() && null == each.getDestroySQL() && null == each.getInitialSQL()) {
                executeSQLCase(containerComposer, each, dataSetIndex, mode);
            }
            if (null != each.getDestroySQL() && null != each.getAssertionSQL()) {
                tearDown(containerComposer, each, dataSetIndex, mode);
            }
            dataSetIndex++;
        }
    }
    
    private void executeSQLCase(final SingleE2EContainerComposer containerComposer, final IntegrationTestCaseAssertion testCaseExecuteSql,
                                final int dataSetIndex, final String mode) throws SQLException {
        try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(containerComposer.getSQL());
                
                executeAssertionSQL(containerComposer, statement, testCaseExecuteSql, dataSetIndex, mode);
            }
        }
    }
    
    private void init(final SingleE2EContainerComposer containerComposer, final IntegrationTestCaseAssertion testCaseInitSql,
                      final int dataSetIndex, final String mode) throws SQLException {
        try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
            executeInitSQLs(containerComposer, connection, testCaseInitSql, dataSetIndex, mode);
        }
    }
    
    private void tearDown(final SingleE2EContainerComposer containerComposer, final IntegrationTestCaseAssertion testCaseDestroySQL,
                          final int dataSetIndex, final String mode) throws SQLException {
        try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
            executeDestroySQLs(containerComposer, connection, testCaseDestroySQL, dataSetIndex, mode);
        }
    }
    
    private void executeInitSQLs(final SingleE2EContainerComposer containerComposer, final Connection connection,
                                 final IntegrationTestCaseAssertion testCaseInitSql, final int dataSetIndex, final String mode) throws SQLException {
        if (null == testCaseInitSql.getInitialSQL() || null == testCaseInitSql.getInitialSQL().getSql()) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(testCaseInitSql.getInitialSQL().getSql());
            
            executeAssertionSQL(containerComposer, statement, testCaseInitSql, dataSetIndex, mode);
        }
    }
    
    private void executeAssertionSQL(final SingleE2EContainerComposer containerComposer, final Statement statement,
                                     final IntegrationTestCaseAssertion testCaseAssertionSQL,
                                     final int dataSetIndex, final String mode) {
        if ("Cluster".equals(mode)) {
            Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> assertResultSet(containerComposer, statement, testCaseAssertionSQL, dataSetIndex));
        } else if ("Standalone".equals(mode)) {
            assertResultSet(containerComposer, statement, testCaseAssertionSQL, dataSetIndex);
        }
    }
    
    private void executeDestroySQLs(final SingleE2EContainerComposer containerComposer, final Connection connection,
                                    final IntegrationTestCaseAssertion testCaseDestroySQL,
                                    final int dataSetIndex, final String mode) throws SQLException {
        if (null == testCaseDestroySQL.getDestroySQL() || null == testCaseDestroySQL.getDestroySQL().getSql()) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(testCaseDestroySQL.getDestroySQL().getSql());
            
            executeAssertionSQL(containerComposer, statement, testCaseDestroySQL, dataSetIndex, mode);
        }
    }
    
    private boolean assertResultSet(final SingleE2EContainerComposer containerComposer, final Statement statement,
                                    final IntegrationTestCaseAssertion testCaseAssertionSql,
                                    final int dataSetIndex) {
        try (ResultSet resultSet = statement.executeQuery(testCaseAssertionSql.getAssertionSQL().getSql())) {
            assertResultSet(containerComposer, resultSet, dataSetIndex);
            return true;
        } catch (final SQLException ignored) {
            return false;
        }
    }
    
    private void assertResultSet(final SingleE2EContainerComposer containerComposer, final ResultSet resultSet,
                                 final int dataSetIndex) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(containerComposer, dataSetIndex));
        assertRows(resultSet, containerComposer.getDataSets().get(dataSetIndex).getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SingleE2EContainerComposer containerComposer, final int dataSetIndex) {
        Collection<DataSetColumn> result = new LinkedList<>();
        List<DataSetMetaData> dataSetMetaDataList = containerComposer.getDataSets().get(dataSetIndex).getMetaDataList();
        for (DataSetMetaData each : dataSetMetaDataList) {
            result.addAll(each.getColumns());
        }
        return result;
    }
    
    private void assertMetaData(final ResultSetMetaData actual, final Collection<DataSetColumn> expected) throws SQLException {
        assertThat(actual.getColumnCount(), is(expected.size()));
        int index = 1;
        for (DataSetColumn each : expected) {
            assertThat(actual.getColumnLabel(index++).toLowerCase(), is(each.getName().toLowerCase()));
        }
    }
    
    private void assertRows(final ResultSet actual, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue(rowCount < expected.size(), "Size of actual result set is different with size of expected dat set rows.");
            assertRow(actual, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", rowCount, is(expected.size()));
    }
    
    private void assertRow(final ResultSet actual, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues("|")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            assertObjectValue(actual, columnIndex, columnLabel, each);
            columnIndex++;
        }
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)), is(expected));
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter() && !E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RDL).isEmpty();
    }
}
