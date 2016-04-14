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

import com.dangdang.ddframe.rdb.sharding.merger.component.ComponentResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.CouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.ReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.coupling.MemoryOrderByCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.MemoryOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.StreamingOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.OrderByColumn;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 结果集管道构建器.
 *
 * @author gaohongtao
 */
@Slf4j
// TODO 最好把mergeContext纳入局部变量, 用于判断是否需要内存还是非内存排序. 通过mergeContext判断, 比零散的通过orderByColumns是否好一些
public class ResultSetPipelineBuilder {
    
    private final List<ResultSet> inputResultSets;
    
    private final List<OrderByColumn> orderByColumns = new ArrayList<>();
    
    private ComponentResultSet tailResultSet;
    
    public ResultSetPipelineBuilder(final List<ResultSet> resultSets, final List<OrderByColumn> orderByColumns) {
        inputResultSets = resultSets;
        this.orderByColumns.addAll(orderByColumns);
    }
    
    /**
     * 链接结果集.
     *
     * @param componentResultSet 链接目标结果集
     * @return 管道建造者
     */
    // TODO 虽然考虑使用builder模式, 但实际并未使用, 是否可以把返回值去掉
    public <T> ResultSetPipelineBuilder join(final ComponentResultSet<T> componentResultSet) throws SQLException {
        Preconditions.checkArgument(componentResultSet instanceof ReducerResultSet || componentResultSet instanceof CouplingResultSet);
        if (componentResultSet instanceof ReducerResultSet) {
            ((ReducerResultSet) componentResultSet).init(inputResultSets);
        } else {
            ((CouplingResultSet) componentResultSet).init(tailResultSet);
        }
        tailResultSet = componentResultSet;
        log.trace("join component {}", tailResultSet.getClass().getSimpleName());
        return this;
    }
    
    /**
     * 链接排序缩减结果集.
     *
     * @param expectOrderList 期望的排序顺序
     * @return 管道建造者
     */
    // TODO 虽然考虑使用builder模式, 但实际并未使用, 是否可以把返回值去掉
    public ResultSetPipelineBuilder joinSortReducer(final List<OrderByColumn> expectOrderList) throws SQLException {
        if (orderByColumns.equals(expectOrderList)) {
            join(new StreamingOrderByReducerResultSet(expectOrderList));
        } else {
            setNewOrder(expectOrderList);
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
    // TODO 是否应该只需要一个join方法, joinSortCoupling => join(new MemoryOrderByCouplingResultSet(expectOrderList))
    public ResultSetPipelineBuilder joinSortCoupling(final List<OrderByColumn> expectOrderList) throws SQLException {
        if (orderByColumns.equals(expectOrderList)) {
            return this;
        }
        setNewOrder(expectOrderList);
        join(new MemoryOrderByCouplingResultSet(expectOrderList));
        return this;
    }
    
    // TODO 是否可以在一开始就初始化完成OrderByColumn, 而不是根据后续判断再改
    private void setNewOrder(final List<OrderByColumn> orderList) {
        orderByColumns.clear();
        orderByColumns.addAll(orderList);
    }
    
    /**
     * 构建结果集.
     *
     * @return 结果集
     */
    public ResultSet build() {
        return tailResultSet;
    }
}
