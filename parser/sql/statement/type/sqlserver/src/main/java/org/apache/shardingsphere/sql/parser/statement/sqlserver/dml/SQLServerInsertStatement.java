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

package org.apache.shardingsphere.sql.parser.statement.sqlserver.dml;

import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.exec.ExecSegment;
import org.apache.shardingsphere.sql.parser.statement.sqlserver.SQLServerStatement;

import java.util.Optional;

/**
 * SQLServer insert statement.
 */
@Setter
public final class SQLServerInsertStatement extends InsertStatement implements SQLServerStatement {
    
    private WithSegment withSegment;
    
    private OutputSegment outputSegment;
    
    private ExecSegment execSegment;
    
    private WithTableHintSegment withTableHintSegment;
    
    private FunctionSegment rowSetFunctionSegment;
    
    @Override
    public Optional<WithSegment> getWithSegment() {
        return Optional.ofNullable(withSegment);
    }
    
    @Override
    public Optional<OutputSegment> getOutputSegment() {
        return Optional.ofNullable(outputSegment);
    }
    
    @Override
    public Optional<ExecSegment> getExecSegment() {
        return Optional.ofNullable(execSegment);
    }
    
    @Override
    public Optional<WithTableHintSegment> getWithTableHintSegment() {
        return Optional.ofNullable(withTableHintSegment);
    }
    
    @Override
    public Optional<FunctionSegment> getRowSetFunctionSegment() {
        return Optional.ofNullable(rowSetFunctionSegment);
    }
}
