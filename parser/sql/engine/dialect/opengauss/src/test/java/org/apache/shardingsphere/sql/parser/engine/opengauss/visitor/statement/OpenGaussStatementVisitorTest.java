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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussStatementVisitorTest {

    @Test
    void assertVisitLimitWithOffsetAndRowCountParameterMarkers() {
        String sql = "SELECT * FROM t_order ORDER BY order_id LIMIT ?, ?";
        SelectStatement statement = parseSelect(sql);
        LimitSegment limit = statement.getLimit().orElseThrow(AssertionError::new);
        Optional<PaginationValueSegment> offset = limit.getOffset();
        Optional<PaginationValueSegment> rowCount = limit.getRowCount();
        assertTrue(offset.isPresent());
        assertTrue(rowCount.isPresent());
        assertThat(offset.get(), instanceOf(ParameterMarkerLimitValueSegment.class));
        assertThat(rowCount.get(), instanceOf(ParameterMarkerLimitValueSegment.class));
        assertThat(((ParameterMarkerLimitValueSegment) offset.get()).getParameterIndex(), is(0));
        assertThat(((ParameterMarkerLimitValueSegment) rowCount.get()).getParameterIndex(), is(1));
    }

    @Test
    void assertVisitLimitWithLiteralOffsetAndRowCount() {
        String sql = "SELECT * FROM t_order LIMIT 1, 2";
        SelectStatement statement = parseSelect(sql);
        LimitSegment limit = statement.getLimit().orElseThrow(AssertionError::new);
        Optional<PaginationValueSegment> offset = limit.getOffset();
        Optional<PaginationValueSegment> rowCount = limit.getRowCount();
        assertTrue(offset.isPresent());
        assertTrue(rowCount.isPresent());
        assertThat(((NumberLiteralLimitValueSegment) offset.get()).getValue(), is(1L));
        assertThat(((NumberLiteralLimitValueSegment) rowCount.get()).getValue(), is(2L));
    }

    @Test
    void assertVisitLimitWithOnlyRowCountParameterMarker() {
        String sql = "SELECT * FROM t_order LIMIT ?";
        SelectStatement statement = parseSelect(sql);
        LimitSegment limit = statement.getLimit().orElseThrow(AssertionError::new);
        assertTrue(limit.getRowCount().isPresent());
        assertThat(limit.getOffset().isPresent(), is(false));
        assertThat(((ParameterMarkerLimitValueSegment) limit.getRowCount().get()).getParameterIndex(), is(0));
    }

    @Test
    void assertVisitOffsetAndLimitWithParameterMarkers() {
        String sql = "SELECT * FROM t_order ORDER BY order_id OFFSET ? LIMIT ?";
        SelectStatement statement = parseSelect(sql);
        LimitSegment limit = statement.getLimit().orElseThrow(AssertionError::new);
        Optional<PaginationValueSegment> offset = limit.getOffset();
        Optional<PaginationValueSegment> rowCount = limit.getRowCount();
        assertTrue(offset.isPresent());
        assertTrue(rowCount.isPresent());
        assertThat(((ParameterMarkerLimitValueSegment) offset.get()).getParameterIndex(), is(0));
        assertThat(((ParameterMarkerLimitValueSegment) rowCount.get()).getParameterIndex(), is(1));
    }

    private SelectStatement parseSelect(final String sql) {
        ParseASTNode parseASTNode = new SQLParserEngine("openGauss", new CacheOption(128, 1024L)).parse(sql, false);
        return (SelectStatement) new SQLStatementVisitorEngine("openGauss").visit(parseASTNode);
    }
}
