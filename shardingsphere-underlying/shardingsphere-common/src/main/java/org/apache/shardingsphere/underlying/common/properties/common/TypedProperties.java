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

package org.apache.shardingsphere.underlying.common.properties.common;

import com.google.common.base.Joiner;
import lombok.Getter;
import org.apache.shardingsphere.underlying.common.config.exception.ShardingSphereConfigurationException;
import org.apache.shardingsphere.underlying.common.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Typed properties with a specified enum.
 */
public abstract class TypedProperties<E extends Enum & TypedPropertiesKey> {
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    @Getter
    private final Properties props;
    
    private final Map<E, Object> cache;
    
    public TypedProperties(final Class<E> keyClass, final Properties props) {
        this.props = props;
        cache = preload(keyClass);
    }
    
    private Map<E, Object> preload(final Class<E> keyClass) {
        Map<E, String> stringValueMap = preloadStringValues(keyClass);
        validate(stringValueMap);
        return preloadActualValues(stringValueMap);
    }
    
    private Map<E, String> preloadStringValues(final Class<E> keyClass) {
        E[] enumConstants = keyClass.getEnumConstants();
        Map<E, String> result = new HashMap<>(enumConstants.length, 1);
        for (E each : enumConstants) {
            result.put(each, props.getOrDefault(each.getKey(), each.getDefaultValue()).toString());
        }
        return result;
    }
    
    private void validate(final Map<E, String> stringValueMap) {
        Collection<String> errorMessages = new LinkedList<>();
        for (Entry<E, String> entry : stringValueMap.entrySet()) {
            validate(entry.getKey(), entry.getValue()).ifPresent(errorMessages::add);
        }
        if (!errorMessages.isEmpty()) {
            throw new ShardingSphereConfigurationException(Joiner.on(LINE_SEPARATOR).join(errorMessages));
        }
    }
    
    private Optional<String> validate(final E enumKey, final String value) {
        Class<?> type = enumKey.getType();
        if (type == boolean.class && !StringUtil.isBooleanValue(value)) {
            return Optional.of(createErrorMessage(enumKey, value));
        }
        if (type == int.class && !StringUtil.isIntValue(value)) {
            return Optional.of(createErrorMessage(enumKey, value));
        }
        if (type == long.class && !StringUtil.isLongValue(value)) {
            return Optional.of(createErrorMessage(enumKey, value));
        }
        return Optional.empty();
    }
    
    private String createErrorMessage(final E enumKey, final String invalidValue) {
        return String.format("Value '%s' of '%s' cannot convert to type '%s'. ", invalidValue, enumKey.getKey(), enumKey.getType().getName());
    }
    
    private Map<E, Object> preloadActualValues(final Map<E, String> stringValueMap) {
        Map<E, Object> result = new ConcurrentHashMap<>(stringValueMap.size(), 1);
        for (Entry<E, String> entry : stringValueMap.entrySet()) {
            result.put(entry.getKey(), getActualValue(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private Object getActualValue(final E enumKey, final String value) {
        if (boolean.class == enumKey.getType()) {
            return Boolean.valueOf(value);
        }
        if (int.class == enumKey.getType()) {
            return Integer.valueOf(value);
        }
        if (long.class == enumKey.getType()) {
            return Long.valueOf(value);
        }
        return value;
    }
    
    /**
     * Get property value.
     *
     * @param enumKey enum key
     * @param <T> class type of return value
     * @return property value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final E enumKey) {
        return (T) cache.get(enumKey);
    }
}
