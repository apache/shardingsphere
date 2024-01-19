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

package org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.CursorForLoopStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.ProcedureBodyEndNameSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.plsql.SQLStatementSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Oracle create procedure statement.
 */
@RequiredArgsConstructor
@Getter
public final class OracleCreateProcedureStatement extends CreateProcedureStatement implements OracleStatement {
    
    private final List<SQLStatementSegment> sqlStatements;
    
    private final List<ProcedureCallNameSegment> procedureCallNames;
    
    private final List<ProcedureBodyEndNameSegment> procedureBodyEndNameSegments;
    
    private final List<ExpressionSegment> dynamicSqlStatementExpressions;
    
    private final List<CursorForLoopStatementSegment> cursorForLoopStatements = new ArrayList<>();
}
