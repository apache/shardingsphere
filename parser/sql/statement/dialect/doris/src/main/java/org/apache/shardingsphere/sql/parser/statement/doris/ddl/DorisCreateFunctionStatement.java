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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Create function statement for Doris.
 */
@Getter
@Setter
public final class DorisCreateFunctionStatement extends CreateFunctionStatement {
    
    private boolean global;
    
    private FunctionType functionType;
    
    private final List<DataTypeSegment> parameterDataTypes = new LinkedList<>();
    
    private final Map<IdentifierValue, DataTypeSegment> namedParameters = new LinkedHashMap<>();
    
    private DataTypeSegment returnType;
    
    private DataTypeSegment intermediateType;
    
    private final List<IdentifierValue> withParameters = new LinkedList<>();
    
    private ExpressionSegment aliasExpression;
    
    private final Map<String, String> properties = new LinkedHashMap<>();
    
    public DorisCreateFunctionStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Function type enumeration for Doris.
     */
    public enum FunctionType {
        AGGREGATE, TABLES, ALIAS
    }
}
