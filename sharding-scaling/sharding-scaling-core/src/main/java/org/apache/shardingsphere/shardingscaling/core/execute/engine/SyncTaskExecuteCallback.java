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

import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sync task execute callback.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
@Slf4j
public final class SyncTaskExecuteCallback implements ExecuteCallback {
    
    private final String syncTaskType;
    
    private final SyncConfiguration syncConfiguration;
    
    private final ReportCallback reportCallback;
    
    @Override
    public void onSuccess() {
        log.info("{} for table {} execute finish", syncTaskType, syncConfiguration.getReaderConfiguration().getTableName());
        reportCallback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
    }
    
    @Override
    public void onFailure(final Throwable throwable) {
        log.error("{} for table {} execute exception exit", syncTaskType, syncConfiguration.getReaderConfiguration().getTableName(), throwable);
        reportCallback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.EXCEPTION_EXIT));
    }
}
