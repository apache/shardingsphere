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

package org.apache.shardingsphere.infra.properties;

import com.google.common.base.Joiner;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.exception.ShardingSphereConfigurationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

/**
 * Typed properties with a specified enum.
 */
public abstract class TypedProperties<E extends Enum<?> & TypedPropertyKey> {
    
    @Getter
    private final Properties props;
    
    private final Map<E, TypedPropertyValue> cache;
    
    protected TypedProperties(final Class<E> keyClass, final Properties props) {
        this.props = props;
        cache = preload(keyClass);
    }
    
    private Map<E, TypedPropertyValue> preload(final Class<E> keyClass) {
        E[] enumConstants = keyClass.getEnumConstants();
        Map<E, TypedPropertyValue> result = new HashMap<>(enumConstants.length, 1);
        Collection<String> errorMessages = new LinkedList<>();
        for (E each : enumConstants) {
            TypedPropertyValue value = null;
            try {
                value = new TypedPropertyValue(each, props.getOrDefault(each.getKey(), each.getDefaultValue()).toString());
            } catch (final TypedPropertyValueException ex) {
                errorMessages.add(ex.getMessage());
            }
            result.put(each, value);
        }
        if (!errorMessages.isEmpty()) {
            throw new ShardingSphereConfigurationException(Joiner.on(System.lineSeparator()).join(errorMessages));
        }
        return result;
    }
    
    /**
     * Get property value.
     *
     * @param key property key
     * @param <T> class type of return value
     * @return property value
     */
    @SuppressWarnings("unchecked")
    public final <T> T getValue(final E key) {
        return (T) cache.get(key).getValue();
    }
}
