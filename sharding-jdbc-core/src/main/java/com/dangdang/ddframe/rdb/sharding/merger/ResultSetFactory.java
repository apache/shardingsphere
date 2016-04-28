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

import com.dangdang.ddframe.rdb.sharding.merger.component.ComponentResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.coupling.GroupByCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.coupling.LimitCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.coupling.MemoryOrderByCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.other.WrapperResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.IteratorReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.MemoryOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.StreamingOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

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
        List<ResultSet> filteredResultSets = filterResultSets(resultSets);
        if (filteredResultSets.isEmpty()) {
            log.trace("Sharding-JDBC: No data found in origin result sets");
            return resultSets.get(0);
        }
        if (1 == filteredResultSets.size()) {
            log.trace("Sharding-JDBC: Only one result set");
            return filteredResultSets.get(0);
        }
        mergeContext.buildContextWithResultSet((WrapperResultSet) filteredResultSets.get(0));
        return buildCoupling(buildReducer(filteredResultSets, mergeContext), mergeContext);
    }
    
    private static List<ResultSet> filterResultSets(final List<ResultSet> resultSets) throws SQLException {
        List<ResultSet> result = new LinkedList<>();
        for (ResultSet each : resultSets) {
            WrapperResultSet wrapperResultSet = new WrapperResultSet(each);
            if (!wrapperResultSet.isEmpty()) {
                result.add(wrapperResultSet);
            }
        }
        return result;
    }
    
    private static ResultSet buildReducer(final List<ResultSet> filteredResultSets, final MergeContext mergeContext) throws SQLException {
        if (mergeContext.hasGroupBy()) {
            if (mergeContext.groupByKeysEqualsOrderByKeys()) {
                return join(new StreamingOrderByReducerResultSet(mergeContext.getCurrentOrderByKeys()), filteredResultSets);
            }
            return join(new MemoryOrderByReducerResultSet(mergeContext.getCurrentOrderByKeys()), filteredResultSets);
        } else if (mergeContext.hasOrderBy()) {
            return join(new StreamingOrderByReducerResultSet(mergeContext.getCurrentOrderByKeys()), filteredResultSets);
        } else {
            return join(new IteratorReducerResultSet(), filteredResultSets);
        }
    }
    
    private static ResultSet buildCoupling(final ResultSet preResultSet, final MergeContext mergeContext) throws SQLException {
        ResultSet currentResultSet = preResultSet;
        if (mergeContext.hasGroupByOrAggregation()) {
            currentResultSet = join(new GroupByCouplingResultSet(mergeContext.getGroupByColumns(), mergeContext.getAggregationColumns()), currentResultSet);
        }
        if (mergeContext.needToSort()) {
            currentResultSet = join(new MemoryOrderByCouplingResultSet(mergeContext.getCurrentOrderByKeys()), currentResultSet);
        }
        if (mergeContext.hasLimit()) {
            currentResultSet = join(new LimitCouplingResultSet(mergeContext.getLimit()), currentResultSet);
        }
        return currentResultSet;
    }
    
    private static <T> ComponentResultSet<T> join(final ComponentResultSet<T> resultSet, final T preResultSet) throws SQLException {
        log.trace("{} joined", resultSet.getClass().getSimpleName());
        resultSet.init(preResultSet);
        return resultSet;
    }
}
