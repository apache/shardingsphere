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
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.oracle.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.quote.QuoteCharacter;
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
    void assertGetColumnName() {
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, new MySQLDatabaseType()).getColumnName(), is("COUNT( A.\"DIRECTION\" )"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, new PostgreSQLDatabaseType()).getColumnName(), is("count"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, new OpenGaussDatabaseType()).getColumnName(), is("count"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( a.\"direction\" )", null, new OracleDatabaseType()).getColumnName(), is("COUNT(A.\"DIRECTION\")"));
    }
    
    @Test
    void assertGetColumnLabelWithAliasNoQuote() {
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0"), new MySQLDatabaseType()).getColumnLabel(),
                is("AVG_DERIVED_COUNT_0"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0"), new PostgreSQLDatabaseType()).getColumnLabel(),
                is("avg_derived_count_0"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0"), new OpenGaussDatabaseType()).getColumnLabel(),
                is("avg_derived_count_0"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("avg_derived_count_0"), new OracleDatabaseType()).getColumnLabel(),
                is("AVG_DERIVED_COUNT_0"));
    }
    
    @Test
    void assertGetColumnLabelWithAliasAndQuote() {
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0", QuoteCharacter.BACK_QUOTE), new MySQLDatabaseType())
                .getColumnLabel(), is("AVG_DERIVED_COUNT_0"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0", QuoteCharacter.QUOTE), new PostgreSQLDatabaseType())
                .getColumnLabel(), is("AVG_DERIVED_COUNT_0"));
        assertThat(
                new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0", QuoteCharacter.QUOTE), new OpenGaussDatabaseType()).getColumnLabel(),
                is("AVG_DERIVED_COUNT_0"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("avg_derived_count_0", QuoteCharacter.QUOTE), new OracleDatabaseType()).getColumnLabel(),
                is("avg_derived_count_0"));
    }
    
    @Test
    void assertGetColumnLabelWithoutAlias() {
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, new MySQLDatabaseType()).getColumnLabel(), is("COUNT( A.\"DIRECTION\" )"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, new PostgreSQLDatabaseType()).getColumnLabel(), is("count"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", null, new OpenGaussDatabaseType()).getColumnLabel(), is("count"));
        assertThat(new AggregationProjection(AggregationType.COUNT, "( a.\"direction\" )", null, new OracleDatabaseType()).getColumnLabel(), is("COUNT(A.\"DIRECTION\")"));
    }
    
    @Test
    void assertGetAlias() {
        Projection projection = new AggregationProjection(AggregationType.COUNT, "( A.\"DIRECTION\" )", new IdentifierValue("AVG_DERIVED_COUNT_0"), mock(DatabaseType.class));
        Optional<IdentifierValue> actual = projection.getAlias();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getValue(), is("AVG_DERIVED_COUNT_0"));
        assertThat(actual.get().getQuoteCharacter(), is(QuoteCharacter.NONE));
    }
}
