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

package org.apache.shardingsphere.sql.parser.binder.segment.select.orderby;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;

/**
 * Order by item.
 */
@RequiredArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public final class OrderByItem {
    
    private final OrderByItemSegment segment;
    
    private int index;
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof OrderByItem)) {
            return false;
        }
        OrderByItem orderByItem = (OrderByItem) obj;
        return segment.getOrderDirection() == orderByItem.segment.getOrderDirection() && index == orderByItem.index;
    }
}
