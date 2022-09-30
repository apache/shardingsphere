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

package org.apache.shardingsphere.test.integration.engine.dql;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.SQLExecuteType;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.framework.param.array.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

@ParallelRuntimeStrategy(ParallelLevel.CASE)
public final class GeneralDQLIT extends BaseDQLIT {
    
    public GeneralDQLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Parameters(name = "{0}")
    public static Collection<AssertionParameterizedArray> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterized(SQLCommandType.DQL);
    }
    
    @Test
    public void assertExecuteQuery() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatement(actualConnection, expectedConnection);
            } else {
                assertExecuteQueryForPreparedStatement(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteQueryForStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                ResultSet actualResultSet = actualStatement.executeQuery(getSQL());
                Statement expectedStatement = expectedConnection.createStatement();
                ResultSet expectedResultSet = expectedStatement.executeQuery(getSQL())) {
            assertResultSet(actualResultSet, expectedResultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL());
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(getSQL())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
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
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection expectedConnection = getExpectedDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatement(actualConnection, expectedConnection);
            } else {
                assertExecuteForPreparedStatement(actualConnection, expectedConnection);
            }
        }
    }
    
    private void assertExecuteForStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                Statement expectedStatement = expectedConnection.createStatement()) {
            assertTrue("Not a query statement", actualStatement.execute(getSQL()) && expectedStatement.execute(getSQL()));
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet expectedResultSet = expectedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatement(final Connection actualConnection, final Connection expectedConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL());
                PreparedStatement expectedPreparedStatement = expectedConnection.prepareStatement(getSQL())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                expectedPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement", actualPreparedStatement.execute() && expectedPreparedStatement.execute());
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet expectedResultSet = expectedPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, expectedResultSet);
            }
        }
    }
}
