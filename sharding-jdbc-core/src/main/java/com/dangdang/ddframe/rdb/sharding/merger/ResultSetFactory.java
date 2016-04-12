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
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.IndexColumn;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.MergeContext;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
            log.trace("Sharding-JDBC:No data found in origin result sets");
            return resultSets.get(0);
        } else if (filteredResultSets.size() == 1) {
            log.trace("Sharding-JDBC:Only one result set");
            return filteredResultSets.get(0);
        }
        setColumnIndex(filteredResultSets.get(0), mergeContext);
        ResultSetPipelineBuilder builder = new ResultSetPipelineBuilder(filteredResultSets, mergeContext.getOrderByColumns());
        buildReducer(builder, mergeContext);
        buildCoupling(builder, mergeContext);
        return builder.build();
    }
    
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
    
    private static void setColumnIndex(final ResultSet resultSet, final MergeContext mergeContext) throws SQLException {
        ResultSetMetaData md = resultSet.getMetaData();
        Map<String, Integer> columnLabelIndexMap = new CaseInsensitiveMap<>(md.getColumnCount());
        for (int i = 1; i <= md.getColumnCount(); i++) {
            String columnLabel = md.getColumnLabel(i);
            columnLabelIndexMap.put(columnLabel, i);
        }
        for (IndexColumn each : extractIndexColumns(mergeContext)) {
            if (each.getColumnIndex() > 0) {
                continue;
            }
            Preconditions.checkState(columnLabelIndexMap.containsKey(each.getColumnLabel().orNull()) || columnLabelIndexMap.containsKey(each.getColumnName().orNull()),
                    String.format("%s has not index", each));
            if (each.getColumnLabel().isPresent() && columnLabelIndexMap.containsKey(each.getColumnLabel().get())) {
                each.setColumnIndex(columnLabelIndexMap.get(each.getColumnLabel().get()));
            } else if (each.getColumnName().isPresent() && columnLabelIndexMap.containsKey(each.getColumnName().get())) {
                each.setColumnIndex(columnLabelIndexMap.get(each.getColumnName().get()));
            }
        }
    }
    
    private static List<IndexColumn> extractIndexColumns(final MergeContext mergeContext) {
        List<IndexColumn> result = new LinkedList<>();
        result.addAll(mergeContext.getGroupByColumns());
        result.addAll(mergeContext.getOrderByColumns());
        LinkedList<AggregationColumn> allAggregationColumns = Lists.newLinkedList(mergeContext.getAggregationColumns());
        while (allAggregationColumns.size() > 0) {
            AggregationColumn head = allAggregationColumns.poll();
            result.add(head);
            if (head.getDerivedColumns().isEmpty()) {
                continue;
            }
            allAggregationColumns.addAll(head.getDerivedColumns());
        }
        return result;
    }
    
    private static void buildReducer(final ResultSetPipelineBuilder builder, final MergeContext mergeContext) throws SQLException {
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
    
    private static void buildCoupling(final ResultSetPipelineBuilder builder, final MergeContext mergeContext) throws SQLException {
        if (mergeContext.hasGroupByOrAggregation()) {
            builder.join(new GroupByCouplingResultSet(mergeContext.getGroupByColumns(), mergeContext.getAggregationColumns()));
        }
        if (mergeContext.hasOrderBy()) {
            builder.joinSortCoupling(mergeContext.getOrderByColumns());
        }
        if (mergeContext.hasLimit()) {
            builder.join(new LimitCouplingResultSet(mergeContext.getLimit()));
        }
    }
}
