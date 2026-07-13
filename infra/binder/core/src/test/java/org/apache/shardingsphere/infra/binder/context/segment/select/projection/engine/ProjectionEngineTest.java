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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectionEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentNotMatched() {
        assertFalse(new ProjectionEngine(databaseType).createProjection(null, new LinkedHashMap<>()).isPresent());
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfShorthandProjectionSegment() {
        ShorthandProjectionSegment shorthandProjectionSegment = new ShorthandProjectionSegment(0, 0);
        shorthandProjectionSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("tbl")));
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(shorthandProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ShorthandProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfColumnProjectionSegment() {
        ColumnProjectionSegment columnProjectionSegment = new ColumnProjectionSegment(new ColumnSegment(0, 10, new IdentifierValue("name")));
        columnProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(columnProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ColumnProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfExpressionProjectionSegment() {
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 10, "text");
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(expressionProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ExpressionProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationDistinctProjectionSegment() {
        AggregationDistinctProjectionSegment aggregationDistinctProjectionSegment = new AggregationDistinctProjectionSegment(0, 10, AggregationType.COUNT, "(1)", "distinctExpression");
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(aggregationDistinctProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(AggregationDistinctProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationProjectionSegment() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 10, AggregationType.COUNT, "COUNT(1)");
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(aggregationProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(AggregationProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationDistinctProjectionSegmentAndAggregationTypeIsAvg() {
        AggregationDistinctProjectionSegment aggregationDistinctProjectionSegment = new AggregationDistinctProjectionSegment(0, 10, AggregationType.AVG, "(1)", "distinctExpression");
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(aggregationDistinctProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(AggregationDistinctProjection.class));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfAggregationProjectionSegmentAndAggregationTypeIsAvg() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 10, AggregationType.AVG, "AVG(1)");
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(aggregationProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(AggregationProjection.class));
    }
    
    @Test
    void assertCreateExpressionProjectionWhenAggregationProjectionSegmentContainsWindow() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 25, AggregationType.MAX, "MAX(id) OVER ()");
        aggregationProjectionSegment.setWindow(new WindowItemSegment(8, 25));
        aggregationProjectionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("max_id")));
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(aggregationProjectionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ExpressionProjection.class));
        assertThat(Objects.requireNonNull(actual.get().getAlias().map(IdentifierValue::getValue).orElse(null)), is("max_id"));
    }
    
    @Test
    void assertCreateProjectionWhenProjectionSegmentInstanceOfParameterMarkerExpressionSegment() {
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment = new ParameterMarkerExpressionSegment(7, 7, 0);
        parameterMarkerExpressionSegment.setAlias(new AliasSegment(0, 0, new IdentifierValue("alias")));
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(parameterMarkerExpressionSegment, new LinkedHashMap<>());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ParameterMarkerProjection.class));
        assertThat(Objects.requireNonNull(actual.get().getAlias().map(IdentifierValue::getValue).orElse(null)), is("alias"));
    }
    
    @Test
    void assertCreateProjectionWithDuplicateExpressionAggregations() {
        FunctionSegment functionSegment = new FunctionSegment(0, 50, "IFNULL", "IFNULL(SUM(price), SUM(price))");
        AggregationProjectionSegment sumSegment1 = new AggregationProjectionSegment(7, 16, AggregationType.SUM, "SUM(price)");
        AggregationProjectionSegment sumSegment2 = new AggregationProjectionSegment(19, 28, AggregationType.SUM, "SUM(price)");
        functionSegment.getParameters().add(sumSegment1);
        functionSegment.getParameters().add(sumSegment2);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 50, "IFNULL(SUM(price), SUM(price))", functionSegment);
        
        ProjectionEngine engine = new ProjectionEngine(databaseType);
        Map<ExpressionProjection, List<AggregationProjection>> derived = new LinkedHashMap<>();
        Optional<Projection> actual = engine.createProjection(expressionSegment, derived);
        
        assertTrue(actual.isPresent());
        assertThat(derived.size(), is(1));
        assertThat(derived.values().iterator().next().size(), is(1));
    }
    
    @Test
    void assertCreateProjectionWithUnsupportedExpression() {
        AggregationProjectionSegment sumSegment = new AggregationProjectionSegment(0, 9, AggregationType.SUM, "SUM(price)");
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(13, 14, 1);
        BinaryOperationExpression binaryExpr = new BinaryOperationExpression(0, 14, sumSegment, literalSegment, "+", "SUM(price) + 1");
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 14, "SUM(price) + 1", binaryExpr);
        
        ProjectionEngine engine = new ProjectionEngine(databaseType);
        Map<ExpressionProjection, List<AggregationProjection>> derived = new LinkedHashMap<>();
        Optional<Projection> actual = engine.createProjection(expressionSegment, derived);
        
        assertTrue(actual.isPresent());
        assertTrue(derived.isEmpty());
    }
    
    @Test
    void assertCreateProjectionWithWindowedAggregationInExpression() {
        FunctionSegment functionSegment = new FunctionSegment(0, 31, "IFNULL", "IFNULL(MAX(id) OVER (), 0)");
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(7, 21, AggregationType.MAX, "MAX(id) OVER ()");
        aggregationProjectionSegment.setWindow(new WindowItemSegment(15, 21));
        LiteralExpressionSegment literalSegment = new LiteralExpressionSegment(24, 24, 0);
        functionSegment.getParameters().add(aggregationProjectionSegment);
        functionSegment.getParameters().add(literalSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 31, "IFNULL(MAX(id) OVER (), 0)", functionSegment);
        
        ProjectionEngine engine = new ProjectionEngine(databaseType);
        Map<ExpressionProjection, List<AggregationProjection>> derived = new LinkedHashMap<>();
        Optional<Projection> actual = engine.createProjection(expressionSegment, derived);
        
        assertTrue(actual.isPresent());
        assertTrue(derived.isEmpty());
    }
    
    @Test
    void assertCreateProjectionWithColumnFallbackInExpression() {
        FunctionSegment functionSegment = new FunctionSegment(0, 30, "IFNULL", "IFNULL(SUM(price), column_a)");
        AggregationProjectionSegment sumSegment = new AggregationProjectionSegment(7, 16, AggregationType.SUM, "SUM(price)");
        ColumnSegment columnSegment = new ColumnSegment(19, 26, new IdentifierValue("column_a"));
        functionSegment.getParameters().add(sumSegment);
        functionSegment.getParameters().add(columnSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 30, "IFNULL(SUM(price), column_a)", functionSegment);
        
        ProjectionEngine engine = new ProjectionEngine(databaseType);
        Map<ExpressionProjection, List<AggregationProjection>> derived = new LinkedHashMap<>();
        Optional<Projection> actual = engine.createProjection(expressionSegment, derived);
        
        assertTrue(actual.isPresent());
        assertTrue(derived.isEmpty());
    }
    
    @Test
    void assertCreateProjectionWithParameterMarkerFallbackInExpression() {
        FunctionSegment functionSegment = new FunctionSegment(0, 23, "IFNULL", "IFNULL(SUM(price), ?)");
        AggregationProjectionSegment sumSegment = new AggregationProjectionSegment(7, 16, AggregationType.SUM, "SUM(price)");
        ParameterMarkerExpressionSegment paramSegment = new ParameterMarkerExpressionSegment(19, 19, 0);
        functionSegment.getParameters().add(sumSegment);
        functionSegment.getParameters().add(paramSegment);
        
        ExpressionProjectionSegment expressionSegment = new ExpressionProjectionSegment(0, 23, "IFNULL(SUM(price), ?)", functionSegment);
        
        ProjectionEngine engine = new ProjectionEngine(databaseType);
        Map<ExpressionProjection, List<AggregationProjection>> derived = new LinkedHashMap<>();
        Optional<Projection> actual = engine.createProjection(expressionSegment, derived);
        
        assertTrue(actual.isPresent());
        assertTrue(derived.isEmpty());
    }
}
