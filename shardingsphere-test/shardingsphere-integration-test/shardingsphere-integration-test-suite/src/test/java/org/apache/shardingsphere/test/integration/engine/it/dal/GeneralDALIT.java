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

package org.apache.shardingsphere.test.integration.engine.it.dal;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.engine.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.engine.param.SQLExecuteType;
import org.apache.shardingsphere.test.integration.engine.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.engine.param.model.ParameterizedArray;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class GeneralDALIT extends BaseDALIT {
    
    public GeneralDALIT(final AssertionParameterizedArray parameterizedArray) throws IOException, JAXBException, SQLException, ParseException {
        super(parameterizedArray.getTestCaseContext().getParentPath(),
                parameterizedArray.getAssertion(),
                parameterizedArray.getAdapter(),
                parameterizedArray.getScenario(),
                parameterizedArray.getDatabaseType(),
                parameterizedArray.getSqlExecuteType(),
                parameterizedArray.getTestCaseContext().getTestCase().getSql());
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterizedArray(SQLCommandType.DAL).stream()
                .filter(each -> "proxy".equals(((ParameterizedArray) each[0]).getAdapter())
                        && each[0] instanceof AssertionParameterizedArray && SQLExecuteType.Literal == ((AssertionParameterizedArray) each[0]).getSqlExecuteType()).collect(Collectors.toList());
    }
    
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void assertExecute() throws SQLException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            assertExecuteForStatement(connection);
        }
    }
    
    private void assertExecuteForStatement(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            boolean isQuery = statement.execute(getSql());
            if (isQuery) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    assertResultSet(resultSet);
                }
            } else {
                assertThat(statement.getUpdateCount(), is(0));
            }
        }
    }
}
