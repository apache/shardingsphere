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
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlProcessList;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper.YamlProcessListSwapper;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.process.KillProcessRequestEvent;
import org.apache.shardingsphere.mode.event.process.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.event.process.ShowProcessListResponseEvent;
import org.apache.shardingsphere.mode.process.ProcessListSubscriber;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

/**
 * Standalone processlist subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class StandaloneProcessListSubscriber implements ProcessListSubscriber {
    
    private final EventBusContext eventBusContext;
    
    private final YamlProcessListSwapper swapper = new YamlProcessListSwapper();
    
    public StandaloneProcessListSubscriber(final EventBusContext eventBusContext) {
        this.eventBusContext = eventBusContext;
        eventBusContext.register(this);
    }
    
    @Override
    @Subscribe
    public void postShowProcessListData(final ShowProcessListRequestEvent event) {
        YamlProcessList yamlProcessList = swapper.swapToYamlConfiguration(ProcessRegistry.getInstance().getAllProcesses());
        eventBusContext.post(new ShowProcessListResponseEvent(Collections.singleton(YamlEngine.marshal(yamlProcessList))));
    }
    
    @Override
    @Subscribe
    public void killProcess(final KillProcessRequestEvent event) throws SQLException {
        Process process = ProcessRegistry.getInstance().getProcess(event.getId());
        if (null == process) {
            return;
        }
        for (Statement each : process.getProcessStatements()) {
            each.cancel();
        }
    }
}
