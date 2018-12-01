/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.config.listener;

import com.google.common.base.Optional;
import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.orchestration.internal.config.node.ConfigurationNode;
import io.shardingsphere.orchestration.internal.config.service.ConfigurationService;
import io.shardingsphere.orchestration.internal.listener.PostShardingOrchestrationEventListener;
import io.shardingsphere.orchestration.internal.listener.ShardingOrchestrationEvent;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.Type;

/**
 * Config map changed listener.
 *
 * @author caohao
 * @author panjuan
 */
public final class ConfigMapChangedListener extends PostShardingOrchestrationEventListener {
    
    private final ConfigurationService configService;
    
    public ConfigMapChangedListener(final String name, final RegistryCenter regCenter) {
        super(regCenter, new ConfigurationNode(name).getConfigMapPath());
        configService = new ConfigurationService(name, regCenter);
    }
    
    @Override
    protected Optional<ShardingOrchestrationEvent> createOrchestrationEvent(final DataChangedEvent event) {
        if (Type.UPDATED != event.getType()) {
            return Optional.absent();
        }
        // TODO use event
        ConfigMapContext.getInstance().getConfigMap().clear();
        ConfigMapContext.getInstance().getConfigMap().putAll(configService.loadConfigMap());
        return Optional.absent();
    }
}
