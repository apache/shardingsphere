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
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.value.SQLValue;
import org.apache.shardingsphere.test.integration.common.ExecutionMode;
import org.apache.shardingsphere.test.integration.engine.it.BatchITCase;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@TestCaseSpec(sqlCommandType = SQLCommandType.DML, executionMode = ExecutionMode.BATCH)
@ParallelRuntimeStrategy(ParallelLevel.SCENARIO)
public final class BatchDMLIT extends BatchITCase {
    
    @Test
    public void assertExecuteBatch() throws SQLException, ParseException {
        switch (getDescription().getScenario()) {
            case "replica_query":
            case "shadow":
            case "encrypt":
                return;
            default:
        }
        int[] actualUpdateCounts;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCounts = executeBatchForPreparedStatement(connection);
        }
        assertDataSets(actualUpdateCounts);
    }
    
    private int[] executeBatchForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getStatement())) {
            for (IntegrationTestCaseAssertion each : getTestCase().getAssertions()) {
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
        switch (getDescription().getScenario()) {
            case "replica_query":
            case "shadow":
            case "encrypt":
                return;
            default:
        }
        try (Connection connection = getTargetDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getStatement())) {
                for (IntegrationTestCaseAssertion each : getTestCase().getAssertions()) {
                    addBatch(preparedStatement, each);
                }
                preparedStatement.clearBatch();
                assertThat(preparedStatement.executeBatch().length, is(0));
            }
        }
    }
}
