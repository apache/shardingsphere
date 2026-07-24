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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;

import java.util.List;
import java.util.Optional;

/**
 * Call statement.
 */
@Getter
public final class CallStatement extends DMLStatement {
    
    private final String procedureName;
    
    private final FunctionNameSegment procedureNameSegment;
    
    private final List<ExpressionSegment> parameters;
    
    public CallStatement(final DatabaseType databaseType, final String procedureName, final List<ExpressionSegment> parameters) {
        this(databaseType, procedureName, null, parameters);
    }
    
    public CallStatement(final DatabaseType databaseType, final String procedureName, final FunctionNameSegment procedureNameSegment, final List<ExpressionSegment> parameters) {
        super(databaseType);
        this.procedureName = procedureName;
        this.procedureNameSegment = procedureNameSegment;
        this.parameters = parameters;
    }
    
    /**
     * Get procedure name segment.
     *
     * @return procedure name segment
     */
    public Optional<FunctionNameSegment> getProcedureNameSegment() {
        return Optional.ofNullable(procedureNameSegment);
    }
}
