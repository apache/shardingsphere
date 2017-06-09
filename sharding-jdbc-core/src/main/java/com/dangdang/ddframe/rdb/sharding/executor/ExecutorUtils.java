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

package com.dangdang.ddframe.rdb.sharding.executor;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.executor.event.AbstractExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.event.ExecutionEventBus;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.SQLException;
import java.util.Map;

/**
 * 执行器工具类.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ExecutorUtils {
    
    static AbstractExecutionEvent getExecutionEvent(final SQLType sqlType, final SQLExecutionUnit sqlExecutionUnit) {
        if (SQLType.SELECT == sqlType) {
            return new DQLExecutionEvent(sqlExecutionUnit.getDataSource(), sqlExecutionUnit.getSql());
        }
        return new DMLExecutionEvent(sqlExecutionUnit.getDataSource(), sqlExecutionUnit.getSql());
    }
    
    static void handleException(final AbstractExecutionEvent event, final SQLException cause) {
        event.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        event.setException(Optional.of(cause));
        ExecutionEventBus.getInstance().post(event);
        ExecutorExceptionHandler.handleException(cause);
    }
    
    static void setThreadLocalData(final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        ExecutorExceptionHandler.setExceptionThrown(isExceptionThrown);
        ExecutorDataMap.setDataMap(dataMap);
    }
}
