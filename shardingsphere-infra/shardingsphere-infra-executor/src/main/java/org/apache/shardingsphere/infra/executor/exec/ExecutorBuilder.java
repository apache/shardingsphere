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

package org.apache.shardingsphere.infra.executor.exec;

import lombok.Getter;
import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSCalc;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSHashAggregate;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSLimitSort;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSMergeSort;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSNestedLoopJoin;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSRel;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSScan;
import org.apache.shardingsphere.infra.optimize.rel.physical.SSSort;

@Getter
public class ExecutorBuilder {
    
    private final ExecContext execContext;
    
    public ExecutorBuilder(final ExecContext execContext) {
        this.execContext = execContext;
    }
    
    /**
     * build an <code>Executor</code> instance for relational operator.
     * @param rel rational operator
     * @return <code>Executor</code> instance.
     */
    public final Executor build(final RelNode rel) {
        Executor executor;
        if (rel instanceof SSScan) {
            executor = ScanExecutorBuilder.build((SSScan) rel, this);
        } else if (rel instanceof SSMergeSort) {
            executor = MergeSortExecutor.build((SSMergeSort) rel, this);
        } else if (rel instanceof SSNestedLoopJoin) {
            executor = NestedLoopJoinExecutor.build((SSNestedLoopJoin) rel, this);
        } else if (rel instanceof SSCalc) {
            executor = CalcExecutor.build((SSCalc) rel, this);
        } else if (rel instanceof SSHashAggregate) {
            executor = HashAggregateExecutor.build((SSHashAggregate) rel, this);
        } else if (rel instanceof SSSort) {
            if (rel instanceof SSLimitSort) {
                executor = LimitSortExecutor.build((SSLimitSort) rel, this);
            } else {
                executor = SortExecutor.build((SSSort) rel, this);
            }
        } else {
            throw new UnsupportedOperationException("unsupported physic operator " + rel.getClass().getName());
        }
        
        return executor;
    }
    
    /**
     * Untility function for building <code>Executor</code> instance from relational operator.  
     * @param execContext execution context
     * @param rel <code>Relnode</code>
     * @return <code>Executor</code> instance.
     */
    public static Executor build(final ExecContext execContext, final RelNode rel) {
        if (!(rel instanceof SSRel)) {
            throw new UnsupportedOperationException("unsupported physic plan: " + rel.getClass());
        }
        return new ExecutorBuilder(execContext).build(rel);
    }
}
