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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global;

import org.apache.shardingsphere.infra.executor.sql.process.lock.ProcessOperationLockRegistry;
import org.apache.shardingsphere.mode.node.path.metadata.ComputeNodePath;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.DataChangedEventHandler;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Show process list handler.
 */
public final class ShowProcessListHandler implements DataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return ComputeNodePath.getShowProcessListTriggerRootPath();
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        Matcher matcher = getShowProcessListTriggerMatcher(event);
        if (matcher.find()) {
            handle(contextManager, event, matcher);
        }
    }
    
    private void handle(final ContextManager contextManager, final DataChangedEvent event, final Matcher matcher) {
        String instanceId = matcher.group(1);
        String taskId = matcher.group(2);
        if (Type.ADDED == event.getType()) {
            if (instanceId.equals(contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getId())) {
                contextManager.getPersistCoordinatorFacade().getProcessPersistCoordinator().reportLocalProcesses(instanceId, taskId);
            }
        } else if (Type.DELETED == event.getType()) {
            ProcessOperationLockRegistry.getInstance().notify(taskId);
        }
    }
    
    private Matcher getShowProcessListTriggerMatcher(final DataChangedEvent event) {
        return Pattern.compile(ComputeNodePath.getShowProcessListTriggerRootPath() + "/([\\S]+):([\\S]+)$", Pattern.CASE_INSENSITIVE).matcher(event.getKey());
    }
}
