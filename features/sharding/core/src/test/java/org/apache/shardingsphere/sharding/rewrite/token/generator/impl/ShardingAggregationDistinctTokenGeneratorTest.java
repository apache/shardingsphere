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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingAggregationDistinctTokenGeneratorTest {
    
    private final ShardingAggregationDistinctTokenGenerator generator = new ShardingAggregationDistinctTokenGenerator();
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotSelectStatementContext() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyAggregationDistinctProjection() {
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getProjectionsContext().getAggregationDistinctProjections().isEmpty()).thenReturn(true);
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLTokenWithAggregationDistinctProjections() {
        assertTrue(generator.isGenerateSQLToken(mock(SelectStatementContext.class, RETURNS_DEEP_STUBS)));
    }
    
    @ParameterizedTest(name = "{0}, explicit={1}")
    @CsvSource({"AVG_DERIVED_COUNT_0,false,user_id AS AVG_DERIVED_COUNT_0", "AGGREGATION_DISTINCT_DERIVED_0,true,user_id", "foo_alias,true,user_id"})
    void assertGenerateSQLToken(final String alias, final boolean explicit, final String expectedToken) {
        IdentifierValue aliasValue = new IdentifierValue(alias);
        AggregationProjectionSegment segment = new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(DISTINCT user_id)");
        if (explicit) {
            segment.setAlias(new AliasSegment(0, 0, aliasValue));
        }
        AggregationDistinctProjection projection = new AggregationDistinctProjection(0, 0, AggregationType.COUNT, segment, aliasValue, "user_id", mock(DatabaseType.class));
        SelectStatementContext context = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(context.getProjectionsContext().getAggregationDistinctProjections()).thenReturn(Collections.singleton(projection));
        List<SQLToken> actual = new ArrayList<>(generator.generateSQLTokens(context));
        assertThat(actual.get(0).toString(), is(expectedToken));
    }
}
