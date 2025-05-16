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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine;

import org.apache.shardingsphere.infra.binder.context.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.dml.SQL92SelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ProjectionsContextEngineTest {
    
    @Test
    void assertProjectionsContextCreatedProperly() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsSegment projectionsSegment = selectStatement.getProjections();
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = projectionsContextEngine.createProjectionsContext(
                projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresent() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void createProjectionsContextWhenOrderByContextOrderItemsPresent() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(0, 1, 0, OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegment() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 1, "", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    private SelectStatementContext createSelectStatementContext(final SelectStatement selectStatement) {
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), mock());
        return new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, "foo_db", Collections.emptyList());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("name"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        selectStatement.setFrom(new SimpleTableSegment(tableNameSegment));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("name"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        selectStatement.setFrom(new SimpleTableSegment(tableNameSegment));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        TableNameSegment tableNameSegment = new TableNameSegment(0, 10, new IdentifierValue("name"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        SimpleTableSegment tableSegment = new SimpleTableSegment(tableNameSegment);
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_db")));
        selectStatement.setFrom(tableSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        SimpleTableSegment table = new SimpleTableSegment(tableNameSegment);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        owner.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        table.setOwner(new OwnerSegment(0, 10, new IdentifierValue("name")));
        shorthandProjectionSegment.setOwner(owner);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("col"));
        columnSegment.setOwner(owner);
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(columnSegment);
        columnProjectionSegment.getColumn().setOwner(owner);
        projectionsSegment.getProjections().addAll(Arrays.asList(columnProjectionSegment, shorthandProjectionSegment));
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTable() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        SelectStatement subquerySelectStatement = new SQL92SelectStatement();
        subquerySelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, subquerySelectStatement, ""));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("d")));
        selectStatement.setFrom(subqueryTableSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase() {
        SelectStatement selectStatement = new SQL92SelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        SelectStatement subquerySelectStatement = new SQL92SelectStatement();
        subquerySelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(0, 0, subquerySelectStatement, ""));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("TABLE")));
        selectStatement.setFrom(subqueryTableSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("TAble")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithOrderByExpressionForMySQL() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        selectStatement.setProjections(projectionsSegment);
        TableNameSegment tableNameSegment = new TableNameSegment(0, 0, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        selectStatement.setFrom(new SimpleTableSegment(tableNameSegment));
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, "id + 1", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertThat(actual.getProjections().size(), is(2));
    }
}
