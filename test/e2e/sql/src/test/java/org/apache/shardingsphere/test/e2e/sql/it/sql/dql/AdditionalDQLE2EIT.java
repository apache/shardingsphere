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

package org.apache.shardingsphere.test.e2e.sql.it.sql.dql;

import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.sql.cases.value.SQLValue;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITArgumentsProvider;
import org.apache.shardingsphere.test.e2e.sql.framework.SQLE2EITSettings;
import org.apache.shardingsphere.test.e2e.sql.framework.param.model.AssertionTestParameter;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLCommandType;
import org.apache.shardingsphere.test.e2e.sql.framework.type.SQLExecuteType;
import org.apache.shardingsphere.test.e2e.sql.it.SQLE2EITContext;
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

@SuppressWarnings("MagicConstant")
@SQLE2EITSettings(SQLCommandType.DQL)
class AdditionalDQLE2EIT extends BaseDQLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteQueryWithResultSetTypeAndConcurrency(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam, context);
        // TODO fix e2e test blocked exception with PostgreSQL or openGauss in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXMLExpected(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } else {
            assertExecuteQueryWithExpectedDataSource(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteQueryWithResultSetTypeAndConcurrencyAndHoldability(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam, context);
        // TODO fix e2e test blocked exception with PostgreSQL or openGauss in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXMLExpected(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        } else {
            assertExecuteQueryWithExpectedDataSource(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,
                    ResultSet.HOLD_CURSORS_OVER_COMMIT);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteWithResultSetTypeAndConcurrency(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam, context);
        // TODO fix e2e test blocked exception with PostgreSQL or openGauss in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXMLExpected(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        } else {
            assertExecuteWithExpectedDataSource(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteWithResultSetTypeAndConcurrencyAndHoldability(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam, context);
        // TODO fix e2e test blocked exception with PostgreSQL or openGauss in #23643
        if (isPostgreSQLOrOpenGauss(testParam.getDatabaseType().getType())) {
            return;
        }
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXMLExpected(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        } else {
            assertExecuteWithExpectedDataSource(testParam, context, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        }
    }
    
    private boolean isPostgreSQLOrOpenGauss(final String databaseType) {
        return "PostgreSQL".equals(databaseType) || "openGauss".equals(databaseType);
    }
    
    private void assertExecuteQueryWithXMLExpected(final AssertionTestParameter testParam, final SQLE2EITContext context, final int... resultSetTypes) throws SQLException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(testParam.getAdapter())) {
            return;
        }
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Statement statement = 2 == resultSetTypes.length ? connection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : connection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet resultSet = statement.executeQuery(context.getSQL())) {
            assertResultSet(context, resultSet);
        }
    }
    
    private void assertExecuteQueryWithExpectedDataSource(final AssertionTestParameter testParam, final SQLE2EITContext context, final int... resultSetTypes) throws SQLException {
        try (
                Connection actualConnection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
                assertExecuteQueryForStatementWithResultSetTypes(context, actualConnection, expectedConnection, testParam, resultSetTypes);
            } else {
                assertExecuteQueryForPreparedStatementWithResultSetTypes(context, actualConnection, expectedConnection, testParam, resultSetTypes);
            }
        }
    }
    
    private void assertExecuteQueryForStatementWithResultSetTypes(final SQLE2EITContext context,
                                                                  final Connection actualConnection, final Connection expectedConnection,
                                                                  final AssertionTestParameter testParam, final int... resultSetTypes) throws SQLException {
        try (
                Statement actualStatement = 2 == resultSetTypes.length ? actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet actualResultSet = actualStatement.executeQuery(context.getSQL());
                Statement expectedStatement = 2 == resultSetTypes.length ? expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                ResultSet expectedResultSet = expectedStatement.executeQuery(context.getSQL())) {
            assertResultSet(actualResultSet, expectedResultSet, testParam);
        }
    }
    
    private void assertExecuteQueryForPreparedStatementWithResultSetTypes(final SQLE2EITContext context, final Connection actualConnection, final Connection expectedConnection,
                                                                          final AssertionTestParameter testParam, final int... resultSetTypes) throws SQLException {
        try (
                PreparedStatement actualPreparedStatement = 2 == resultSetTypes.length ? actualConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                PreparedStatement expectedPreparedStatement = 2 == resultSetTypes.length ? expectedConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (
                    ResultSet actualResultSet = actualPreparedStatement.executeQuery();
                    ResultSet expectedResultSet = expectedPreparedStatement.executeQuery()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    private void assertExecuteWithXMLExpected(final AssertionTestParameter testParam, final SQLE2EITContext context, final int... resultSetTypes) throws SQLException {
        // TODO Fix jdbc adapter
        if ("jdbc".equals(testParam.getAdapter())) {
            return;
        }
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Statement statement = 2 == resultSetTypes.length ? connection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : connection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            assertTrue(statement.execute(context.getSQL()), "Not a query statement.");
            ResultSet resultSet = statement.getResultSet();
            assertResultSet(context, resultSet);
        }
    }
    
    private void assertExecuteWithExpectedDataSource(final AssertionTestParameter testParam, final SQLE2EITContext context, final int... resultSetTypes) throws SQLException {
        try (
                Connection actualConnection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
                assertExecuteForStatementWithResultSetTypes(context, actualConnection, expectedConnection, testParam, resultSetTypes);
            } else {
                assertExecuteForPreparedStatementWithResultSetTypes(context, actualConnection, expectedConnection, testParam, resultSetTypes);
            }
        }
    }
    
    private void assertExecuteForStatementWithResultSetTypes(final SQLE2EITContext context, final Connection actualConnection, final Connection expectedConnection,
                                                             final AssertionTestParameter testParam, final int... resultSetTypes) throws SQLException {
        try (
                Statement actualStatement = 2 == resultSetTypes.length ? actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                Statement expectedStatement = 2 == resultSetTypes.length ? expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.createStatement(resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            assertTrue(actualStatement.execute(context.getSQL()) && expectedStatement.execute(context.getSQL()), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    private void assertExecuteForPreparedStatementWithResultSetTypes(final SQLE2EITContext context, final Connection actualConnection, final Connection expectedConnection,
                                                                     final AssertionTestParameter testParam, final int... resultSetTypes) throws SQLException {
        try (
                PreparedStatement actualPreparedStatement = 2 == resultSetTypes.length ? actualConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : actualConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2]);
                PreparedStatement expectedPreparedStatement = 2 == resultSetTypes.length ? expectedConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1])
                        : expectedConnection.prepareStatement(context.getSQL(), resultSetTypes[0], resultSetTypes[1], resultSetTypes[2])) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue(actualPreparedStatement.execute() && expectedPreparedStatement.execute(), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestEnvironment.getInstance().isValid() && E2ETestEnvironment.getInstance().getRunEnvironment().isRunAdditionalCases();
    }
}
