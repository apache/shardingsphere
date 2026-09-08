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

package org.apache.shardingsphere.infra.binder.context.statement.type.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.packages.PackageSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.FunctionNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.ProcedureCallNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.procedure.SQLStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.pkg.CreatePackageStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.trigger.CreateTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CreateDatabaseObjectStatementContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertNewInstanceWithTriggerTable() {
        CreateTriggerStatement statement = new CreateTriggerStatement(databaseType);
        statement.setTable(new SimpleTableSegment(new TableNameSegment(28, 36, new IdentifierValue("t_account"))));
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(statement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getSqlStatement(), is(statement));
        assertThat(actual.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getValue(), is("t_account"));
    }
    
    @Test
    void assertNewInstanceWithProcedure() {
        CreateProcedureStatement sqlStatement = mock(CreateProcedureStatement.class);
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(sqlStatement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
    }
    
    @Test
    void assertNewInstanceWithProcedureInternalSQLTablesContext() {
        CreateProcedureStatement sqlStatement = new CreateProcedureStatement(databaseType);
        sqlStatement.getSqlStatements().add(new SQLStatementSegment(0, 0, new TableAwareSQLStatement("t_encrypt")));
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(sqlStatement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test
    void assertNewInstanceWithProcedureRoutineBodyTablesContext() {
        CreateProcedureStatement sqlStatement = new CreateProcedureStatement(databaseType);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 50);
        ValidStatementSegment validStatement = new ValidStatementSegment(10, 45);
        validStatement.setSqlStatement(InsertStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(22, 30, new IdentifierValue("t_encrypt")))).build());
        routineBody.getValidStatements().add(validStatement);
        sqlStatement.setRoutineBody(routineBody);
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(sqlStatement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test
    void assertNewInstanceWithPackageInternalSQLTablesContext() {
        PackageSQLStatement sqlStatement = new PackageSQLStatement(databaseType);
        sqlStatement.getSqlStatements().add(new SQLStatementSegment(0, 0, new TableAwareSQLStatement("t_encrypt")));
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(sqlStatement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getSqlStatement(), is(sqlStatement));
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("t_encrypt")));
    }
    
    @Test
    void assertNewInstanceWithPackageTypeAttributeTablesContext() {
        PackageSQLStatement sqlStatement = new PackageSQLStatement(databaseType, new TableSQLStatementAttribute(
                new SimpleTableSegment(new TableNameSegment(40, 45, new IdentifierValue("t_type")))));
        sqlStatement.getSqlStatements().add(new SQLStatementSegment(0, 0, new TableAwareSQLStatement("t_encrypt")));
        CreateDatabaseObjectStatementContext actual = new CreateDatabaseObjectStatementContext(sqlStatement, "foo_db", mock(ShardingSphereMetaData.class));
        assertThat(actual.getTablesContext().getSimpleTables().size(), is(2));
        assertTrue(actual.getTablesContext().getTableNames().contains("t_type"));
        assertTrue(actual.getTablesContext().getTableNames().contains("t_encrypt"));
    }
    
    private static final class TableAwareSQLStatement extends SQLStatement {
        
        private final SQLStatementAttributes attributes;
        
        private TableAwareSQLStatement(final String tableName) {
            super(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
            attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(new SimpleTableSegment(new TableNameSegment(0, tableName.length(), new IdentifierValue(tableName)))));
        }
        
        @Override
        public SQLStatementAttributes getAttributes() {
            return attributes;
        }
    }
    
    private static final class PackageSQLStatement extends SQLStatement implements CreatePackageStatement {
        
        private final Collection<SQLStatementSegment> sqlStatements = new LinkedList<>();
        
        private final SQLStatementAttributes attributes;
        
        private PackageSQLStatement(final DatabaseType databaseType) {
            super(databaseType);
            attributes = new SQLStatementAttributes();
        }
        
        private PackageSQLStatement(final DatabaseType databaseType, final TableSQLStatementAttribute tableSQLStatementAttribute) {
            super(databaseType);
            attributes = new SQLStatementAttributes(tableSQLStatementAttribute);
        }
        
        @Override
        public SQLStatementAttributes getAttributes() {
            return attributes;
        }
        
        @Override
        public PackageSegment getPackageName() {
            return new PackageSegment(0, 6, new IdentifierValue("foo_pkg"));
        }
        
        @Override
        public boolean isBody() {
            return true;
        }
        
        @Override
        public Optional<PackageSegment> getPackageEndName() {
            return Optional.empty();
        }
        
        @Override
        public Collection<SQLStatementSegment> getSqlStatements() {
            return sqlStatements;
        }
        
        @Override
        public Collection<ColumnSegment> getColumns() {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<ProcedureCallNameSegment> getProcedureCallNames() {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<FunctionNameSegment> getPackageRoutineNames() {
            return Collections.emptyList();
        }
        
        @Override
        public Collection<ExpressionSegment> getDynamicSqlStatementExpressions() {
            return Collections.emptyList();
        }
    }
}
