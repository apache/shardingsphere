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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.CursorForLoopStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ProcedureCallNamesSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.pkg.CreatePackageStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Create package statement for Oracle.
 */
@Getter
public final class OracleCreatePackageStatement extends DDLStatement implements CreatePackageStatement {
    
    private final PackageSegment packageName;
    
    @Getter(AccessLevel.NONE)
    private final PackageSegment packageEndName;
    
    private final boolean body;
    
    private final boolean replace;
    
    private final boolean ifNotExists;
    
    @Getter(AccessLevel.NONE)
    private final Edition edition;
    
    @Getter(AccessLevel.NONE)
    private final Authorization authorization;
    
    @Getter(AccessLevel.NONE)
    private final RoutineBodySegment initialization;
    
    private final List<SQLStatementSegment> sqlStatements = new ArrayList<>();
    
    private final List<ProcedureCallNameSegment> procedureCallNames = new ArrayList<>();
    
    private final List<FunctionNameSegment> packageRoutineNames = new ArrayList<>();
    
    private final List<ExpressionSegment> dynamicSqlStatementExpressions = new ArrayList<>();
    
    private final List<CursorForLoopStatementSegment> cursorForLoopStatements = new ArrayList<>();
    
    private final Collection<ColumnSegment> columns = new ArrayList<>();
    
    private final SQLStatementAttributes attributes;
    
    /**
     * Construct an Oracle create package statement.
     *
     * @param databaseType database type
     * @param packageName package name
     * @param packageEndName package end name, or {@code null} when absent
     * @param body whether this statement creates a package body
     * @param replace whether existing package should be replaced
     * @param ifNotExists whether creation should be skipped when the package exists
     * @param edition edition mode, or {@code null} when unspecified
     * @param authorization authorization mode, or {@code null} when unspecified
     * @param initialization package initialization body, or {@code null} when absent
     * @param tables tables referenced by package declaration types
     */
    public OracleCreatePackageStatement(final DatabaseType databaseType, final PackageSegment packageName, final PackageSegment packageEndName,
                                        final boolean body, final boolean replace, final boolean ifNotExists, final Edition edition, final Authorization authorization,
                                        final RoutineBodySegment initialization, final Collection<SimpleTableSegment> tables) {
        super(databaseType);
        this.packageName = packageName;
        this.packageEndName = packageEndName;
        this.body = body;
        this.replace = replace;
        this.ifNotExists = ifNotExists;
        this.edition = edition;
        this.authorization = authorization;
        this.initialization = initialization;
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(tables), new ProcedureCallNamesSQLStatementAttribute(procedureCallNames));
    }
    
    @Override
    public Optional<PackageSegment> getPackageEndName() {
        return Optional.ofNullable(packageEndName);
    }
    
    /**
     * Get package edition mode.
     *
     * @return package edition mode
     */
    public Optional<Edition> getEdition() {
        return Optional.ofNullable(edition);
    }
    
    /**
     * Get package authorization mode.
     *
     * @return package authorization mode
     */
    public Optional<Authorization> getAuthorization() {
        return Optional.ofNullable(authorization);
    }
    
    /**
     * Get package initialization body.
     *
     * @return package initialization body
     */
    public Optional<RoutineBodySegment> getInitialization() {
        return Optional.ofNullable(initialization);
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
