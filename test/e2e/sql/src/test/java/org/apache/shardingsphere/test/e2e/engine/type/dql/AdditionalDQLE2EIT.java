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

package org.apache.shardingsphere.test.e2e.engine.type.dql;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.cases.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseArgumentsProvider;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseSettings;
import org.apache.shardingsphere.test.e2e.engine.composer.SingleE2EContainerComposer;
import org.apache.shardingsphere.test.e2e.env.runtime.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@E2ETestCaseSettings(SQLCommandType.DQL)
class AdditionalDQLE2EIT extends BaseDQLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecuteQueryWithResultSetTypeAndConcurrency(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        init(testParam, containerComposer);
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXMLExpected(testParam, containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } else {
            assertExecuteQueryWithExpectedDataSource(containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecuteQueryWithResultSetTypeAndConcurrencyAndHoldability(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        init(testParam, containerComposer);
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXMLExpected(testParam, containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        } else {
            assertExecuteQueryWithExpectedDataSource(containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecuteWithResultSetTypeAndConcurrency(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        init(testParam, containerComposer);
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXMLExpected(testParam, containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } else {
            assertExecuteWithExpectedDataSource(containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(E2ETestCaseArgumentsProvider.class)
    void assertExecuteWithResultSetTypeAndConcurrencyAndHoldability(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SingleE2EContainerComposer containerComposer = new SingleE2EContainerComposer(testParam);
        init(testParam, containerComposer);
        // TODO fix e2e test blocked exception with PostgreSQL or openGuass in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXMLExpected(testParam, containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        } else {
            assertExecuteWithExpectedDataSource(containerComposer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        }
    }
    
    private boolean isPostgreSQLOrOpenGauss(final String databaseType) {
        return "PostgreSQL".equals(databaseType) || "openGauss".equals(databaseType);
    }
    
    private void assertExecuteQueryWithXMLExpected(final AssertionTestParameter testParam,
                                                   final SingleE2EContainerComposer containerComposer, final int... resultSetTypes) throws SQLException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(testParam.getAdapter())) {
            return;
        }
        try (
                Connection connection = containerComposer.getTargetDataSource().getConnection();
                Statement statement = 2 == resultSetTypes.length ? connection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : connection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet resultSet = statement.executeQuery(containerComposer.getSQL())) {
            assertResultSet(containerComposer, resultSet);
        }
    }
    
    private void assertExecuteQueryWithExpectedDataSource(final SingleE2EContainerComposer containerComposer, final int... resultSetTypes) throws SQLException {
        try (
                Connection actualConnection = containerComposer.getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == containerComposer.getSqlExecuteType()) {
                assertExecuteQueryForStatementWithResultSetTypes(containerComposer, actualConnection, expectedConnection, resultSetTypes);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypes(containerComposer, actualConnection, expectedConnection, resultSetTypes);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypes(final SingleE2EContainerComposer containerComposer,
                                                                  final Connection actualConnection, final Connection expectedConnection,
                                                                  final int... resultSetTypes) throws SQLException {
        try (
                Statement actualStatement = 2 == resultSetTypes.length ? actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet actualResultSet = actualStatement.executeQuery(containerComposer.getSQL());
                Statement expectedStatement = 2 == resultSetTypes.length ? expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet expectedResultSet = expectedStatement.executeQuery(containerComposer.getSQL())) {
            assertResultSet(actualResultSet, expectedResultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypes(final SingleE2EContainerComposer containerComposer, final Connection actualConnection, final Connection expectedConnection,
                                                                          final int... resultSetTypes) throws SQLException {
        try (
                PreparedStatement actualPreparedStatement = 2 == resultSetTypes.length ? actualConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                PreparedStatement expectedPreparedStatement = 2 == resultSetTypes.length ? expectedConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            for (SQLValue each : containerComposer.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (
                    ResultSet actualResultSet = actualPreparedStatement.executeQuery();
                    ResultSet expectedResultSet = expectedPreparedStatement.executeQuery()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private void assertExecuteWithXMLExpected(final AssertionTestParameter testParam,
                                              final SingleE2EContainerComposer containerComposer, final int... resultSetTypes) throws SQLException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(testParam.getAdapter())) {
            return;
        }
        try (
                Connection connection = containerComposer.getTargetDataSource().getConnection();
                Statement statement = 2 == resultSetTypes.length ? connection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : connection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            assertTrue(statement.execute(containerComposer.getSQL()), "Not a query statement.");
            ResultSet resultSet = statement.getResultSet();
            assertResultSet(containerComposer, resultSet);
        }
    }
    
    private void assertExecuteWithExpectedDataSource(final SingleE2EContainerComposer containerComposer, final int... resultSetTypes) throws SQLException {
        try (
                Connection actualConnection = containerComposer.getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == containerComposer.getSqlExecuteType()) {
                assertExecuteForStatementWithResultSetTypes(containerComposer, actualConnection, expectedConnection, resultSetTypes);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypes(containerComposer, actualConnection, expectedConnection, resultSetTypes);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypes(final SingleE2EContainerComposer containerComposer,
                                                             final Connection actualConnection, final Connection expectedConnection, final int... resultSetTypes) throws SQLException {
        try (
                Statement actualStatement = 2 == resultSetTypes.length ? actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                Statement expectedStatement = 2 == resultSetTypes.length ? expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            assertTrue(actualStatement.execute(containerComposer.getSQL()) && expectedStatement.execute(containerComposer.getSQL()), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypes(final SingleE2EContainerComposer containerComposer, final Connection actualConnection, final Connection expectedConnection,
                                                                     final int... resultSetTypes) throws SQLException {
        try (
                PreparedStatement actualPreparedStatement = 2 == resultSetTypes.length ? actualConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                PreparedStatement expectedPreparedStatement = 2 == resultSetTypes.length ? expectedConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.prepareStatement(containerComposer.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            for (SQLValue each : containerComposer.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue(actualPreparedStatement.execute() && expectedPreparedStatement.execute(), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestParameterFactory.containsTestParameter() && IntegrationTestEnvironment.getInstance().isRunAdditionalTestCases();
    }
}
