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
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseSettings;
import org.apache.shardingsphere.test.e2e.engine.composer.E2EContainerComposer;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@E2ETestCaseSettings(SQLCommandType.DAL)
class DALE2EIT {
    
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        assertExecute(containerComposer);
    }
    
    private void assertExecute(final SingleE2EContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(containerComposer.getSQL());
                assertExecuteResult(containerComposer, statement);
            }
        }
    }
    
    private void assertExecuteResult(final SingleE2EContainerComposer containerComposer, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.getResultSet()) {
            if (null == containerComposer.getAssertion().getAssertionSQL()) {
                assertResultSet(containerComposer, resultSet);
            } else {
                statement.execute(containerComposer.getAssertion().getAssertionSQL().getSql());
                try (ResultSet assertionSQLResultSet = statement.getResultSet()) {
                    assertResultSet(containerComposer, assertionSQLResultSet);
                }
            }
        }
    }
    
    private void assertResultSet(final SingleE2EContainerComposer containerComposer, final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(containerComposer));
        assertRows(resultSet, containerComposer.getDataSet().getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SingleE2EContainerComposer containerComposer) {
        Collection<DataSetColumn> result = new LinkedList<>();
        for (DataSetMetaData each : containerComposer.getDataSet().getMetaDataList()) {
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
        for (String each : expected.splitValues(", ")) {
            String columnLabel = actualMetaData.getColumnLabel(columnIndex);
            if (Types.DATE == actual.getMetaData().getColumnType(columnIndex)) {
                assertDateValue(actual, columnIndex, columnLabel, each);
            } else {
                assertObjectValue(actual, columnIndex, columnLabel, each);
            }
            columnIndex++;
        }
    }
    
    private void assertDateValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        if (E2EContainerComposer.NOT_VERIFY_FLAG.equals(expected)) {
            return;
        }
        assertThat(dateTimeFormatter.format(actual.getDate(columnIndex).toLocalDate()), is(expected));
        assertThat(dateTimeFormatter.format(actual.getDate(columnLabel).toLocalDate()), is(expected));
    }
    
    private void assertObjectValue(final ResultSet actual, final int columnIndex, final String columnLabel, final String expected) throws SQLException {
        assertThat(String.valueOf(actual.getObject(columnIndex)), is(expected));
        assertThat(String.valueOf(actual.getObject(columnLabel)), is(expected));
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter();
    }
}
