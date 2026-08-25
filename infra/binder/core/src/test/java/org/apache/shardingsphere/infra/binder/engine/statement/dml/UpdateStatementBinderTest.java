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

package org.apache.shardingsphere.infra.binder.engine.statement.dml;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.context.type.SimpleTableSegmentBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpdateStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType sqlServerDatabaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    @Test
    void assertBind() {
        SimpleTableSegment simpleTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType)
                .table(simpleTableSegment)
                .where(new WhereSegment(0, 0, new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("status")),
                        new LiteralExpressionSegment(0, 0, 0), "=", "status = 1")))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement, new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), updateStatement));
        assertThat(actual, not(updateStatement));
        assertThat(actual.getTable(), not(updateStatement.getTable()));
        assertThat(actual.getTable(), isA(SimpleTableSegment.class));
        assertTrue(actual.getWhere().isPresent());
        assertThat(actual.getWhere().get(), not(updateStatement.getWhere()));
        assertThat(actual.getWhere().get(), isA(WhereSegment.class));
        assertTrue(updateStatement.getWhere().isPresent());
        assertThat(actual.getWhere().get().getExpr(), not(updateStatement.getWhere().get().getExpr()));
        assertThat(actual.getWhere().get().getExpr(), isA(BinaryOperationExpression.class));
        assertThat(((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft(), isA(ColumnSegment.class));
        assertThat(((ColumnSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft()).getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    @Test
    void assertBindOrderByColumnWithCte() {
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType)
                .with(createWithSegment())
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))))
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("status"))), new LiteralExpressionSegment(0, 0, 1)))))
                .orderBy(new OrderBySegment(0, 0, Collections.singletonList(new ColumnOrderByItemSegment(
                        new ColumnSegment(0, 0, new IdentifierValue("user_id")), OrderDirection.ASC, null))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement, new SQLStatementBinderContext(createMetaData(),
                "foo_db", new HintValueContext(), updateStatement));
        ColumnOrderByItemSegment actualOrderByItem = (ColumnOrderByItemSegment) actual.getOrderBy().get().getOrderByItems().iterator().next();
        assertThat(actualOrderByItem.getColumn().getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
    }
    
    @Test
    void assertBindUpdateTargetTableAlias() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment), new LiteralExpressionSegment(0, 0, 1)))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualColumn = actual.getAssignment().get().getAssignments().iterator().next()
                .getColumns().iterator().next();
        assertThat(((SimpleTableSegment) actual.getTable()).getTableName().getIdentifier().getValue(), is("t_order"));
        assertThat(((SimpleTableSegment) actual.getTable()).getAliasName().get(), is("o"));
        assertThat(actualColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertTrue(actual.isTargetTableIsFromAlias());
    }
    
    @Test
    void assertBindUpdateTargetTableNameWithFromAlias() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(columnSegment), new LiteralExpressionSegment(0, 0, 1)))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(((SimpleTableSegment) actual.getTable()).getTableName().getIdentifier().getValue(), is("t_order"));
        assertThat(actualColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("t_order"));
        assertTrue(actual.isTargetTableIsFromAlias());
    }
    
    @Test
    void assertBindSchemaQualifiedUpdateTargetTableAlias() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_db")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(databaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("status"))), new LiteralExpressionSegment(0, 0, 1)))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createMetaData(), "foo_db", new HintValueContext(), updateStatement));
        assertThat(((SimpleTableSegment) actual.getTable()).getTableName().getIdentifier().getValue(), is("t_order"));
        assertThat(((SimpleTableSegment) actual.getTable()).getAliasName().get(), is("o"));
        assertTrue(((SimpleTableSegment) actual.getTable()).getOwner().isPresent());
        assertThat(((SimpleTableSegment) actual.getTable()).getOwner().get().getIdentifier().getValue(), is("foo_db"));
        assertTrue(actual.isTargetTableIsFromAlias());
    }
    
    @Test
    void assertBindUpdateTableVariableTarget() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("NewVacationHours"));
        ColumnSegment fromColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationHours"));
        fromColumn.setOwner(new OwnerSegment(0, 0, new IdentifierValue("e")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar"))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn),
                        new BinaryOperationExpression(0, 0, fromColumn, new LiteralExpressionSegment(0, 0, 20), "+", "e.VacationHours + 20")))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        BinaryOperationExpression actualValue = (BinaryOperationExpression) actual.getAssignment().get().getAssignments().iterator().next().getValue();
        assertThat(((SimpleTableSegment) actual.getTable()).getTableName().getIdentifier().getValue(), is("@MyTableVar"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("NewVacationHours"));
        assertThat(((ColumnSegment) actualValue.getLeft()).getColumnBoundInfo().getOriginalTable().getValue(), is("Employee"));
        assertThat(((ColumnSegment) actualValue.getLeft()).getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationHours"));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(((SimpleTableSegment) updateStatementContext.getSqlStatement().getTable()).getTableName().getIdentifier().getValue(), is("@MyTableVar"));
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("Employee")));
        assertFalse(updateStatementContext.getTablesContext().getTableNames().contains("@MyTableVar"));
    }
    
    @Test
    void assertBindUpdateTableVariableTargetPreservesOwnershipWhenSetColumnCollidesWithPhysicalSource() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        ColumnSegment whereLeft = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        whereLeft.setOwner(new OwnerSegment(0, 0, new IdentifierValue("e")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar"))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "annual leave")))))
                .where(new WhereSegment(0, 0, new BinaryOperationExpression(
                        0, 0, whereLeft, new LiteralExpressionSegment(0, 0, "annual leave"), "=", "e.VacationNote = 'annual leave'")))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        ColumnSegment actualWhereColumn = (ColumnSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTableVar"));
        assertThat(actualWhereColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("Employee"));
        assertThat(actualWhereColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
    }
    
    @Test
    void assertBindUpdateTableVariableTargetPreservesOwnershipWhenSetColumnCollidesWithPhysicalSourceForParameter() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar"))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new ParameterMarkerExpressionSegment(0, 0, 0)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTableVar"));
    }
    
    @Test
    void assertBindPhysicalTargetPreservesOwnershipWhenFromContainsTableVariableWithCollidingSetColumn() {
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(employee);
        fromJoin.setRight(tableVariable);
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "x")))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("Employee"));
    }
    
    @Test
    void assertBindAliasedTableVariableTargetPreservesOwnershipWhenSetColumnCollidesWithPhysicalSource() {
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(tableVariable);
        fromJoin.setRight(employee);
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        ColumnSegment whereLeft = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        whereLeft.setOwner(new OwnerSegment(0, 0, new IdentifierValue("e")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "annual leave")))))
                .where(new WhereSegment(0, 0, new BinaryOperationExpression(
                        0, 0, whereLeft, new LiteralExpressionSegment(0, 0, "annual leave"), "=", "e.VacationNote = 'annual leave'")))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        ColumnSegment actualWhereColumn = (ColumnSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTableVar"));
        assertThat(actualWhereColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("Employee"));
        assertThat(actualWhereColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
    }
    
    @Test
    void assertBindAliasedTableVariableTargetPreservesOwnershipWhenSetColumnCollidesWithPhysicalSourceForParameter() {
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(tableVariable);
        fromJoin.setRight(employee);
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new ParameterMarkerExpressionSegment(0, 0, 0)))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTableVar"));
    }
    
    @Test
    void assertBindAliasedTableVariableTargetPreservesOwnershipAmongMultipleTableVariables() {
        SimpleTableSegment otherVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@OtherVar")));
        otherVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment rightJoin = new JoinTableSegment();
        rightJoin.setLeft(tableVariable);
        rightJoin.setRight(employee);
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(otherVar);
        fromJoin.setRight(rightJoin);
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        ColumnSegment whereLeft = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        whereLeft.setOwner(new OwnerSegment(0, 0, new IdentifierValue("e")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "annual leave")))))
                .where(new WhereSegment(0, 0, new BinaryOperationExpression(
                        0, 0, whereLeft, new LiteralExpressionSegment(0, 0, "annual leave"), "=", "e.VacationNote = 'annual leave'")))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(((SimpleTableSegment) actual.getTable()).getTableName().getIdentifier().getValue(), is("@MyTableVar"));
        assertThat(((SimpleTableSegment) actual.getTable()).getAliasName().get(), is("target"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTableVar"));
        ColumnSegment actualWhereColumn = (ColumnSegment) ((BinaryOperationExpression) actual.getWhere().get().getExpr()).getLeft();
        assertThat(actualWhereColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("Employee"));
        assertThat(actualWhereColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
    }
    
    @Test
    void assertBindAliasedTableVariableTargetPreservesOwnershipAmongMultipleTableVariablesForParameter() {
        SimpleTableSegment otherVar = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@OtherVar")));
        otherVar.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        SimpleTableSegment employee = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        employee.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        employee.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        JoinTableSegment rightJoin = new JoinTableSegment();
        rightJoin.setLeft(tableVariable);
        rightJoin.setRight(employee);
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(otherVar);
        fromJoin.setRight(rightJoin);
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("VacationNote"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new ParameterMarkerExpressionSegment(0, 0, 0)))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("VacationNote"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTableVar"));
    }
    
    @Test
    void assertBindBracketDelimitedAtSignTargetRetainsPhysicalTableValidation() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
    }
    
    @Test
    void assertBindQuoteDelimitedAtSignTargetRetainsPhysicalTableValidation() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
    }
    
    @Test
    void assertBindSchemaQualifiedBracketDelimitedAtSignTargetRetainsPhysicalTableValidation() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS)));
        targetTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
        assertTrue(boundTable.getOwner().isPresent());
        assertThat(boundTable.getOwner().get().getIdentifier().getValue(), is("dbo"));
        assertThat(boundTable.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("dbo"));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
    }
    
    @Test
    void assertBindSchemaQualifiedQuoteDelimitedAtSignTargetRetainsPhysicalTableValidation() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE)));
        targetTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("Employee")));
        fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("HumanResources")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("e")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
        assertTrue(boundTable.getOwner().isPresent());
        assertThat(boundTable.getOwner().get().getIdentifier().getValue(), is("dbo"));
        assertThat(boundTable.getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("dbo"));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("@MyTable", "Employee"))));
    }
    
    @Test
    void assertBindBracketDelimitedAtSignTargetAndFromRetainsPhysicalTableValidation() {
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        setColumn.setOwner(new OwnerSegment(0, 0, new IdentifierValue("src")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS)));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("src")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @Test
    void assertBindQuoteDelimitedAtSignTargetAndFromRetainsPhysicalTableValidation() {
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        setColumn.setOwner(new OwnerSegment(0, 0, new IdentifierValue("src")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE)));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("src")));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @Test
    void assertBindBracketDelimitedAtSignTargetWithUnquotedSameTextTableVariableSourceRetainsPhysicalTableValidation() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.PHYSICAL_TABLE));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @Test
    void assertBindQuoteDelimitedAtSignTargetWithUnquotedSameTextTableVariableSourceRetainsPhysicalTableValidation() {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.PHYSICAL_TABLE));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @Test
    void assertBindSchemaQualifiedBracketDelimitedAtSignTargetWithUnquotedSameTextTableVariableSourceRetainsPhysicalTableValidation() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS)));
        targetTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
        assertTrue(boundTable.getOwner().isPresent());
        assertThat(boundTable.getOwner().get().getIdentifier().getValue(), is("dbo"));
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        assertFalse(boundFrom.getOwner().isPresent());
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(QuoteCharacter.BRACKETS));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.PHYSICAL_TABLE));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @Test
    void assertBindSchemaQualifiedQuoteDelimitedAtSignTargetWithUnquotedSameTextTableVariableSourceRetainsPhysicalTableValidation() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", QuoteCharacter.QUOTE)));
        targetTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable")));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Status"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, 1)))))
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
        assertTrue(boundTable.getOwner().isPresent());
        assertThat(boundTable.getOwner().get().getIdentifier().getValue(), is("dbo"));
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        assertFalse(boundFrom.getOwner().isPresent());
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(QuoteCharacter.QUOTE));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Status"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.PHYSICAL_TABLE));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("delimitedAtSignPhysicalTargetWithAliasedTableVariableSourceArguments")
    void assertBindDelimitedAtSignPhysicalTargetWithAliasedTableVariableSourceRetainsPhysicalTableValidation(final String name, final QuoteCharacter quoteCharacter,
                                                                                                             final boolean schemaQualified) {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("x")));
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", quoteCharacter)));
        if (schemaQualified) {
            targetTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        }
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Remark"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "x")))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(quoteCharacter));
        assertThat(boundTable.getOwner().isPresent(), is(schemaQualified));
        if (schemaQualified) {
            assertThat(boundTable.getOwner().get().getIdentifier().getValue(), is("dbo"));
        }
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        assertThat(boundFrom.getAliasName().get(), is("x"));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(quoteCharacter));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Remark"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.PHYSICAL_TABLE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("tableVariableTargetWithAliasedDelimitedAtSignPhysicalSourceArguments")
    void assertBindTableVariableTargetWithAliasedDelimitedAtSignPhysicalSourceKeepsVariableTargetValidation(final String name, final QuoteCharacter quoteCharacter,
                                                                                                            final boolean schemaQualified, final String fromAlias) {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable", quoteCharacter)));
        if (schemaQualified) {
            fromTable.setOwner(new OwnerSegment(0, 0, new IdentifierValue("dbo")));
        }
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue(fromAlias)));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Remark"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable"))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "x")))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        assertFalse(boundTable.getOwner().isPresent());
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(quoteCharacter));
        assertThat(boundFrom.getOwner().isPresent(), is(schemaQualified));
        if (schemaQualified) {
            assertThat(boundFrom.getOwner().get().getIdentifier().getValue(), is("dbo"));
        }
        assertThat(boundFrom.getAliasName().get(), is(fromAlias));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(QuoteCharacter.NONE));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Remark"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
    }
    
    private static Stream<Arguments> delimitedAtSignPhysicalTargetWithAliasedTableVariableSourceArguments() {
        return Stream.of(
                Arguments.of("bracket delimited physical target with aliased table variable source", QuoteCharacter.BRACKETS, false),
                Arguments.of("quote delimited physical target with aliased table variable source", QuoteCharacter.QUOTE, false),
                Arguments.of("schema qualified bracket delimited physical target with aliased table variable source", QuoteCharacter.BRACKETS, true),
                Arguments.of("schema qualified quote delimited physical target with aliased table variable source", QuoteCharacter.QUOTE, true));
    }
    
    private static Stream<Arguments> tableVariableTargetWithAliasedDelimitedAtSignPhysicalSourceArguments() {
        return Stream.of(
                Arguments.of("table variable target with aliased bracket delimited physical source", QuoteCharacter.BRACKETS, false, "src"),
                Arguments.of("table variable target with aliased quote delimited physical source", QuoteCharacter.QUOTE, false, "x"),
                Arguments.of("table variable target with schema qualified aliased bracket delimited physical source", QuoteCharacter.BRACKETS, true, "src"),
                Arguments.of("table variable target with schema qualified aliased quote delimited physical source", QuoteCharacter.QUOTE, true, "src"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("sameTextDelimitedAliasArguments")
    void assertBindSameTextDelimitedAliasDoesNotConflateTableVariableAndPhysicalTable(final String name, final boolean variableTarget, final QuoteCharacter quoteCharacter) {
        IdentifierValue delimitedName = new IdentifierValue("@MyTable", quoteCharacter);
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, variableTarget ? delimitedName : new IdentifierValue("@MyTable")));
        fromTable.setAlias(new AliasSegment(0, 0, delimitedName));
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 0, variableTarget ? new IdentifierValue("@MyTable") : delimitedName));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Remark"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(targetTable)
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "x")))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(variableTarget ? QuoteCharacter.NONE : quoteCharacter));
        SimpleTableSegment boundFrom = (SimpleTableSegment) actual.getFrom().get();
        assertThat(boundFrom.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundFrom.getTableName().getIdentifier().getQuoteCharacter(), is(variableTarget ? quoteCharacter : QuoteCharacter.NONE));
        assertThat(boundFrom.getAlias().get().getValue(), is("@MyTable"));
        assertThat(boundFrom.getAlias().get().getQuoteCharacter(), is(quoteCharacter));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(variableTarget ? QuoteCharacter.NONE : quoteCharacter));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Remark"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(variableTarget ? TableSourceType.TEMPORARY_TABLE : TableSourceType.PHYSICAL_TABLE));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("@MyTable")));
        if (variableTarget) {
            assertThat(((SimpleTableSegment) updateStatementContext.getSqlStatement().getTable()).getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        }
    }
    
    private static Stream<Arguments> sameTextDelimitedAliasArguments() {
        return Stream.of(
                Arguments.of("table variable target with bracket delimited same-text alias", true, QuoteCharacter.BRACKETS),
                Arguments.of("table variable target with quote delimited same-text alias", true, QuoteCharacter.QUOTE),
                Arguments.of("bracket delimited physical target with table variable source and bracket delimited same-text alias", false, QuoteCharacter.BRACKETS),
                Arguments.of("quote delimited physical target with table variable source and quote delimited same-text alias", false, QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertBindTableVariableTargetWithJoinAndBracketDelimitedSameTextAliasKeepsVariableTarget() {
        UpdateStatement actual = bindTableVariableTargetWithJoinAndDelimitedSameTextAlias(QuoteCharacter.BRACKETS, new LiteralExpressionSegment(0, 0, "x"));
        assertTableVariableTargetWithJoinAndDelimitedSameTextAlias(actual, QuoteCharacter.BRACKETS);
    }
    
    @Test
    void assertBindTableVariableTargetWithJoinAndQuoteDelimitedSameTextAliasKeepsVariableTargetForParameter() {
        UpdateStatement actual = bindTableVariableTargetWithJoinAndDelimitedSameTextAlias(QuoteCharacter.QUOTE, new ParameterMarkerExpressionSegment(0, 0, 0));
        assertTableVariableTargetWithJoinAndDelimitedSameTextAlias(actual, QuoteCharacter.QUOTE);
    }
    
    private UpdateStatement bindTableVariableTargetWithJoinAndDelimitedSameTextAlias(final QuoteCharacter quoteCharacter, final ExpressionSegment setValue) {
        IdentifierValue delimitedName = new IdentifierValue("@MyTable", quoteCharacter);
        SimpleTableSegment userTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")));
        userTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("u")));
        SimpleTableSegment physicalTable = new SimpleTableSegment(new TableNameSegment(0, 0, delimitedName));
        physicalTable.setAlias(new AliasSegment(0, 0, delimitedName));
        ColumnSegment userIdColumn = new ColumnSegment(0, 0, new IdentifierValue("user_id"));
        userIdColumn.setOwner(new OwnerSegment(0, 0, new IdentifierValue("u")));
        ColumnSegment physicalIdColumn = new ColumnSegment(0, 0, new IdentifierValue("id"));
        physicalIdColumn.setOwner(new OwnerSegment(0, 0, delimitedName));
        JoinTableSegment fromJoin = new JoinTableSegment();
        fromJoin.setLeft(userTable);
        fromJoin.setRight(physicalTable);
        fromJoin.setJoinType("JOIN");
        fromJoin.setCondition(new BinaryOperationExpression(0, 0, userIdColumn, physicalIdColumn, "=", "u.user_id = id"));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Remark"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable"))))
                .from(fromJoin)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), setValue))))
                .targetTableIsFromAlias(true)
                .build();
        return new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
    }
    
    private void assertTableVariableTargetWithJoinAndDelimitedSameTextAlias(final UpdateStatement actual, final QuoteCharacter quoteCharacter) {
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        JoinTableSegment boundFrom = (JoinTableSegment) actual.getFrom().get();
        SimpleTableSegment boundPhysicalTable = (SimpleTableSegment) boundFrom.getRight();
        assertThat(boundPhysicalTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundPhysicalTable.getTableName().getIdentifier().getQuoteCharacter(), is(quoteCharacter));
        assertThat(boundPhysicalTable.getAlias().get().getValue(), is("@MyTable"));
        assertThat(boundPhysicalTable.getAlias().get().getQuoteCharacter(), is(quoteCharacter));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(QuoteCharacter.NONE));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalColumn().getValue(), is("Remark"));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(new HashSet<>(Arrays.asList("t_user", "@MyTable"))));
        assertThat(((SimpleTableSegment) updateStatementContext.getSqlStatement().getTable()).getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("delimitedSameTextAliasOverPhysicalSourceArguments")
    void assertBindTableVariableTargetWithDelimitedSameTextAliasOverPhysicalSourceKeepsVariableTarget(final String name, final QuoteCharacter quoteCharacter) {
        SimpleTableSegment fromTable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")));
        fromTable.setAlias(new AliasSegment(0, 0, new IdentifierValue("@MyTable", quoteCharacter)));
        ColumnSegment setColumn = new ColumnSegment(0, 0, new IdentifierValue("Remark"));
        UpdateStatement updateStatement = UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTable"))))
                .from(fromTable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(setColumn), new LiteralExpressionSegment(0, 0, "x")))))
                .targetTableIsFromAlias(true)
                .build();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithDelimitedAtSignPhysicalTable(), "foo_db", new HintValueContext(), updateStatement));
        SimpleTableSegment boundTable = (SimpleTableSegment) actual.getTable();
        assertThat(boundTable.getTableName().getIdentifier().getValue(), is("@MyTable"));
        assertThat(boundTable.getTableName().getIdentifier().getQuoteCharacter(), is(QuoteCharacter.NONE));
        ColumnSegment actualSetColumn = actual.getAssignment().get().getAssignments().iterator().next().getColumns().iterator().next();
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getValue(), is("@MyTable"));
        assertThat(actualSetColumn.getColumnBoundInfo().getOriginalTable().getQuoteCharacter(), is(QuoteCharacter.NONE));
        assertThat(actualSetColumn.getColumnBoundInfo().getTableSourceType(), is(TableSourceType.TEMPORARY_TABLE));
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertThat(updateStatementContext.getTablesContext().getTableNames(), is(Collections.singleton("t_user")));
    }
    
    private static Stream<Arguments> delimitedSameTextAliasOverPhysicalSourceArguments() {
        return Stream.of(
                Arguments.of("table variable target with bracket delimited same-text alias over physical source", QuoteCharacter.BRACKETS),
                Arguments.of("table variable target with quote delimited same-text alias over physical source", QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertBindAliasedTableVariableTarget() {
        UpdateStatement updateStatement = createAliasedTableVariableTargetUpdateStatement();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement));
        assertAliasedTableVariableTarget(actual);
    }
    
    @Test
    void assertBindAliasedTableVariableTargetPrecedesSameNamedPhysicalTable() {
        UpdateStatement updateStatement = createAliasedTableVariableTargetUpdateStatement();
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement,
                new SQLStatementBinderContext(createSQLServerMetaDataWithAtSignTable(), "foo_db", new HintValueContext(), updateStatement));
        assertAliasedTableVariableTarget(actual);
    }
    
    @Test
    void assertBindAliasedTableVariableTargetPrecedesSameNamedExternalContext() {
        UpdateStatement updateStatement = createAliasedTableVariableTargetUpdateStatement();
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(createSQLServerMetaData(), "foo_db", new HintValueContext(), updateStatement);
        binderContext.getExternalTableBinderContexts().put(CaseInsensitiveString.of("@MyTableVar"),
                new SimpleTableSegmentBinderContext(Collections.emptyList(), TableSourceType.TEMPORARY_TABLE));
        UpdateStatement actual = new UpdateStatementBinder().bind(updateStatement, binderContext);
        assertAliasedTableVariableTarget(actual);
    }
    
    private UpdateStatement createAliasedTableVariableTargetUpdateStatement() {
        SimpleTableSegment tableVariable = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("@MyTableVar")));
        tableVariable.setAlias(new AliasSegment(0, 0, new IdentifierValue("target")));
        return UpdateStatement.builder()
                .databaseType(sqlServerDatabaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("target"))))
                .from(tableVariable)
                .setAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(
                        new ColumnAssignmentSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("Value"))), new LiteralExpressionSegment(0, 0, 1)))))
                .targetTableIsFromAlias(true)
                .build();
    }
    
    private void assertAliasedTableVariableTarget(final UpdateStatement actual) {
        assertThat(((SimpleTableSegment) actual.getTable()).getTableName().getIdentifier().getValue(), is("@MyTableVar"));
        assertThat(((SimpleTableSegment) actual.getTable()).getAliasName().get(), is("target"));
        assertTrue(actual.isTargetTableIsFromAlias());
        UpdateStatementContext updateStatementContext = new UpdateStatementContext(actual);
        assertFalse(updateStatementContext.getTablesContext().getTableNames().contains("@MyTableVar"));
    }
    
    private WithSegment createWithSegment() {
        return new WithSegment(0, 0, new LinkedList<>(Collections.singletonList(
                new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("combined_users")),
                        new SubquerySegment(0, 0, createWithSelectStatement(), "")))),
                false);
    }
    
    private SelectStatement createWithSelectStatement() {
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("user_id"))));
        return SelectStatement.builder().databaseType(databaseType).projections(projections)
                .from(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_user")))).build();
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue tOrder = new IdentifierValue("t_order");
        IdentifierValue tUser = new IdentifierValue("t_user");
        when(schema.getTable("t_order").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable("t_user").getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(tOrder).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.INTEGER, false, false, false, true, false, false)));
        when(schema.getTable(tUser).getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getDatabase("foo_db").getSchema("foo_db")).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase)).thenReturn(schema);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase(fooDatabase).getDefaultSchemaName()).thenReturn("foo_db");
        when(result.getDatabase("foo_db").containsSchema("foo_db")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_order")).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("foo_db").containsTable("t_user")).thenReturn(true);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase).containsTable(tOrder)).thenReturn(true);
        when(result.getDatabase(fooDatabase).getSchema(fooDatabase).containsTable(tUser)).thenReturn(true);
        return result;
    }
    
    private ShardingSphereMetaData createSQLServerMetaData() {
        ShardingSphereSchema humanResourcesSchema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue humanResources = new IdentifierValue("HumanResources");
        IdentifierValue employee = new IdentifierValue("Employee");
        when(humanResourcesSchema.getName()).thenReturn("HumanResources");
        when(humanResourcesSchema.containsTable(employee)).thenReturn(true);
        when(humanResourcesSchema.containsTable("Employee")).thenReturn(true);
        when(humanResourcesSchema.getTable(employee).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("BusinessEntityID", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationHours", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationNote", Types.VARCHAR, false, false, false, true, false, false)));
        when(humanResourcesSchema.getTable("Employee").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("BusinessEntityID", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationHours", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationNote", Types.VARCHAR, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getDefaultSchemaName()).thenReturn("dbo");
        when(result.getDatabase(fooDatabase).getDefaultSchemaName()).thenReturn("dbo");
        when(result.getDatabase("foo_db").containsSchema("dbo")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(new IdentifierValue("dbo"))).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("HumanResources")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(humanResources)).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("HumanResources")).thenReturn(humanResourcesSchema);
        when(result.getDatabase(fooDatabase).getSchema(humanResources)).thenReturn(humanResourcesSchema);
        when(result.getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(humanResourcesSchema));
        when(result.getDatabase(fooDatabase).getAllSchemas()).thenReturn(Collections.singleton(humanResourcesSchema));
        return result;
    }
    
    private ShardingSphereMetaData createSQLServerMetaDataWithAtSignTable() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue dboSchema = new IdentifierValue("dbo");
        IdentifierValue atSignTable = new IdentifierValue("@MyTableVar");
        when(schema.getName()).thenReturn("dbo");
        when(schema.containsTable(atSignTable)).thenReturn(true);
        when(schema.containsTable("@MyTableVar")).thenReturn(true);
        when(schema.getTable(atSignTable).getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("OtherColumn", Types.INTEGER, false, false, false, true, false, false)));
        ShardingSphereMetaData result = createSQLServerMetaData();
        when(result.getDatabase("foo_db").getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.getDatabase(fooDatabase).getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.getDatabase("foo_db").getSchema("dbo")).thenReturn(schema);
        when(result.getDatabase(fooDatabase).getSchema(dboSchema)).thenReturn(schema);
        return result;
    }
    
    private ShardingSphereMetaData createSQLServerMetaDataWithDelimitedAtSignPhysicalTable() {
        ShardingSphereSchema dboSchema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema humanResourcesSchema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        IdentifierValue fooDatabase = new IdentifierValue("foo_db");
        IdentifierValue dbo = new IdentifierValue("dbo");
        IdentifierValue humanResources = new IdentifierValue("HumanResources");
        IdentifierValue bracketAtSignTable = new IdentifierValue("@MyTable", QuoteCharacter.BRACKETS);
        IdentifierValue quoteAtSignTable = new IdentifierValue("@MyTable", QuoteCharacter.QUOTE);
        IdentifierValue employee = new IdentifierValue("Employee");
        when(dboSchema.getName()).thenReturn("dbo");
        when(dboSchema.containsTable(bracketAtSignTable)).thenReturn(true);
        when(dboSchema.containsTable(quoteAtSignTable)).thenReturn(true);
        when(dboSchema.containsTable("@MyTable")).thenReturn(true);
        when(dboSchema.getTable(bracketAtSignTable).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("Status", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("Remark", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)));
        when(dboSchema.getTable(quoteAtSignTable).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("Status", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("Remark", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)));
        when(dboSchema.getTable("@MyTable").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("Status", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("Remark", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)));
        IdentifierValue tUser = new IdentifierValue("t_user");
        when(dboSchema.containsTable(tUser)).thenReturn(true);
        when(dboSchema.containsTable("t_user")).thenReturn(true);
        when(dboSchema.getTable(tUser).getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        when(dboSchema.getTable("t_user").getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)));
        when(humanResourcesSchema.getName()).thenReturn("HumanResources");
        when(humanResourcesSchema.containsTable(employee)).thenReturn(true);
        when(humanResourcesSchema.containsTable("Employee")).thenReturn(true);
        when(humanResourcesSchema.getTable(employee).getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("BusinessEntityID", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationHours", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationNote", Types.VARCHAR, false, false, false, true, false, false)));
        when(humanResourcesSchema.getTable("Employee").getAllColumns()).thenReturn(Arrays.asList(
                new ShardingSphereColumn("BusinessEntityID", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationHours", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("VacationNote", Types.VARCHAR, false, false, false, true, false, false)));
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.containsDatabase("foo_db")).thenReturn(true);
        when(result.containsDatabase(fooDatabase)).thenReturn(true);
        when(result.getDatabase("foo_db").getDefaultSchemaName()).thenReturn("dbo");
        when(result.getDatabase(fooDatabase).getDefaultSchemaName()).thenReturn("dbo");
        when(result.getDatabase("foo_db").containsSchema("dbo")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(dbo)).thenReturn(true);
        when(result.getDatabase("foo_db").containsSchema("HumanResources")).thenReturn(true);
        when(result.getDatabase(fooDatabase).containsSchema(humanResources)).thenReturn(true);
        when(result.getDatabase("foo_db").getSchema("dbo")).thenReturn(dboSchema);
        when(result.getDatabase(fooDatabase).getSchema(dbo)).thenReturn(dboSchema);
        when(result.getDatabase("foo_db").getSchema("HumanResources")).thenReturn(humanResourcesSchema);
        when(result.getDatabase(fooDatabase).getSchema(humanResources)).thenReturn(humanResourcesSchema);
        when(result.getDatabase("foo_db").getAllSchemas()).thenReturn(Arrays.asList(dboSchema, humanResourcesSchema));
        when(result.getDatabase(fooDatabase).getAllSchemas()).thenReturn(Arrays.asList(dboSchema, humanResourcesSchema));
        return result;
    }
}
