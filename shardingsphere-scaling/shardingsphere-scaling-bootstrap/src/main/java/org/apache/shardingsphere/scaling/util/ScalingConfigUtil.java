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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Scaling config util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ScalingConfigUtil {
    
    private static final String DEFAULT_CONFIG_PATH = "conf/";
    
    private static final String DEFAULT_CONFIG_FILE_NAME = "server.yaml";
    
    /**
     * Init scaling config.
     *
     * @throws IOException IO exception
     */
    public static void initScalingConfig() throws IOException {
        log.info("Init scaling config");
        File yamlFile = new File(Resources.getResource(DEFAULT_CONFIG_PATH + DEFAULT_CONFIG_FILE_NAME).getPath());
        ServerConfiguration serverConfig = YamlEngine.unmarshal(yamlFile, ServerConfiguration.class);
        Preconditions.checkNotNull(serverConfig, "Server configuration file `%s` is invalid.", yamlFile.getName());
        ScalingContext.getInstance().init(serverConfig);
    }
}
