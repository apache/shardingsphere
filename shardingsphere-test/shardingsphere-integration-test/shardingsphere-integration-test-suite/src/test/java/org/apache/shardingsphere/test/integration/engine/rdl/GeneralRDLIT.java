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

package org.apache.shardingsphere.test.integration.engine.rdl;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.framework.param.array.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.framework.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

@ParallelRuntimeStrategy(ParallelLevel.SCENARIO)
public final class GeneralRDLIT extends BaseRDLIT {
    
    public GeneralRDLIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Parameters(name = "{0}")
    public static Collection<AssertionParameterizedArray> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterized(SQLCommandType.RDL);
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        assertNotNull("Assertion SQL is required", getAssertion().getAssertionSQL());
        try (Connection connection = getTargetDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                executeSQLCase(statement);
                sleep();
                assertResultSet(statement);
            }
        }
    }
    
    private void executeSQLCase(final Statement statement) throws SQLException, ParseException {
        statement.execute(getSQL());
    }
    
    private void assertResultSet(final Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(getAssertion().getAssertionSQL().getSql())) {
            assertResultSet(resultSet);
        }
    }
    
    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (final InterruptedException ignored) {
        }
    }
}
