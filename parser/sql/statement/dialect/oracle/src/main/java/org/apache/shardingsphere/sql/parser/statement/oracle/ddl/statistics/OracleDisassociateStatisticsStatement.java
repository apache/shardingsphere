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

package org.apache.shardingsphere.sql.parser.statement.oracle.ddl.statistics;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexTypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.type.TypeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Disassociate statistics statement for Oracle.
 */
@Getter
@Setter
public final class OracleDisassociateStatisticsStatement extends DDLStatement {
    
    private List<IndexSegment> indexes = new LinkedList<>();
    
    private List<SimpleTableSegment> tables = new LinkedList<>();
    
    private List<ColumnSegment> columns = new LinkedList<>();
    
    private List<FunctionSegment> functions = new LinkedList<>();
    
    private List<PackageSegment> packages = new LinkedList<>();
    
    private List<TypeSegment> types = new LinkedList<>();
    
    private List<IndexTypeSegment> indexTypes = new LinkedList<>();
    
    public OracleDisassociateStatisticsStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
}
