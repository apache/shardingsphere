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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.IndexOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSimpleSelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectionsContextEngineTest {
    
    @Test
    void assertProjectionsContextCreatedProperlyForMySQL() {
        assertProjectionsContextCreatedProperly(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForOracle() {
        assertProjectionsContextCreatedProperly(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForPostgreSQL() {
        assertProjectionsContextCreatedProperly(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForSQL92() {
        assertProjectionsContextCreatedProperly(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForSQLServer() {
        assertProjectionsContextCreatedProperly(new SQLServerSimpleSelectStatement());
    }
    
    private void assertProjectionsContextCreatedProperly(final SimpleSelectStatement selectStatement) {
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsSegment projectionsSegment = selectStatement.getProjections();
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = projectionsContextEngine
                .createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForMySQL() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForOracle() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForPostgreSQL() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForSQL92() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForSQLServer() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new SQLServerSimpleSelectStatement());
    }
    
    private void assertProjectionsContextCreatedProperlyWhenProjectionPresent(final SimpleSelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(),
                projectionsSegment, new GroupByContext(Collections.emptyList()), new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForMySQL() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForOracle() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForPostgreSQL() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForSQL92() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForSQLServer() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new SQLServerSimpleSelectStatement());
    }
    
    private void createProjectionsContextWhenOrderByContextOrderItemsPresent(final SimpleSelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new IndexOrderByItemSegment(0, 1, 0, OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForMySQL() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForOracle() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForPostgreSQL() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForSQL92() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForSQLServer() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateProjectionsContextWithoutIndexOrderByItemSegment(final SimpleSelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 1, "", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    private SelectStatementContext createSelectStatementContext(final SimpleSelectStatement selectStatement) {
        ShardingSphereDatabase database = mockDatabase();
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class),
                mock(RuleMetaData.class), mock(ConfigurationProperties.class));
        return new SelectStatementContext(metaData, Collections.emptyList(), selectStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return result;
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForMySQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(final SimpleSelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("name"))));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForMySQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(final SimpleSelectStatement selectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("name"))));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
        shorthandProjectionSegment.setOwner(owner);
        projectionsSegment.getProjections().add(shorthandProjectionSegment);
        OrderByItem orderByItem = new OrderByItem(new ColumnOrderByItemSegment(new ColumnSegment(0, 0, new IdentifierValue("name")), OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singletonList(orderByItem), true);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForMySQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(final SimpleSelectStatement selectStatement) {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("name")));
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        selectStatement.setProjections(projectionsSegment);
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue(DefaultDatabase.LOGIC_NAME)));
        selectStatement.setFrom(tableSegment);
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("name")));
        OwnerSegment owner = new OwnerSegment(0, 10, new IdentifierValue("name"));
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
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForMySQL() {
        assertCreateProjectionsContextWithTemporaryTable(new MySQLSimpleSelectStatement(), new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForOracle() {
        assertCreateProjectionsContextWithTemporaryTable(new OracleSimpleSelectStatement(), new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForPostgreSQL() {
        assertCreateProjectionsContextWithTemporaryTable(new PostgreSQLSimpleSelectStatement(), new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForSQL92() {
        assertCreateProjectionsContextWithTemporaryTable(new SQL92SimpleSelectStatement(), new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForSQLServer() {
        assertCreateProjectionsContextWithTemporaryTable(new SQLServerSimpleSelectStatement(), new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateProjectionsContextWithTemporaryTable(final SimpleSelectStatement selectStatement, final SimpleSelectStatement subquerySimpleSelectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        subquerySimpleSelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, subquerySimpleSelectStatement, ""));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("d")));
        selectStatement.setFrom(subqueryTableSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("d")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForMySQL() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new MySQLSimpleSelectStatement(), new MySQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForOracle() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new OracleSimpleSelectStatement(), new OracleSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForPostgreSQL() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new PostgreSQLSimpleSelectStatement(), new PostgreSQLSimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForSQL92() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new SQL92SimpleSelectStatement(), new SQL92SimpleSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForSQLServer() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new SQLServerSimpleSelectStatement(), new SQLServerSimpleSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(final SimpleSelectStatement selectStatement, final SimpleSelectStatement subquerySimpleSelectStatement) {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("table")));
        projectionsSegment.getProjections().add(projectionSegment);
        selectStatement.setProjections(projectionsSegment);
        subquerySimpleSelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, subquerySimpleSelectStatement, ""));
        subqueryTableSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("TABLE")));
        selectStatement.setFrom(subqueryTableSegment);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        columnSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("TAble")));
        OrderByItem groupByItem = new OrderByItem(new ColumnOrderByItemSegment(columnSegment, OrderDirection.ASC, NullsOrderType.FIRST));
        GroupByContext groupByContext = new GroupByContext(Collections.singleton(groupByItem));
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, groupByContext, new OrderByContext(Collections.emptyList(), false));
        assertNotNull(actual);
    }
    
    @Test
    void assertCreateProjectionsContextWithOrderByExpressionForMySQL() {
        SimpleSelectStatement selectStatement = new MySQLSimpleSelectStatement();
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        projectionsSegment.getProjections().add(new ShorthandProjectionSegment(0, 0));
        selectStatement.setProjections(projectionsSegment);
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order"))));
        OrderByItem orderByItem = new OrderByItem(new ExpressionOrderByItemSegment(0, 0, "id + 1", OrderDirection.ASC, NullsOrderType.FIRST));
        OrderByContext orderByContext = new OrderByContext(Collections.singleton(orderByItem), false);
        SelectStatementContext selectStatementContext = createSelectStatementContext(selectStatement);
        ProjectionsContextEngine engine = new ProjectionsContextEngine(selectStatementContext.getDatabaseType());
        ProjectionsContext actual = engine.createProjectionsContext(selectStatement.getFrom(), projectionsSegment, new GroupByContext(Collections.emptyList()), orderByContext);
        assertThat(actual.getProjections().size(), is(2));
    }
}
