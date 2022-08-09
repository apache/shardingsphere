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

import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class TableExtractorTest {
    
    private final TableExtractor tableExtractor = new TableExtractor();
    
    @Test
    public void assertExtractTablesFromSelectProjects() {
        AggregationProjectionSegment aggregationProjection = new AggregationProjectionSegment(10, 20, AggregationType.SUM, "t_order.id");
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
    public void assertExtractTablesFromSelectLockWithValue() {
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
    public void assertExtractTablesFromInsert() {
        MySQLInsertStatement mySQLInsertStatement = new MySQLInsertStatement();
        mySQLInsertStatement.setTable(new SimpleTableSegment(new TableNameSegment(122, 128, new IdentifierValue("t_order"))));
        Collection<AssignmentSegment> assignmentSegments = new ArrayList<>();
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
    
    private void assertTableSegment(final SimpleTableSegment actual, final int expectedStartIndex, final int expectedStopIndex, final String expectedTableName) {
        assertThat(actual.getStartIndex(), is(expectedStartIndex));
        assertThat(actual.getStopIndex(), is(expectedStopIndex));
        Optional<String> actualTableName = Optional.ofNullable(actual.getTableName()).map(TableNameSegment::getIdentifier).map(IdentifierValue::getValue);
        assertTrue(actualTableName.isPresent());
        assertThat(actualTableName.get(), is(expectedTableName));
    }
}
