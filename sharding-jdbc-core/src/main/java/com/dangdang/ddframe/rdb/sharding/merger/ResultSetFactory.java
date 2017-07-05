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
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer.IteratorReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.pipeline.reducer.StreamingOrderByReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.resultset.memory.MemorySortResultSet;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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
     * @throws SQLException SQL异常
     */
    public static ResultSet getResultSet(final List<ResultSet> resultSets, final SQLStatement sqlStatement) throws SQLException {
        ShardingResultSets shardingResultSets = new ShardingResultSets(resultSets);
        if (shardingResultSets.getResultSets().isEmpty()) {
            return resultSets.get(0);
        }
        return buildResultSet(shardingResultSets, sqlStatement);
    }
    
    private static ResultSet buildResultSet(final ShardingResultSets shardingResultSets, final SQLStatement sqlStatement) throws SQLException {
        ResultSetMergeContext resultSetMergeContext = new ResultSetMergeContext(shardingResultSets, sqlStatement);
        ResultSet result;
        if (resultSetMergeContext.getSqlStatement().isGroupByAndOrderByDifferent()) {
            result = buildMemoryResultSet(resultSetMergeContext);
        } else {
            result = buildStreamResultSet(resultSetMergeContext);
        }
        if (null != resultSetMergeContext.getSqlStatement().getLimit()) {
            result = new LimitCouplingResultSet(result, resultSetMergeContext.getSqlStatement());
        }
        return result;
    }
    
    private static ResultSet buildMemoryResultSet(final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        ResultSet result = new MemorySortResultSet(resultSetMergeContext.getShardingResultSets().getResultSets(), resultSetMergeContext.getSqlStatement().getGroupByItems());
        result = new GroupByCouplingResultSet(result, resultSetMergeContext);
        result = new MemorySortResultSet(Collections.singletonList(result), resultSetMergeContext.getSqlStatement().getOrderByItems());
        return result;
    }
    
    private static ResultSet buildStreamResultSet(final ResultSetMergeContext resultSetMergeContext) throws SQLException {
        ResultSet result;
        if (resultSetMergeContext.getSqlStatement().getGroupByItems().isEmpty() && resultSetMergeContext.getSqlStatement().getOrderByItems().isEmpty()) {
            result = new IteratorReducerResultSet(resultSetMergeContext);
        } else {
            result = new StreamingOrderByReducerResultSet(resultSetMergeContext);
        }
        if (!resultSetMergeContext.getSqlStatement().getGroupByItems().isEmpty() || !resultSetMergeContext.getSqlStatement().getAggregationSelectItems().isEmpty()) {
            result = new GroupByCouplingResultSet(result, resultSetMergeContext);
        }
        return result;
    }
}
