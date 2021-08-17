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

package org.apache.shardingsphere.proxy.initializer.impl;

import org.apache.shardingsphere.governance.context.ClusterContextManagerBuilder;
import org.apache.shardingsphere.infra.context.manager.ContextManagerBuilder;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.scaling.core.api.ScalingWorker;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import java.util.Optional;

/**
 * Cluster bootstrap initializer.
 */
public final class ClusterBootstrapInitializer extends AbstractBootstrapInitializer {
    
    public ClusterBootstrapInitializer(final ShardingSphereMode mode) {
        super(mode);
    }
    
    @Override
    protected ContextManagerBuilder createContextManagerBuilder() {
        return new ClusterContextManagerBuilder();
    }
    
    @Override
    protected void initScaling(final YamlProxyConfiguration yamlConfig) {
        Optional<ServerConfiguration> scalingConfig = getScalingConfiguration(yamlConfig);
        if (!scalingConfig.isPresent()) {
            return;
        }
        scalingConfig.ifPresent(optional -> initScalingDetails(yamlConfig.getServerConfiguration().getMode(), optional));
    }
    
    private void initScalingDetails(final YamlModeConfiguration yamlModeConfig, final ServerConfiguration scalingConfig) {
        scalingConfig.setModeConfiguration(new ModeConfigurationYamlSwapper().swapToObject(yamlModeConfig));
        ScalingContext.getInstance().init(scalingConfig);
        ScalingWorker.init();
    }
}
