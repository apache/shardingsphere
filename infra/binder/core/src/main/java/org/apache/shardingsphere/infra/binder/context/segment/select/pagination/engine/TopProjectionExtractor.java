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

package org.apache.shardingsphere.infra.binder.context.segment.select.pagination.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Top projection extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopProjectionExtractor {
    
    /**
     * Find top projection.
     *
     * @param selectStatement select statement
     * @return top projection
     */
    public static Optional<TopProjectionSegment> findTopProjection(final SelectStatement selectStatement) {
        for (ProjectionSegment each : selectStatement.getProjections().getProjections()) {
            if (each instanceof SubqueryProjectionSegment) {
                return findTopProjection0(((SubqueryProjectionSegment) each).getSubquery().getSelect());
            }
        }
        return findTopProjection0(selectStatement);
    }
    
    private static Optional<TopProjectionSegment> findTopProjection0(final SelectStatement selectStatement) {
        for (ProjectionSegment each : selectStatement.getProjections().getProjections()) {
            if (each instanceof TopProjectionSegment) {
                return Optional.of((TopProjectionSegment) each);
            }
        }
        List<SubqueryTableSegment> subqueryTableSegments = selectStatement.getFrom().map(SQLUtils::getSubqueryTableSegmentFromTableSegment).orElse(Collections.emptyList());
        for (SubqueryTableSegment subquery : subqueryTableSegments) {
            SelectStatement subquerySelect = subquery.getSubquery().getSelect();
            for (ProjectionSegment each : subquerySelect.getProjections().getProjections()) {
                if (each instanceof TopProjectionSegment) {
                    return Optional.of((TopProjectionSegment) each);
                }
            }
        }
        return Optional.empty();
    }
}
