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

package org.apache.shardingsphere.orchestration.internal.registry.config.listener;

import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.internal.registry.listener.PostShardingOrchestrationEventListener;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.reg.listener.DataChangedEvent;

/**
 * Authentication changed listener.
 *
 * @author panjuan
 */
public final class AuthenticationChangedListener extends PostShardingOrchestrationEventListener {
    
    public AuthenticationChangedListener(final String name, final RegistryCenter regCenter) {
        super(regCenter, new ConfigurationNode(name).getAuthenticationPath());
    }
    
    @Override
    protected AuthenticationChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        return new AuthenticationChangedEvent(new AuthenticationYamlSwapper().swap(YamlEngine.unmarshal(event.getValue(), YamlAuthenticationConfiguration.class)));
    }
}
