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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStatementContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType sqlServerDatabaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    @Mock
    private WhereSegment whereSegment;
    
    @Mock
    private ColumnSegment columnSegment;
    
    @Test
    void assertNewInstance() {
        OwnerSegment ownerSegment = new OwnerSegment(0, 0, new IdentifierValue("tbl_2"));
        ownerSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        when(columnSegment.getOwner()).thenReturn(Optional.of(ownerSegment));
        BinaryOperationExpression expression = new BinaryOperationExpression(0, 0, columnSegment, null, null, null);
        when(whereSegment.getExpr()).thenReturn(expression);
        TableNameSegment tableNameSegment1 = new TableNameSegment(0, 0, new IdentifierValue("tbl_1"));
        tableNameSegment1.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        TableNameSegment tableNameSegment2 = new TableNameSegment(0, 0, new IdentifierValue("tbl_2"));
        tableNameSegment2.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        UpdateStatement updateStatement = createUpdateStatement(tableNameSegment1, tableNameSegment2);
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("tbl_1", "tbl_2"))));
        assertThat(actual.getWhereSegments(), is(Collections.singletonList(whereSegment)));
        assertThat(actual.getTablesContext().getSimpleTables().stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList()),
                is(Arrays.asList("tbl_1", "tbl_2", "tbl_2")));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerUpdateAliasTargetExcludesAlias() {
        SimpleTableSegment scrapReason = new SimpleTableSegment(new TableNameSegment(50, 65, new IdentifierValue("ScrapReason")));
        scrapReason.setAlias(new AliasSegment(67, 68, new IdentifierValue("sr")));
        SimpleTableSegment workOrder = new SimpleTableSegment(new TableNameSegment(75, 83, new IdentifierValue("WorkOrder")));
        workOrder.setAlias(new AliasSegment(85, 86, new IdentifierValue("wo")));
        JoinTableSegment joinTable = new JoinTableSegment();
        joinTable.setLeft(scrapReason);
        joinTable.setRight(workOrder);
        SimpleTableSegment aliasTarget = new SimpleTableSegment(new TableNameSegment(7, 8, new IdentifierValue("sr")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType).table(aliasTarget).from(joinTable).setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .targetTableIsFromAlias(true).build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("ScrapReason", "WorkOrder"))));
        assertFalse(actual.getTablesContext().getTableNames().contains("sr"));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerUpdateTableVariableTargetExcludesVariableTable() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar"))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@MyTableVar"));
        assertThat(((SimpleTableSegment) actual.getSqlStatement().getTable()).getTableName().getIdentifier().getValue(), is("@MyTableVar"));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerUpdateTableVariableTargetRepeatedInFromExcludesVariableTable() {
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar"))))
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar"))))
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertTrue(actual.getTablesContext().getTableNames().isEmpty());
        assertFalse(actual.getTablesContext().getTableNames().contains("@MyTableVar"));
        assertThat(((SimpleTableSegment) actual.getSqlStatement().getTable()).getTableName().getIdentifier().getValue(), is("@MyTableVar"));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerBracketDelimitedAtSignTargetIncludesPhysicalTable() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerSchemaQualifiedBracketDelimitedAtSignTargetIncludesPhysicalTable() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS)));
        targetTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
        assertTrue(((SimpleTableSegment) actual.getSqlStatement().getTable()).getOwner().isPresent());
        assertThat(((SimpleTableSegment) actual.getSqlStatement().getTable()).getOwner().get().getIdentifier().getValue(), is("dbo"));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerSchemaQualifiedQuoteDelimitedAtSignTargetIncludesPhysicalTable() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE)));
        targetTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
        assertTrue(((SimpleTableSegment) actual.getSqlStatement().getTable()).getOwner().isPresent());
        assertThat(((SimpleTableSegment) actual.getSqlStatement().getTable()).getOwner().get().getIdentifier().getValue(), is("dbo"));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerBracketDelimitedAtSignTargetAndFromIncludesPhysicalTable() {
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS))))
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS))))
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerQuoteDelimitedAtSignTargetAndFromIncludesPhysicalTable() {
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE))))
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE))))
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerQuoteDelimitedAtSignTargetIncludesPhysicalTable() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerAliasedTableVariableTargetExcludesVariableTable() {
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(tableVariable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertTrue(actual.getTablesContext().getTableNames().isEmpty());
        assertFalse(actual.getTablesContext().getTableNames().contains("@MyTableVar"));
        assertThat(((SimpleTableSegment) actual.getSqlStatement().getTable()).getTableName().getIdentifier().getValue(), is("target"));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerAliasedTableVariableAndJoinIncludesPhysicalTable() {
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment joinTable = new JoinTableSegment();
        joinTable.setLeft(tableVariable);
        joinTable.setRight(employee);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(joinTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@MyTableVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("target"));
    }
    
    @Test
    void assertGetTableNamesWithSQLServerPhysicalTableBeforeAliasedTableVariableJoinIncludesPhysicalTable() {
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment joinTable = new JoinTableSegment();
        joinTable.setLeft(employee);
        joinTable.setRight(tableVariable);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(joinTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@MyTableVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("target"));
    }
    
    @Test
    void assertGetTableNamesWithDirectVariableTargetAndExtraVariableSourceExcludesAllVariables() {
        SimpleTableSegment targetVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar")));
        SimpleTableSegment sourceVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@SourceVar")));
        sourceVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("src")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment rightJoin = new JoinTableSegment();
        rightJoin.setLeft(sourceVar);
        rightJoin.setRight(employee);
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar"))));
        fromJoin.setRight(rightJoin);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetVar)
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@TargetVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("@SourceVar"));
    }
    
    @Test
    void assertGetTableNamesWithAliasedVariableTargetAndExtraVariableSourceExcludesAllVariables() {
        SimpleTableSegment targetVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar")));
        targetVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment sourceVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@SourceVar")));
        sourceVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("src")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment rightJoin = new JoinTableSegment();
        rightJoin.setLeft(sourceVar);
        rightJoin.setRight(employee);
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(targetVar);
        fromJoin.setRight(rightJoin);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@TargetVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("@SourceVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("target"));
    }
    
    @Test
    void assertGetTableNamesWithDirectVariableTargetAndThreeVariableSourcesExcludesAllVariables() {
        SimpleTableSegment targetVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar")));
        SimpleTableSegment sourceVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@SourceVar")));
        sourceVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("src")));
        SimpleTableSegment otherVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@OtherVar")));
        otherVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("oth")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment sourceAndOther = new JoinTableSegment();
        sourceAndOther.setLeft(sourceVar);
        sourceAndOther.setRight(otherVar);
        JoinTableSegment variablesAndEmployee = new JoinTableSegment();
        variablesAndEmployee.setLeft(sourceAndOther);
        variablesAndEmployee.setRight(employee);
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar"))));
        fromJoin.setRight(variablesAndEmployee);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetVar)
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@TargetVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("@SourceVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("@OtherVar"));
    }
    
    @Test
    void assertGetTableNamesWithAliasedVariableTargetAndThreeVariableSourcesExcludesAllVariables() {
        SimpleTableSegment targetVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar")));
        targetVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment sourceVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@SourceVar")));
        sourceVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("src")));
        SimpleTableSegment otherVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@OtherVar")));
        otherVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("oth")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment sourceAndOther = new JoinTableSegment();
        sourceAndOther.setLeft(sourceVar);
        sourceAndOther.setRight(otherVar);
        JoinTableSegment variablesAndEmployee = new JoinTableSegment();
        variablesAndEmployee.setLeft(sourceAndOther);
        variablesAndEmployee.setRight(employee);
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(targetVar);
        fromJoin.setRight(variablesAndEmployee);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@TargetVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("@SourceVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("@OtherVar"));
        assertFalse(actual.getTablesContext().getTableNames().contains("target"));
    }
    
    @Test
    void assertGetTableNamesWithVariableJoiningBracketQuotedAtTableKeepsQuotedPhysicalTable() {
        SimpleTableSegment targetVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar")));
        SimpleTableSegment bracketQuotedTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS)));
        bracketQuotedTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("q")));
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar"))));
        fromJoin.setRight(bracketQuotedTable);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetVar)
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@TargetVar"));
        assertThat(actual.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
    }
    
    @Test
    void assertGetTableNamesWithVariableJoiningDoubleQuotedAtTableKeepsQuotedPhysicalTable() {
        SimpleTableSegment targetVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar")));
        SimpleTableSegment doubleQuotedTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE)));
        doubleQuotedTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("q")));
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@TargetVar"))));
        fromJoin.setRight(doubleQuotedTable);
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetVar)
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
        assertFalse(actual.getTablesContext().getTableNames().contains("@TargetVar"));
        assertThat(actual.getTablesContext().getSimpleTables().iterator().next().getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetTableNamesWithPostgreSQLUpdateFromClauseIncludesTargetTable() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(7, 18, new IdentifierValue("ScrapReason")));
        targetTable.setAlias(new AliasSegment(20, 21, new IdentifierValue("sr")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(50, 58, new IdentifierValue("WorkOrder")));
        fromTable.setAlias(new AliasSegment(60, 61, new IdentifierValue("wo")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType).table(targetTable).from(fromTable).setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
        UpdateStatementContext actual = new UpdateStatementContext(updateStatement);
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("ScrapReason", "WorkOrder"))));
        assertFalse(actual.getTablesContext().getTableNames().contains("sr"));
    }
    
    private UpdateStatement createUpdateStatement(final TableNameSegment tableNameSegment1, final TableNameSegment tableNameSegment2) {
        SimpleTableSegment table1 = new SimpleTableSegment(tableNameSegment1);
        SimpleTableSegment table2 = new SimpleTableSegment(tableNameSegment2);
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(table1);
        joinTableSegment.setRight(table2);
        return UpdateStatement.builder()
                .databaseType(databaseType)
                .table(joinTableSegment)
                .where(whereSegment)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()))
                .build();
    }
}
