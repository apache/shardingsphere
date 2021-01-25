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

package org.apache.shardingsphere.spring.boot.converter;

import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlNoneShardingStrategyConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * String convert to none sharding strategy configuration.
 */
@Component
@ConfigurationPropertiesBinding
public final class StringToNoneShardingStrategyConfigurationConverter implements Converter<String, YamlNoneShardingStrategyConfiguration> {
    
    @Override
    public YamlNoneShardingStrategyConfiguration convert(final String value) {
        return new YamlNoneShardingStrategyConfiguration();
    }
}
