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

package com.dangdang.ddframe.rdb.sharding.executor.wrapper;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLAbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLAbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.AbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingPreparedStatement;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.google.common.base.Optional;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * 预编译语句对象的执行上下文.
 * 
 * @author zhangliang
 */
public final class PreparedStatementExecutorWrapper {
    
    @Getter
    private final SQLExecutionUnit sqlExecutionUnit;
    
    @Getter
    private final PreparedStatement preparedStatement;
    
    private final Optional<? extends AbstractExecutionEvent> executionEvent;
    
    @Getter
    private final List<Integer[]> batchIndices = new ArrayList<>();
    
    private int batchIndex;
    
    public PreparedStatementExecutorWrapper(final SQLType sqlType, final PreparedStatement preparedStatement, final SQLExecutionUnit sqlExecutionUnit) {
        this.sqlExecutionUnit = sqlExecutionUnit;
        this.preparedStatement = preparedStatement;
        switch (sqlType) {
            case SELECT:
                executionEvent = Optional.of(new DQLAbstractExecutionEvent(getSqlExecutionUnit().getDataSource(), getSqlExecutionUnit().getSql()));
                break;
            case INSERT:
            case UPDATE:
            case DELETE:
                executionEvent = Optional.of(new DMLAbstractExecutionEvent(getSqlExecutionUnit().getDataSource(), getSqlExecutionUnit().getSql()));
                break;
            default:
                executionEvent = Optional.absent();
        }
    }
    
    public Optional<? extends AbstractExecutionEvent> getExecutionEvent() {
        return executionEvent;
    }
    
    /**
     * 映射批量执行索引.
     * 将{@linkplain ShardingPreparedStatement}批量执行索引映射为真实的{@linkplain PreparedStatement}批量执行索引.
     * 
     * @param shardingBatchIndex 分片批量执行索引
     */
    public void mapBatchIndex(final int shardingBatchIndex) {
        batchIndices.add(new Integer[]{shardingBatchIndex, this.batchIndex++});
    }
}
