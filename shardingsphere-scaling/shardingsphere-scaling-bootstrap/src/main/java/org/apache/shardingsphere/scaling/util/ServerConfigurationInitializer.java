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

package org.apache.shardingsphere.scaling.util;

import com.google.common.base.Preconditions;
import com.google.common.io.Resources;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.yaml.ServerConfigurationYamlSwapper;
import org.apache.shardingsphere.scaling.core.config.yaml.YamlServerConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Scaling server configuration initializer.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ServerConfigurationInitializer {
    
    private static final String SERVER_FILE = "conf/server.yaml";
    
    /**
     * Init server configuration.
     */
    @SneakyThrows(IOException.class)
    public static void init() {
        log.info("Initialize server configuration.");
        File yamlFile = new File(Resources.getResource(SERVER_FILE).getPath());
        YamlServerConfiguration serverConfig = YamlEngine.unmarshal(yamlFile, YamlServerConfiguration.class);
        Preconditions.checkNotNull(serverConfig, "Server configuration file `%s` is invalid.", yamlFile.getName());
        Preconditions.checkNotNull(serverConfig.getGovernance(), "Governance configuration is required.");
        ScalingContext.getInstance().init(new ServerConfigurationYamlSwapper().swapToObject(serverConfig));
    }
}
