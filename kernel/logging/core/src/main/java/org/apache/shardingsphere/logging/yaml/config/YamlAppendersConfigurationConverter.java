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

package org.apache.shardingsphere.logging.yaml.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.logging.logger.ShardingSphereAppender;
import org.apache.shardingsphere.logging.yaml.swapper.YamlAppenderSwapper;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Configuration converter for YAML appenders content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlAppendersConfigurationConverter {
    
    private static final YamlAppenderSwapper SWAPPER = new YamlAppenderSwapper();
    
    /**
     * Convert to YAML appender configurations.
     *
     * @param appenders ShardingSphere appenders
     * @return YAML appenders content
     */
    public static Collection<YamlAppenderConfiguration> convertYamlAppenderConfigurations(final Collection<ShardingSphereAppender> appenders) {
        return appenders.stream().map(SWAPPER::swapToYamlConfiguration).collect(Collectors.toList());
    }
    
    /**
     * Convert to ShardingSphere appenders.
     *
     * @param appenders YAML appenders content
     * @return ShardingSphere appenders
     */
    public static Collection<ShardingSphereAppender> convertShardingSphereAppender(final Collection<YamlAppenderConfiguration> appenders) {
        return appenders.stream().map(SWAPPER::swapToObject).collect(Collectors.toList());
    }
}
