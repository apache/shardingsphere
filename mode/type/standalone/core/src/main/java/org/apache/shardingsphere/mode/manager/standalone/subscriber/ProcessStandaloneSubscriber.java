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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.executor.sql.process.ShowProcessListManager;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.YamlAllExecuteProcessContexts;
import org.apache.shardingsphere.infra.executor.sql.process.yaml.swapper.YamlAllExecuteProcessContextsSwapper;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.process.KillProcessListIdRequestEvent;
import org.apache.shardingsphere.mode.event.process.ShowProcessListRequestEvent;
import org.apache.shardingsphere.mode.event.process.ShowProcessListResponseEvent;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;

/**
 * Process standalone subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ProcessStandaloneSubscriber {
    
    private final EventBusContext eventBusContext;
    
    private final YamlAllExecuteProcessContextsSwapper swapper = new YamlAllExecuteProcessContextsSwapper();
    
    public ProcessStandaloneSubscriber(final EventBusContext eventBusContext) {
        this.eventBusContext = eventBusContext;
        eventBusContext.register(this);
    }
    
    /**
     * Load show process list data.
     *
     * @param event get children request event.
     */
    @Subscribe
    public void loadShowProcessListData(final ShowProcessListRequestEvent event) {
        YamlAllExecuteProcessContexts yamlContexts = swapper.swapToYamlConfiguration(ShowProcessListManager.getInstance().getProcessContexts().values());
        eventBusContext.post(new ShowProcessListResponseEvent(yamlContexts.getContexts().isEmpty() ? Collections.emptyList() : Collections.singleton(YamlEngine.marshal(yamlContexts))));
    }
    
    /**
     * Kill process list id.
     *
     * @param event kill process list id request event.
     */
    @Subscribe
    @SneakyThrows(SQLException.class)
    public void killProcessListId(final KillProcessListIdRequestEvent event) {
        Collection<Statement> statements = ShowProcessListManager.getInstance().getProcessStatement(event.getProcessListId());
        for (Statement statement : statements) {
            statement.cancel();
        }
    }
}
