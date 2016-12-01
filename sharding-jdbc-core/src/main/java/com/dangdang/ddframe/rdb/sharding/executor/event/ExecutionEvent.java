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

package com.dangdang.ddframe.rdb.sharding.executor.event;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * SQL执行时事件.
 *
 * @author gaohongtao
 */
@Getter
public class ExecutionEvent {
    
    private final String id;
    
    private final String dataSource;
    
    private final String sql;
    
    private final List<List<Object>> parameters = new ArrayList<>();
    
    @Setter
    private EventExecutionType eventExecutionType = EventExecutionType.BEFORE_EXECUTE;
    
    @Setter
    private Optional<SQLException> exp;
    
    ExecutionEvent(final String dataSource, final String sql) {
        this(dataSource, sql, Collections.emptyList());
    }
    
    ExecutionEvent(final String dataSource, final String sql, final List<Object> parameters) {
        // TODO 替换UUID为更有效率的id生成器
        id = UUID.randomUUID().toString();
        this.dataSource = dataSource;
        this.sql = sql;
        this.parameters.add(parameters);
    }
    
    /**
     * 获取参数.
     * 调用该方法前需要调用{@linkplain #isBatch()},
     * 如果返回值为{@code false}那么可以调用该方法获取参数.
     * 
     * @return 参数列表
     */
    public List<Object> getParameters() {
        return parameters.get(0);
    }
    
    /**
     * 判断事件是否为批量操作事件.
     * 如果返回值为{@code false}那么可以调用{@link #getParameters()}获取参数,
     * 如果返回值为{@code true}那么可以调用{@link #getBatchParameters()}获取参数.
     * 
     * @return {@code true}是批量操作事件,{@code false}不是批量操作事件
     */
    public boolean isBatch() {
        return parameters.size() > 1;
    }
    
    /**
     * 获取批量参数.
     * 不论{@linkplain #isBatch()}返回值是什么,该方法都可以获得所有的参数.
     *
     * @return 参数列表
     */
    public List<List<Object>> getBatchParameters() {
        return parameters;
    }
    
    public void addBatchParameters(final List<Object> parameters) {
        this.parameters.add(Lists.newArrayList(parameters));
    }
}
