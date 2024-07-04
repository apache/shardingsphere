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

import org.apache.shardingsphere.mode.path.GlobalNodePath;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.dispatch.config.AlterPropertiesEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Optional;

/**
 * Properties dispatch event builder.
 */
public final class PropertiesDispatchEventBuilder implements DispatchEventBuilder<AlterPropertiesEvent> {
    
    @Override
    public Collection<String> getSubscribedKeys() {
        return Collections.singleton(GlobalNode.getPropsRootNode());
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public Optional<AlterPropertiesEvent> build(final DataChangedEvent event) {
        if (GlobalNodePath.isPropsActiveVersionPath(event.getKey())) {
            return Optional.of(new AlterPropertiesEvent(event.getKey(), event.getValue()));
        }
        return Optional.empty();
    }
}
