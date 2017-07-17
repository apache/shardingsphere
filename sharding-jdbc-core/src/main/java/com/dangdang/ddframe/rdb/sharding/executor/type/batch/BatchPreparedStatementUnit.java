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

package com.dangdang.ddframe.rdb.sharding.executor.type.batch;

import com.dangdang.ddframe.rdb.sharding.executor.BaseStatementUnit;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * 预编译语句对象的执行上下文.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class BatchPreparedStatementUnit implements BaseStatementUnit {
    
    private final SQLExecutionUnit sqlExecutionUnit;
    
    private final PreparedStatement statement;
    
    private final Map<Integer, Integer> jdbcAndActualAddBatchCallTimesMap = new HashMap<>();
    
    @Getter(AccessLevel.NONE)
    private int actualCallAddBatchTimes;
    
    /**
     * 映射通过JDBC API调用addBatch与路由之后的每个PreparedStatement内部真实调用addBatch的次数.
     * 
     * @param jdbcAddBatchTimes 通过JDBC API调用addBatch的次数
     */
    public void mapAddBatchCount(final int jdbcAddBatchTimes) {
        jdbcAndActualAddBatchCallTimesMap.put(jdbcAddBatchTimes, actualCallAddBatchTimes++);
    }
}
