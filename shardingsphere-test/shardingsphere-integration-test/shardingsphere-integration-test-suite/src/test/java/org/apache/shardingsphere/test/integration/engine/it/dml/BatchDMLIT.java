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
import org.apache.shardingsphere.test.integration.cases.IntegrationTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.engine.it.BatchIT;
import org.apache.shardingsphere.test.integration.engine.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedWrapper;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BatchDMLIT extends BatchIT {
    
    private final IntegrationTestCaseContext testCaseContext;
    
    public BatchDMLIT(final ParameterizedWrapper parameterizedWrapper) throws IOException, JAXBException, SQLException {
        super(parameterizedWrapper.getTestCaseContext(),
                parameterizedWrapper.getAdapter(),
                parameterizedWrapper.getScenario(),
                parameterizedWrapper.getDatabaseType(),
                parameterizedWrapper.getTestCaseContext().getTestCase().getSql());
        this.testCaseContext = parameterizedWrapper.getTestCaseContext();
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return ParameterizedArrayFactory.getCaseParameterizedArray(SQLCommandType.DML);
    }
    
    @Test
    public void assertExecuteBatch() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getScenario())) {
            return;
        }
        // TODO fix shadow
        if ("shadow".equals(getScenario())) {
            return;
        }
        // TODO fix encrypt
        if ("encrypt".equals(getScenario())) {
            return;
        }
        int[] actualUpdateCounts;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCounts = executeBatchForPreparedStatement(connection);
        }
        assertDataSets(actualUpdateCounts);
    }
    
    private int[] executeBatchForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (IntegrationTestCaseAssertion each : testCaseContext.getTestCase().getAssertions()) {
                addBatch(preparedStatement, each);
            }
            return preparedStatement.executeBatch();
        }
    }
    
    private void addBatch(final PreparedStatement preparedStatement, final IntegrationTestCaseAssertion assertion) throws ParseException, SQLException {
        for (SQLValue each : assertion.getSQLValues()) {
            preparedStatement.setObject(each.getIndex(), each.getValue());
        }
        preparedStatement.addBatch();
    }
    
    @Test
    public void assertClearBatch() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getScenario())) {
            return;
        }
        // TODO fix shadow
        if ("shadow".equals(getScenario())) {
            return;
        }
        // TODO fix encrypt
        if ("encrypt".equals(getScenario())) {
            return;
        }
        try (Connection connection = getTargetDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
                for (IntegrationTestCaseAssertion each : testCaseContext.getTestCase().getAssertions()) {
                    addBatch(preparedStatement, each);
                }
                preparedStatement.clearBatch();
                assertThat(preparedStatement.executeBatch().length, is(0));
            }
        }
    }
}
