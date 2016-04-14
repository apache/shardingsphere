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

import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.merger.component.coupling.GroupByCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.coupling.LimitCouplingResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.other.WrapperResultSet;
import com.dangdang.ddframe.rdb.sharding.merger.component.reducer.IteratorReducerResultSet;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.IndexColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
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
        // TODO 如果不filter, 直接操纵可能为空的resultSet会有什么结果, 能否统一处理
        List<ResultSet> filteredResultSets = filterResultSets(resultSets);
        if (filteredResultSets.isEmpty()) {
            log.trace("Sharding-JDBC: No data found in origin result sets");
            return resultSets.get(0);
        }
        // TODO 只有1个的情况和多个情况有何不同, 需要单独处理么
        if (1 == filteredResultSets.size()) {
            log.trace("Sharding-JDBC: Only one result set");
            return filteredResultSets.get(0);
        }
        setColumnIndex((WrapperResultSet) filteredResultSets.get(0), mergeContext);
        ResultSetPipelineBuilder builder = new ResultSetPipelineBuilder(filteredResultSets, mergeContext.getOrderByColumns());
        buildReducer(builder, mergeContext);
        buildCoupling(builder, mergeContext);
        return builder.build();
    }
    
    // TODO 能否直接使用WrapperResultSet
    private static List<ResultSet> filterResultSets(final List<ResultSet> resultSets) {
        return Lists.newArrayList(Collections2.filter(Lists.transform(resultSets, new Function<ResultSet, ResultSet>() {
            
            @Override
            public ResultSet apply(final ResultSet input) {
                try {
                    return new WrapperResultSet(input);
                } catch (final SQLException ex) {
                    throw new ShardingJdbcException(ex);
                }
            }
        }), new Predicate<ResultSet>() {
            
            @Override
            public boolean apply(final ResultSet input) {
                return !((WrapperResultSet) input).isEmpty();
            }
        }));
    }
    
    private static void setColumnIndex(final WrapperResultSet resultSet, final MergeContext mergeContext) {
        for (IndexColumn each : mergeContext.getMergeFocusedColumns()) {
            if (0 == each.getColumnIndex()) {
                each.setColumnIndex(resultSet.getColumnIndex(each));
            }
        }
    }
    
    // TODO reducer目的是什么, 是为了确定读取resultSet的next走内存还是走streaming吗, 如果是,是否抽象出两个Reducer就够了
    private static void buildReducer(final ResultSetPipelineBuilder builder, final MergeContext mergeContext) throws SQLException {
        // TODO 判断hasGroupByOrAggregation并获取什么样的OrderByColumns, 能否封装到mergeContext对象里
        if (mergeContext.hasGroupByOrAggregation()) {
            builder.joinSortReducer(mergeContext.transformGroupByColumnToOrderByColumn());
            return;
        }
        if (mergeContext.hasOrderBy()) {
            builder.joinSortReducer(mergeContext.getOrderByColumns());
            return;
        }
        builder.join(new IteratorReducerResultSet());
    }
    
    // TODO Reducer和Coupling大致流程一致, 两个有什么区别
    private static void buildCoupling(final ResultSetPipelineBuilder builder, final MergeContext mergeContext) throws SQLException {
        if (mergeContext.hasGroupByOrAggregation()) {
            // TODO 保持一致, 都new一个CouplingResultSet
            builder.join(new GroupByCouplingResultSet(mergeContext.getGroupByColumns(), mergeContext.getAggregationColumns()));
        }
        if (mergeContext.hasOrderBy()) {
            // TODO 保持一致, 都new一个CouplingResultSet
            builder.joinSortCoupling(mergeContext.getOrderByColumns());
        }
        if (mergeContext.hasLimit()) {
            builder.join(new LimitCouplingResultSet(mergeContext.getLimit()));
        }
    }
}
