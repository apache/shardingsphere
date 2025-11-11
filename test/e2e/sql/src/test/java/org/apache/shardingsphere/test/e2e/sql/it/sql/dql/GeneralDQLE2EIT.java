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

@SQLE2EITSettings(SQLCommandType.DQL)
class GeneralDQLE2EIT extends BaseDQLE2EIT {
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecuteQuery(final AssertionTestParameter testParam) throws SQLException, IOException, JAXBException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam, context);
        assertExecuteQuery(testParam, context);
    }
    
    private void assertExecuteQuery(final AssertionTestParameter testParam, final SQLE2EITContext context) throws SQLException {
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteQueryWithXmlExpected(testParam, context);
        } else {
            assertExecuteQueryWithExpectedDataSource(testParam, context);
        }
    }
    
    private void assertExecuteQueryWithXmlExpected(final AssertionTestParameter testParam, final SQLE2EITContext context) throws SQLException {
        // TODO Fix jdbc adapter
        if (isNeedSkipExecuteQueryWithXmlExcepted(testParam)) {
            return;
        }
        if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
            assertQueryForStatementWithXmlExpected(context);
        } else {
            assertQueryForPreparedStatementWithXmlExpected(context);
        }
    }
    
    private boolean isNeedSkipExecuteQueryWithXmlExcepted(final AssertionTestParameter testParam) {
        return "jdbc".equals(testParam.getAdapter());
    }
    
    private void assertQueryForStatementWithXmlExpected(final SQLE2EITContext context) throws SQLException {
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(context.getSQL())) {
            assertResultSet(context, resultSet);
        }
    }
    
    private void assertQueryForPreparedStatementWithXmlExpected(final SQLE2EITContext context) throws SQLException {
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(context.getSQL())) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                assertResultSet(context, resultSet);
            }
        }
    }
    
    private void assertExecuteQueryWithExpectedDataSource(final AssertionTestParameter testParam, final SQLE2EITContext context) throws SQLException {
        try (
                Connection expectedConnection = getExpectedDataSource().getConnection();
                Connection actualConnection = getEnvironmentEngine().getTargetDataSource().getConnection()) {
            if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
                assertExecuteQueryForStatement(context, actualConnection, expectedConnection, testParam);
            } else {
                assertExecuteQueryForPreparedStatement(context, actualConnection, expectedConnection, testParam);
            }
        }
    }
    
    private void assertExecuteQueryForStatement(final SQLE2EITContext context, final Connection actualConnection, final Connection expectedConnection,
                                                final AssertionTestParameter testParam) throws SQLException {
        String assertionSQL = null == context.getAssertion().getAssertionSQL() ? context.getSQL() : context.getAssertion().getAssertionSQL().getSql();
        try (
                Statement expectedStatement = expectedConnection.createStatement();
                ResultSet expectedResultSet = expectedStatement.executeQuery(assertionSQL);
                Statement actualStatement = actualConnection.createStatement();
                ResultSet actualResultSet = actualStatement.executeQuery(context.getSQL())) {
            assertResultSet(actualResultSet, expectedResultSet, testParam);
        }
    }
    
    private void assertExecuteQueryForPreparedStatement(final SQLE2EITContext context, final Connection actualConnection, final Connection expectedConnection,
                                                        final AssertionTestParameter testParam) throws SQLException {
        String assertionSQL = null == context.getAssertion().getAssertionSQL() ? context.getSQL() : context.getAssertion().getAssertionSQL().getSql();
        try (
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(assertionSQL);
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(context.getSQL())) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (
                    ResultSet expectedResultSet = expectedPreparedStatement.executeQuery();
                    ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(SQLE2EITArgumentsProvider.class)
    void assertExecute(final AssertionTestParameter testParam) throws SQLException, JAXBException, IOException {
        // TODO make sure test case can not be null
        if (null == testParam.getTestCaseContext()) {
            return;
        }
        SQLE2EITContext context = new SQLE2EITContext(testParam);
        init(testParam, context);
        assertExecute(testParam, context);
    }
    
    private void assertExecute(final AssertionTestParameter testParam, final SQLE2EITContext context) throws SQLException {
        if (isUseXMLAsExpectedDataset()) {
            assertExecuteWithXmlExpected(testParam, context);
        } else {
            assertExecuteWithExpectedDataSource(testParam, context);
        }
    }
    
    private void assertExecuteWithXmlExpected(final AssertionTestParameter testParam, final SQLE2EITContext context) throws SQLException {
        // TODO Fix jdbc adapter
        if (isNeedSkipExecuteWithXmlExcepted(testParam)) {
            return;
        }
        if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
            assertExecuteForStatementWithXmlExpected(context);
        } else {
            assertExecuteForPreparedStatementWithXmlExpected(context);
        }
    }
    
    private boolean isNeedSkipExecuteWithXmlExcepted(final AssertionTestParameter testParam) {
        return "jdbc".equals(testParam.getAdapter());
    }
    
    private void assertExecuteForStatementWithXmlExpected(final SQLE2EITContext context) throws SQLException {
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Statement statement = connection.createStatement()) {
            assertTrue(statement.execute(context.getSQL()), "Not a query statement.");
            ResultSet resultSet = statement.getResultSet();
            assertResultSet(context, resultSet);
        }
    }
    
    private void assertExecuteForPreparedStatementWithXmlExpected(final SQLE2EITContext context) throws SQLException {
        try (
                Connection connection = getEnvironmentEngine().getTargetDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(context.getSQL())) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue(preparedStatement.execute(), "Not a query preparedStatement.");
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertResultSet(context, resultSet);
            }
        }
    }
    
    private void assertExecuteWithExpectedDataSource(final AssertionTestParameter testParam, final SQLE2EITContext context) throws SQLException {
        try (
                Connection actualConnection = getEnvironmentEngine().getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.LITERAL == context.getSqlExecuteType()) {
                assertExecuteForStatement(context, actualConnection, expectedConnection, testParam);
            } else {
                assertExecuteForPreparedStatement(context, actualConnection, expectedConnection, testParam);
            }
        }
    }
    
    private void assertExecuteForStatement(final SQLE2EITContext context, final Connection actualConnection, final Connection expectedConnection,
                                           final AssertionTestParameter testParam) throws SQLException {
        String assertionSQL = null == context.getAssertion().getAssertionSQL() ? context.getSQL() : context.getAssertion().getAssertionSQL().getSql();
        try (
                Statement actualStatement = actualConnection.createStatement();
                Statement expectedStatement = expectedConnection.createStatement()) {
            assertTrue(expectedStatement.execute(assertionSQL) && actualStatement.execute(context.getSQL()), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    private void assertExecuteForPreparedStatement(final SQLE2EITContext context, final Connection actualConnection, final Connection expectedConnection,
                                                   final AssertionTestParameter testParam) throws SQLException {
        String assertionSQL = null == context.getAssertion().getAssertionSQL() ? context.getSQL() : context.getAssertion().getAssertionSQL().getSql();
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(context.getSQL());
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(assertionSQL)) {
            for (SQLValue each : context.getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue(expectedPreparedStatement.execute() && actualPreparedStatement.execute(), "Not a query statement.");
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet, testParam);
            }
        }
    }
    
    private static boolean isEnabled() {
        return E2ETestEnvironment.getInstance().isValid();
    }
}
