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
import org.apache.shardingsphere.test.integration.framework.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.framework.runner.parallel.annotaion.ParallelRuntimeStrategy;
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
                Connection verificationConnection = getVerificationDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteQueryForStatement(actualConnection, verificationConnection);
            } else {
                assertExecuteQueryForPreparedStatement(actualConnection, verificationConnection);
            }
        }
    }
    
    private void assertExecuteQueryForStatement(final Connection actualConnection, final Connection verificationConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                ResultSet actualResultSet = actualStatement.executeQuery(getSQL());
                Statement verificationStatement = verificationConnection.createStatement();
                ResultSet verificationResultSet = verificationStatement.executeQuery(getSQL())) {
            assertResultSet(actualResultSet, verificationResultSet);
        }
    }
    
    private void assertExecuteQueryForPreparedStatement(final Connection actualConnection, final Connection verificationConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL());
                PreparedStatement verificationPreparedStatement = verificationConnection.prepareStatement(getSQL())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                verificationPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            try (
                    ResultSet actualResultSet = actualPreparedStatement.executeQuery();
                    ResultSet verificationResultSet = verificationPreparedStatement.executeQuery()) {
                assertResultSet(actualResultSet, verificationResultSet);
            }
        }
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        try (
                Connection actualConnection = getTargetDataSource().getConnection();
                Connection verificationConnection = getVerificationDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                assertExecuteForStatement(actualConnection, verificationConnection);
            } else {
                assertExecuteForPreparedStatement(actualConnection, verificationConnection);
            }
        }
    }
    
    private void assertExecuteForStatement(final Connection actualConnection, final Connection verificationConnection) throws SQLException, ParseException {
        try (
                Statement actualStatement = actualConnection.createStatement();
                Statement verificationStatement = verificationConnection.createStatement()) {
            assertTrue("Not a query statement.", actualStatement.execute(getSQL()) && verificationStatement.execute(getSQL()));
            try (
                    ResultSet actualResultSet = actualStatement.getResultSet();
                    ResultSet verificationResultSet = verificationStatement.getResultSet()) {
                assertResultSet(actualResultSet, verificationResultSet);
            }
        }
    }
    
    private void assertExecuteForPreparedStatement(final Connection actualConnection, final Connection verificationConnection) throws SQLException, ParseException {
        try (
                PreparedStatement actualPreparedStatement = actualConnection.prepareStatement(getSQL());
                PreparedStatement verificationPreparedStatement = verificationConnection.prepareStatement(getSQL())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                actualPreparedStatement.setObject(each.getIndex(), each.getValue());
                verificationPreparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertTrue("Not a query statement.", actualPreparedStatement.execute() && verificationPreparedStatement.execute());
            try (
                    ResultSet actualResultSet = actualPreparedStatement.getResultSet();
                    ResultSet verificationResultSet = verificationPreparedStatement.getResultSet()) {
                assertResultSet(actualResultSet, verificationResultSet);
            }
        }
    }
}
