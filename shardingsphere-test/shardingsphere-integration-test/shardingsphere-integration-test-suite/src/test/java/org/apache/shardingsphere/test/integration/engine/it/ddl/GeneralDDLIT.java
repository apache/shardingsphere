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

package org.apache.shardingsphere.test.integration.engine.it.ddl;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.engine.param.SQLExecuteType;
import org.apache.shardingsphere.test.integration.engine.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.engine.param.domain.ParameterizedWrapper;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

public final class GeneralDDLIT extends BaseDDLIT {
    
    public GeneralDDLIT(final ParameterizedWrapper parameterizedWrapper) throws IOException, JAXBException, SQLException, ParseException {
        super(parameterizedWrapper.getTestCaseContext().getParentPath(),
                parameterizedWrapper.getAssertion(),
                parameterizedWrapper.getAdapter(),
                parameterizedWrapper.getScenario(),
                parameterizedWrapper.getDatabaseType(),
                parameterizedWrapper.getSqlExecuteType(),
                parameterizedWrapper.getTestCaseContext().getTestCase().getSql());
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterizedArray(SQLCommandType.DDL);
    }
    
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void assertExecuteUpdate() throws SQLException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                connection.createStatement().executeUpdate(getSql());
            } else {
                connection.prepareStatement(getSql()).executeUpdate();
            }
            assertTableMetaData();
        }
    }
    
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void assertExecute() throws SQLException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                connection.createStatement().execute(getSql());
            } else {
                connection.prepareStatement(getSql()).execute();
            }
            assertTableMetaData();
        }
    }
}
