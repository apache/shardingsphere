/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Sharding group execute callback.
 * 
 * @author zhangliang
 * 
 * @param <I> type of inputs value
 * @param <O> type of outputs value
 */
public interface ShardingGroupExecuteCallback<I, O> {
    
    /**
     * Execute callback.
     * 
     * @param inputs input values
     * @param isTrunkThread is execution in trunk thread
     * @param shardingExecuteDataMap sharding execute data map
     * @return execute result
     * @throws SQLException throw when execute failure
     */
    Collection<O> execute(Collection<I> inputs, boolean isTrunkThread, Map<String, Object> shardingExecuteDataMap) throws SQLException;
}
