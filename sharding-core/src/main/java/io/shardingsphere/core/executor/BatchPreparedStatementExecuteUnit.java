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

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.routing.RouteUnit;
import lombok.AccessLevel;
import lombok.Getter;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Execute unit for JDBC prepared statement add batch.
 * 
 * @author zhangliang
 * @author panjuan
 */
@Getter
public final class BatchPreparedStatementExecuteUnit extends StatementExecuteUnit {
    
    private final Map<Integer, Integer> jdbcAndActualAddBatchCallTimesMap = new LinkedHashMap<>();
    
    @Getter(AccessLevel.NONE)
    private int actualCallAddBatchTimes;
    
    public BatchPreparedStatementExecuteUnit(final RouteUnit routeUnit, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        super(routeUnit, statement, connectionMode);
    }
    
    /**
     * Map times of use JDBC API call addBatch and times of actual call addBatch after route.
     * 
     * @param jdbcAddBatchTimes times of use JDBC API call addBatch
     */
    public void mapAddBatchCount(final int jdbcAddBatchTimes) {
        jdbcAndActualAddBatchCallTimesMap.put(jdbcAddBatchTimes, actualCallAddBatchTimes++);
    }
}

