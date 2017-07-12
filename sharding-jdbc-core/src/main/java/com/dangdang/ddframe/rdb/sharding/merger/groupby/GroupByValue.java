/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.merger.groupby;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderItem;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 分组值对象.
 * 
 * @author zhangliang
 */
@Getter
@EqualsAndHashCode
public final class GroupByValue {
    
    private final List<Comparable<?>> groupValues;
    
    public GroupByValue(final ResultSet resultSet, final List<OrderItem> groupByItems) throws SQLException {
        groupValues = getGroupByValues(resultSet, groupByItems);
    }
    
    private List<Comparable<?>> getGroupByValues(final ResultSet resultSet, final List<OrderItem> groupByItems) throws SQLException {
        List<Comparable<?>> result = new ArrayList<>(groupByItems.size());
        for (OrderItem each : groupByItems) {
            Object value = resultSet.getObject(each.getIndex());
            Preconditions.checkState(value instanceof Comparable, "Group by value must implements Comparable");
            result.add((Comparable<?>) value);
        }
        return result;
    }
}
