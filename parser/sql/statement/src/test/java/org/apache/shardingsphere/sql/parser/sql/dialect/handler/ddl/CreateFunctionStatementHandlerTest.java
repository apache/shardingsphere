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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateFunctionStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateFunctionStatementHandlerTest {
    
    @Test
    void assertGetRoutineBodySegmentWithRoutineBodySegmentForMySQL() {
        MySQLCreateFunctionStatement createFunctionStatement = new MySQLCreateFunctionStatement();
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        createFunctionStatement.setRoutineBody(routineBody);
        Optional<RoutineBodySegment> routineBodySegment = CreateFunctionStatementHandler.getRoutineBodySegment(createFunctionStatement);
        assertTrue(routineBodySegment.isPresent());
    }
    
    @Test
    void assertGetRoutineBodySegmentWithoutRoutineBodySegmentForMySQL() {
        MySQLCreateFunctionStatement createFunctionStatement = new MySQLCreateFunctionStatement();
        Optional<RoutineBodySegment> routineBodySegment = CreateFunctionStatementHandler.getRoutineBodySegment(createFunctionStatement);
        assertFalse(routineBodySegment.isPresent());
    }
    
    @Test
    void assertGetRoutineBodySegmentForOtherDatabases() {
        assertFalse(CreateFunctionStatementHandler.getRoutineBodySegment(new OpenGaussCreateFunctionStatement()).isPresent());
        assertFalse(CreateFunctionStatementHandler.getRoutineBodySegment(new OracleCreateFunctionStatement(Collections.emptyList(), Collections.emptyList(), Collections.emptyList())).isPresent());
        assertFalse(CreateFunctionStatementHandler.getRoutineBodySegment(new PostgreSQLCreateFunctionStatement()).isPresent());
        assertFalse(CreateFunctionStatementHandler.getRoutineBodySegment(new SQLServerCreateFunctionStatement()).isPresent());
    }
}
