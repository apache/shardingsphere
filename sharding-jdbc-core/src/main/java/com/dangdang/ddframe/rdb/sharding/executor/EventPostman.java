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
import com.dangdang.ddframe.rdb.sharding.executor.wrapper.AbstractExecutorWrapper;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;
import java.util.Collection;

/**
 * 消息投递员.
 * 负责SQL执行消息的投递
 * 
 * @author gaohongtao
 */
@RequiredArgsConstructor
class EventPostman {
    
    private final Collection<? extends AbstractExecutorWrapper> statementExecutorWrappers;
    
    void postExecutionEvents() {
        for (AbstractExecutorWrapper each : statementExecutorWrappers) {
            if (each.getDMLExecutionEvent().isPresent()) {
                DMLExecutionEventBus.post(each.getDMLExecutionEvent().get());
            } else if (each.getDQLExecutionEvent().isPresent()) {
                DQLExecutionEventBus.post(each.getDQLExecutionEvent().get());
            }
        }
    }
    
    void postExecutionEventsAfterExecution(final AbstractExecutorWrapper statementExecutorWrapper) {
        postExecutionEventsAfterExecution(statementExecutorWrapper, EventExecutionType.EXECUTE_SUCCESS, Optional.<SQLException>absent());
    }
    
    void postExecutionEventsAfterExecution(final AbstractExecutorWrapper statementExecutorWrapper, final EventExecutionType eventExecutionType, final Optional<SQLException> exp) {
        if (statementExecutorWrapper.getDMLExecutionEvent().isPresent()) {
            DMLExecutionEvent event = statementExecutorWrapper.getDMLExecutionEvent().get();
            event.setEventExecutionType(eventExecutionType);
            event.setExp(exp);
            DMLExecutionEventBus.post(event);
        } else if (statementExecutorWrapper.getDQLExecutionEvent().isPresent()) {
            DQLExecutionEvent event = statementExecutorWrapper.getDQLExecutionEvent().get();
            event.setEventExecutionType(eventExecutionType);
            event.setExp(exp);
            DQLExecutionEventBus.post(event);
        }
    }
}
