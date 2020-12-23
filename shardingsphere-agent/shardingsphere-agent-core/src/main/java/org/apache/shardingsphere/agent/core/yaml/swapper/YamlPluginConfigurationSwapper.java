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

package org.apache.shardingsphere.agent.core.yaml.swapper;

import org.apache.shardingsphere.agent.core.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.spi.AgentTypedSPI;
import org.apache.shardingsphere.agent.core.yaml.config.YamlPluginConfiguration;

/**
 * YAML plugin configuration swapper.
 * 
 * @param <Y> type of YAML plugin configuration
 * @param <T> type of plugin configuration
 */
public interface YamlPluginConfigurationSwapper<Y extends YamlPluginConfiguration, T extends PluginConfiguration> extends YamlSwapper<Y, T>, AgentTypedSPI {
    
    /**
     * Get YAML plugin tag name.
     *
     * @return YAML plugin tag name
     */
    String getPluginTagName();
}
