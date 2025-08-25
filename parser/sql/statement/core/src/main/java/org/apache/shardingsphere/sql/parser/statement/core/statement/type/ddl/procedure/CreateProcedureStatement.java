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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureBodyEndNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Create procedure statement.
 */
@Getter
@Setter
public class CreateProcedureStatement extends DDLStatement {
    
    private FunctionNameSegment procedureName;
    
    private RoutineBodySegment routineBody;
    
    private final List<SQLStatementSegment> sqlStatements = new ArrayList<>();
    
    private final List<ProcedureCallNameSegment> procedureCallNames = new ArrayList<>();
    
    private final List<ProcedureBodyEndNameSegment> procedureBodyEndNameSegments = new ArrayList<>();
    
    private final List<ExpressionSegment> dynamicSqlStatementExpressions = new ArrayList<>();
    
    public CreateProcedureStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get procedure name segment.
     *
     * @return procedure name segment
     */
    public Optional<FunctionNameSegment> getProcedureName() {
        return Optional.ofNullable(procedureName);
    }
    
    /**
     * Get routine body.
     *
     * @return routine body
     */
    public Optional<RoutineBodySegment> getRoutineBody() {
        return Optional.ofNullable(routineBody);
    }
}
