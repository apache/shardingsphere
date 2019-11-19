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

package org.apache.shardingsphere.orchestration.temp.internal.registry.config.listener;

import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.AuthenticationYamlSwapper;

import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.temp.internal.registry.config.event.AuthenticationChangedEvent;
import org.apache.shardingsphere.orchestration.temp.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.temp.internal.registry.listener.PostShardingConfigCenterEventListener;

/**
 * Authentication changed listener.
 *
 * @author panjuan
 * @author wangguangyuan
 */
public final class AuthenticationChangedListener extends PostShardingConfigCenterEventListener {
    
    public AuthenticationChangedListener(final String name, final ConfigCenter configCenter) {
        super(configCenter, new ConfigurationNode(name).getAuthenticationPath());
    }
    
    @Override
    protected AuthenticationChangedEvent createShardingOrchestrationEvent(final DataChangedEvent event) {
        return new AuthenticationChangedEvent(new AuthenticationYamlSwapper().swap(YamlEngine.unmarshal(event.getValue(), YamlAuthenticationConfiguration.class)));
    }
}
