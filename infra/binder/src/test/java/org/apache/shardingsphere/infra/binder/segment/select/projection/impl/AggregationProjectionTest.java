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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class AggregationProjectionTest {
    
    @Test
    public void assertGetExpression() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, mock(DatabaseType.class));
        assertThat(projection.getExpression(), is("COUNT( A.\"DIRECTION\" )"));
    }
    
    @Test
    public void assertGetAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", "AVG_DERIVED_COUNT_0", mock(DatabaseType.class));
        Optional<String> actual = projection.getAlias();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("AVG_DERIVED_COUNT_0"));
    }
    
    @Test
    public void assertGetColumnLabelWithAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", "AVG_DERIVED_COUNT_0", mock(DatabaseType.class));
        assertThat(projection.getColumnLabel(), is("AVG_DERIVED_COUNT_0"));
    }
    
    @Test
    public void assertGetColumnLabelWithoutAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, mock(DatabaseType.class));
        assertThat(projection.getColumnLabel(), is("COUNT( A.\"DIRECTION\" )"));
    }
    
    @Test
    public void assertGetColumnLabelWithoutAliasForPostgreSQL() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, mock(PostgreSQLDatabaseType.class));
        assertThat(projection.getColumnLabel(), is("count"));
    }
    
    @Test
    public void assertGetColumnLabelWithoutAliasForOpenGauss() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, mock(OpenGaussDatabaseType.class));
        assertThat(projection.getColumnLabel(), is("count"));
    }
}
