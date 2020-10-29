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

package org.apache.shardingsphere.distsql.parser.binder.util;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Sharding algorithm properties util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingAlgorithmPropertiesUtil {
    
    public static final Map<String, Collection<String>> TYPE_AND_PROPERTIES;
    
    static {
        TYPE_AND_PROPERTIES = new LinkedHashMap<>();
        TYPE_AND_PROPERTIES.put("MOD", Collections.singleton("sharding-count"));
        TYPE_AND_PROPERTIES.put("HASH_MOD", Collections.singleton("sharding-count"));
        TYPE_AND_PROPERTIES.put("VOLUME_RANGE", Arrays.asList("range-lower", "range-upper", "sharding-volume"));
        TYPE_AND_PROPERTIES.put("BOUNDARY_RANGE", Collections.singleton("sharding-ranges"));
    }
    
    /**
     * Get properties.
     *
     * @param shardingAlgorithmType sharding algorithm type
     * @param properties properties
     * @return properties
     */
    public static Properties getProperties(final String shardingAlgorithmType, final Collection<String> properties) {
        String algorithmType = shardingAlgorithmType.toUpperCase();
        Preconditions.checkArgument(TYPE_AND_PROPERTIES.containsKey(algorithmType), "Bad sharding algorithm type: %s.", algorithmType);
        Preconditions.checkArgument(TYPE_AND_PROPERTIES.get(algorithmType).size() == properties.size(),
                "%s needs %d properties, but %s properties are given.", algorithmType, TYPE_AND_PROPERTIES.get(algorithmType).size(), properties.size());
        Properties result = new Properties();
        Iterator<String> keys = TYPE_AND_PROPERTIES.get(algorithmType).iterator();
        Iterator<String> values = properties.iterator();
        while (keys.hasNext()) {
            result.setProperty(keys.next(), values.next());
        }
        return result;
    }
}
