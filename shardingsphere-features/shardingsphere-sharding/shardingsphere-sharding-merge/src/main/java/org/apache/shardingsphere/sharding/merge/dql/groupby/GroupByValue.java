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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.sql.result.query.QueryResult;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Group by value.
 */
@Getter
@EqualsAndHashCode
public final class GroupByValue {
    
    private final List<?> groupValues;
    
    public GroupByValue(final QueryResult queryResult, final Collection<OrderByItem> groupByItems) throws SQLException {
        groupValues = getGroupByValues(queryResult, groupByItems);
    }
    
    private List<?> getGroupByValues(final QueryResult queryResult, final Collection<OrderByItem> groupByItems) throws SQLException {
        List<Object> result = new ArrayList<>(groupByItems.size());
        for (OrderByItem each : groupByItems) {
            result.add(queryResult.getValue(each.getIndex(), Object.class));
        }
        return result;
    }
}
