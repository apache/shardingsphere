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

package com.dangdang.ddframe.rdb.sharding.merger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationInvokeHandler;
import com.dangdang.ddframe.rdb.sharding.merger.aggregation.AggregationResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.GroupByInvokeHandler;
import com.dangdang.ddframe.rdb.sharding.merger.groupby.GroupByResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.iterator.IteratorResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.orderby.OrderByResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext.ResultSetType;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 创建归并分片结果集的工厂.
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ResultSetFactory {
    
    /**
     * 获取结果集.
     * 
     * @param resultSets 结果集列表
     * @param mergeContext 结果归并上下文
     * @return 结果集包装
     */
    public static ResultSet getResultSet(final List<ResultSet> resultSets, final MergeContext mergeContext) throws SQLException {
        ResultSetType resultSetType  = mergeContext.getResultSetType();
        log.trace("Get '{}' result set", resultSetType);
        switch (resultSetType) {
            case GroupBy: 
                return createDelegateResultSet(new GroupByInvokeHandler(new GroupByResultSet(resultSets, mergeContext)));
            case Aggregate: 
                return createDelegateResultSet(new AggregationInvokeHandler(new AggregationResultSet(resultSets, mergeContext)));
            case OrderBy: 
                return new OrderByResultSet(resultSets, mergeContext);
            case Iterator: 
                return new IteratorResultSet(resultSets, mergeContext);
            default: 
                throw new UnsupportedOperationException(resultSetType.name());
        }
    }
    
    private static ResultSet createDelegateResultSet(final InvocationHandler handler) {
        return (ResultSet) Proxy.newProxyInstance(ResultSetFactory.class.getClassLoader(), new Class[]{ResultSet.class}, handler);
    }
}
