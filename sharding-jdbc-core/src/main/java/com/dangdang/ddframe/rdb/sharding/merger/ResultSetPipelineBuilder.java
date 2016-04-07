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

package com.dangdang.ddframe.rdb.sharding.merger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.merger.component.ComponentResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.CouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.ReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.coupling.MemoryOrderByCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.MemoryOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.StreamingOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 结果集管道构建器.
 *
 * @author gaohongtao
 */
@Slf4j
public class ResultSetPipelineBuilder {
    
    @Getter
    private final List<ResultSet> inputResultSets;
    
    private AbstractList<OrderByColumn> orderByColumns = new ArrayList<>();
    
    private ComponentResultSet tailResultSet;
    
    public ResultSetPipelineBuilder(final List<ResultSet> resultSets, final List<OrderByColumn> orderByColumns) throws SQLException {
        inputResultSets = resultSets;
        this.orderByColumns.addAll(orderByColumns);
    }
    
    /**
     * 链接结果集.
     *
     * @param componentResultSet 链接目标结果集
     * @return 管道建造者
     */
    public <T> ResultSetPipelineBuilder join(final ComponentResultSet<T> componentResultSet) throws SQLException {
        Preconditions.checkArgument(componentResultSet instanceof ReducerResultSet || componentResultSet instanceof CouplingResultSet);
        if (componentResultSet instanceof ReducerResultSet) {
            ((ReducerResultSet) componentResultSet).inject(inputResultSets);
        } else {
            ((CouplingResultSet) componentResultSet).inject(tailResultSet);
        }
        tailResultSet = componentResultSet;
        return this;
    }
    
    /**
     * 链接排序缩减结果集.
     *
     * @param expectOrderList 期望的排序顺序
     * @return 管道建造者
     */
    public ResultSetPipelineBuilder joinSortReducer(final List<OrderByColumn> expectOrderList) throws SQLException {
        if (orderEqual(expectOrderList)) {
            join(new StreamingOrderByReducerResultSet(expectOrderList));
        } else {
            join(new MemoryOrderByReducerResultSet(expectOrderList));
        }
        return this;
    }
    
    /**
     * 对管道内底层数据进行排序.
     * 排序规则按照入参传入的顺序进行.
     *
     * @param expectOrderList 期望的排序顺序
     * @return 管道建造者
     */
    public ResultSetPipelineBuilder joinSortCoupling(final List<OrderByColumn> expectOrderList) throws SQLException {
        if (orderEqual(expectOrderList)) {
            return this;
        }
        join(new MemoryOrderByCouplingResultSet(expectOrderList));
        return this;
    }
    
    /**
     * 构建结果集.
     *
     * @return 结果集
     */
    public ResultSet build() {
        log.trace("The pipeline of result set handling is : {}", tailResultSet);
        return tailResultSet;
    }
    
    private boolean orderEqual(final List<OrderByColumn> expectOrderList) {
        return orderByColumns.equals(expectOrderList);
    }
    
}
