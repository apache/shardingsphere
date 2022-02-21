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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateProcedureStatement;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class CreateProcedureStatementHandlerTest {
    
    @Test
    public void assertGetRoutineBodySegmentWithRoutineBodySegmentForMySQL() {
        MySQLCreateProcedureStatement createProcedureStatement = new MySQLCreateProcedureStatement();
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        createProcedureStatement.setRoutineBody(routineBody);
        Optional<RoutineBodySegment> routineBodySegment = CreateProcedureStatementHandler.getRoutineBodySegment(createProcedureStatement);
        assertTrue(routineBodySegment.isPresent());
    }
    
    @Test
    public void assertGetRoutineBodySegmentWithoutRoutineBodySegmentForMySQL() {
        MySQLCreateProcedureStatement createProcedureStatement = new MySQLCreateProcedureStatement();
        Optional<RoutineBodySegment> routineBodySegment = CreateProcedureStatementHandler.getRoutineBodySegment(createProcedureStatement);
        assertFalse(routineBodySegment.isPresent());
    }
}
