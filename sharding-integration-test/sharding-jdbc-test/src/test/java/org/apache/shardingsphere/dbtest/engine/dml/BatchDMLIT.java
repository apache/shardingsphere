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

import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCase;
import org.apache.shardingsphere.dbtest.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.dbtest.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.dbtest.engine.BatchIT;
import org.apache.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BatchDMLIT extends BatchIT {
    
    private final IntegrateTestCase integrateTestCase;
    
    public BatchDMLIT(final String sqlCaseId, final IntegrateTestCase integrateTestCase,
                      final String shardingRuleType, final DatabaseTypeEnvironment databaseTypeEnvironment) throws IOException, JAXBException, SQLException {
        super(sqlCaseId, integrateTestCase, shardingRuleType, databaseTypeEnvironment);
        this.integrateTestCase = integrateTestCase;
    }
    
    @Test
    public void assertExecuteBatch() throws JAXBException, IOException, SQLException, ParseException {
        // TODO fix masterslave
        if (!getDatabaseTypeEnvironment().isEnabled() || "masterslave".equals(getRuleType())) {
            return;
        }
        int[] actualUpdateCounts;
        try (Connection connection = getDataSource().getConnection()) {
            actualUpdateCounts = executeBatchForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCounts);
    }
    
    private int[] executeBatchForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
                addBatch(preparedStatement, each);
            }
            return preparedStatement.executeBatch();
        }
    }
    
    private void addBatch(final PreparedStatement preparedStatement, final IntegrateTestCaseAssertion assertion) throws ParseException, SQLException {
        for (SQLValue each : assertion.getSQLValues()) {
            preparedStatement.setObject(each.getIndex(), each.getValue());
        }
        preparedStatement.addBatch();
    }
    
    @Test
    public void assertClearBatch() throws SQLException, ParseException {
        // TODO fix masterslave
        if (!getDatabaseTypeEnvironment().isEnabled() || "masterslave".equals(getRuleType())) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
                for (IntegrateTestCaseAssertion each : integrateTestCase.getIntegrateTestCaseAssertions()) {
                    addBatch(preparedStatement, each);
                }
                preparedStatement.clearBatch();
                assertThat(preparedStatement.executeBatch().length, is(0));
            }
        }
    }
}
