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

package org.apache.shardingsphere.infra.binder.segment.select.projection.impl;

import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AggregationProjectionTest {
    
    @Test
    void assertGetExpression() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, mock(DatabaseType.class));
        assertThat(projection.getColumnName(), is("COUNT( A.\"DIRECTION\" )"));
    }
    
    @Test
    void assertGetAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0"), mock(DatabaseType.class));
        Optional<IdentifierValue> actual = projection.getAlias();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getValue(), is("AVG_DERIVED_COUNT_0"));
    }
    
    @Test
    void assertGetColumnLabelWithAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0"), mock(DatabaseType.class));
        assertThat(projection.getColumnLabel(), is("AVG_DERIVED_COUNT_0"));
    }
    
    @Test
    void assertGetColumnLabelWithoutAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, mock(DatabaseType.class));
        assertThat(projection.getColumnLabel(), is("COUNT( A.\"DIRECTION\" )"));
    }
    
    @Test
    void assertGetColumnLabelWithoutAliasForPostgreSQL() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        assertThat(projection.getColumnLabel(), is("count"));
    }
    
    @Test
    void assertGetColumnLabelWithoutAliasForOpenGauss() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, TypedSPILoader.getService(DatabaseType.class, "openGauss"));
        assertThat(projection.getColumnLabel(), is("count"));
    }
}
