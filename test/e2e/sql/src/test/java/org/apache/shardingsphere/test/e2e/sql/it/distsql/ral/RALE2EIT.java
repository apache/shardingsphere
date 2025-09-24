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

package org.apache.shardingsphere.test.e2e.sql.it.distsql.ral;

import com.google.common.base.Splitter;
import lombok.Setter;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.metadata.DataSetMetaData;
import org.apache.shardingsphere.test.e2e.sql.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.e2e.sql.env.SQLE2EEnvironmentEngine;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITArgumentsProvider;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITSettings;
import org.apache.shardingsphere.test.e2e.sql.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EIT;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EITContext;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

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

@SQLE2EITSettings(SQLCommandType.RAL)
@Setter
class RALE2EIT implements SQLE2EIT {
    
    private SQLE2EEnvironmentEngine environmentEngine;
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(context);
        try {
            assertExecute(context);
        } finally {
            tearDown(context);
        }
    }
    
    private void assertExecute(final SQLE2EITContext context) throws SQLException {
        try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                assertResultSet(context, statement);
            }
        }
    }
    
    private void init(final SQLE2EITContext context) throws SQLException {
        if (null != context.getAssertion().getInitialSQL()) {
            try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
                executeInitSQLs(context, connection);
            }
        }
    }
    
    private void executeInitSQLs(final SQLE2EITContext context, final Connection connection) throws SQLException {
        if (null == context.getAssertion().getInitialSQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().splitToList(context.getAssertion().getInitialSQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
        Awaitility.await().pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private void tearDown(final SQLE2EITContext context) throws SQLException {
        if (null != context.getAssertion().getDestroySQL()) {
            try (Connection connection = environmentEngine.getTargetDataSource().getConnection()) {
                executeDestroySQLs(context, connection);
            }
        }
    }
    
    private void executeDestroySQLs(final SQLE2EITContext context, final Connection connection) throws SQLException {
        if (null == context.getAssertion().getDestroySQL().getSql()) {
            return;
        }
        for (String each : Splitter.on(";").trimResults().omitEmptyStrings().splitToList(context.getAssertion().getDestroySQL().getSql())) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(each)) {
                preparedStatement.executeUpdate();
            }
        }
        Awaitility.await().pollDelay(1L, TimeUnit.SECONDS).until(() -> true);
    }
    
    private void assertResultSet(final SQLE2EITContext context, final Statement statement) throws SQLException {
        if (null == context.getAssertion().getAssertionSQL()) {
            assertResultSet(context, statement, context.getSQL());
        } else {
            statement.execute(context.getSQL());
            Awaitility.await().pollDelay(2L, TimeUnit.SECONDS).until(() -> true);
            assertResultSet(context, statement, context.getAssertion().getAssertionSQL().getSql());
        }
    }
    
    private void assertResultSet(final SQLE2EITContext context, final Statement statement, final String sql) throws SQLException {
        statement.execute(sql);
        try (ResultSet resultSet = statement.getResultSet()) {
            assertResultSet(context, resultSet);
        }
    }
    
    private void assertResultSet(final SQLE2EITContext context, final ResultSet resultSet) throws SQLException {
        assertMetaData(resultSet.getMetaData(), getExpectedColumns(context));
        assertRows(resultSet, getIgnoreAssertColumns(context), context.getDataSet().getRows());
    }
    
    private Collection<DataSetColumn> getExpectedColumns(final SQLE2EITContext context) {
        Collection<DataSetColumn> result = new LinkedList<>();
        for (DataSetMetaData each : context.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns());
        }
        return result;
    }
    
    private Collection<String> getIgnoreAssertColumns(final SQLE2EITContext context) {
        Collection<String> result = new LinkedList<>();
        for (DataSetMetaData each : context.getDataSet().getMetaDataList()) {
            result.addAll(each.getColumns().stream().filter(DataSetColumn::isIgnoreAssertData).map(DataSetColumn::getName).collect(Collectors.toList()));
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
            assertTrue(rowCount < expected.size(), "Size of actual result set is different with size of expected data set rows.");
            assertRow(actual, notAssertionColumns, actualMetaData, expected.get(rowCount));
            rowCount++;
        }
        assertThat("Size of actual result set is different with size of expected data set rows.", rowCount, is(expected.size()));
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
        return E2ETestEnvironment.getInstance().isValid() && !E2ETestParameterFactory.getAssertionTestParameters(SQLCommandType.RAL).isEmpty();
    }
}
