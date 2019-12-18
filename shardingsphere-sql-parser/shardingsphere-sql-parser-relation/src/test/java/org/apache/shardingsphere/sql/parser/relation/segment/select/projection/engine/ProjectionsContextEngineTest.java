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

package org.apache.shardingsphere.sql.parser.relation.segment.select.projection.engine;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.TextOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProjectionsContextEngineTest {
    
    @Test
    public void assertProjectionsContextCreatedProperly() {
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(null);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getSelectItems()).thenReturn(mock(SelectItemsSegment.class));
        ProjectionsContext actual = projectionsContextEngine.createProjectionsContext(null, selectStatement, mock(GroupByContext.class), mock(OrderByContext.class));
        assertNotNull(actual);
    }
    
    @Test
    public void assertProjectionsContextCreatedProperlyWhenSelectItemPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        ProjectionsContext actual = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), mock(OrderByContext.class));
        assertNotNull(actual);
    }
    
    @Test
    public void createProjectionsContextWhenOrderByContextOrderItemsPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        OrderByContext orderByContext = mock(OrderByContext.class);
        OrderByItem orderByItem = mock(OrderByItem.class);
        when(orderByItem.getSegment()).thenReturn(mock(IndexOrderByItemSegment.class));
        when(orderByContext.getItems()).thenReturn(Collections.singletonList(orderByItem));
        ProjectionsContext actual = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWithoutIndexOrderByItemSegment() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        OrderByContext orderByContext = mock(OrderByContext.class);
        OrderByItem orderByItem = mock(OrderByItem.class);
        when(orderByItem.getSegment()).thenReturn(mock(TextOrderByItemSegment.class));
        when(orderByContext.getItems()).thenReturn(Collections.singletonList(orderByItem));
        ProjectionsContext actual = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        OrderByContext orderByContext = mock(OrderByContext.class);
        OrderByItem orderByItem = mock(OrderByItem.class);
        ColumnOrderByItemSegment columnOrderByItemSegment = mock(ColumnOrderByItemSegment.class);
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        when(columnSegment.getOwner()).thenReturn(Optional.<TableSegment>absent());
        when(columnOrderByItemSegment.getColumn()).thenReturn(columnSegment);
        when(orderByItem.getSegment()).thenReturn(columnOrderByItemSegment);
        when(orderByContext.getItems()).thenReturn(Collections.singletonList(orderByItem));
        ProjectionsContext actual = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Collections.<SelectItemSegment>singleton(shorthandSelectItemSegment));
        OrderByContext orderByContext = mock(OrderByContext.class);
        OrderByItem orderByItem = mock(OrderByItem.class);
        ColumnOrderByItemSegment columnOrderByItemSegment = mock(ColumnOrderByItemSegment.class);
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        when(columnSegment.getOwner()).thenReturn(Optional.of(mock(TableSegment.class)));
        when(columnOrderByItemSegment.getColumn()).thenReturn(columnSegment);
        when(orderByItem.getSegment()).thenReturn(columnOrderByItemSegment);
        when(orderByContext.getItems()).thenReturn(Collections.singletonList(orderByItem));
        ProjectionsContext actual = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    public void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SelectItemsSegment selectItemsSegment = mock(SelectItemsSegment.class);
        when(selectStatement.getSelectItems()).thenReturn(selectItemsSegment);
        TableSegment tableSegment = new TableSegment(0, 10, "name");
        SchemaSegment schemaSegment = mock(SchemaSegment.class);
        when(schemaSegment.getName()).thenReturn("name");
        tableSegment.setOwner(schemaSegment);
        ShorthandSelectItemSegment shorthandSelectItemSegment = new ShorthandSelectItemSegment(0, 10, "text");
        TableSegment owner = new TableSegment(0, 10, "name");
        owner.setOwner(new SchemaSegment(0, 10, "name"));
        shorthandSelectItemSegment.setOwner(owner);
        when(selectStatement.findSQLSegments(TableAvailable.class)).thenReturn(Collections.singletonList((TableAvailable) tableSegment));
        ColumnSegment columnSegment = mock(ColumnSegment.class);
        when(columnSegment.getName()).thenReturn("name");
        when(columnSegment.getOwner()).thenReturn(Optional.of(tableSegment));
        ColumnSelectItemSegment columnSelectItemSegment = new ColumnSelectItemSegment("ColumnSelectItemSegment", columnSegment);
        columnSelectItemSegment.setOwner(owner);
        when(selectItemsSegment.getSelectItems()).thenReturn(Lists.<SelectItemSegment>newArrayList(columnSelectItemSegment, shorthandSelectItemSegment));
        OrderByContext orderByContext = mock(OrderByContext.class);
        OrderByItem orderByItem = mock(OrderByItem.class);
        ColumnOrderByItemSegment columnOrderByItemSegment = mock(ColumnOrderByItemSegment.class);
        when(columnOrderByItemSegment.getColumn()).thenReturn(columnSegment);
        when(orderByItem.getSegment()).thenReturn(columnOrderByItemSegment);
        when(orderByContext.getItems()).thenReturn(Collections.singletonList(orderByItem));
        ProjectionsContext actual = new ProjectionsContextEngine(null).createProjectionsContext(null, selectStatement, mock(GroupByContext.class), orderByContext);
        assertNotNull(actual);
    }
}
