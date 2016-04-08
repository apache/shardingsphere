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

package com.dangdang.ddframe.rdb.sharding.merger.aggregation;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dangdang.ddframe.rdb.sharding.merger.common.AbstractMergerInvokeHandler;
import com.dangdang.ddframe.rdb.sharding.merger.common.ResultSetQueryIndex;
import com.dangdang.ddframe.rdb.sharding.parser.result.merger.AggregationColumn;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;

/**
 * 聚合函数动态代理.
 * 
 * @author gaohongtao, zhangliang
 */
public final class AggregationInvokeHandler extends AbstractMergerInvokeHandler<AggregationResultSet> {
    
    public AggregationInvokeHandler(final AggregationResultSet aggregationResultSet) {
        super(aggregationResultSet);
    }
    
    @SuppressWarnings("unchecked")
    protected Object doMerge(final AggregationResultSet aggregationResultSet, final Method method, final ResultSetQueryIndex resultSetQueryIndex) throws ReflectiveOperationException, SQLException {
        Optional<AggregationColumn> aggregationColumn = findAggregationColumn(aggregationResultSet, resultSetQueryIndex);
        if (!aggregationColumn.isPresent()) {
            return invokeOriginal(method, resultSetQueryIndex);
        }
        return aggregate(aggregationResultSet, (Class<Comparable<?>>) method.getReturnType(), resultSetQueryIndex, aggregationColumn.get());
    }
    
    private Optional<AggregationColumn> findAggregationColumn(final AggregationResultSet aggregationResultSet, final ResultSetQueryIndex resultSetQueryIndex) {
        for (AggregationColumn each : aggregationResultSet.getAggregationColumns()) {
            if (resultSetQueryIndex.isQueryBySequence() && each.getIndex() == resultSetQueryIndex.getQueryIndex()) {
                return Optional.of(each);
            } else if (each.getAlias().isPresent() && each.getAlias().get().equals(resultSetQueryIndex.getQueryName())) {
                return Optional.of(each);
            } else if (SQLUtil.getExactlyValue(each.getExpression()).equalsIgnoreCase(SQLUtil.getExactlyValue(resultSetQueryIndex.getQueryName()))) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private Object aggregate(final AggregationResultSet aggregationResultSet, final Class<Comparable<?>> returnType, 
            final ResultSetQueryIndex resultSetQueryIndex, final AggregationColumn aggregationColumn) 
            throws SQLException {
        AggregationUnit unit = AggregationUnitFactory.create(aggregationColumn.getAggregationType(), returnType);
        for (ResultSet each : aggregationResultSet.getEffectiveResultSets()) {
            unit.merge(aggregationColumn, new ResultSetAggregationValue(each), resultSetQueryIndex);
        }
        return unit.getResult();
    }
}
