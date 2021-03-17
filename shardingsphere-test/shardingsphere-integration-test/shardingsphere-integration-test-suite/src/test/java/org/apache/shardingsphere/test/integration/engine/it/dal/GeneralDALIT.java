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
import org.apache.shardingsphere.test.integration.junit.annotation.ParameterFilter;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;
import org.apache.shardingsphere.test.integration.junit.param.TestCaseParameters;
import org.apache.shardingsphere.test.integration.junit.runner.TestCaseBeanContext;
import org.apache.shardingsphere.test.integration.junit.runner.TestCaseDescription;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ParameterFilter(filter = GeneralDALIT.Filter.class)
@TestCaseSpec(sqlCommandType = SQLCommandType.DAL)
@ParallelRuntimeStrategy(ParallelLevel.SCENARIO)
public final class GeneralDALIT extends BaseDALIT {
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            assertExecuteForStatement(connection);
        }
    }
    
    private void assertExecuteForStatement(final Connection connection) throws SQLException, ParseException {
        try (Statement statement = connection.createStatement()) {
            boolean isQuery = statement.execute(getStatement());
            if (isQuery) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    assertResultSet(resultSet);
                }
            } else {
                assertThat(statement.getUpdateCount(), is(0));
            }
        }
    }
    
    public static class Filter implements ParameterFilter.Filter {
        @Override
        public boolean filter(final TestCaseBeanContext context) {
            return context.getBean(TestCaseParameters.class).getExecuteType() == SQLExecuteType.Literal
                    && "proxy".equals(context.getBean(TestCaseDescription.class).getAdapter());
        }
    }
}
