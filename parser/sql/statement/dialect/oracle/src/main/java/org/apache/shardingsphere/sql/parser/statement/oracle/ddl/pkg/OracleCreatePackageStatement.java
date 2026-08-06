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

package org.apache.shardingsphere.sql.parser.statement.oracle.ddl.pkg;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Create package statement for Oracle.
 */
@Getter
public final class OracleCreatePackageStatement extends DDLStatement {
    
    private final PackageSegment packageName;
    
    private final boolean body;
    
    private final boolean replace;
    
    private final boolean ifNotExists;
    
    private final Optional<Edition> edition;
    
    private final Optional<Authorization> authorization;
    
    private final Optional<RoutineBodySegment> initialization;
    
    private final List<SQLStatementSegment> sqlStatements = new ArrayList<>();
    
    private final List<ProcedureCallNameSegment> procedureCallNames = new ArrayList<>();
    
    private final List<ExpressionSegment> dynamicSqlStatementExpressions = new ArrayList<>();
    
    /**
     * Construct an Oracle create package statement.
     *
     * @param databaseType database type
     * @param packageName package name
     * @param body whether this statement creates a package body
     * @param replace whether existing package should be replaced
     * @param ifNotExists whether creation should be skipped when the package exists
     * @param edition edition mode, or {@code null} when unspecified
     * @param authorization authorization mode, or {@code null} when unspecified
     * @param initialization package initialization body, or {@code null} when absent
     */
    public OracleCreatePackageStatement(final DatabaseType databaseType, final PackageSegment packageName, final boolean body, final boolean replace,
                                        final boolean ifNotExists, final Edition edition, final Authorization authorization, final RoutineBodySegment initialization) {
        super(databaseType);
        this.packageName = packageName;
        this.body = body;
        this.replace = replace;
        this.ifNotExists = ifNotExists;
        this.edition = Optional.ofNullable(edition);
        this.authorization = Optional.ofNullable(authorization);
        this.initialization = Optional.ofNullable(initialization);
    }
    
    /**
     * Oracle package edition mode.
     */
    public enum Edition {
        
        EDITIONABLE,
        
        NONEDITIONABLE
    }
    
    /**
     * Oracle package authorization mode.
     */
    public enum Authorization {
        
        CURRENT_USER,
        
        DEFINER
    }
}
