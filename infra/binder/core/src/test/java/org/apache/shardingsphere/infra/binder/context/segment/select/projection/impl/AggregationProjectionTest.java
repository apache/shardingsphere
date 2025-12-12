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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregationProjectionTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetColumnName() {
        assertThat(new AggregationProjection(AggregationType.COUNT, new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT( A.\"DIRECTION\" )"), null, databaseType).getColumnName(),
                is("COUNT( A.\"DIRECTION\" )"));
    }
    
    @Test
    void assertGetColumnLabelWithAliasNoQuote() {
        assertThat(new AggregationProjection(AggregationType.COUNT,
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT( A.\"DIRECTION\" )"), new IdentifierValue("DIRECTION_COUNT"), databaseType).getColumnLabel(),
                is("DIRECTION_COUNT"));
    }
    
    @Test
    void assertGetColumnLabelWithAliasAndQuote() {
        assertThat(new AggregationProjection(AggregationType.COUNT, new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT( A.\"DIRECTION\" )"),
                new IdentifierValue("DIRECTION_COUNT", QuoteCharacter.BACK_QUOTE), databaseType).getColumnLabel(), is("DIRECTION_COUNT"));
    }
    
    @Test
    void assertGetColumnLabelWithoutAlias() {
        assertThat(new AggregationProjection(AggregationType.COUNT, new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT( A.\"DIRECTION\" )"), null, databaseType).getColumnLabel(),
                is("COUNT( A.\"DIRECTION\" )"));
    }
    
    @Test
    void assertGetAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT,
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT( A.\"DIRECTION\" )"), new IdentifierValue("AVG_DERIVED_COUNT_0"), databaseType);
        Optional<IdentifierValue> actual = projection.getAlias();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getValue(), is("AVG_DERIVED_COUNT_0"));
        assertThat(actual.get().getQuoteCharacter(), is(QuoteCharacter.NONE));
    }
}
