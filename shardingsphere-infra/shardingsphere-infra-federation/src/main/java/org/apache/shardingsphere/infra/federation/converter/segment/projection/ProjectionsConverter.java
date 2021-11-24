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

package org.apache.shardingsphere.infra.federation.converter.segment.projection;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.converter.segment.projection.impl.AggregationProjectionConverter;
import org.apache.shardingsphere.infra.federation.converter.segment.projection.impl.ColumnProjectionConverter;
import org.apache.shardingsphere.infra.federation.converter.segment.projection.impl.ExpressionProjectionConverter;
import org.apache.shardingsphere.infra.federation.converter.segment.projection.impl.ShorthandProjectionConverter;
import org.apache.shardingsphere.infra.federation.converter.segment.projection.impl.SubqueryProjectionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Projection converter.
 */
public final class ProjectionsConverter implements SQLSegmentConverter<ProjectionsSegment, SqlNodeList> {
    
    @Override
    public Optional<SqlNodeList> convertToSQLNode(final ProjectionsSegment segment) {
        Collection<SqlNode> projectionSQLNodes = new ArrayList<>(segment.getProjections().size());
        for (ProjectionSegment each : segment.getProjections()) {
            getProjectionSQLNode(each).ifPresent(projectionSQLNodes::add);
        }
        return Optional.of(new SqlNodeList(projectionSQLNodes, SqlParserPos.ZERO));
    }
    
    private Optional<SqlNode> getProjectionSQLNode(final ProjectionSegment segment) {
        if (segment instanceof ColumnProjectionSegment) {
            return new ColumnProjectionConverter().convertToSQLNode((ColumnProjectionSegment) segment);
        } else if (segment instanceof ExpressionProjectionSegment) {
            return new ExpressionProjectionConverter().convertToSQLNode((ExpressionProjectionSegment) segment);
        } else if (segment instanceof ShorthandProjectionSegment) {
            return new ShorthandProjectionConverter().convertToSQLNode((ShorthandProjectionSegment) segment).map(optional -> optional);
        } else if (segment instanceof SubqueryProjectionSegment) {
            return new SubqueryProjectionConverter().convertToSQLNode((SubqueryProjectionSegment) segment);
        } else if (segment instanceof AggregationProjectionSegment) {
            return new AggregationProjectionConverter().convertToSQLNode((AggregationProjectionSegment) segment).map(optional -> optional);
        }
        // TODO process other projection
        return Optional.empty();
    }
    
    @Override
    public Optional<ProjectionsSegment> convertToSQLSegment(final SqlNodeList sqlNodeList) {
        List<ProjectionSegment> projections = new ArrayList<>();
        for (SqlNode each : sqlNodeList) {
            getProjectionSegment(each).ifPresent(projections::add);
        }
        int startIndex = projections.get(0).getStartIndex();
        int stopIndex = getProjectionsSegmentStopIndex(sqlNodeList.get(sqlNodeList.size() - 1), projections.get(projections.size() - 1));
        ProjectionsSegment result = new ProjectionsSegment(startIndex, stopIndex);
        result.getProjections().addAll(projections);
        return Optional.of(result);
    }
    
    private int getProjectionsSegmentStopIndex(final SqlNode lastSqlNode, final ProjectionSegment projectionSegment) {
        int stopIndex = projectionSegment.getStopIndex();
        if (lastSqlNode instanceof SqlBasicCall && SqlKind.AS == ((SqlBasicCall) lastSqlNode).getOperator().getKind()) {
            stopIndex = getStopIndex(((SqlBasicCall) lastSqlNode).getOperandList().get(1));
        }
        return stopIndex;
    }
    
    private Optional<ProjectionSegment> getProjectionSegment(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlIdentifier) {
            SqlIdentifier sqlIdentifier = (SqlIdentifier) sqlNode;
            if (SqlIdentifier.STAR.names.equals(sqlIdentifier.names)) {
                return new ShorthandProjectionConverter().convertToSQLSegment(sqlIdentifier).map(optional -> optional);    
            }
            return new ColumnProjectionConverter().convertToSQLSegment(sqlIdentifier).map(optional -> optional);
        } else if (sqlNode instanceof SqlBasicCall) {
            SqlBasicCall sqlBasicCall = (SqlBasicCall) sqlNode;
            if (AggregationType.isAggregationType(sqlBasicCall.getOperator().getName()) || AggregationProjectionConverter.isAsOperatorAggregationType(sqlBasicCall)) {
                return new AggregationProjectionConverter().convertToSQLSegment(sqlBasicCall).map(optional -> optional);
            }
            if (null != sqlBasicCall.getOperator() && SqlKind.AS == sqlBasicCall.getOperator().getKind()) {
                return new ColumnProjectionConverter().convertToSQLSegment(sqlNode).map(optional -> optional);
            }
            return new ExpressionProjectionConverter().convertToSQLSegment(sqlNode).map(optional -> optional);
        } else if (sqlNode instanceof SqlSelect || sqlNode instanceof SqlOrderBy) {
            return new SubqueryProjectionConverter().convertToSQLSegment(sqlNode).map(optional -> optional);
        }
        // TODO process other projection
        return Optional.empty();
    }
}
