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

package com.dangdang.ddframe.rdb.sharding.parser.result.merger;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * 是否包含分组.
     * 
     * @return 是否包含分组
     */
    public boolean hasGroupBy() {
        return !groupByColumns.isEmpty();
    }
    
    /**
     * 判断是否为分组或者聚合计算.
     * 此处将聚合计算想象成为特殊的分组计算,统一进行处理.
     *
     * @return true:是分组或者聚合计算 false:不是分组且不是聚合计算
     */
    public boolean hasGroupByOrAggregation() {
        return hasGroupBy() || !aggregationColumns.isEmpty();
    }
    
    /**
     * 判断是否包含排序列.
     * 
     * @return 是否包含排序列
     */
    public boolean hasOrderBy() {
        return !orderByColumns.isEmpty();
    }
    
    /**
     * 判断是否有限定结果集计算.
     *
     * @return true:是限定结果集计算 false:不是限定结果集计算
     */
    public boolean hasLimit() {
        return null != limit;
    }
}
