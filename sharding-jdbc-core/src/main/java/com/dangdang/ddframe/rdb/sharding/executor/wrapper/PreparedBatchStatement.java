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

import com.dangdang.ddframe.rdb.sharding.jdbc.core.statement.ShardingPreparedStatement;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 预编译语句对象的执行上下文.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class PreparedBatchStatement {
    
    private final SQLExecutionUnit sqlExecutionUnit;
    
    private final PreparedStatement preparedStatement;
    
    private final Map<Integer, List<Integer>> batchIndexes = new HashMap<>();
    
    @Getter(AccessLevel.NONE)
    private int batchIndex;
    
    /**
     * 映射批量执行索引.
     * 将{@linkplain ShardingPreparedStatement}批量执行索引映射为真实的{@linkplain PreparedStatement}批量执行索引.
     * 
     * @param addBatchCount 分片批量执行索引
     */
    public void mapBatchIndex(final int addBatchCount) {
        if (!batchIndexes.containsKey(addBatchCount)) {
            batchIndexes.put(addBatchCount, new LinkedList<Integer>());
        }
        batchIndexes.get(addBatchCount).add(batchIndex++);
    }
}
