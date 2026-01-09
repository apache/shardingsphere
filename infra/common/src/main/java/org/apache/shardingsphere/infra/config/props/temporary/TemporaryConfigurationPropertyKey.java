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

package org.apache.shardingsphere.infra.config.props.temporary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.props.TypedPropertyKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Temporary typed property key of configuration.
 */
@RequiredArgsConstructor
@Getter
public enum TemporaryConfigurationPropertyKey implements TypedPropertyKey {
    
    /**
     * Proxy meta data collector enabled.
     */
    PROXY_META_DATA_COLLECTOR_ENABLED("proxy-meta-data-collector-enabled", String.valueOf(Boolean.FALSE), boolean.class, false),
    
    /**
     * System schema metadata assembly enabled.
     */
    SYSTEM_SCHEMA_METADATA_ASSEMBLY_ENABLED("system-schema-metadata-assembly-enabled", String.valueOf(Boolean.TRUE), boolean.class, true),
    
    /**
     * Proxy meta data collector cron.
     */
    PROXY_META_DATA_COLLECTOR_CRON("proxy-meta-data-collector-cron", "0 0/1 * * * ?", String.class, false),
    
    /**
     * Instance connection enabled.
     */
    INSTANCE_CONNECTION_ENABLED("instance-connection-enabled", String.valueOf(Boolean.FALSE), boolean.class, false);
    
    private final String key;
    
    private final String defaultValue;
    
    private final Class<?> type;
    
    private final boolean rebootRequired;
    
    /**
     * Get internal property key names.
     *
     * @return collection of internal key names
     */
    public static Collection<String> getKeyNames() {
        return Arrays.stream(values()).map(TemporaryConfigurationPropertyKey::name).collect(Collectors.toList());
    }
}
