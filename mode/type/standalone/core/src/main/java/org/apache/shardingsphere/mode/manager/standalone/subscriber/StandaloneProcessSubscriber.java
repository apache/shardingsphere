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

package org.apache.shardingsphere.mode.manager.standalone.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessRegistry;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.process.ProcessSubscriber;
import org.apache.shardingsphere.mode.process.event.KillProcessRequestEvent;
import org.apache.shardingsphere.mode.process.event.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.process.event.ShowProcessListResponseEvent;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Standalone process subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class StandaloneProcessSubscriber implements ProcessSubscriber {
    
    private final EventBusContext eventBusContext;
    
    public StandaloneProcessSubscriber(final EventBusContext eventBusContext) {
        this.eventBusContext = eventBusContext;
        eventBusContext.register(this);
    }
    
    @Override
    @Subscribe
    public void postShowProcessListData(final ShowProcessListRequestEvent event) {
        eventBusContext.post(new ShowProcessListResponseEvent(ProcessRegistry.getInstance().listAll()));
    }
    
    @Override
    @Subscribe
    public void killProcess(final KillProcessRequestEvent event) throws SQLException {
        Process process = ProcessRegistry.getInstance().get(event.getId());
        if (null == process) {
            return;
        }
        for (Statement each : process.getProcessStatements().values()) {
            each.cancel();
        }
    }
}
