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

import com.google.common.util.concurrent.ListenableFuture;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncExecutor;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncExecutorGroup;

import java.util.Collection;
import java.util.List;

/**
 * Sync task execute engine.
 */
public interface SyncTaskExecuteEngine {
    
    /**
     * Submit a group sync executor.
     *
     * @param syncExecutorGroup sync executor group
     */
    void submitGroup(SyncExecutorGroup syncExecutorGroup);
    
    /**
     * Submit sync executor to execute.
     *
     * @param syncExecutors sync executor list
     * @return listenable future
     */
    List<ListenableFuture<Object>> submit(Collection<SyncExecutor> syncExecutors);
}
