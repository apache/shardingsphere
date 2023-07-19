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
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.JoinType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectionEngineTest {
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private DatabaseType databaseType;
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentNotMatched() {
        assertFalse(new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), null).isPresent());
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfShorthandProjectionSegment() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        shorthandProjectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("tbl")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), shorthandProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfShorthandProjectionSegmentAndDuplicateTableSegment() {
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "content"));
        when(databaseType.getQuoteCharacter()).thenReturn(QuoteCharacter.NONE);
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(table, new ShorthandProjectionSegment(0, 0));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getColumnProjections().size(), is(2));
        Collection<ColumnProjection> columnProjections = new LinkedList<>();
        columnProjections.add(new ColumnProjection("t_order", "order_id", null));
        columnProjections.add(new ColumnProjection("t_order", "content", null));
        assertThat(((ShorthandProjection) actual.get()).getColumnProjections(), is(columnProjections));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfColumnProjectionSegment() {
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(new ColumnSegment(0, 10, new IdentifierValue("name")));
        columnProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), columnProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ColumnProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfExpressionProjectionSegment() {
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 10, "text");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), expressionProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ExpressionProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationDistinctProjectionSegment() {
        AggregationDistinctProjectionSegment aggregationDistinctProjectionSegment = new AggregationDistinctProjectionSegment(0, 10, AggregationType.COUNT, "(1)", "distinctExpression");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationDistinctProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationDistinctProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationProjectionSegment() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 10, AggregationType.COUNT, "(1)");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationDistinctProjectionSegmentAndAggregationTypeIsAvg() {
        AggregationDistinctProjectionSegment aggregationDistinctProjectionSegment = new AggregationDistinctProjectionSegment(0, 10, AggregationType.AVG, "(1)", "distinctExpression");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationDistinctProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationDistinctProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationProjectionSegmentAndAggregationTypeIsAvg() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 10, AggregationType.AVG, "(1)");
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), aggregationProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(AggregationProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfParameterMarkerExpressionSegment() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(7, 7, 0);
        parameterMarkerExpressionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(mock(TableSegment.class), parameterMarkerExpressionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ParameterMarkerProjection.class));
        assertThat(actual.get().getAlias().map(IdentifierValue::getValue).orElse(null), is("alias"));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfShorthandProjectionSegmentAndJoinTableSegment() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "customer_id"));
        when(databaseType.getQuoteCharacter()).thenReturn(QuoteCharacter.NONE);
        SimpleTableSegment customersTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_customer")));
        when(schema.getVisibleColumnNames("t_customer")).thenReturn(Collections.singletonList("customer_id"));
        JoinTableSegment table = new JoinTableSegment();
        SimpleTableSegment ordersTableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        table.setLeft(ordersTableSegment);
        table.setRight(customersTableSegment);
        table.setCondition(new CommonExpressionSegment(0, 0, "t_order.customer_id=t_customer.customer_id"));
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 10);
        Optional<Projection> actual = new ProjectionEngine(
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(table, shorthandProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        Collection<ColumnProjection> columnProjections = ((ShorthandProjection) actual.get()).getColumnProjections();
        assertThat(columnProjections.size(), is(3));
        Iterator<ColumnProjection> iterator = columnProjections.iterator();
        assertThat(iterator.next(), is(new ColumnProjection("t_order", "order_id", null)));
        assertThat(iterator.next(), is(new ColumnProjection("t_order", "customer_id", null)));
        assertThat(iterator.next(), is(new ColumnProjection("t_customer", "customer_id", null)));
    }
    
    @Test
    void assertCreateProjectionWithNotExistedSchema() {
        SimpleTableSegment tableSegment = mock(SimpleTableSegment.class, RETURNS_DEEP_STUBS);
        OwnerSegment ownerSegment = mock(OwnerSegment.class, RETURNS_DEEP_STUBS);
        when(tableSegment.getOwner()).thenReturn(Optional.of(ownerSegment));
        when(ownerSegment.getIdentifier().getValue()).thenReturn("public");
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        assertThrows(SchemaNotFoundException.class, () -> new ProjectionEngine(
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema), databaseType).createProjection(tableSegment, shorthandProjectionSegment));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsColumnProjectionAndExpressionProjection() {
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
        assertThat(((ShorthandProjection) actual.get()).getColumnProjections().size(), is(2));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(2));
        Collection<ColumnProjection> columnProjections = new LinkedList<>();
        columnProjections.add(new ColumnProjection(null, "name", null));
        columnProjections.add(new ColumnProjection(null, "leave_date", null));
        assertThat(((ShorthandProjection) actual.get()).getColumnProjections(), is(columnProjections));
        Collection<Projection> expectedColumnProjections = new LinkedHashSet<>();
        expectedColumnProjections.add(new ColumnProjection(null, "name", null));
        expectedColumnProjections.add(new ColumnProjection(null, "leave_date", null));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(expectedColumnProjections));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsJoinUsingColumnForPostgreSQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        when(schema.getVisibleColumnNames("t_order_item")).thenReturn(Arrays.asList("item_id", "order_id", "user_id", "product_id", "quantity", "creation_date"));
        Optional<Projection> actual = new ProjectionEngine("public", Collections.singletonMap("public", schema), TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))
                .createProjection(createJoinTableSegmentWithUsingColumn(), new ShorthandProjectionSegment(0, 0));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(9));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithoutOwnerForPostgreSQL()));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsJoinUsingColumnForMySQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        when(schema.getVisibleColumnNames("t_order_item")).thenReturn(Arrays.asList("item_id", "order_id", "user_id", "product_id", "quantity", "creation_date"));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema),
                TypedSPILoader.getService(DatabaseType.class, "MySQL")).createProjection(createJoinTableSegmentWithUsingColumn(), new ShorthandProjectionSegment(0, 0));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(9));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithoutOwnerForMySQL()));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsJoinUsingColumnAndOwnerForPostgreSQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("o")));
        Optional<Projection> actual =
                new ProjectionEngine("public", Collections.singletonMap("public", schema),
                        TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")).createProjection(createJoinTableSegmentWithUsingColumn(), projectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(6));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithOwner(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsJoinUsingColumnAndOwnerForMySQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("o")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema),
                TypedSPILoader.getService(DatabaseType.class, "MySQL")).createProjection(createJoinTableSegmentWithUsingColumn(), projectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(6));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithOwner(TypedSPILoader.getService(DatabaseType.class, "MySQL"))));
    }
    
    private JoinTableSegment createJoinTableSegmentWithUsingColumn() {
        SimpleTableSegment left = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        left.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment right = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        right.setAlias(new AliasSegment(0, 0, new IdentifierValue("i")));
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(left);
        result.setRight(right);
        result.setJoinType(JoinType.RIGHT.name());
        result.setUsing(Arrays.asList(new ColumnSegment(0, 0, new IdentifierValue("user_id")), new ColumnSegment(0, 0, new IdentifierValue("order_id")),
                new ColumnSegment(0, 0, new IdentifierValue("creation_date"))));
        return result;
    }
    
    private Collection<Projection> crateExpectedColumnsWithoutOwnerForPostgreSQL() {
        Collection<Projection> result = new LinkedHashSet<>();
        DatabaseType postgresDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("user_id", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("order_id", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("creation_date", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("status", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("merchant_id", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("remark", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("item_id", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("product_id", postgresDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("quantity", postgresDatabaseType.getQuoteCharacter()), null));
        return result;
    }
    
    private Collection<Projection> crateExpectedColumnsWithoutOwnerForMySQL() {
        Collection<Projection> result = new LinkedHashSet<>();
        DatabaseType mysqlDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("order_id", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("user_id", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("creation_date", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("item_id", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("product_id", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("i"), new IdentifierValue("quantity", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("status", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("merchant_id", mysqlDatabaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("remark", mysqlDatabaseType.getQuoteCharacter()), null));
        return result;
    }
    
    private Collection<Projection> crateExpectedColumnsWithOwner(final DatabaseType databaseType) {
        Collection<Projection> result = new LinkedHashSet<>();
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("order_id", databaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("user_id", databaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("status", databaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("merchant_id", databaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("remark", databaseType.getQuoteCharacter()), null));
        result.add(new ColumnProjection(new IdentifierValue("o"), new IdentifierValue("creation_date", databaseType.getQuoteCharacter()), null));
        return result;
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsNaturalJoinForPostgreSQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        when(schema.getVisibleColumnNames("t_order_item")).thenReturn(Arrays.asList("item_id", "order_id", "user_id", "product_id", "quantity", "creation_date"));
        Optional<Projection> actual = new ProjectionEngine("public", Collections.singletonMap("public", schema), TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))
                .createProjection(createJoinTableSegmentWithNaturalJoin(), new ShorthandProjectionSegment(0, 0));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(9));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithoutOwnerForPostgreSQL()));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsNaturalJoinForMySQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        when(schema.getVisibleColumnNames("t_order_item")).thenReturn(Arrays.asList("item_id", "order_id", "user_id", "product_id", "quantity", "creation_date"));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema),
                TypedSPILoader.getService(DatabaseType.class, "MySQL")).createProjection(createJoinTableSegmentWithNaturalJoin(), new ShorthandProjectionSegment(0, 0));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(9));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithoutOwnerForMySQL()));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsNaturalJoinAndOwnerForPostgreSQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("o")));
        Optional<Projection> actual =
                new ProjectionEngine("public", Collections.singletonMap("public", schema),
                        TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")).createProjection(createJoinTableSegmentWithNaturalJoin(), projectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(6));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithOwner(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"))));
    }
    
    @Test
    void assertCreateProjectionWhenShorthandProjectionContainsNaturalJoinAndOwnerForMySQL() {
        when(schema.getVisibleColumnNames("t_order")).thenReturn(Arrays.asList("order_id", "user_id", "status", "merchant_id", "remark", "creation_date"));
        ShorthandProjectionSegment projectionSegment = new ShorthandProjectionSegment(0, 0);
        projectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("o")));
        Optional<Projection> actual = new ProjectionEngine(DefaultDatabase.LOGIC_NAME, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, schema),
                TypedSPILoader.getService(DatabaseType.class, "MySQL")).createProjection(createJoinTableSegmentWithNaturalJoin(), projectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShorthandProjection.class));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns().size(), is(6));
        assertThat(((ShorthandProjection) actual.get()).getActualColumns(), is(crateExpectedColumnsWithOwner(TypedSPILoader.getService(DatabaseType.class, "MySQL"))));
    }
    
    private JoinTableSegment createJoinTableSegmentWithNaturalJoin() {
        SimpleTableSegment left = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order")));
        left.setAlias(new AliasSegment(0, 0, new IdentifierValue("o")));
        SimpleTableSegment right = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        right.setAlias(new AliasSegment(0, 0, new IdentifierValue("i")));
        JoinTableSegment result = new JoinTableSegment();
        result.setLeft(left);
        result.setRight(right);
        result.setNatural(true);
        result.setJoinType(JoinType.RIGHT.name());
        return result;
    }
}
