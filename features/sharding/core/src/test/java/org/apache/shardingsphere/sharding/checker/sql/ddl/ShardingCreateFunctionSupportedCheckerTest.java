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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.exception.syntax.UnsupportedShardingOperationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingCreateFunctionSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule rule;
    
    @Test
    void assertCheckCreateFunction() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("bar_tbl")));
        selectStatement.setFrom(fromTable);
        CreateTableStatement createTableStatement = mock(CreateTableStatement.class);
        when(createTableStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(createTableStatement);
        ValidStatementSegment selectValidStatementSegment = new ValidStatementSegment(0, 0);
        selectValidStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        routineBody.getValidStatements().add(selectValidStatementSegment);
        CreateFunctionStatement sqlStatement = mock(CreateFunctionStatement.class);
        when(sqlStatement.getRoutineBody()).thenReturn(Optional.of(routineBody));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableSQLStatementAttribute(fromTable)));
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("bar_tbl")).thenReturn(true);
        assertDoesNotThrow(() -> new ShardingCreateFunctionSupportedChecker().check(rule, database, schema, sqlStatementContext));
    }
    
    @Test
    void assertCheckCreateFunctionWithShardingTable() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        selectStatement.setFrom(fromTable);
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        CreateFunctionStatement sqlStatement = mock(CreateFunctionStatement.class);
        when(sqlStatement.getRoutineBody()).thenReturn(Optional.of(routineBody));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableSQLStatementAttribute(fromTable)));
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(rule.isShardingTable("foo_tbl")).thenReturn(true);
        assertThrows(UnsupportedShardingOperationException.class, () -> new ShardingCreateFunctionSupportedChecker().check(rule, database, mock(), sqlStatementContext));
    }
    
    @Test
    void assertCheckCreateFunctionWithNoSuchTable() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        selectStatement.setFrom(fromTable);
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(selectStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        CreateFunctionStatement sqlStatement = mock(CreateFunctionStatement.class);
        when(sqlStatement.getRoutineBody()).thenReturn(Optional.of(routineBody));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableSQLStatementAttribute(fromTable)));
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        assertThrows(NoSuchTableException.class, () -> new ShardingCreateFunctionSupportedChecker().check(rule, database, mock(), sqlStatementContext));
    }
    
    @Test
    void assertCheckCreateFunctionWithTableExists() {
        CreateTableStatement createTableStatement = new CreateTableStatement(databaseType);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl")));
        createTableStatement.setTable(table);
        ValidStatementSegment validStatementSegment = new ValidStatementSegment(0, 0);
        validStatementSegment.setSqlStatement(createTableStatement);
        RoutineBodySegment routineBody = new RoutineBodySegment(0, 0);
        routineBody.getValidStatements().add(validStatementSegment);
        CreateFunctionStatement sqlStatement = mock(CreateFunctionStatement.class);
        when(sqlStatement.getRoutineBody()).thenReturn(Optional.of(routineBody));
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes(new TableSQLStatementAttribute(table)));
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        assertThrows(TableExistsException.class, () -> new ShardingCreateFunctionSupportedChecker().check(rule, database, schema, sqlStatementContext));
    }
}
