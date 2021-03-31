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
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.junit.compose.ComposeManager;
import org.apache.shardingsphere.test.integration.junit.param.ParameterizedArrayFactory;
import org.apache.shardingsphere.test.integration.junit.param.model.AssertionParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ParallelRuntimeStrategy(ParallelLevel.SCENARIO)
public final class GeneralDALIT extends BaseDALIT {
    
    @ClassRule
    public static ComposeManager composeManager = new ComposeManager("GeneralDALIT");
    
    public GeneralDALIT(final AssertionParameterizedArray parameterizedArray) {
        super(parameterizedArray);
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<ParameterizedArray> getParameters() {
        return ParameterizedArrayFactory.getAssertionParameterized(SQLCommandType.DAL)
                .stream()
                .filter(each -> SQLExecuteType.Literal == each.getSqlExecuteType())
                .filter(each -> "proxy".equals(each.getAdapter()))
                .peek(each -> each.setCompose(composeManager.getOrCreateCompose(each)))
                .collect(Collectors.toList());
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            assertExecuteForStatement(connection);
        }
    }
    
    private void assertExecuteForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            boolean isQuery = statement.execute(getSQL());
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
