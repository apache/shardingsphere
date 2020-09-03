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

package org.apache.shardingsphere.sql.parser.binder.segment.select.projection.engine;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.constant.OrderDirection;
//import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableFactorSegment;
//import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.Assert.assertNotNull;

public final class ProjectionsContextEngineTest {

    private SchemaMetaData schemaMetaData;
    
    @Before
    public void setUp() {
        schemaMetaData = new SchemaMetaData(Collections.emptyMap());
    }
    
    @Test
    public void assertProjectionsContextCreatedProperly() {
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(null);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, new LinkedList<>(), selectStatement);
        Collection<SimpleTableSegment> tables = selectStatementContext.getSimpleTableSegments();
        ProjectionsSegment projectionsSegment = selectStatement.getProjections();
        ProjectionsContext actual = projectionsContextEngine
                .createProjectionsContext(tables, projectionsSegment, new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    public void assertProjectionsContextCreatedProperlyWhenProjectionPresent() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().addAll(Collections.singleton(shorthandProjectionSegment));
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, new LinkedList<>(), selectStatement);
        Collection<SimpleTableSegment> tables = selectStatementContext.getSimpleTableSegments();
        ProjectionsContext actual = new ProjectionsContextEngine(schemaMetaData)
                .createProjectionsContext(tables, projectionsSegment, new GroupByContext(Collections.emptyList(), 0), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    public void createProjectionsContextWhenOrderByContextOrderItemsPresent() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().addAll(Collections.singleton(shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(0, 1, 0, OrderDirection.ASC));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, new LinkedList<>(), selectStatement);
        Collection<SimpleTableSegment> tables = selectStatementContext.getSimpleTableSegments();
        ProjectionsContext actual = new ProjectionsContextEngine(schemaMetaData)
                .createProjectionsContext(tables, projectionsSegment, new GroupByContext(Collections.emptyList(), 0), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWithoutIndexOrderByItemSegment() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().addAll(Collections.singleton(shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 1, "", OrderDirection.ASC));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, new LinkedList<>(), selectStatement);
        Collection<SimpleTableSegment> tables = selectStatementContext.getSimpleTableSegments();
        ProjectionsContext actual = new ProjectionsContextEngine(schemaMetaData)
                .createProjectionsContext(tables, projectionsSegment, new GroupByContext(Collections.emptyList(), 0), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(projectionsSegment);
//        TableReferenceSegment tableReferenceSegment = new TableReferenceSegment();
//        TableFactorSegment tableFactorSegment = new TableFactorSegment();
//        tableFactorSegment.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("name")));
//        tableReferenceSegment.setTableFactor(tableFactorSegment);
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("name")));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().addAll(Collections.singleton(shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, new LinkedList<>(), selectStatement);
        Collection<SimpleTableSegment> tables = selectStatementContext.getSimpleTableSegments();
        ProjectionsContext actual = new ProjectionsContextEngine(schemaMetaData)
                .createProjectionsContext(tables, projectionsSegment, new GroupByContext(Collections.emptyList(), 0), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(projectionsSegment);
//        TableReferenceSegment tableReferenceSegment = new TableReferenceSegment();
//        TableFactorSegment tableFactorSegment = new TableFactorSegment();
//        tableFactorSegment.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("name")));
//        tableReferenceSegment.setTableFactor(tableFactorSegment);
        selectStatement.setFrom(new SimpleTableSegment(0, 0, new IdentifierValue("name")));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().addAll(Collections.singleton(shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, new LinkedList<>(), selectStatement);
        Collection<SimpleTableSegment> tables = selectStatementContext.getSimpleTableSegments();
        ProjectionsContext actual = new ProjectionsContextEngine(schemaMetaData)
                .createProjectionsContext(tables, projectionsSegment, new GroupByContext(Collections.emptyList(), 0), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent() {
        SelectStatement selectStatement = new SelectStatement();
        SimpleTableSegment tableSegment = new SimpleTableSegment(0, 10, new IdentifierValue("name"));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("schema")));
//        TableReferenceSegment tableReferenceSegment = new TableReferenceSegment();
//        TableFactorSegment tableFactorSegment = new TableFactorSegment();
//        tableFactorSegment.setTable(tableSegment);
//        tableReferenceSegment.setTableFactor(tableFactorSegment);
        selectStatement.setFrom(tableSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        SimpleTableSegment table = new SimpleTableSegment(0, 10, new IdentifierValue("name"));
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        table.setOwner(new OwnerSegment(0, 10, new IdentifierValue("name")));
        shorthandProjectionSegment.setOwner(owner);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        columnSegment.setOwner(owner);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        columnProjectionSegment.getColumn().setOwner(owner);
        projectionsSegment.getProjections().addAll(Lists.newArrayList(columnProjectionSegment, shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        SelectStatementContext selectStatementContext = new SelectStatementContext(schemaMetaData, new LinkedList<>(), selectStatement);
        Collection<SimpleTableSegment> tables = selectStatementContext.getSimpleTableSegments();
        ProjectionsContext actual = new ProjectionsContextEngine(schemaMetaData)
                .createProjectionsContext(tables, projectionsSegment, new GroupByContext(Collections.emptyList(), 0), orderByContext);
        assertNotNull(actual);
    }
}
