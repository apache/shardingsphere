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

package io.shardingjdbc.core.executor.type.batch;

import io.shardingjdbc.core.executor.BaseStatementUnit;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

/**
 * PreparedStatement add batch execute unit.
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
     * Map times of use JDBC API call addBatch and times of actual call addBatch after route.
     * 
     * @param jdbcAddBatchTimes times of use JDBC API call addBatch
     */
    public void mapAddBatchCount(final int jdbcAddBatchTimes) {
        jdbcAndActualAddBatchCallTimesMap.put(jdbcAddBatchTimes, actualCallAddBatchTimes++);
    }
}
