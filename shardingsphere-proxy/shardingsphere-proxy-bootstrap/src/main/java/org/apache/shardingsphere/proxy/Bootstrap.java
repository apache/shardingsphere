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

package org.apache.shardingsphere.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.builder.ModeBuilderEngine;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.mode.config.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.mode.impl.standalone.StandaloneMode;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
import org.apache.shardingsphere.proxy.arguments.BootstrapArguments;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.proxy.initializer.impl.GovernanceBootstrapInitializer;
import org.apache.shardingsphere.proxy.initializer.impl.StandardBootstrapInitializer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * ShardingSphere-Proxy Bootstrap.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static void main(final String[] args) throws IOException, SQLException {
        BootstrapArguments bootstrapArgs = new BootstrapArguments(args);
        YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(bootstrapArgs.getConfigurationPath());
        BootstrapInitializer initializer = createBootstrapInitializer(yamlConfig);
        initializer.init(yamlConfig);
        new ShardingSphereProxy().start(bootstrapArgs.getPort());
    }
    
    private static BootstrapInitializer createBootstrapInitializer(final YamlProxyConfiguration yamlConfig) {
        ModeConfiguration modeConfig = getModeConfiguration(yamlConfig);
        ShardingSphereMode mode = ModeBuilderEngine.build(modeConfig);
        // TODO split to pluggable SPI
        if (mode instanceof StandaloneMode) {
            return new StandardBootstrapInitializer(mode, modeConfig.isOverwrite());
        }
        // TODO process MemoryMode
        return new GovernanceBootstrapInitializer(mode, modeConfig.isOverwrite());
    }
    
    private static ModeConfiguration getModeConfiguration(final YamlProxyConfiguration yamlConfig) {
        return null == yamlConfig.getServerConfiguration().getMode()
                ? new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("Local", new Properties()), true)
                : new ModeConfigurationYamlSwapper().swapToObject(yamlConfig.getServerConfiguration().getMode());
    }
}
