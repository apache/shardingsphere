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

package org.apache.shardingsphere.mode.manager.cluster.event.builder;

import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessCompletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.state.compute.KillLocalProcessEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kill process dispatch event builder.
 */
public final class KillProcessDispatchEventBuilder implements DispatchEventBuilder<DispatchEvent> {
    
    @Override
    public String getSubscribedKey() {
        return ComputeNode.getKillProcessTriggerNodePath();
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.DELETED);
    }
    
    @Override
    public Optional<DispatchEvent> build(final DataChangedEvent event) {
        return createKillLocalProcessEvent(event);
    }
    
    private Optional<DispatchEvent> createKillLocalProcessEvent(final DataChangedEvent event) {
        Matcher matcher = getKillProcessTriggerMatcher(event);
        if (!matcher.find()) {
            return Optional.empty();
        }
        if (Type.ADDED == event.getType()) {
            return Optional.of(new KillLocalProcessEvent(matcher.group(1), matcher.group(2)));
        }
        if (Type.DELETED == event.getType()) {
            return Optional.of(new KillLocalProcessCompletedEvent(matcher.group(2)));
        }
        return Optional.empty();
    }
    
    private Matcher getKillProcessTriggerMatcher(final DataChangedEvent event) {
        return Pattern.compile(ComputeNode.getKillProcessTriggerNodePath() + "/([\\S]+):([\\S]+)$", Pattern.CASE_INSENSITIVE).matcher(event.getKey());
    }
}
