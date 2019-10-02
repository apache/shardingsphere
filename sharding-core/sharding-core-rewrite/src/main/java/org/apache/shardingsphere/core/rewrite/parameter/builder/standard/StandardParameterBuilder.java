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

package org.apache.shardingsphere.core.rewrite.parameter.builder.standard;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.core.optimize.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * standard parameter builder.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class StandardParameterBuilder implements ParameterBuilder {
    
    private final List<Object> originalParameters;
    
    @Getter
    private final Map<Integer, Object> addedIndexAndParameters = new TreeMap<>();
    
    @Getter
    private final Map<Integer, Object> replacedIndexAndParameters = new HashMap<>();
    
    public StandardParameterBuilder(final List<Object> originalParameters, final SQLRouteResult sqlRouteResult) {
        this.originalParameters = originalParameters;
        setReplacedIndexAndParameters(sqlRouteResult);
    }
    
    private void setReplacedIndexAndParameters(final SQLRouteResult sqlRouteResult) {
        if (isNeedRewritePagination(sqlRouteResult)) {
            PaginationContext pagination = ((SelectSQLStatementContext) sqlRouteResult.getSqlStatementContext()).getPaginationContext();
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
        return sqlRouteResult.getSqlStatementContext() instanceof SelectSQLStatementContext
                && ((SelectSQLStatementContext) sqlRouteResult.getSqlStatementContext()).getPaginationContext().isHasPagination() && !sqlRouteResult.getRoutingResult().isSingleRouting();
    }
    
    private void rewriteOffset(final PaginationContext pagination, final int offsetParameterIndex) {
        replacedIndexAndParameters.put(offsetParameterIndex, pagination.getRevisedOffset());
    }
    
    private void rewriteRowCount(final PaginationContext pagination, final int rowCountParameterIndex, final SQLRouteResult sqlRouteResult) {
        replacedIndexAndParameters.put(rowCountParameterIndex, pagination.getRevisedRowCount((SelectSQLStatementContext) sqlRouteResult.getSqlStatementContext()));
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> result = new LinkedList<>(originalParameters);
        for (Entry<Integer, Object> entry : replacedIndexAndParameters.entrySet()) {
            result.set(entry.getKey(), entry.getValue());
        }
        for (Entry<Integer, Object> entry : ((TreeMap<Integer, Object>) addedIndexAndParameters).descendingMap().entrySet()) {
            result.add(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    @Override
    public List<Object> getParameters(final RoutingUnit routingUnit) {
        return getParameters();
    }
}
