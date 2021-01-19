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

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrationTestCaseAssertion;
import org.apache.shardingsphere.test.integration.engine.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.engine.param.SQLExecuteType;
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
    
    public GeneralDALIT(final String parentPath, final IntegrationTestCaseAssertion assertion, final String adapter, final String scenario,
                        final String databaseType, final SQLExecuteType sqlExecuteType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, adapter, scenario, DatabaseTypeRegistry.getActualDatabaseType(databaseType), sqlExecuteType, sql);
    }
    
    @Parameters(name = "{2}: {3} -> {4} -> {5} -> {6}")
    public static Collection<Object[]> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterizedArray(SQLCommandType.DAL).stream()
                .filter(each -> "proxy".equals(each[2]) && SQLExecuteType.Literal == each[5]).collect(Collectors.toList());
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
