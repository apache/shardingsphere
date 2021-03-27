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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.func.BuiltinFunction;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;

import java.util.List;

/**
 * BuiltinFunction form Aggregate operator.
 * @param <R> result type for aggregation result.
 */
public interface AggregateBuiltinFunction<R> extends BuiltinFunction<Object, R> {
    
    /**
     * aggregation method.
     * @param row row to be aggregate.
     */
    void aggregate(Row row);
    
    /**
     * copy this function instance.
     * @return a new aggregation BuiltinFunction.
     */
    AggregateBuiltinFunction newFunc();
    
    /**
     * Get the result for aggregation operator.
     * @return the result.
     */
    R getResult();
    
    @EqualsAndHashCode
    @Getter
    class GroupByKey {
        
        private final Object[] groupByVals;
        
        private final List<Integer> groupByColumnIdx;
        
        public GroupByKey(final List<Integer> groupByColumnIdx, final Object[] groupByVals) {
            this.groupByColumnIdx = groupByColumnIdx;
            this.groupByVals = groupByVals;
        }
        
        public int length() {
            return groupByColumnIdx.size();
        }
        
    }
    
}
