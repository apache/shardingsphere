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

package org.apache.shardingsphere.control.panel.spi.engine;

import org.apache.shardingsphere.control.panel.spi.ControlPanelConfiguration;
import org.apache.shardingsphere.control.panel.spi.ControlPanelFacade;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Control panel facade engine.
 */
public final class ControlPanelFacadeEngine {
    
    static {
        ShardingSphereServiceLoader.register(ControlPanelFacade.class);
    }
    
    /**
     * Initialize control panel facade.
     *
     * @param controlPanelConfigs control panel configurations
     */
    @SuppressWarnings("rawtypes")
    public void init(final Collection<ControlPanelConfiguration> controlPanelConfigs) {
        Collection<Class<?>> controlPanelConfigClass = controlPanelConfigs.stream().map(ControlPanelConfiguration::getClass).collect(Collectors.toList());
        for (Entry<Class<?>, ControlPanelFacade> entry : OrderedSPIRegistry.getRegisteredServicesByClass(controlPanelConfigClass, ControlPanelFacade.class).entrySet()) {
            init(controlPanelConfigs, entry.getKey(), entry.getValue());
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void init(final Collection<ControlPanelConfiguration> controlPanelConfigs, final Class<?> controlPanelConfigClass, final ControlPanelFacade facade) {
        for (ControlPanelConfiguration each : controlPanelConfigs) {
            if (each.getClass().equals(controlPanelConfigClass)) {
                facade.init(each);
            }
        }
    }
}
