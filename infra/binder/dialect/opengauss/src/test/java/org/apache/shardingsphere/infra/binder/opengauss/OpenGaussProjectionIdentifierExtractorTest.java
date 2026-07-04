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

package org.apache.shardingsphere.infra.binder.opengauss;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine.ProjectionEngine;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.DialectProjectionIdentifierExtractor;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussProjectionIdentifierExtractorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DialectProjectionIdentifierExtractor extractor = DatabaseTypedSPILoader.getService(DialectProjectionIdentifierExtractor.class, databaseType);
    
    @Test
    void assertGetIdentifierValue() {
        assertThat(extractor.getIdentifierValue(new IdentifierValue("Data", QuoteCharacter.NONE)), is("data"));
    }
    
    @Test
    void assertGetColumnNameFromFunction() {
        assertThat(extractor.getColumnNameFromFunction("Function", "FunctionExpression"), is("function"));
    }
    
    @Test
    void assertGetColumnNameFromExpression() {
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "expression")), is("?column?"));
    }
    
    @Test
    void assertGetColumnNameFromFunctionExpression() {
        assertThat(extractor.getColumnNameFromExpression(new ExpressionProjectionSegment(0, 0, "SUM(ID)", new FunctionSegment(0, 0, "SUM", "SUM(ID)"))), is("SUM"));
    }
    
    @Test
    void assertGetColumnLabelFromWindowAggregationProjection() {
        AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(0, 0, AggregationType.MAX, "pg_catalog.max(ref_0.c36) over ()");
        aggregationProjectionSegment.setWindow(new WindowItemSegment(19, 27));
        Optional<Projection> actual = new ProjectionEngine(databaseType).createProjection(aggregationProjectionSegment);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ExpressionProjection.class));
        assertThat(actual.get().getColumnLabel(), is("max"));
    }
    
    @Test
    void assertGetColumnNameFromSubquery() {
        assertThat(extractor.getColumnNameFromSubquery(new SubqueryProjectionSegment(new SubquerySegment(0, 0, "text"), "text")), is("text"));
    }
}
