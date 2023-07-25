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

package org.apache.shardingsphere.sql.parser.sql.common.extractor;

import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.CombineType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.RoutineBodySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.routine.ValidStatementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TableExtractorTest {
    
    private final TableExtractor tableExtractor = new TableExtractor();
    
    @Test
    void assertExtractTablesFromSelectProjects() {
        AggregationProjectionSegment aggregationProjection = new AggregationProjectionSegment(10, 20, AggregationType.SUM, "SUM(t_order.id)");
        ColumnSegment columnSegment = new ColumnSegment(133, 136, new IdentifierValue("id"));
        columnSegment.setOwner(new OwnerSegment(130, 132, new IdentifierValue("t_order")));
        aggregationProjection.getParameters().add(columnSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(10, 20);
        projectionsSegment.getProjections().add(aggregationProjection);
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(projectionsSegment);
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(1));
        Iterator<SimpleTableSegment> tableSegmentIterator = tableExtractor.getRewriteTables().iterator();
        assertTableSegment(tableSegmentIterator.next(), 130, 132, "t_order");
    }
    
    @Test
    void assertExtractTablesFromSelectLockWithValue() {
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        LockSegment lockSegment = new LockSegment(108, 154);
        selectStatement.setLock(lockSegment);
        lockSegment.getTables().add(new SimpleTableSegment(new TableNameSegment(122, 128, new IdentifierValue("t_order"))));
        lockSegment.getTables().add(new SimpleTableSegment(new TableNameSegment(143, 154, new IdentifierValue("t_order_item"))));
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(2));
        Iterator<SimpleTableSegment> tableSegmentIterator = tableExtractor.getRewriteTables().iterator();
        assertTableSegment(tableSegmentIterator.next(), 122, 128, "t_order");
        assertTableSegment(tableSegmentIterator.next(), 143, 154, "t_order_item");
    }
    
    @Test
    void assertExtractTablesFromInsert() {
        MySQLInsertStatement mySQLInsertStatement = new MySQLInsertStatement();
        mySQLInsertStatement.setTable(new SimpleTableSegment(new TableNameSegment(122, 128, new IdentifierValue("t_order"))));
        Collection<AssignmentSegment> assignmentSegments = new LinkedList<>();
        ColumnSegment columnSegment = new ColumnSegment(133, 136, new IdentifierValue("id"));
        columnSegment.setOwner(new OwnerSegment(130, 132, new IdentifierValue("t_order")));
        assignmentSegments.add(new ColumnAssignmentSegment(130, 140, Collections.singletonList(columnSegment), new LiteralExpressionSegment(141, 142, 1)));
        mySQLInsertStatement.setOnDuplicateKeyColumns(new OnDuplicateKeyColumnsSegment(130, 140, assignmentSegments));
        tableExtractor.extractTablesFromInsert(mySQLInsertStatement);
        assertThat(tableExtractor.getRewriteTables().size(), is(2));
        Iterator<SimpleTableSegment> tableSegmentIterator = tableExtractor.getRewriteTables().iterator();
        assertTableSegment(tableSegmentIterator.next(), 122, 128, "t_order");
        assertTableSegment(tableSegmentIterator.next(), 130, 132, "t_order");
    }
    
    @Test
    void assertNotExistTableFromRoutineBody() {
        RoutineBodySegment routineBodySegment = new RoutineBodySegment(0, 3);
        ValidStatementSegment validStatement = new ValidStatementSegment(0, 1);
        validStatement.setSqlStatement(() -> 0);
        routineBodySegment.getValidStatements().add(validStatement);
        ValidStatementSegment newValidStatement = new ValidStatementSegment(0, 1);
        validStatement.setSqlStatement(mock(CreateTableStatement.class));
        routineBodySegment.getValidStatements().add(newValidStatement);
        Collection<SimpleTableSegment> nonExistingTables = tableExtractor.extractNotExistTableFromRoutineBody(routineBodySegment);
        assertThat(nonExistingTables, hasSize(1));
    }
    
    private void assertTableSegment(final SimpleTableSegment actual, final int expectedStartIndex, final int expectedStopIndex, final String expectedTableName) {
        assertThat(actual.getStartIndex(), is(expectedStartIndex));
        assertThat(actual.getStopIndex(), is(expectedStopIndex));
        Optional<String> actualTableName = Optional.ofNullable(actual.getTableName()).map(TableNameSegment::getIdentifier).map(IdentifierValue::getValue);
        assertTrue(actualTableName.isPresent());
        assertThat(actualTableName.get(), is(expectedTableName));
    }
    
    @Test
    void assertExtractTablesFromCombineSegment() {
        MySQLSelectStatement selectStatement = createSelectStatement("t_order");
        selectStatement.setCombine(new CombineSegment(0, 0, createSelectStatement("t_order"), CombineType.UNION, createSelectStatement("t_order_item")));
        tableExtractor.extractTablesFromSelect(selectStatement);
        Collection<SimpleTableSegment> actual = tableExtractor.getRewriteTables();
        assertThat(actual.size(), is(2));
        Iterator<SimpleTableSegment> iterator = actual.iterator();
        assertTableSegment(iterator.next(), 0, 0, "t_order");
        assertTableSegment(iterator.next(), 0, 0, "t_order_item");
    }
    
    private static MySQLSelectStatement createSelectStatement(final String tableName) {
        MySQLSelectStatement result = new MySQLSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new ShorthandProjectionSegment(0, 0));
        result.setProjections(projections);
        result.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName))));
        return result;
    }
    
    @Test
    void assertExtractTablesFromCombineSegmentWithColumnProjection() {
        MySQLSelectStatement selectStatement = createSelectStatementWithColumnProjection("t_order");
        selectStatement.setCombine(new CombineSegment(0, 0, createSelectStatementWithColumnProjection("t_order"), CombineType.UNION, createSelectStatementWithColumnProjection("t_order_item")));
        tableExtractor.extractTablesFromSelect(selectStatement);
        Collection<SimpleTableSegment> actual = tableExtractor.getRewriteTables();
        assertThat(actual.size(), is(2));
        Iterator<SimpleTableSegment> iterator = actual.iterator();
        assertTableSegment(iterator.next(), 0, 0, "t_order");
        assertTableSegment(iterator.next(), 0, 0, "t_order_item");
    }
    
    private MySQLSelectStatement createSelectStatementWithColumnProjection(final String tableName) {
        MySQLSelectStatement result = new MySQLSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("id"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("a")));
        projections.getProjections().add(new ColumnProjectionSegment(columnSegment));
        result.setProjections(projections);
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(tableName)));
        tableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("a")));
        result.setFrom(tableSegment);
        return result;
    }
    
    @Test
    void assertExtractJoinTableSegmentsFromSelect() {
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(new SimpleTableSegment(new TableNameSegment(16, 22, new IdentifierValue("t_order"))));
        joinTableSegment.setRight(new SimpleTableSegment(new TableNameSegment(37, 48, new IdentifierValue("t_order_item"))));
        joinTableSegment.setJoinType("INNER");
        joinTableSegment.setCondition(new BinaryOperationExpression(56, 79, new ColumnSegment(56, 65, new IdentifierValue("order_id")),
                new ColumnSegment(69, 79, new IdentifierValue("order_id")), "=", "oi.order_id = o.order_id"));
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(joinTableSegment);
        tableExtractor.extractTablesFromSelect(selectStatement);
        assertThat(tableExtractor.getJoinTables().size(), is(1));
        assertThat(tableExtractor.getJoinTables().iterator().next(), is(joinTableSegment));
    }
}
