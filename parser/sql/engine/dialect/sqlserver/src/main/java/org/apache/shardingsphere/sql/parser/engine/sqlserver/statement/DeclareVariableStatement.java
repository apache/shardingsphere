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

package org.apache.shardingsphere.sql.parser.engine.sqlserver.statement;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.Collection;

/**
 * Declare variable statement for SQLServer.
 */
@Getter
public final class DeclareVariableStatement extends DDLStatement {
    
    private final VariableSegment variableName;
    
    private final boolean tableVariable;
    
    private final Collection<ColumnDefinitionSegment> columnDefinitions;
    
    private final DataTypeSegment dataType;
    
    public DeclareVariableStatement(final DatabaseType databaseType, final VariableSegment variableName, final boolean tableVariable,
                                    final Collection<ColumnDefinitionSegment> columnDefinitions) {
        this(databaseType, variableName, tableVariable, columnDefinitions, null);
    }
    
    public DeclareVariableStatement(final DatabaseType databaseType, final VariableSegment variableName, final boolean tableVariable,
                                    final Collection<ColumnDefinitionSegment> columnDefinitions, final DataTypeSegment dataType) {
        super(databaseType);
        this.variableName = variableName;
        this.tableVariable = tableVariable;
        this.columnDefinitions = columnDefinitions;
        this.dataType = dataType;
    }
}
