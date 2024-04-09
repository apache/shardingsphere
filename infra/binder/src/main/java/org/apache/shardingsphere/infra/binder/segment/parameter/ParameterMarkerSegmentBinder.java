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

package org.apache.shardingsphere.infra.binder.segment.parameter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.parameter.impl.ParameterMarkerExpressionSegmentBinder;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ParameterMarkerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.bounded.ColumnSegmentBoundedInfo;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Parameter marker segment binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterMarkerSegmentBinder {
    
    /**
     * Bind parameter marker segment with metadata.
     * 
     * @param parameterMarkerSegments parameter marker segments
     * @param parameterMarkerSegmentBoundedInfos parameter marker segment bounded infos
     * @return bounded parameter marker segment
     */
    public static Collection<ParameterMarkerSegment> bind(final Collection<ParameterMarkerSegment> parameterMarkerSegments,
                                                          final Map<ParameterMarkerSegment, ColumnSegmentBoundedInfo> parameterMarkerSegmentBoundedInfos) {
        Collection<ParameterMarkerSegment> result = new LinkedList<>();
        parameterMarkerSegments.forEach(each -> result.add(bind(each, parameterMarkerSegmentBoundedInfos)));
        return result;
    }
    
    private static ParameterMarkerSegment bind(final ParameterMarkerSegment parameterMarkerSegment,
                                               final Map<ParameterMarkerSegment, ColumnSegmentBoundedInfo> parameterMarkerSegmentBoundedInfos) {
        if (parameterMarkerSegment instanceof ParameterMarkerExpressionSegment) {
            return ParameterMarkerExpressionSegmentBinder.bind((ParameterMarkerExpressionSegment) parameterMarkerSegment, parameterMarkerSegmentBoundedInfos);
        }
        // TODO support more ParameterMarkerSegment bind
        return parameterMarkerSegment;
    }
}
