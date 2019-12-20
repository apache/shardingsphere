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

package org.apache.shardingsphere.shardingscaling.core.execute.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.apache.shardingsphere.shardingscaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.Channel;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Sync runner group.
 *
 * @author yangyi
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SyncRunnerGroup {
    
    private final ExecuteCallback executeCallback;
    
    private final Collection<SyncRunner> syncRunners = new LinkedList<>();
    
    private Channel channel;
    
    /**
     * Add {@code SyncRunner}.
     *
     * @param syncRunner sync runner
     */
    public void addSyncRunner(final SyncRunner syncRunner) {
        syncRunners.add(syncRunner);
    }
    
    /**
     * Add all {@code SyncRunner}.
     *
     * @param syncRunners collection of sync runners
     */
    public void addAllSyncRunner(final Collection<? extends SyncRunner> syncRunners) {
        this.syncRunners.addAll(syncRunners);
    }
    
    /**
     * Invoked when this group is successful.
     */
    public void onSuccess() {
        channel.close();
        executeCallback.onSuccess();
    }
    
    /**
     * Invoked when this group fails or is canceled.
     *
     * @param throwable throwable
     */
    public void onFailure(final Throwable throwable) {
        for (SyncRunner each : syncRunners) {
            each.stop();
        }
        channel.close();
        executeCallback.onFailure(throwable);
    }
}
