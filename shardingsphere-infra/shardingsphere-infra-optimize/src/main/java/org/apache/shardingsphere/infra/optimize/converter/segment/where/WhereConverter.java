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

package org.apache.shardingsphere.infra.optimize.converter.segment.where;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;

import java.util.Optional;

/**
 * Where converter.
 */
public final class WhereConverter implements SQLSegmentConverter<WhereSegment, SqlNode> {
    
    private static final int WHERE_SEGMENT_LENGTH = 6;
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final WhereSegment segment) {
        return null == segment ? Optional.empty() : new ExpressionConverter().convertToSQLNode(segment.getExpr());
    }
    
    @Override
    public Optional<WhereSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (null == sqlNode) {
            return Optional.empty();
        }
        // FIXME Now sqlNode position returned by the CalCite parser does not contain WHERE and requires manual calculation
        int startIndex = getStartIndex(sqlNode) - WHERE_SEGMENT_LENGTH;
        return new ExpressionConverter().convertToSQLSegment(sqlNode).map(optional -> new WhereSegment(startIndex, optional.getStopIndex(), optional));
    }
}
