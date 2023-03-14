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

import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.yaml.swapper.YamlLoggerSwapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Configuration converter for YAML loggers content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlLoggersConfigurationConverter {
    
    private static final YamlLoggerSwapper SWAPPER = new YamlLoggerSwapper();
    
    /**
     * Convert to YAML logger configurations.
     *
     * @param loggers ShardingSphere loggers
     * @return YAML loggers content
     */
    public static Collection<YamlLoggerConfiguration> convertYamlLoggerConfigurations(final Collection<ShardingSphereLogger> loggers) {
        return loggers.stream().map(SWAPPER::swapToYamlConfiguration).collect(Collectors.toList());
    }
    
    /**
     * Convert to ShardingSphere loggers.
     *
     * @param loggers YAML loggers content
     * @return ShardingSphere loggers
     */
    public static Collection<ShardingSphereLogger> convertShardingSphereLogger(final Collection<YamlLoggerConfiguration> loggers) {
        return loggers.stream().map(SWAPPER::swapToObject).collect(Collectors.toList());
    }
}
