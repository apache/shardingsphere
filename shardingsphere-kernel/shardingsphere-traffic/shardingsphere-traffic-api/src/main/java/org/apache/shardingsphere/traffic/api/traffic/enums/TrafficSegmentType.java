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

package org.apache.shardingsphere.traffic.api.traffic.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

/**
 * Traffic segment type.
 */
@RequiredArgsConstructor
@Getter
public enum TrafficSegmentType {
    
    TABLE(TableSegment.class), WHERE(WhereSegment.class), GROUP_BY(GroupBySegment.class), ORDER_BY(OrderBySegment.class), LIMIT(LimitSegment.class), ALL(SQLSegment.class);
    
    private final Class<? extends SQLSegment> clazz;
    
    /**
     * Get segment type by clazz.
     *
     * @param clazz class
     * @return segment type
     */
    public static TrafficSegmentType getSegmentTypeByClazz(final Class<? extends SQLSegment> clazz) {
        for (TrafficSegmentType each : values()) {
            if (each.getClazz().isAssignableFrom(clazz)) {
                return each;
            }
        }
        return TrafficSegmentType.ALL;
    }
}
