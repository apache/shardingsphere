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

package org.apache.shardingsphere.test.integration.engine.dml;

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
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;

import static org.junit.Assert.assertFalse;

@ParallelRuntimeStrategy(ParallelLevel.SCENARIO)
public final class GeneralDMLIT extends BaseDMLIT {
    
    public GeneralDMLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Parameters(name = "{0}")
    public static Collection<AssertionParameterizedArray> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterized(SQLCommandType.DML);
    }
    
    @Test
    public void assertExecuteUpdate() throws SQLException, ParseException {
        switch (getScenario()) {
            case "replica_query":
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
            return statement.executeUpdate(getSQL());
        }
    }
    
    private int executeUpdateForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSQL())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        switch (getScenario()) {
            case "replica_query":
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
            assertFalse("Not a DML statement", statement.execute(getSQL()));
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSQL())) {
            for (SQLValue each : getAssertion().getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
        }
    }
}
