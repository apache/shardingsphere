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

package org.apache.shardingsphere.logging.yaml.swapper;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.yaml.config.YamlAppenderConfiguration;

/**
 * YAML appender swapper.
 */
public final class YamlAppenderSwapper implements YamlConfigurationSwapper<YamlAppenderConfiguration, ShardingSphereAppender> {
    
    @Override
    public YamlAppenderConfiguration swapToYamlConfiguration(final ShardingSphereAppender data) {
        if (null == data) {
            return null;
        }
        YamlAppenderConfiguration result = new YamlAppenderConfiguration();
        result.setAppenderName(data.getAppenderName());
        result.setAppenderClass(data.getAppenderClass());
        result.setPattern(data.getPattern());
        result.setFile(data.getFile());
        return result;
    }
    
    @Override
    public ShardingSphereAppender swapToObject(final YamlAppenderConfiguration yamlConfig) {
        if (null == yamlConfig) {
            return null;
        }
        ShardingSphereAppender result = new ShardingSphereAppender(yamlConfig.getAppenderName(), yamlConfig.getAppenderClass(), yamlConfig.getPattern());
        result.setFile(yamlConfig.getFile());
        return result;
    }
}
