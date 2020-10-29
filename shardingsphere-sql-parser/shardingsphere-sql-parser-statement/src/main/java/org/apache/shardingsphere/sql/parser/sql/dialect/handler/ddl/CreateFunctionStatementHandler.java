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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateFunctionStatement;

import java.util.Optional;

/**
 * CreateFunctionStatement handler for different dialect SQLStatements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateFunctionStatementHandler implements SQLStatementHandler {
    
    /**
     * Get RoutineBodySegment.
     *
     * @param createFunctionStatement CreateFunctionStatement
     * @return RoutineBodySegment
     */
    public static Optional<RoutineBodySegment> getRoutineBodySegment(final CreateFunctionStatement createFunctionStatement) {
        if (createFunctionStatement instanceof MySQLStatement) {
            return ((MySQLCreateFunctionStatement) createFunctionStatement).getRoutineBody();
        }
        return Optional.empty();
    }
}
