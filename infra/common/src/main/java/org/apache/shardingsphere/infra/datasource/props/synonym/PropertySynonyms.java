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

package org.apache.shardingsphere.infra.datasource.props.synonym;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Property synonyms.
 */
@Getter
@EqualsAndHashCode
public abstract class PropertySynonyms {
    
    private final Collection<String> standardPropertyKeys;
    
    private final Map<String, Object> standardProperties;
    
    private final Map<String, Object> localProperties;
    
    public PropertySynonyms(final Map<String, Object> props, final Collection<String> standardPropertyKeys, final Map<String, String> propertySynonyms) {
        this.standardPropertyKeys = standardPropertyKeys;
        standardProperties = buildStandardProperties(props, standardPropertyKeys, propertySynonyms);
        localProperties = buildLocalProperties(props, standardPropertyKeys, propertySynonyms);
    }
    
    private Map<String, Object> buildStandardProperties(final Map<String, Object> props, final Collection<String> standardPropertyKeys, final Map<String, String> propertySynonyms) {
        Map<String, Object> result = new LinkedHashMap<>(standardPropertyKeys.size(), 1);
        for (String each : standardPropertyKeys) {
            if (props.containsKey(each)) {
                result.put(each, props.get(each));
            } else if (props.containsKey(propertySynonyms.get(each))) {
                result.put(each, props.get(propertySynonyms.get(each)));
            }
        }
        return result;
    }
    
    private Map<String, Object> buildLocalProperties(final Map<String, Object> props, final Collection<String> standardPropertyKeys, final Map<String, String> propertySynonyms) {
        Map<String, Object> result = new LinkedHashMap<>(standardPropertyKeys.size(), 1);
        for (String each : getLocalPropertyKeys(standardPropertyKeys, propertySynonyms)) {
            if (props.containsKey(each)) {
                result.put(each, props.get(each));
            }
        }
        for (String each : standardPropertyKeys) {
            if (props.containsKey(each)) {
                result.put(propertySynonyms.getOrDefault(each, each), props.get(each));
            }
        }
        return result;
    }
    
    private Collection<String> getLocalPropertyKeys(final Collection<String> standardPropertyKey, final Map<String, String> propertySynonyms) {
        return standardPropertyKey.stream().filter(propertySynonyms::containsKey).map(propertySynonyms::get).collect(Collectors.toSet());
    }
}
