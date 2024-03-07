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
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;
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
        assertProjectionsContextCreatedProperly(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForOracle() {
        assertProjectionsContextCreatedProperly(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForPostgreSQL() {
        assertProjectionsContextCreatedProperly(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForSQL92() {
        assertProjectionsContextCreatedProperly(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyForSQLServer() {
        assertProjectionsContextCreatedProperly(new SQLServerGenericSelectStatement());
    }
    
    private void assertProjectionsContextCreatedProperly(final GenericSelectStatement selectStatement) {
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
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForOracle() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForPostgreSQL() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForSQL92() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertProjectionsContextCreatedProperlyWhenProjectionPresentForSQLServer() {
        assertProjectionsContextCreatedProperlyWhenProjectionPresent(new SQLServerGenericSelectStatement());
    }
    
    private void assertProjectionsContextCreatedProperlyWhenProjectionPresent(final GenericSelectStatement selectStatement) {
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
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForOracle() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForPostgreSQL() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForSQL92() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenOrderByContextOrderItemsPresentForSQLServer() {
        createProjectionsContextWhenOrderByContextOrderItemsPresent(new SQLServerGenericSelectStatement());
    }
    
    private void createProjectionsContextWhenOrderByContextOrderItemsPresent(final GenericSelectStatement selectStatement) {
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
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForOracle() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForPostgreSQL() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForSQL92() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithoutIndexOrderByItemSegmentForSQLServer() {
        assertCreateProjectionsContextWithoutIndexOrderByItemSegment(new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateProjectionsContextWithoutIndexOrderByItemSegment(final GenericSelectStatement selectStatement) {
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
    
    private SelectStatementContext createSelectStatementContext(final GenericSelectStatement selectStatement) {
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
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerAbsent(final GenericSelectStatement selectStatement) {
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
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresent(final GenericSelectStatement selectStatement) {
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
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForOracle() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForPostgreSQL() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForSQL92() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresentForSQLServer() {
        assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenColumnOrderByItemSegmentOwnerPresentAndTablePresent(final GenericSelectStatement selectStatement) {
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
        assertCreateProjectionsContextWithTemporaryTable(new MySQLGenericSelectStatement(), new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForOracle() {
        assertCreateProjectionsContextWithTemporaryTable(new OracleGenericSelectStatement(), new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForPostgreSQL() {
        assertCreateProjectionsContextWithTemporaryTable(new PostgreSQLGenericSelectStatement(), new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForSQL92() {
        assertCreateProjectionsContextWithTemporaryTable(new SQL92GenericSelectStatement(), new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWithTemporaryTableForSQLServer() {
        assertCreateProjectionsContextWithTemporaryTable(new SQLServerGenericSelectStatement(), new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateProjectionsContextWithTemporaryTable(final GenericSelectStatement selectStatement, final GenericSelectStatement subquerySimpleSelectStatement) {
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
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new MySQLGenericSelectStatement(), new MySQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForOracle() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new OracleGenericSelectStatement(), new OracleGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForPostgreSQL() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new PostgreSQLGenericSelectStatement(), new PostgreSQLGenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForSQL92() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new SQL92GenericSelectStatement(), new SQL92GenericSelectStatement());
    }
    
    @Test
    void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCaseForSQLServer() {
        assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(new SQLServerGenericSelectStatement(), new SQLServerGenericSelectStatement());
    }
    
    private void assertCreateProjectionsContextWhenTableNameOrAliasIgnoreCase(final GenericSelectStatement selectStatement, final GenericSelectStatement subquerySimpleSelectStatement) {
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
        GenericSelectStatement selectStatement = new MySQLGenericSelectStatement();
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
