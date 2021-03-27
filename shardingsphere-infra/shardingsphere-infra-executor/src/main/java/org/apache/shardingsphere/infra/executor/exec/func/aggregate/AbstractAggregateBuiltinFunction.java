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

package org.apache.shardingsphere.infra.executor.exec.func.aggregate;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.List;

public abstract class AbstractAggregateBuiltinFunction<R> implements AggregateBuiltinFunction<R> {
    
    @Getter(AccessLevel.PROTECTED)
    private final List<Integer> aggColumnIdx;
    
    @Getter(AccessLevel.PROTECTED)
    private final boolean distinct;
    
    public AbstractAggregateBuiltinFunction(final List<Integer> aggColumnIdx, final boolean distinct) {
        this.aggColumnIdx = aggColumnIdx;
        this.distinct = distinct;
    }
    
    @Override
    public final void aggregate(final Row row) {
        Object[] values = getColumnVals(row, aggColumnIdx);
        accumulate(values);
    }
    
    /**
     * accumulating method for aggregate operator.
     * @param args args for accumulating.
     */
    public abstract void accumulate(Object[] args);
    
    private Object[] getColumnVals(final Row row, final List<Integer> columnIdx) {
        Object[] columnVals = new Object[columnIdx.size()];
        int idx = 0;
        for (Integer groupByIdx : columnIdx) {
            columnVals[idx++] = row.getColumnValue(groupByIdx);
        }
        return columnVals;
    }
    
}
