/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.rewrite.builder;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base parameter builder.
 *
 * @author panjuan
 */
@Getter
public final class BaseParameterBuilder implements ParameterBuilder {
    
    private final List<Object> originalParameters = new LinkedList<>();
    
    private final Map<Integer, Object> addedIndexAndParameters = new HashMap<>();
    
    private final Map<Integer, Object> replacedIndexAndParameters = new HashMap<>();
    
    public BaseParameterBuilder(final List<Object> parameters) {
        originalParameters.addAll(parameters);
    }
    
    public BaseParameterBuilder(final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        this(parameters);
        setReplacedIndexAndParameters(sqlRouteResult);
    }
    
    private void setReplacedIndexAndParameters(final SQLRouteResult sqlRouteResult) {
        if (isNeedRewritePagination(sqlRouteResult)) {
            Pagination pagination = ((ShardingSelectOptimizedStatement) sqlRouteResult.getShardingStatement()).getPagination();
            Optional<Integer> offsetParameterIndex = pagination.getOffsetParameterIndex();
            if (offsetParameterIndex.isPresent()) {
                rewriteOffset(pagination, offsetParameterIndex.get());
            }
            Optional<Integer> rowCountParameterIndex = pagination.getRowCountParameterIndex();
            if (rowCountParameterIndex.isPresent()) {
                rewriteRowCount(pagination, rowCountParameterIndex.get(), sqlRouteResult);
            }
        }
    }
    
    private boolean isNeedRewritePagination(final SQLRouteResult sqlRouteResult) {
        return sqlRouteResult.getShardingStatement() instanceof ShardingSelectOptimizedStatement
                && ((ShardingSelectOptimizedStatement) sqlRouteResult.getShardingStatement()).getPagination().isHasPagination() && !sqlRouteResult.getRoutingResult().isSingleRouting();
    }
    
    private void rewriteOffset(final Pagination pagination, final int offsetParameterIndex) {
        replacedIndexAndParameters.put(offsetParameterIndex, pagination.getRevisedOffset());
    }
    
    private void rewriteRowCount(final Pagination pagination, final int rowCountParameterIndex, final SQLRouteResult sqlRouteResult) {
        replacedIndexAndParameters.put(rowCountParameterIndex, pagination.getRevisedRowCount((ShardingSelectOptimizedStatement) sqlRouteResult.getShardingStatement()));
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>(originalParameters);
        for (Entry<Integer, Object> entry : addedIndexAndParameters.entrySet()) {
            result.add(entry.getKey(), entry.getValue());
        }
        for (Entry<Integer, Object> entry : replacedIndexAndParameters.entrySet()) {
            result.set(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    @Override
    public List<Object> getParameters(final RoutingUnit routingUnit) {
        return getParameters();
    }
}
