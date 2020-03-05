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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.internal.registry.listener.PostShardingConfigCenterEventListener;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

/**
 * Authentication changed listener.
 */
public final class AuthenticationChangedListener extends PostShardingConfigCenterEventListener {
    
    public AuthenticationChangedListener(final String name, final ConfigCenterRepository configCenterRepository) {
        super(configCenterRepository, Lists.newArrayList(new ConfigurationNode(name).getAuthenticationPath()));
    }
    
    @Override
    protected AuthenticationChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        return new AuthenticationChangedEvent(new AuthenticationYamlSwapper().swap(YamlEngine.unmarshal(event.getValue(), YamlAuthenticationConfiguration.class)));
    }
}
