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

package org.apache.shardingsphere.sharding.merge.dql.pagination.builder.dialect;

import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.decorator.DecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.LimitDecoratorMergedResult;
import org.apache.shardingsphere.sharding.merge.dql.pagination.TopAndRowNumberDecoratorMergedResult;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class SQLServerPaginationDecoratorMergedResultBuilderTest {
    
    @Test
    void assertBuildWithLimitValueSegment() throws SQLException {
        SQLServerPaginationDecoratorMergedResultBuilder builder = new SQLServerPaginationDecoratorMergedResultBuilder();
        PaginationContext paginationContext = new PaginationContext(
                new NumberLiteralLimitValueSegment(0, 0, 10L), new NumberLiteralLimitValueSegment(0, 0, 10L), Collections.emptyList());
        DecoratorMergedResult actual = builder.build(new StubMergedResult(), paginationContext);
        assertThat(actual, isA(LimitDecoratorMergedResult.class));
    }
    
    @Test
    void assertBuildWithRowNumberValueSegment() throws SQLException {
        SQLServerPaginationDecoratorMergedResultBuilder builder = new SQLServerPaginationDecoratorMergedResultBuilder();
        PaginationContext paginationContext = new PaginationContext(
                new NumberLiteralRowNumberValueSegment(0, 0, 10L, true), new NumberLiteralRowNumberValueSegment(0, 0, 10L, true), Collections.emptyList());
        DecoratorMergedResult actual = builder.build(new StubMergedResult(), paginationContext);
        assertThat(actual, isA(TopAndRowNumberDecoratorMergedResult.class));
    }
    
    private static final class StubMergedResult implements MergedResult {
        
        @Override
        public boolean next() {
            return false;
        }
        
        @Override
        public Object getValue(final int columnIndex, final Class<?> type) {
            return null;
        }
        
        @Override
        public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
            return null;
        }
        
        @Override
        public InputStream getInputStream(final int columnIndex, final String type) {
            return null;
        }
        
        @Override
        public Reader getCharacterStream(final int columnIndex) {
            return null;
        }
        
        @Override
        public boolean wasNull() {
            return false;
        }
    }
}
