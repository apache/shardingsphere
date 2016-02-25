/**
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

package com.dangdang.ddframe.rdb.sharding.parser.result.merger;

import java.util.ArrayList;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.executor.ExecutorEngine;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 结果归并上下文.
 * 
 * @author zhangliang
 */
@Getter
@ToString
public final class MergeContext {
    
    private final List<OrderByColumn> orderByColumns = new ArrayList<>();
    
    private final List<GroupByColumn> groupByColumns = new ArrayList<>();
    
    private final List<AggregationColumn> aggregationColumns = new ArrayList<>();
    
    @Setter
    private Limit limit;
    
    @Setter
    private ExecutorEngine executorEngine;
    
    /**
     * 获取结果集类型.
     * 
     * @return 结果集类型
     */
    public ResultSetType getResultSetType() {
        if (!groupByColumns.isEmpty()) {
            return ResultSetType.GroupBy;
        }
        if (!aggregationColumns.isEmpty()) {
            return ResultSetType.Aggregate;
        }
        if (!orderByColumns.isEmpty()) {
            return ResultSetType.OrderBy;
        }
        return ResultSetType.Iterator;
    }
    
    /**
     * 结果集类型.
     * 
     * @author zhangliang
     */
    public enum ResultSetType {
        Iterator, OrderBy, Aggregate, GroupBy
    }
}
