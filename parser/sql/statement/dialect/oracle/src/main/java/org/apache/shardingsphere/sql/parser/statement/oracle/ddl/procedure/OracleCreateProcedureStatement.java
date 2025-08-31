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

package org.apache.shardingsphere.sql.parser.statement.oracle.ddl.procedure;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.CursorForLoopStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Create procedure statement for Oracle.
 */
@Getter
public final class OracleCreateProcedureStatement extends CreateProcedureStatement {
    
    private final List<CursorForLoopStatementSegment> cursorForLoopStatements = new ArrayList<>();
    
    public OracleCreateProcedureStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
}
