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
import java.util.LinkedList;
import java.util.Map;
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
    
    private final Map<Enum, Object> cache = new ConcurrentHashMap<>(64);
    
    public TypedProperties(final Class<E> keyClass, final Properties props) {
        this.props = props;
        validate(keyClass);
    }
    
    private void validate(final Class<E> keyClass) {
        Collection<String> errorMessages = new LinkedList<>();
        for (String each : props.stringPropertyNames()) {
            find(keyClass, each).ifPresent(typedEnum -> errorMessages.addAll(getErrorMessages(each, typedEnum)));
        }
        if (!errorMessages.isEmpty()) {
            throw new ShardingSphereConfigurationException(Joiner.on(LINE_SEPARATOR).join(errorMessages));
        }
    }
    
    private Optional<E> find(final Class<E> keyClass, final String key) {
        for (E each : keyClass.getEnumConstants()) {
            if (each.getKey().equals(key)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private Collection<String> getErrorMessages(final String key, final E typedEnum) {
        Collection<String> result = new LinkedList<>();
        Class<?> type = typedEnum.getType();
        String value = props.getProperty(key);
        if (type == boolean.class && !StringUtil.isBooleanValue(value)) {
            result.add(getErrorMessage(typedEnum, value));
        } else if (type == int.class && !StringUtil.isIntValue(value)) {
            result.add(getErrorMessage(typedEnum, value));
        }
        return result;
    }
    
    private String getErrorMessage(final E typedEnum, final String invalidValue) {
        return String.format("Value '%s' of '%s' cannot convert to type '%s'.", invalidValue, typedEnum.getKey(), typedEnum.getType().getName());
    }
    
    /**
     * Get property value.
     *
     * @param typedEnum properties constant
     * @param <T> class type of return value
     * @return property value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final E typedEnum) {
        if (cache.containsKey(typedEnum)) {
            return (T) cache.get(typedEnum);
        }
        Object result = getValue(typedEnum, props.getOrDefault(typedEnum.getKey(), typedEnum.getDefaultValue()).toString());
        cache.put(typedEnum, result);
        return (T) result;
    }
    
    private Object getValue(final E typedEnum, final String value) {
        if (boolean.class == typedEnum.getType()) {
            return Boolean.valueOf(value);
        }
        if (int.class == typedEnum.getType()) {
            return Integer.valueOf(value);
        }
        if (long.class == typedEnum.getType()) {
            return Long.valueOf(value);
        }
        return value;
    }
}
