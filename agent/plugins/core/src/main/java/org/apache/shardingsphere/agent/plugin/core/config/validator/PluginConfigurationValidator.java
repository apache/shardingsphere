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

package org.apache.shardingsphere.agent.plugin.core.config.validator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.plugin.core.preconditions.PluginPreconditions;

/**
 * Remote plugin configuration validator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginConfigurationValidator {
    
    /**
     * Validate host and port.
     * 
     * @param type plugin type 
     * @param pluginConfig to be validated plugin configuration
     */
    public static void validateHostAndPort(final String type, final PluginConfiguration pluginConfig) {
        validateHost(type, pluginConfig);
        validatePort(type, pluginConfig);
    }
    
    /**
     * Validate host.
     * 
     * @param type plugin type
     * @param pluginConfig to be validated plugin configuration
     */
    public static void validateHost(final String type, final PluginConfiguration pluginConfig) {
        PluginPreconditions.checkArgument(!(null == pluginConfig.getHost() || pluginConfig.getHost().isEmpty()), String.format("Hostname of %s is required.", type));
    }
    
    /**
     * Validate port.
     * 
     * @param type plugin type
     * @param pluginConfig to be validated plugin configuration
     */
    public static void validatePort(final String type, final PluginConfiguration pluginConfig) {
        PluginPreconditions.checkArgument(pluginConfig.getPort() > 0, String.format("Port `%s` of %s must be a positive number.", pluginConfig.getPort(), type));
    }
}
