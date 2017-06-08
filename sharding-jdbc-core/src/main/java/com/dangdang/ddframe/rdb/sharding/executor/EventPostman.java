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

import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DMLExecutionEventBus;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEvent;
import com.dangdang.ddframe.rdb.sharding.executor.event.DQLExecutionEventBus;
import com.dangdang.ddframe.rdb.sharding.executor.event.EventExecutionType;
import com.dangdang.ddframe.rdb.sharding.executor.event.ExecutionEvent;
import com.google.common.base.Optional;

import java.sql.SQLException;

/**
 * 消息投递员.
 * 负责SQL执行消息的投递
 * 
 * @author gaohongtao
 */
class EventPostman {
    
    void post(final ExecutionEvent executionEvent) {
        if (executionEvent instanceof DQLExecutionEvent) {
            DQLExecutionEventBus.post((DQLExecutionEvent) executionEvent);
        } else if (executionEvent instanceof DMLExecutionEvent) {
            DMLExecutionEventBus.post((DMLExecutionEvent) executionEvent);
        }
    }
    
    void postForExecuteSuccess(final ExecutionEvent executionEvent) {
        executionEvent.setEventExecutionType(EventExecutionType.EXECUTE_SUCCESS);
        post(executionEvent);
    }
    
    void postForExecuteFailure(final ExecutionEvent executionEvent, final SQLException cause) {
        executionEvent.setEventExecutionType(EventExecutionType.EXECUTE_FAILURE);
        executionEvent.setException(Optional.of(cause));
        post(executionEvent);
    }
}
