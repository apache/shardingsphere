/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config;

import com.dangdang.ddframe.rdb.sharding.util.StringUtil;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

/**
 * The properties for Sharding-JDBC configuration.
 *
 * @author gaohongtao
 * @author zhangliang
 */
public final class ShardingProperties {
    
    private final Properties props;
    
    public ShardingProperties(final Properties props) {
        this.props = props;
        validate();
    }
    
    private void validate() {
        Set<String> propertyNames = props.stringPropertyNames();
        Collection<String> errorMessages = new ArrayList<>(propertyNames.size());
        for (String each : propertyNames) {
            ShardingPropertiesConstant shardingPropertiesConstant = ShardingPropertiesConstant.findByKey(each);
            if (null == shardingPropertiesConstant) {
                continue;
            }
            Class<?> type = shardingPropertiesConstant.getType();
            String value = props.getProperty(each);
            if (type == boolean.class && !StringUtil.isBooleanValue(value)) {
                errorMessages.add(getErrorMessage(shardingPropertiesConstant, value));
                continue;
            }
            if (type == int.class && !StringUtil.isIntValue(value)) {
                errorMessages.add(getErrorMessage(shardingPropertiesConstant, value));
                continue;
            }
            if (type == long.class && !StringUtil.isLongValue(value)) {
                errorMessages.add(getErrorMessage(shardingPropertiesConstant, value));
            }
        }
        if (!errorMessages.isEmpty()) {
            throw new IllegalArgumentException(Joiner.on(" ").join(errorMessages));
        }
    }
    
    private String getErrorMessage(final ShardingPropertiesConstant shardingPropertiesConstant, final String invalidValue) {
        return String.format("Value '%s' of '%s' cannot convert to type '%s'.", invalidValue, shardingPropertiesConstant.getKey(), shardingPropertiesConstant.getType().getName());
    }
    
    /**
     * Get property value.
     *
     * @param shardingPropertiesConstant sharding properties constant
     * @return property value
     */
    public Object getValue(final ShardingPropertiesConstant shardingPropertiesConstant) {
        Object result = props.getOrDefault(shardingPropertiesConstant.getKey(), shardingPropertiesConstant.getDefaultValue());
        if (boolean.class == shardingPropertiesConstant.getType()) {
            if (!(result instanceof Boolean)) {
                return Boolean.valueOf(String.valueOf(result));
            }
        }else if (int.class == shardingPropertiesConstant.getType()) {
            if (!(result instanceof Integer)) {
                return Integer.valueOf(String.valueOf(result));
            }
        } else if (long.class == shardingPropertiesConstant.getType() ) {
            if (!(result instanceof Long)){
                return Long.valueOf(String.valueOf(result));
            }
        }
        return result;
    }
}
