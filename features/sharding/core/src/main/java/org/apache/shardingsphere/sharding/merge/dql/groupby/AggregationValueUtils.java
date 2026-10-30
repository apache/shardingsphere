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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.sharding.exception.data.NotImplementComparableValueException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;

import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Aggregation value utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class AggregationValueUtils {
    
    /**
     * Read an aggregation value, preserving SQL {@code NULL}.
     *
     * @param queryResult query result
     * @param aggregationProjection aggregation projection
     * @return aggregation value, or {@code null} for SQL {@code NULL}
     * @throws SQLException SQL exception
     */
    @HighFrequencyInvocation
    static Comparable<?> getValue(final QueryResult queryResult, final AggregationProjection aggregationProjection) throws SQLException {
        Object result = queryResult.getValue(aggregationProjection.getIndex(), Object.class);
        ShardingSpherePreconditions.checkState(null == result || result instanceof Comparable, () -> new NotImplementComparableValueException("Aggregation", result));
        if (result instanceof LocalTime && aggregationProjection instanceof AggregationDistinctProjection && AggregationType.GROUP_CONCAT == aggregationProjection.getType()
                && Types.TIME == queryResult.getMetaData().getColumnType(aggregationProjection.getIndex())) {
            return DateTimeFormatter.ISO_LOCAL_TIME.format((LocalTime) result);
        }
        return (Comparable<?>) result;
    }
}
