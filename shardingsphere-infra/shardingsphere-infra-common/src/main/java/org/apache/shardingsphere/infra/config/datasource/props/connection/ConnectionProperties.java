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

package org.apache.shardingsphere.infra.config.datasource.props.connection;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Connection properties.
 */
@Getter
@EqualsAndHashCode
public final class ConnectionProperties {
    
    private static final Collection<String> STANDARD_PROPERTY_KEYS = new HashSet<>();
    
    static {
        STANDARD_PROPERTY_KEYS.add("url");
        STANDARD_PROPERTY_KEYS.add("username");
        STANDARD_PROPERTY_KEYS.add("password");
    }
    
    private final Map<String, Object> standardProperties;
    
    private final Map<String, Object> localProperties;
    
    public ConnectionProperties(final Map<String, Object> props, final Map<String, String> propertySynonyms) {
        standardProperties = buildStandardProperties(props, propertySynonyms);
        localProperties = buildLocalProperties(props, propertySynonyms);
    }
    
    private Map<String, Object> buildStandardProperties(final Map<String, Object> props, final Map<String, String> propertySynonyms) {
        Map<String, Object> result = new LinkedHashMap<>(STANDARD_PROPERTY_KEYS.size(), 1);
        for (String each : STANDARD_PROPERTY_KEYS) {
            if (props.containsKey(each)) {
                result.put(each, props.get(each));
            } else if (props.containsKey(propertySynonyms.get(each))) {
                result.put(each, props.get(propertySynonyms.get(each)));
            }
        }
        return result;
    }
    
    private Map<String, Object> buildLocalProperties(final Map<String, Object> props, final Map<String, String> propertySynonyms) {
        Map<String, Object> result = new LinkedHashMap<>(STANDARD_PROPERTY_KEYS.size(), 1);
        for (String each : getLocalPropertyKeys(propertySynonyms)) {
            if (props.containsKey(each)) {
                result.put(each, props.get(each));
            }
        }
        for (String each : STANDARD_PROPERTY_KEYS) {
            if (props.containsKey(each)) {
                result.put(propertySynonyms.getOrDefault(each, each), props.get(each));
            }
        }
        return result;
    }
    
    private Collection<String> getLocalPropertyKeys(final Map<String, String> propertySynonyms) {
        return STANDARD_PROPERTY_KEYS.stream().filter(propertySynonyms::containsKey).map(propertySynonyms::get).collect(Collectors.toSet());
    }
}
