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

package org.apache.shardingsphere.dbtest.engine.dml;

import org.apache.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.dbtest.engine.SQLType;
import org.apache.shardingsphere.dbtest.engine.util.IntegrateTestParameters;
import org.apache.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;

import static org.junit.Assert.assertFalse;

public final class GeneralDMLIT extends BaseDMLIT {
    
    private final DMLIntegrateTestCaseAssertion assertion;
    
    public GeneralDMLIT(final String sqlCaseId, final String path, final DMLIntegrateTestCaseAssertion assertion, final String shardingRuleType,
                        final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException, ParseException {
        super(sqlCaseId, path, assertion, shardingRuleType, databaseTypeEnvironment, caseType);
        this.assertion = assertion;
    }
    
    @Parameters(name = "{0} -> Rule:{3} -> {4} -> {5}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestParameters.getParametersWithAssertion(SQLType.DML);
    }
    
    @Test
    public void assertExecuteUpdate() throws JAXBException, IOException, SQLException, ParseException {
        // TODO fix masterslave
        if (!getDatabaseTypeEnvironment().isEnabled() || "masterslave".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeUpdateForStatement(connection) : executeUpdateForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeUpdateForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(String.format(getSql(), assertion.getSQLValues().toArray()));
        }
    }
    
    private int executeUpdateForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            return preparedStatement.executeUpdate();
        }
    }
    
    @Test
    public void assertExecute() throws JAXBException, IOException, SQLException, ParseException {
        // TODO fix masterslave
        if (!getDatabaseTypeEnvironment().isEnabled() || "masterslave".equals(getRuleType())) {
            return;
        }
        int actualUpdateCount;
        try (Connection connection = getDataSource().getConnection()) {
            actualUpdateCount = SQLCaseType.Literal == getCaseType() ? executeForStatement(connection) : executeForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCount);
    }
    
    private int executeForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            assertFalse("Not a DML statement.", statement.execute(String.format(getSql(), assertion.getSQLValues().toArray())));
            return statement.getUpdateCount();
        }
    }
    
    private int executeForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (SQLValue each : assertion.getSQLValues()) {
                preparedStatement.setObject(each.getIndex(), each.getValue());
            }
            assertFalse("Not a DML statement.", preparedStatement.execute());
            return preparedStatement.getUpdateCount();
        }
    }
}
