/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingscaling.core.execute.engine;

import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sync task execute callback.
 */
@RequiredArgsConstructor
@Slf4j
public final class SyncTaskExecuteCallback implements ExecuteCallback {
    
    private final String syncTaskType;
    
    private final String syncTaskId;
    
    private final ReportCallback reportCallback;
    
    @Override
    public void onSuccess() {
        log.info("{} {} execute finish", syncTaskType, syncTaskId);
        reportCallback.report(new Event(syncTaskId, EventType.FINISHED));
    }
    
    @Override
    public void onFailure(final Throwable throwable) {
        log.error("{} {} execute exception exit", syncTaskType, syncTaskId, throwable);
        reportCallback.report(new Event(syncTaskId, EventType.EXCEPTION_EXIT));
    }
}
