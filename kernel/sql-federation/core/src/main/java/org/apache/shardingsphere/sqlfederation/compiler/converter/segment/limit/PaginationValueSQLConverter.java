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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.limit;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;

import java.util.Optional;

/**
 * Pagination value converter.
 */
@RequiredArgsConstructor
public final class PaginationValueSQLConverter implements SQLSegmentConverter<PaginationValueSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final PaginationValueSegment segment) {
        return Optional.of(segment instanceof NumberLiteralPaginationValueSegment
                ? getLiteralSQLNode((NumberLiteralPaginationValueSegment) segment)
                : getParameterMarkerSQLNode((ParameterMarkerLimitValueSegment) segment));
    }
    
    private SqlNode getLiteralSQLNode(final NumberLiteralPaginationValueSegment segment) {
        return SqlLiteral.createExactNumeric(String.valueOf(segment.getValue()), SqlParserPos.ZERO);
    }
    
    private SqlNode getParameterMarkerSQLNode(final ParameterMarkerLimitValueSegment segment) {
        return new SqlDynamicParam(segment.getParameterIndex(), SqlParserPos.ZERO);
    }
}
