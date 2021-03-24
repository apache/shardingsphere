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

package org.apache.shardingsphere.test.integration.engine.it.dcl;

import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.junit.annotation.TestCaseSpec;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelLevel;
import org.apache.shardingsphere.test.integration.junit.runner.parallel.annotaion.ParallelRuntimeStrategy;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

@TestCaseSpec(sqlCommandType = SQLCommandType.DCL)
@ParallelRuntimeStrategy(ParallelLevel.SCENARIO)
public final class GeneralDCLIT extends BaseDCLIT {
    
    @Test
    public void assertExecuteUpdate() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                connection.createStatement().executeUpdate(getStatement());
            } else {
                connection.prepareStatement(getStatement()).executeUpdate();
            }
        }
    }
    
    @Test
    public void assertExecute() throws SQLException, ParseException {
        try (Connection connection = getTargetDataSource().getConnection()) {
            if (SQLExecuteType.Literal == getSqlExecuteType()) {
                connection.createStatement().execute(getStatement());
            } else {
                connection.prepareStatement(getStatement()).execute();
            }
        }
    }
}
