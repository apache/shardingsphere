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

import com.google.common.base.Splitter;
import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseSettings;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@E2ETestCaseSettings(SQLCommandType.RAL)
class RALE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        init(containerComposer);
        assertExecute(containerComposer);
        tearDown(containerComposer);
    }
    
    private void assertExecute(final SingleE2EContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                assertResultSet(containerComposer, statement);
            }
        }
    }
    
    private void init(final SingleE2EContainerComposer containerComposer) throws SQLException {
        if (null != containerComposer.getAssertion().getInitialSQL()) {
            try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
                executeInitSQLs(containerComposer, connection);
            }
        }
    }
    
    private void executeInitSQLs(final SingleE2EContainerComposer containerComposer, final Connection connection) throws SQLException {
        if (null == containerComposer.getAssertion().getInitialSQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(containerComposer.getAssertion().getInitialSQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
        Awaitility.await().pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private void tearDown(final SingleE2EContainerComposer containerComposer) throws SQLException {
        if (null != containerComposer.getAssertion().getDestroySQL()) {
            try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
                executeDestroySQLs(containerComposer, connection);
            }
        }
    }
    
    private void executeDestroySQLs(final SingleE2EContainerComposer containerComposer, final Connection connection) throws SQLException {
        if (null == containerComposer.getAssertion().getDestroySQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(containerComposer.getAssertion().getDestroySQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
        Awaitility.await().pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private void assertResultSet(final SingleE2EContainerComposer containerComposer, final Statement statement) throws SQLException {
        if (null == containerComposer.getAssertion().getAssertionSQL()) {
            assertResultSet(containerComposer, statement, containerComposer.getSQL());
        } else {
            statement.execute(containerComposer.getSQL());
            Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
            assertResultSet(containerComposer, statement, containerComposer.getAssertion().getAssertionSQL().getSql());
        }
    }
    
    private void assertResultSet(final SingleE2EContainerComposer containerComposer, final Statement statement, final String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            assertResultSet(containerComposer, resultSet);
        }
    }
    
    private void assertResultSet(final SingleE2EContainerComposer containerComposer, final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(containerComposer));
        assertRows(resultSet, getNotAssertionColumns(containerComposer), containerComposer.getDataSet().getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SingleE2EContainerComposer containerComposer) {
        Collection<DataSetColumn> result = new LinkedList<>();
        for (DataSetMetaData each : containerComposer.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns());
        }
        return result;
    }
    
    private Collection<String> getNotAssertionColumns(final SingleE2EContainerComposer containerComposer) {
        Collection<String> result = new LinkedList<>();
        for (DataSetMetaData each : containerComposer.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns().stream().filter(column -> "false".equals(column.getAssertion())).map(DataSetColumn::getName).collect(Collectors.toList()));
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
    
    private void assertRows(final ResultSet actual, final Collection<String> notAssertionColumns, final List<DataSetRow> expected) throws SQLException {
        int rowCount = 0;
        ResultSetMetaData actualMetaData = actual.getMetaData();
        while (actual.next()) {
            assertTrue(rowCount < expected.size(), "Size of actual result set is different with size of expected dat set rows.");
            assertRow(actual, notAssertionColumns, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", rowCount, is(expected.size()));
    }
    
    private void assertRow(final ResultSet actual, final Collection<String> notAssertionColumns, final ResultSetMetaData actualMetaData, final DataSetRow expected) throws SQLException {
        int columnIndex = 1;
        for (String each : expected.splitValues("|")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            if (!notAssertionColumns.contains(columnLabel)) {
                assertObjectValue(actual, columnIndex, columnLabel, each);
            }
            columnIndex++;
        }
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)).trim(), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)).trim(), is(expected));
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter() && !E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RAL).isEmpty();
    }
}
