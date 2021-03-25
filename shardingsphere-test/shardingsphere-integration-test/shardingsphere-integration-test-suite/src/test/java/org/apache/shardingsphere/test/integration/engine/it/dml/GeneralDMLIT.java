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

package org.apache.shardingsphere.test.integration.engine.it.dml;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

import static org.junit.Assert.assertFalse;

@TestCaseSpec(name = "General DML", sqlCommandType = SQLCommandType.DML)
@ParallelRuntimeStrategy(ParallelLevel.SCENARIO)
public final class GeneralDMLIT extends BaseDMLIT {
    
    @Test
    public void assertExecuteUpdate() throws SQLException, ParseException {
        switch (getDescription().getScenario()) {
            case "replica_query":
            case "shadow":
            case "encrypt":
                return;
            default:
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLExecuteType.Literal == getSqlExecuteType() ? executeUpdateForStatement(connection) : executeUpdateForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeUpdateForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(getStatement());
        }
    }
    
    private int executeUpdateForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getStatement())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        switch (getDescription().getScenario()) {
            case "replica_query":
            case "shadow":
            case "encrypt":
                return;
            default:
        }
        int actualUpdateCount;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCount = SQLExecuteType.Literal == getSqlExecuteType() ? executeForStatement(connection) : executeForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(getStatement()));
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getStatement())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
        }
    }
}
