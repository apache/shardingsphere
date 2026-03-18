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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ParameterMarkerExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.AggregationProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.ColumnProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.ExpressionProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.ShorthandProjectionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl.SubqueryProjectionConverter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Projection converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectionsConverter {
    
    /**
     * Convert projections segment to SQL node list.
     *
     * @param segment projections segment
     * @return SQL node list
     */
    public static Optional<SqlNodeList> convert(final ProjectionsSegment segment) {
        Collection<SqlNode> projectionSQLNodes = new LinkedList<>();
        for (ProjectionSegment each : segment.getProjections()) {
            getProjectionSQLNode(each).ifPresent(projectionSQLNodes::add);
        }
        return Optional.of(new SqlNodeList(projectionSQLNodes, SqlParserPos.ZERO));
    }
    
    private static Optional<SqlNode> getProjectionSQLNode(final ProjectionSegment segment) {
        if (segment instanceof ColumnProjectionSegment) {
            return Optional.of(ColumnProjectionConverter.convert((ColumnProjectionSegment) segment));
        }
        if (segment instanceof ExpressionProjectionSegment) {
            return ExpressionProjectionConverter.convert((ExpressionProjectionSegment) segment);
        }
        if (segment instanceof ShorthandProjectionSegment) {
            return ShorthandProjectionConverter.convert((ShorthandProjectionSegment) segment);
        }
        if (segment instanceof SubqueryProjectionSegment) {
            return SubqueryProjectionConverter.convert((SubqueryProjectionSegment) segment);
        }
        if (segment instanceof AggregationProjectionSegment) {
            return AggregationProjectionConverter.convert((AggregationProjectionSegment) segment);
        }
        if (segment instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(ParameterMarkerExpressionConverter.convert((ParameterMarkerExpressionSegment) segment));
        }
        // TODO process other projection
        return Optional.empty();
    }
}
