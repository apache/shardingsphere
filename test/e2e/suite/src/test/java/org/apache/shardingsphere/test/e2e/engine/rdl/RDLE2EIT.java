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

package org.apache.shardingsphere.test.e2e.engine.rdl;

import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.engine.SingleE2EITContainerComposer;
import org.apache.shardingsphere.test.e2e.framework.E2EITExtension;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(E2EITExtension.class)
public final class RDLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public void assertExecute(final AssertionTestParameter testParam) throws SQLException, ParseException {
        // TODO make sure RDL test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        try (SingleE2EITContainerComposer containerComposer = new SingleE2EITContainerComposer(testParam)) {
            init(containerComposer);
            assertExecute(testParam, containerComposer);
            tearDown(containerComposer);
        }
    }
    
    private void assertExecute(final AssertionTestParameter testParam, final SingleE2EITContainerComposer containerComposer) throws SQLException, ParseException {
        assertNotNull(testParam.getAssertion().getAssertionSQL(), "Assertion SQL is required");
        try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                executeSQLCase(containerComposer, statement);
                waitCompleted();
                assertResultSet(containerComposer, statement);
            }
        }
    }
    
    private void executeSQLCase(final SingleE2EITContainerComposer containerComposer, final Statement statement) throws SQLException, ParseException {
        statement.execute(containerComposer.getSQL());
    }
    
    private void init(final SingleE2EITContainerComposer containerComposer) throws SQLException {
        try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
            executeInitSQLs(containerComposer, connection);
        }
    }
    
    private void tearDown(final SingleE2EITContainerComposer containerComposer) throws SQLException {
        if (null != containerComposer.getAssertion().getDestroySQL()) {
            try (Connection connection = containerComposer.getContainerComposer().getTargetDataSource().getConnection()) {
                executeDestroySQLs(containerComposer, connection);
            }
        }
        waitCompleted();
    }
    
    private void executeInitSQLs(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException {
        if (null == containerComposer.getAssertion().getInitialSQL() || null == containerComposer.getAssertion().getInitialSQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(containerComposer.getAssertion().getInitialSQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
                waitCompleted();
            }
        }
    }
    
    private void executeDestroySQLs(final SingleE2EITContainerComposer containerComposer, final Connection connection) throws SQLException {
        if (null == containerComposer.getAssertion().getDestroySQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().splitToList(containerComposer.getAssertion().getDestroySQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
                waitCompleted();
            }
        }
    }
    
    private void assertResultSet(final SingleE2EITContainerComposer containerComposer, final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(containerComposer.getAssertion().getAssertionSQL().getSql())) {
            assertResultSet(containerComposer, resultSet);
        }
    }
    
    private void assertResultSet(final SingleE2EITContainerComposer containerComposer, final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(containerComposer));
        assertRows(resultSet, containerComposer.getDataSet().getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SingleE2EITContainerComposer containerComposer) {
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
    
    @SneakyThrows(InterruptedException.class)
    private void waitCompleted() {
        Thread.sleep(2000L);
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter() && !E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RDL).isEmpty();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            Collection<AssertionTestParameter> result = E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RDL);
            // TODO make sure RDL test case can not be null
            return result.isEmpty() ? Stream.of(Arguments.of(new AssertionTestParameter(null, null, null, null, null, null, null))) : result.stream().map(Arguments::of);
        }
    }
}
