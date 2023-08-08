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

package org.apache.shardingsphere.infra.binder.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.expression.ExpressionSegmentBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;

/**
 * In expression binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InExpressionBinder {
    
    /**
     * Bind in expression segment with metadata.
     *
     * @param segment in expression
     * @param metaData metaData
     * @param defaultDatabaseName default database name
     * @return bounded in expression
     */
    public static InExpression bind(final InExpression segment, final ShardingSphereMetaData metaData, final String defaultDatabaseName) {
        ExpressionSegment boundedLeft = ExpressionSegmentBinder.bind(segment.getLeft(), metaData, defaultDatabaseName);
        ExpressionSegment boundedRight = ExpressionSegmentBinder.bind(segment.getRight(), metaData, defaultDatabaseName);
        return new InExpression(segment.getStartIndex(), segment.getStopIndex(), boundedLeft, boundedRight, segment.isNot());
    }
}
