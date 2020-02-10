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

package org.apache.shardingsphere.underlying.common.constant.properties;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.underlying.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere properties for configuration.
 *
 * @author gaohongtao
 * @author zhangliang
 * @author panjuan
 */
public final class ShardingSphereProperties {
    
    @Getter
    private final Properties props;
    
    private final Map<PropertiesConstant, Object> cachedProperties = new ConcurrentHashMap<>(64, 1);
    
    public ShardingSphereProperties(final Properties props) {
        this.props = props;
        validate();
    }
    
    private void validate() {
        Set<String> propertyNames = props.stringPropertyNames();
        Collection<String> errorMessages = new ArrayList<>(propertyNames.size());
        for (String each : propertyNames) {
            PropertiesConstant propertiesConstant = PropertiesConstant.findByKey(each);
            if (null == propertiesConstant) {
                continue;
            }
            Class<?> type = propertiesConstant.getType();
            String value = props.getProperty(each);
            if (type == boolean.class && !StringUtil.isBooleanValue(value)) {
                errorMessages.add(getErrorMessage(propertiesConstant, value));
            } else if (type == int.class && !StringUtil.isIntValue(value)) {
                errorMessages.add(getErrorMessage(propertiesConstant, value));
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(Joiner.on(" ").join(errorMessages));
        }
    }
    
    private String getErrorMessage(final PropertiesConstant propertiesConstant, final String invalidValue) {
        return String.format("Value '%s' of '%s' cannot convert to type '%s'.", invalidValue, propertiesConstant.getKey(), propertiesConstant.getType().getName());
    }
    
    /**
     * Get property value.
     *
     * @param propertiesConstant properties constant
     * @param <T> class type of return value
     * @return property value
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final PropertiesConstant propertiesConstant) {
        if (cachedProperties.containsKey(propertiesConstant)) {
            return (T) cachedProperties.get(propertiesConstant);
        }
        String value = props.getProperty(propertiesConstant.getKey());
        if (Strings.isNullOrEmpty(value)) {
            Object obj = props.get(propertiesConstant.getKey());
            if (null == obj) {
                value = propertiesConstant.getDefaultValue();
            } else {
                value = obj.toString();
            }
        }
        Object result;
        if (boolean.class == propertiesConstant.getType()) {
            result = Boolean.valueOf(value);
        } else if (int.class == propertiesConstant.getType()) {
            result = Integer.valueOf(value);
        } else if (long.class == propertiesConstant.getType()) {
            result = Long.valueOf(value);
        } else {
            result = value;
        }
        cachedProperties.put(propertiesConstant, result);
        return (T) result;
    }
}
