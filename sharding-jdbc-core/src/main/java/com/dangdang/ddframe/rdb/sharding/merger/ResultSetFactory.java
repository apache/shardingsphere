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

import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.GroupByCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.LimitCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.coupling.MemoryOrderByCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer.IteratorReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer.MemoryOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer.StreamingOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 分片结果集归并工厂.
 *
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ResultSetFactory {
    
    /**
     * 获取结果集.
     *
     * @param resultSets 结果集列表
     * @param sqlStatement SQL语句对象
     * @return 结果集包装
     */
    public static ResultSet getResultSet(final List<ResultSet> resultSets, final SQLStatement sqlStatement) throws SQLException {
        ShardingResultSets shardingResultSets = new ShardingResultSets(resultSets);
        log.debug("Sharding-JDBC: Sharding result sets type is '{}'", shardingResultSets.getType().toString());
        switch (shardingResultSets.getType()) {
            case EMPTY:
                return buildEmpty(resultSets);
            case SINGLE:
                return buildSingle(shardingResultSets);
            case MULTIPLE:
                return buildMultiple(shardingResultSets, sqlStatement);
            default:
                throw new UnsupportedOperationException(shardingResultSets.getType().toString());
        }
    }
    
    private static ResultSet buildEmpty(final List<ResultSet> resultSets) {
        return resultSets.get(0);
    }
    
    private static ResultSet buildSingle(final ShardingResultSets shardingResultSets) {
        return shardingResultSets.getResultSets().get(0);
    }
    
    private static ResultSet buildMultiple(final ShardingResultSets shardingResultSets, final SQLStatement sqlStatement) throws SQLException {
        ResultSetMergeContext resultSetMergeContext = new ResultSetMergeContext(shardingResultSets, sqlStatement);
        return buildCoupling(buildReducer(resultSetMergeContext), resultSetMergeContext);
    }
    
    private static ResultSet buildReducer(final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        if (resultSetMergeContext.isNeedMemorySortForGroupBy()) {
            resultSetMergeContext.setGroupByKeysToCurrentOrderByKeys();
            return new MemoryOrderByReducerResultSet(resultSetMergeContext);
        }
        if (!resultSetMergeContext.getSqlStatement().getGroupByList().isEmpty() || !resultSetMergeContext.getSqlStatement().getOrderByList().isEmpty()) {
            return new StreamingOrderByReducerResultSet(resultSetMergeContext);
        }
        return new IteratorReducerResultSet(resultSetMergeContext);
    }
    
    private static ResultSet buildCoupling(final ResultSet resultSet, final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        ResultSet result = resultSet;
        if (!resultSetMergeContext.getSqlStatement().getGroupByList().isEmpty() || !resultSetMergeContext.getSqlStatement().getAggregationSelectItems().isEmpty()) {
            result = new GroupByCouplingResultSet(result, resultSetMergeContext);
        }
        if (resultSetMergeContext.isNeedMemorySortForOrderBy()) {
            resultSetMergeContext.setOrderByKeysToCurrentOrderByKeys();
            result = new MemoryOrderByCouplingResultSet(result, resultSetMergeContext);
        }
        if (null != resultSetMergeContext.getSqlStatement().getLimit()) {
            result = new LimitCouplingResultSet(result, resultSetMergeContext.getSqlStatement());
        }
        return result;
    }
}
