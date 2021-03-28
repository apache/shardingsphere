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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.calcite.rel.core.JoinRelType;
import org.apache.shardingsphere.infra.executor.exec.evaluator.Evaluator;
import org.apache.shardingsphere.infra.executor.exec.meta.JoinColumnMetaData;
import org.apache.shardingsphere.infra.executor.exec.meta.JoinRow;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

public abstract class AbstractJoinExecutor extends AbstractExecutor {
    
    @Getter(AccessLevel.PROTECTED)
    private final Executor outer;
    
    @Getter(AccessLevel.PROTECTED)
    private final Executor inner;
    
    @Getter(AccessLevel.PROTECTED)
    private final Evaluator joinEvaluator;
    
    @Getter(AccessLevel.PROTECTED)
    private final JoinRelType joinType;
    
    public AbstractJoinExecutor(final Executor outer, final Executor inner, final JoinRelType joinType, 
                                final Evaluator joinEvaluator, final ExecContext execContext) {
        super(execContext);
        this.outer = outer;
        this.inner = inner;
        this.joinType = joinType;
        this.joinEvaluator = joinEvaluator;
    }
    
    /**
     * init outer and inner Executor.
     */
    @Override
    protected void executeInit() {
        outer.init();
        inner.init();
    }
    
    /**
     * return left parameter for join operator according to the join type.
     * @param outer outer
     * @param inner inner
     * @param <T> parameter type
     * @return left according joinType
     */
    protected <T> T left(final T outer, final T inner) {
        return this.joinType.generatesNullsOnLeft() ? outer : inner;
    }
    
    /**
     * return right parameter for join operator according to the join type.
     * @param outer outer
     * @param inner inner
     * @param <T> parameter type
     * @return right according joinType
     */
    protected <T> T right(final T outer, final T inner) {
        return this.joinType.generatesNullsOnLeft() ? inner : outer;
    }
    
    @Override
    public final QueryResultMetaData getMetaData() {
        return new JoinColumnMetaData(left(outer.getMetaData(), inner.getMetaData()), right(outer.getMetaData(), inner.getMetaData()), joinType);
    }
    
    protected final JoinRow newJoinRow(final Row outerRow, final Row innerRow) {
        return new JoinRow(left(outerRow, innerRow), right(outerRow, innerRow));
    }
    
    @Override
    public final void close() {
        if (outer != null) {
            outer.close();
        }
        if (inner != null) {
            inner.close();
        }
    }
}
