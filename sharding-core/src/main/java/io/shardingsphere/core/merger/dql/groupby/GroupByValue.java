/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.merger.dql.groupby;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.context.orderby.OrderItem;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Group by value.
 * 
 * @author zhangliang
 */
@Getter
@EqualsAndHashCode
public final class GroupByValue {
    
    private final List<?> groupValues;
    
    public GroupByValue(final QueryResult queryResult, final List<OrderItem> groupByItems) throws SQLException {
        groupValues = getGroupByValues(queryResult, groupByItems);
    }
    
    private List<?> getGroupByValues(final QueryResult queryResult, final List<OrderItem> groupByItems) throws SQLException {
        List<Object> result = new ArrayList<>(groupByItems.size());
        for (OrderItem each : groupByItems) {
            result.add(queryResult.getValue(each.getIndex(), Object.class));
        }
        return result;
    }
}
