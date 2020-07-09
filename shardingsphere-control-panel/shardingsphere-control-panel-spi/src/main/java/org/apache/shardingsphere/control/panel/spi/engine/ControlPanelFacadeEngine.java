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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.shardingsphere.control.panel.spi.ControlPanelFacade;
import org.apache.shardingsphere.control.panel.spi.FacadeConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;

/**
 * Control panel facade engine.
 */
@SuppressWarnings("ALL")
public final class ControlPanelFacadeEngine {
    
    static {
        ShardingSphereServiceLoader.register(ControlPanelFacade.class);
    }
    
    /**
     * Init control panel facade.
     *
     * @param facadeConfigurations facade configurations
     */
    public void init(final Collection<FacadeConfiguration> facadeConfigurations) {
        Collection<Class<?>> facadeClassTypes = facadeConfigurations.stream().map(FacadeConfiguration::getClass).collect(Collectors.toList());
        for (Map.Entry<Class<?>, ControlPanelFacade> entry : OrderedSPIRegistry.getRegisteredServicesByClass(facadeClassTypes, ControlPanelFacade.class).entrySet()) {
            doInit(facadeConfigurations, entry.getKey(), entry.getValue());
        }
    }
    
    private void doInit(final Collection<FacadeConfiguration> facadeConfigurations, final Class<?> configurationType, final ControlPanelFacade facade) {
        for (FacadeConfiguration each : facadeConfigurations) {
            if (each.getClass().equals(configurationType)) {
                facade.init(each);
            }
        }
    }
}
