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

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 执行上下文基类.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractExecutorWrapper {
    
    private final SQLExecutionUnit sqlExecutionUnit;
    
    /**
     * 判断SQL是否为DML语句.
     * 
     * @return 是否为DML语句
     */
    final boolean isDML() {
        String sql = sqlExecutionUnit.getSQL();
        return sql.toLowerCase().startsWith("insert") || sql.toLowerCase().startsWith("update") || sql.toLowerCase().startsWith("delete");
    }
    
    /**
     * 判断SQL是否为DQL语句.
     * 
     * @return 是否为DQL语句
     */
    final boolean isDQL() {
        return sqlExecutionUnit.getSQL().toLowerCase().startsWith("select");
    }
    
    /**
     * 获取DML类SQL执行时事件.
     * 
     * @return DML类SQL执行时事件
     */
    public abstract Optional<DMLExecutionEvent> getDMLExecutionEvent();
    
    /**
     * 获取DML类SQL执行时事件.
     * 
     * @return DQL类SQL执行时事件
     */
    public abstract Optional<DQLExecutionEvent> getDQLExecutionEvent();
}
