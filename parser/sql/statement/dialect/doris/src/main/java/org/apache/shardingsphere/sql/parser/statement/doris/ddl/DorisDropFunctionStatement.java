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

package org.apache.shardingsphere.sql.parser.statement.doris.ddl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Drop function statement for Doris.
 */
@Getter
@Setter
public final class DorisDropFunctionStatement extends DDLStatement {
    
    private FunctionNameSegment functionName;
    
    private boolean global;
    
    private final List<DataTypeSegment> parameterDataTypes = new LinkedList<>();
    
    public DorisDropFunctionStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get function name segment.
     *
     * @return function name segment
     */
    public Optional<FunctionNameSegment> getFunctionName() {
        return Optional.ofNullable(functionName);
    }
}
