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

package org.apache.shardingsphere.infra.binder.segment.select.projection.engine;

import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.*;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.*;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProjectionEngineTest {
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private DatabaseType databaseType;
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentNotMatched() {
        assertFalse(new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), null).isPresent());
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfShorthandProjectionSegment() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        shorthandProjectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("tbl")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), shorthandProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfShorthandProjectionSegmentAndDuplicateTableSegment() {
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "content"));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(table, new ShorthandProjectionSegment(0, 0));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(2));
        Map<String, ColumnProjection> actualColumns = new LinkedHashMap<>();
        actualColumns.put("t_order.order_id", new ColumnProjection("t_order", "order_id", null));
        actualColumns.put("t_order.content", new ColumnProjection("t_order", "content", null));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(actualColumns));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfColumnProjectionSegment() {
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(new ColumnSegment(0, 10, new IdentifierValue("name")));
        columnProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), columnProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ColumnProjection.class));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfExpressionProjectionSegment() {
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 10, "text");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), expressionProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ExpressionProjection.class));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationDistinctProjectionSegment() {
        AggregationDistinctProjectionSegment aggregationDistinctProjectionSegment = new AggregationDistinctProjectionSegment(0, 10, AggregationType.COUNT, "(1)", "distinctExpression");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationDistinctProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationDistinctProjection.class));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationProjectionSegment() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 10, AggregationType.COUNT, "(1)");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationProjection.class));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationDistinctProjectionSegmentAndAggregationTypeIsAvg() {
        AggregationDistinctProjectionSegment aggregationDistinctProjectionSegment = new AggregationDistinctProjectionSegment(0, 10, AggregationType.AVG, "(1)", "distinctExpression");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationDistinctProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationDistinctProjection.class));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationProjectionSegmentAndAggregationTypeIsAvg() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 10, AggregationType.AVG, "(1)");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationProjection.class));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfParameterMarkerExpressionSegment() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(7, 7, 0);
        parameterMarkerExpressionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), parameterMarkerExpressionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ParameterMarkerProjection.class));
        assertThat(actual.get().getAlias().orElse(null), is("alias"));
    }
    
    @Test
    public void assertCreateProjectionWhenProjectionSegmentInstanceOfShorthandProjectionSegmentAndJoinTableSegment() {
        SimpleTableSegment ordersTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "customer_id"));
        SimpleTableSegment customersTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_customer")));
        when(schema.getVisibleColumnNames("t_customer")).thenReturn(Collections.singletonList("customer_id"));
        JoinTableSegment table = new JoinTableSegment();
        table.setLeft(ordersTableSegment);
        table.setRight(customersTableSegment);
        table.setCondition(new CommonExpressionSegment(0, 0, "t_order.customer_id=t_customer.customer_id"));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        Optional<Projection> actual = new ProjectionEngine(
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(table, shorthandProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        Map<String, ColumnProjection> actualColumns = ((ShorthandProjection) actual.get()).getActualColumns();
        assertThat(actualColumns.size(), is(3));
        assertThat(actualColumns.get("t_order.order_id"), is(new ColumnProjection("t_order", "order_id", null)));
        assertThat(actualColumns.get("t_order.customer_id"), is(new ColumnProjection("t_order", "customer_id", null)));
        assertThat(actualColumns.get("t_customer.customer_id"), is(new ColumnProjection("t_customer", "customer_id", null)));
    }
    
    @Test(expected = SchemaNotFoundException.class)
    public void assertCreateProjectionWithNotExistedSchema() {
        SimpleTableSegment tableSegment = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        OwnerSegment ownerSegment = mock(OwnerSegment.class, RETURNS_DEEP_STUBS);
        when(tableSegment.getOwner()).thenReturn(Optional.of(ownerSegment));
        when(ownerSegment.getIdentifier().getValue()).thenReturn("public");
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        new ProjectionEngine(DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(tableSegment, shorthandProjectionSegment);
    }

    @Test
    public void assertResultSetColumnsWithColumnProjectionAndExpressionProjectionOfShorthandProjection() {
        ProjectionsSegment subQuerySegment = new ProjectionsSegment(0, 0);
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("name"));
        subQuerySegment.getProjections().add(new ColumnProjectionSegment(columnSegment));
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 0, "nvl(leave_date, '20991231')");
        expressionProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("leave_date")));
        subQuerySegment.getProjections().add(expressionProjectionSegment);
        OracleSelectStatement subSelectStatement = new OracleSelectStatement();
        subSelectStatement.setProjections(subQuerySegment);
        subSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("staff_info"))));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(new SubquerySegment(0, 0, subSelectStatement));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType)
                .createProjection(subqueryTableSegment, shorthandProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(1));
        assertThat(((ShorthandProjection) actual.get()).getResultSetColumns().size(), is(2));
        Map<String, ColumnProjection> actualColumns = new LinkedHashMap<>();
        actualColumns.put("name", new ColumnProjection(null, "name", null));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(actualColumns));
        Map<String, Projection> resultSetColumns = new LinkedHashMap<>();
        resultSetColumns.put("name", new ColumnProjection(null, "name", null));
        resultSetColumns.put("nvl(leave_date, '20991231')", new ExpressionProjection("nvl(leave_date, '20991231')", "leave_date"));
        assertThat(((ShorthandProjection) actual.get()).getResultSetColumns(), is(resultSetColumns));
    }
}
